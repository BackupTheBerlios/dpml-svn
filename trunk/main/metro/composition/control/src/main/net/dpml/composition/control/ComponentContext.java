/*
 * Copyright 2005 Stephen J. McConnell.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.composition.control;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.EventObject;

import net.dpml.composition.event.EventProducer;
import net.dpml.composition.data.ComponentDirective;
import net.dpml.composition.data.ValueDirective;
import net.dpml.composition.data.ReferenceDirective;
import net.dpml.composition.data.ClassLoaderDirective;
import net.dpml.composition.data.ClasspathDirective;
import net.dpml.composition.info.Type;
import net.dpml.part.EntryDescriptor;

import net.dpml.part.Directive;
import net.dpml.part.Part;
import net.dpml.part.PartReference;
import net.dpml.part.Context;
import net.dpml.part.ContextException;
import net.dpml.part.EntryDescriptor;

import net.dpml.transit.model.Value;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.Plugin.Category;

/**
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ComponentContext extends EventProducer implements Context
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final ComponentDirective m_directive;
    private final ClassLoader m_classloader;
    private final Type m_type;
    private final EntryDescriptor[] m_entries;
    private final String[] m_partKeys;
    private final HashMap m_contextTable = new HashMap(); // key (String), value (Value)
    private final HashMap m_parts = new HashMap();

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public ComponentContext( ComponentDirective directive )
      throws ContextException, RemoteException
    {
         this( Thread.currentThread().getContextClassLoader(), directive );
    }

    public ComponentContext( ClassLoader anchor, ComponentDirective directive ) 
      throws ContextException, RemoteException
    {
        super();
        
        m_directive = directive;
        m_classloader = createClassLoader( anchor, directive );
        m_type = loadType( m_classloader, directive );
        m_entries = getEntries();

        for( int i=0; i < m_entries.length; i++ )
        {
            String key = m_entries[i].getKey();
            try
            {
                Directive d = resolveDirective( key );
                m_contextTable.put( key, d );
            }
            catch( UnknownKeyException e )
            {
                final String error = 
                  "Component directive does not declare a directive for the key [" + key + "].";
                throw new ContextException( error, e );
            }
        }

        m_partKeys = getPartKeys( m_type );
        for( int i=0; i < m_partKeys.length; i++ )
        {
            String key = m_partKeys[i];
            Part part = m_type.getPart( key );
            if( part instanceof ValueDirective )
            {
                ValueDirective value = (ValueDirective) part;
                ValueContext context = new ValueContext( value );
                m_parts.put( key, context );
            }
            else if( part instanceof ComponentDirective )
            {
                ComponentDirective component = (ComponentDirective) part;
                ComponentContext context = new ComponentContext( m_classloader, component );
                m_parts.put( key, context );
            }
        }
    }

    protected void processEvent( EventObject event )
    {
    }

    // ------------------------------------------------------------------------
    // Context
    // ------------------------------------------------------------------------

    public EntryDescriptor[] getEntries() throws RemoteException
    {
        return m_type.getContextDescriptor().getEntryDescriptors();
    }

    public Directive getDirective( String key ) throws UnknownKeyException, RemoteException
    {
        checkContextEntryKey( key );
        return (Directive) m_contextTable.get( key );
    }

    public void setDirective( String key, Directive directive ) 
      throws UnknownKeyException, ContextException, RemoteException
    {
        checkContextEntryKey( key );
        //Part part = getPartDirective( key );
        //if( null == part )
        //{
        //    try
        //    {
        //        ValueDirective directive = new ValueDirective( ...
        //    }
        //}
        // TODO: check value type compliance
        m_contextTable.put( key, directive );
        
    }

    public String[] getChildKeys() throws RemoteException
    {
        return m_partKeys;
    }

    public Context getChild( String key ) throws UnknownKeyException, RemoteException
    {
        Context context = (Context) m_parts.get( key );
        if( null == context )
        {
            throw new UnknownKeyException( key );
        }
        else
        {
            return context;
        }
    }

    // ------------------------------------------------------------------------
    // internals
    // ------------------------------------------------------------------------

    private ClassLoader createClassLoader( ClassLoader anchor, ComponentDirective profile )
    {
        ClassLoader parent = anchor;
        final ClassLoader base = getClass().getClassLoader();
        final String name = profile.getName();
        final ClassLoaderDirective cld = profile.getClassLoaderDirective();
        final ClasspathDirective[] cpds = cld.getClasspathDirectives();
        for( int i=0; i<cpds.length; i++ )
        {
            ClasspathDirective cpd = cpds[i];
            Category tag = cpd.getCategory();
            URI[] uris = filter( cpd.getURIs(), parent );
            if( uris.length > 0 )
            {
                parent = new CompositionClassLoader( null, tag, base, uris, parent );
            }
        }
        return parent;
    }

    private URI[] filter( URI[] uris, ClassLoader classloader )
    {
        if( classloader instanceof URLClassLoader )
        {
            URLClassLoader loader = (URLClassLoader) classloader;
            return filterURLClassLoader( uris, loader );
        }
        else
        {
            return uris;
        }
    }

    private URI[] filterURLClassLoader( URI[] uris, URLClassLoader parent )
    {
        ArrayList list = new ArrayList();
        for( int i=(uris.length - 1); i>-1; i-- )
        {
            URI uri = uris[i];
            String path = uri.toString();
            if( false == exists( uri, parent ) )
            {
                list.add( uri );
            }
        }
        return (URI[]) list.toArray( new URI[0] );
    }

    private boolean exists( URI uri, URLClassLoader classloader )
    {
        ClassLoader parent = classloader.getParent();
        if( parent instanceof URLClassLoader )
        {
            URLClassLoader loader = (URLClassLoader) parent;
            if( exists( uri, loader ) )
            {
                return true;
            }
        }

        String ref = uri.toString();
        URL[] urls = classloader.getURLs();
        for( int i=0; i<urls.length; i++ )
        {
            URL url = urls[i];
            String spec = url.toString();
            if( spec.equals( ref ) )
            {
                return true;
            }
        }

        return false;
    }

    private Type loadType( ClassLoader classloader, ComponentDirective directive ) throws ContextException
    {
        String classname = directive.getClassname();
        try
        {
            Class c = classloader.loadClass( classname );
            return Type.decode( c );
        }
        catch( Throwable e )
        {
            final String error =
              "Cannot load component type defintion: " + classname;
            throw new ContextException( error, e );
        }
    }

    private String[] getContextKeys( Type type )
    {
        EntryDescriptor[] entries = type.getContextDescriptor().getEntryDescriptors();
        String[] keys = new String[ entries.length ];
        for( int i=0; i<entries.length; i++ )
        {
            keys[i] = entries[i].getKey();
        }
        return keys;
    }

    private String[] getPartKeys( Type type )
    {
        PartReference[] references = m_type.getPartReferences();
        String[] keys = new String[ references.length ];
        for( int i=0; i<references.length; i++ )
        {
            keys[i] = references[i].getKey();
        }
        return keys;
    }

    private Directive resolveDirective( String key ) throws UnknownKeyException
    {
        checkContextEntryKey( key );
        Part part = getPartDirective( key );
        if( null == part )
        {
            return null;
        }
        else if( part instanceof Directive )
        {
            return (Directive) part;
        }
        else
        {
            throw new UnsupportedOperationException( "Unsupported directive: " + part.getClass().getName() );
        }
    }

    private Part getPartDirective( String key ) throws UnknownKeyException
    {
        return m_directive.getContextDirective().getPartDirective( key );
    }

    private void checkContextEntryKey( String key ) throws UnknownKeyException
    {
        EntryDescriptor entry = m_type.getContextDescriptor().getEntryDescriptor( key );
        if( null == entry )
        {
            throw new UnknownKeyException( key );
        }
    }
}


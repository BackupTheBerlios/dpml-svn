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

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.EventObject;

import net.dpml.activity.Executable;
import net.dpml.activity.Startable;

import net.dpml.component.data.ComponentDirective;
import net.dpml.component.data.ValueDirective;
import net.dpml.component.data.ReferenceDirective;
import net.dpml.component.data.ClassLoaderDirective;
import net.dpml.component.data.ClasspathDirective;
import net.dpml.component.data.ContextDirective;
import net.dpml.component.data.Directive;
import net.dpml.component.info;.LifestylePolicy;
import net.dpml.component.info;.CollectionPolicy;
import net.dpml.component.info;.Type;
import net.dpml.component.info;.EntryDescriptor;
import net.dpml.component.info;.PartReference;

import net.dpml.composition.event.EventProducer;

import net.dpml.component.model;.ComponentModel;
import net.dpml.component.model;.ContextModel;

import net.dpml.configuration.Configuration;

import net.dpml.logging.Logger;

import net.dpml.parameters.Parameters;

import net.dpml.part.Part;
import net.dpml.part.ControlException;

import net.dpml.state.State;
import net.dpml.component.info;.ActivationPolicy;
import net.dpml.state.impl.DefaultState;
import net.dpml.state.impl.DefaultStateMachine;

import net.dpml.transit.model.Value;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.Plugin.Category;

/**
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultComponentModel extends EventProducer implements ComponentModel
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final Type m_type;
    private final ComponentDirective m_directive;
    private final ClassLoader m_classloader;
    private final String[] m_partKeys;
    private final HashMap m_parts = new HashMap();
    private final ContextModel m_context;
    
    private String m_classname;
    private ActivationPolicy m_activation;
    private LifestylePolicy m_lifestyle;
    private CollectionPolicy m_collection;
    private Parameters m_parameters;  // <------------ remove this (covered by context)
    private Configuration m_configuration; // <----- move to a context entry where the resolved value is a Configuration 
    private Class m_class;
    private State m_graph;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public DefaultComponentModel( Logger logger, ComponentDirective directive )
      throws ControlException, RemoteException
    {
         this( Thread.currentThread().getContextClassLoader(), logger, directive );
    }

    public DefaultComponentModel( ClassLoader anchor, Logger logger, ComponentDirective directive ) 
      throws ControlException, RemoteException
    {
        super();
        
        m_directive = directive;
        m_classloader = createClassLoader( anchor, directive );
        m_class = loadComponentClass( m_classloader, directive );
        m_type = loadType( m_class );
        m_graph = loadStateGraph( m_class );
        m_classname = directive.getClassname();
        m_activation = directive.getActivationPolicy();
        m_lifestyle = directive.getLifestylePolicy();
        m_collection = directive.getCollectionPolicy();
        m_parameters = directive.getParameters();
        m_configuration = directive.getConfiguration();
        
        ContextDirective context = directive.getContextDirective();
        m_context = new DefaultContextModel( m_classloader, logger, m_type, context );
        
        m_partKeys = getPartKeys( m_type );
        for( int i=0; i < m_partKeys.length; i++ )
        {
            String key = m_partKeys[i];
            Part part = m_type.getPart( key );
            if( part instanceof ComponentDirective )
            {
                ComponentDirective component = (ComponentDirective) part;
                ComponentModel model = new DefaultComponentModel( m_classloader, logger, component );
                m_parts.put( key, model );
            }
        }
    }

    protected void processEvent( EventObject event )
    {
    }

    // ------------------------------------------------------------------------
    // ComponentModel
    // ------------------------------------------------------------------------

   /**
    * Return the immutable state graph for the component.
    * @return the state graph.
    */
    public State getStateGraph()
    {
        return m_graph;
    }
    
   /**
    * Return the component implementation class name.
    *
    * @return the classname of the implementation 
    */
    public String getImplementationClassName()
    {
        return m_classname;
    }
    
   /**
    * Return the activation policy for the component.
    * @return the activation policy value
    */
    public ActivationPolicy getActivationPolicy()
    {
        return m_activation;
    }

   /**
    * Set the component activation policy to the supplied value.
    * @return the new activation policy
    */
    public void setActivationPolicy( ActivationPolicy policy )
    {
        m_activation = policy;
    }

   /**
    * Return the component lifestyle policy. The value return is a non-null
    * lifestyle policy established via overriding policy assignments and 
    * defaulting to the type lifestyle policy if no override is present.
    *
    * @return the lifestyle policy value
    */
    public LifestylePolicy getLifestylePolicy()
    {
        return m_lifestyle;
    }

   /**
    * Override the default lifestyle policy with the supplied lifestyle.
    * A null value will result in lifestyle policy selection based on the 
    * component type's preferred lifestyle policy.
    *
    * @param lifestyle the lifestyle policy
    */
    public void setLifestylePolicy( LifestylePolicy lifestyle )
    {
        m_lifestyle = lifestyle;
    }

   /**
    * Return the current component collection policy.  If null, the component
    * type collection policy will be returned.
    *
    * @return a HARD, WEAK, SOFT or SYSTEM
    */
    public CollectionPolicy getCollectionPolicy()
    {
        return m_collection;
    }

   /**
    * Override the assigned collection policy.
    * @param policy the collection policy value
    */
    public void setCollectionPolicy( CollectionPolicy policy )
    {
        m_collection = policy;
    }

   /**
    * Return the current context model.
    *
    * @return the context model
    */
    public ContextModel getContextModel()
    {
        return m_context;
    }
    
   /**
    * Return the set of component model keys.
    * @return the component part keys
    */
    public String[] getPartKeys()
    {
        return m_partKeys;
    }

   /**
    * Return the component model of an internal part referenced by the supplied key.
    * @return the internal part component model 
    */
    public ComponentModel getComponentModel( String key ) throws UnknownKeyException
    {
        ComponentModel model = (ComponentModel) m_parts.get( key );
        if( null == model )
        {
            throw new UnknownKeyException( key );
        }
        else
        {
            return model;
        }
    }
    
    public Configuration getConfiguration()
    {
        return m_configuration;
    }

    public Parameters getParameters()
    {
        return m_parameters;
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
    
    private Class loadComponentClass( ClassLoader classloader, ComponentDirective directive ) throws ControlException
    {
        String classname = directive.getClassname();
        try
        {
            return classloader.loadClass( classname );
        }
        catch( Throwable e )
        {
            final String error =
              "Cannot load component class: " + classname;
            throw new ControlException( error, e );
        }
    }
    
    private Type loadType( Class subject ) throws ControlException
    {
        try
        {
            return Type.decode( subject );
        }
        catch( Throwable e )
        {
            final String error =
              "Cannot load component type defintion: " + subject.getName();
            throw new ControlException( error, e );
        }
    }

    private State loadStateGraph( Class subject ) throws ControlException
    {
        if( Executable.class.isAssignableFrom( subject ) )
        {
            return loadState( Executable.class );
        }
        else if( Startable.class.isAssignableFrom( subject ) )
        {
            return loadState( Startable.class );
        }
        else
        {
            return loadState( subject );
        }
    }
    
    State loadState( Class subject ) throws ControlException
    {
        String resource = subject.getName().replace( '.', '/' ) + ".xgraph";
        try
        {
            URL url = subject.getClassLoader().getResource( resource );
            if( null == url )
            {
                return new DefaultState( "" );
            }
            else
            {
                InputStream input = url.openConnection().getInputStream();
                return DefaultStateMachine.load( input );
            }
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to load component state graph resource [" 
              + resource 
              + "].";
            throw new ControlException( error, e );
        }
    }
}


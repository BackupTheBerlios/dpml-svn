/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.composition.runtime;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;

import net.dpml.part.DelegationException;
import net.dpml.part.Part;
import net.dpml.part.PartException;
import net.dpml.part.PartHandlerNotFoundException;

import net.dpml.component.data.FeatureDirective;
import net.dpml.component.data.ReferenceDirective;
import net.dpml.component.data.ValueDirective;
import net.dpml.component.runtime.Container;
import net.dpml.component.runtime.Component;
import net.dpml.component.runtime.ComponentException;
import net.dpml.component.runtime.ComponentRuntimeException;
import net.dpml.component.runtime.DuplicateKeyException;
import net.dpml.component.runtime.ComponentNotFoundException;
import net.dpml.component.runtime.Service;
import net.dpml.component.runtime.ServiceContext;
import net.dpml.component.runtime.ServiceException;
import net.dpml.component.runtime.ServiceNotFoundException;
import net.dpml.component.runtime.Control;
import net.dpml.component.control.Controller;

import net.dpml.composition.control.CompositionController;


/**
 * The context map is a utility class that handles the set of components that 
 * make up the context model for an enclosing component.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ContextMap extends Hashtable
{
    private final DependencyGraph m_consumers;
    private final CompositionController m_controller;
    private final ComponentHandler m_component;
    private final Component m_parent;

   /**
    * Creation of a new context entry table.
    * 
    * @param component the component that is managing this context map
    * @param parent an enclosing partent component used for lookup of part references
    */
    public ContextMap( ComponentHandler component, Component parent, DependencyGraph graph )
    {
        super();

        m_component = component;
        m_controller = component.getController();
        m_parent = parent;
        m_consumers = graph;
    }

    public void addEntry( String key, Part part ) 
      throws PartException, RemoteException
    {
        if( containsKey( key ) )
        {
            throw new DuplicateKeyException( key );
        }
        else
        {
            setProvider( key, part );
        }
    }

    public void setProvider( String key, Part part )
      throws PartException, RemoteException
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        else if( null == part )
        {
            throw new NullPointerException( "part" );
        }
        else if( part instanceof FeatureDirective )
        {
            put( key, part );
        }
        else if( part instanceof ReferenceDirective )
        {
            ReferenceDirective reference = (ReferenceDirective) part;
            URI uri = reference.getURI();
            if( ( null != m_parent ) && ( m_parent instanceof ServiceContext ) )
            {
                ServiceContext context = (ServiceContext) m_parent;
                try
                {
                    Component service = context.lookup( uri );
                    addEntry( key, service );
                }
                catch( ServiceException e )
                {
                    final String error = 
                      "Unresolvable context reference."
                      + "\nComponent: " + m_component.getLocalURI()
                      + "\nContext Key: " + key
                      + "\nContext Entry URI: " + uri;
                    throw new ComponentException( error, e );
                }
            }
            else
            {
                final String error = 
                  "Unresolvable reference."
                  + "\nComponent: " + m_component.getLocalURI()
                  + "\nContext Key: " + key
                  + "\nContext Entry URI: " + uri;
                throw new ComponentException( error );
            }
            /*
            if( "parts".equals( uri.getScheme() ) )
            {
                String ref = uri.getSchemeSpecificPart();
                if( m_component instanceof Container )
                {
                    Container container = (Container) m_component;
                    try
                    {
                        Component entry = container.getComponent( ref );
                        addEntry( key, entry );
                    }
                    catch( ComponentNotFoundException cnfe )
                    {
                        final String error = 
                          "Component not found."
                          + "\nComponent: " + m_component.getLocalURI()
                          + "\nContext Key: " + key;
                        throw new ComponentException( error );
                    }
                }
                else
                {
                    final String error = 
                      "Enclosing component is not a container."
                      + "\nComponent: " + m_component.getLocalURI()
                      + "\nContext Key: " + key;
                    throw new ComponentException( error );
                }
            }
            else if( m_component instanceof ServiceContext )
            {
                ServiceContext context = (ServiceContext) m_component;
                try
                {
                    Component service = context.lookup( uri );
                    addEntry( key, service );
                }
                catch( ServiceException e )
                {
                    final String error = 
                      "Unresolvable context reference."
                      + "\nComponent: " + m_component.getLocalURI()
                      + "\nContext Key: " + key
                      + "\nContext Entry URI: " + uri;
                    throw new ComponentException( error, e );
                }
            }
            else
            {
                final String error = 
                  "Part reference resolution not supported at this time."
                  + "\nReference URI: " + uri
                  + "\nComponent: " + m_component.getLocalURI()
                  + "\nContext Key: " + key;
                throw new ComponentException( error );
            }
            */
        }
        else
        {
            CompositionController controller = m_component.getController();
            Component provider = controller.newComponent( m_component, part, key );
            setEntry( key, provider );
        }
    }

    public void addEntry( String key, Component provider ) throws DuplicateKeyException
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        if( null == provider )
        {
            throw new NullPointerException( "provider" );
        }
        if( containsKey( key ) )
        {
            throw new DuplicateKeyException( key );
        }
        setEntry( key, provider );
    }

    public void setEntry( String key, Component value )
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        //m_consumers.add( value );
        put( key, value );
    }

    public Object getValue( String key, Object[] args ) throws RemoteException
    {
        Object value = get( key );
        if( null == value )
        {
            if( null == args )
            {
                return null;
            }
            else if( args.length < 1 )
            {
                return null;
            }
            else
            {
                return args[0];
            }
        }
        else
        {
            return value;
        }
    }

    public Object get( String key )
    {
        Object entry = super.get( key );
        if( null == entry )
        {
            return null;
        }
        else if( entry instanceof FeatureDirective )
        {
            FeatureDirective directive = (FeatureDirective) entry;
            int feature = directive.getFeature();
            if( FeatureDirective.URI == feature )
            {
                return m_component.getLocalURI();
            }
            else if( FeatureDirective.NAME == feature )
            {
                return m_component.getName();
            }
            else if( FeatureDirective.WORK == feature )
            {
                return m_controller.getControllerContext().getWorkingDirectory();
            }
            else if( FeatureDirective.TEMP == feature )
            {
                return m_controller.getControllerContext().getTempDirectory();
            }
            else
            {
                // will not happen
                throw new IllegalStateException( "Bad feature reference: " + feature );
            }
        }
        else if( entry instanceof Control )
        {
            Control service = (Control) entry;
            try
            {
                return service.resolve();
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while attempting to resolve the value of context entry."
                  + "\nEnclosing component: " + m_component.getLocalURI()
                  + "\nProvider: " + service
                  + "\nContext Key: " + key;
                throw new ComponentRuntimeException( error, e );
            }
        }
        else
        {
            return entry;
        }
    }

    public synchronized Component getProvider( String key )
    {
        throw new UnsupportedOperationException( "getProvider/1" );
    }

    public synchronized Component[] getProviders()
    {
        ArrayList list = new ArrayList();
        Object[] values = values().toArray();
        for( int i=0; i<values.length; i++ )
        {
            Object value = values[i];
            if( value instanceof ComponentHandler )
            {
                list.add( value );
            }
        }
        return (Component[]) list.toArray( new Component[0] );
    }
}
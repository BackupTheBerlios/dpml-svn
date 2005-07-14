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

import net.dpml.part.control.Controller;
import net.dpml.part.DelegationException;
import net.dpml.part.PartHandlerNotFoundException;
import net.dpml.part.DelegationException;
import net.dpml.part.control.Container;
import net.dpml.part.control.Component;
import net.dpml.part.control.ComponentException;
import net.dpml.part.control.ComponentRuntimeException;
import net.dpml.part.control.DuplicateKeyException;
import net.dpml.part.control.ComponentNotFoundException;
import net.dpml.part.service.Service;
import net.dpml.part.service.ServiceContext;
import net.dpml.part.service.ServiceException;
import net.dpml.part.service.ServiceNotFoundException;
import net.dpml.part.Part;

import net.dpml.composition.control.CompositionController;
import net.dpml.composition.data.ReferenceDirective;
import net.dpml.composition.data.ValueDirective;

/**
 * The context map is a utility class that handles the set of components that 
 * make up the context model for an enclosing component.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ContextMap extends Hashtable
{
    private final CompositionController m_controller;
    private final ComponentHandler m_component;
    private final Container m_parent;

   /**
    * Creation of a new context entry table.
    * 
    * @param component the component that is managing this context map
    * @param parent an enclosing partent component used for lookup of part references
    */
    public ContextMap( ComponentHandler component, Component parent )
    {
        super();

        m_component = component;
        m_controller = component.getController();
        if( component instanceof Container )
        {
            m_parent = (Container) parent;
        }
        else
        {
            m_parent = null;
        }
    }

    public void addEntry( String key, Part part ) 
      throws ComponentException, PartHandlerNotFoundException, DelegationException
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        else if( null == part )
        {
            throw new NullPointerException( "part" );
        }
        else if( containsKey( key ) )
        {
            throw new DuplicateKeyException( key );
        }
        else if( part instanceof ReferenceDirective )
        {
            ReferenceDirective reference = (ReferenceDirective) part;
            URI uri = reference.getURI();
            if( "parts".equals( uri.getScheme() ) )
            {
                if( null == m_parent )
                {
                    final String error = 
                      "Cannot resolve a reference to an enclosing container from a root component."
                      + "\nComponent: " + m_component.getURI()
                      + "\nContext Key: " + key;
                    throw new ComponentException( error );
                }

                String ref = uri.getSchemeSpecificPart();
                try
                {
                    Component entry = m_parent.getComponent( ref );
                    addEntry( key, entry );
                }
                catch( ComponentNotFoundException cnfe )
                {
                    final String error = 
                      "Component not found."
                      + "\nContainer: " + m_parent.getURI()
                      + "\nComponent: " + m_component.getURI()
                      + "\nContext Key: " + key;
                    throw new ComponentException( error );
                }
            }
            else if( m_parent instanceof ServiceContext )
            {
                ServiceContext context = (ServiceContext) m_parent;
                try
                {
                    Service service = context.lookup( uri );
                    addEntry( key, service );
                }
                catch( ServiceException e )
                {
                    final String error = 
                      "Unresolvable context reference."
                      + "\nComponent: " + m_component.getURI()
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
                  + "\nContainer: " + m_parent.getURI()
                  + "\nComponent: " + m_component.getURI()
                  + "\nContext Key: " + key;
                throw new ComponentException( error );
            }
        }
        else
        {
            CompositionController controller = m_component.getController();
            Service provider = controller.newService( m_component, part, key );
            addEntry( key, provider );
        }
    }

    public void addEntry( String key, Service provider ) throws DuplicateKeyException
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

    public void setEntry( String key, Service value )
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        put( key, value );
    }

    public Service getEntry( String key )
    {
        return (Service) super.get( key );
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
        else if( entry instanceof Service )
        {
            Service service = (Service) entry;
            try
            {
                return service.resolve();
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while attempting to resolve the value of context entry."
                  + "\nEnclosing component: " + m_component.getURI()
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

    public synchronized Service[] getProviders()
    {
        ArrayList list = new ArrayList();
        Object[] values = values().toArray();
        for( int i=0; i<values.length; i++ )
        {
            Object value = values[i];
            if( value instanceof Service )
            {
                list.add( value );
            }
        }
        return (Service[]) list.toArray( new Service[0] );
    }
}

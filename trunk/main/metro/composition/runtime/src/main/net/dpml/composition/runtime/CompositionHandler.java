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

package net.dpml.composition.runtime;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.Map;
import java.util.Hashtable;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

import net.dpml.composition.control.CompositionController;
import net.dpml.component.data.ComponentDirective;
import net.dpml.component.info.ServiceDescriptor;

import net.dpml.part.DelegationException;
import net.dpml.part.PartHandlerNotFoundException;
import net.dpml.part.PartNotFoundException;

import net.dpml.component.runtime.Available;
import net.dpml.component.runtime.Component;
import net.dpml.component.runtime.Consumer;
import net.dpml.component.runtime.ComponentException;
import net.dpml.component.runtime.ComponentNotFoundException;
import net.dpml.component.runtime.Container;
import net.dpml.component.runtime.Service;
import net.dpml.component.runtime.ServiceContext;
import net.dpml.component.runtime.ServiceException;
import net.dpml.component.runtime.ServiceNotFoundException;

import net.dpml.state.State;

import net.dpml.part.Part;

import net.dpml.logging.Logger;

/**
 * The CompositionHandler class manages the containment aspects of a component.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class CompositionHandler extends ComponentHandler implements Container, ServiceContext
{ 
    private final Map m_components = new Hashtable();

    public CompositionHandler(
      Logger logger, CompositionController controller, ClassLoader classloader, URI uri, 
      ComponentDirective profile, Component parent ) 
      throws ComponentException, PartHandlerNotFoundException, DelegationException, RemoteException
    {
        super( logger, controller, classloader, uri, profile, parent );
    }

   /**
    * Retrieve a component using a supplied key.
    * @param key the key
    * @return the component
    * @exception ComponentNotFoundException if the key is unknown
    */
    public Component getComponent( String key ) throws ComponentNotFoundException
    {
        Component component = getPartsTable().getComponent( key );
        if( null == component )
        {
            throw new ComponentNotFoundException( key );
        }
        else
        {
            return component;
        }
    }

   /**
    * Handle a request for the provision of a service relative to the supplied
    * uri. 
    *
    * @param uri a uri identifying or resolvable to a service
    */
    public Component lookup( URI uri ) throws ServiceException, RemoteException
    {
        String scheme = uri.getScheme();
        if( "service".equals( scheme ) )
        {
            String spec = uri.getSchemeSpecificPart();
            ServiceDescriptor request = new ServiceDescriptor( spec );
            return lookup( request );
        }
        else
        {
            final String error = 
              "Service lookup scheme [" + scheme + "] not recognized.";
            throw new ServiceException( error );
        }
    }

    public Component lookup( ServiceDescriptor spec ) throws ServiceException, RemoteException
    {
        getLogger().debug( "looking for [" + spec + "] in the parts of [" + this + "]" );
        Component[] candidates = getPartsTable().getComponents( spec );
        if( candidates.length == 0 )
        {
            getLogger().debug( "no candidates for [" + spec + "] in the parts of [" + this + "]" );

            Component parent = getParent();
            if( null == parent )
            {
                String value = spec.toString();
                throw new ServiceNotFoundException( value );
            }
            else if( parent instanceof ServiceContext )
            {
                ServiceContext context = (ServiceContext) parent;
                return context.lookup( spec );
            }
            else
            {
                String value = spec.toString();
                throw new ServiceNotFoundException( value );
            }
        }
        else
        {
            Component candidate = candidates[0];
            getLogger().debug( "selected [" + candidate + "] in [" + this + "]" );
            return createComponentReference( candidate );
        }
    }

    private Component createComponentReference( Component component )
    {
        ClassLoader classloader = getClassLoader();
        DefaultInvocationHandler handler = new DefaultInvocationHandler( component );
        ArrayList interfaces = new ArrayList();
        interfaces.add( Component.class );
        if( component instanceof Available )
        {
            interfaces.add( Available.class );
        }
        if( component instanceof Consumer )
        {
            interfaces.add( Consumer.class );
        }
        Class[] classes = (Class[]) interfaces.toArray( new Class[0] );
        return (Component) Proxy.newProxyInstance( classloader, classes, handler );
    }

    public boolean equals( Object other )
    {
        if( other instanceof CompositionHandler )
        {
            CompositionHandler handler = (CompositionHandler) other;
            return getLocalURI().equals( handler.getLocalURI() );
        }
        else if( Proxy.isProxyClass( other.getClass() ) )
        {
            InvocationHandler handler = Proxy.getInvocationHandler( other );
            if( handler instanceof DefaultInvocationHandler )
            {
                DefaultInvocationHandler h = (DefaultInvocationHandler) handler;
                Object instance = h.getInstance();
                return instance.equals( other );
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }
}

/*
 * Copyright 2004 Stephen J. McConnell.
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

import java.lang.reflect.Method;
import java.util.Map;

import net.dpml.composition.controller.CompositionController;

import net.dpml.component.control.LifecycleException;

/**
 * This makes a dynamic proxy for an object.  The object can be represented
 * by one, some or all of it's interfaces.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ApplianceInvocationHandler.java 2106 2005-03-21 18:46:10Z mcconnell@dpml.net $
 */
final class ContextManagerInvocationHandler extends ContextInvocationHandler
{
    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a context invocation handler.
    *
    * @param system the system context
    * @param model the component model
    */
    ContextManagerInvocationHandler( ComponentHandler component )
    {
        super( component );
    }

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

   /**
    * Invoke the specified method on underlying object.
    * This is called by the proxy object.
    *
    * @param proxy the proxy object
    * @param method the method invoked on proxy object
    * @param args the arguments supplied to method
    * @return the return value of method
    * @throws Throwable if an error occurs
    * @exception NullPointerException if any one of the proxy or method arguments
    *            is null, or if the object instance has been destroyed earlier.
    */
    public Object invoke( final Object proxy, final Method method, final Object[] args )
        throws Throwable, NullPointerException
    {
        String name = method.getName();
        Class source = method.getDeclaringClass();
        if( Object.class == method.getDeclaringClass() )
        {
            return method.invoke( this, args );
        }
        else if( Map.class == source )
        {
            ContextMap map = getComponentHandler().getContextMap();
            return method.invoke( map, args );
        }
        else if( name.startsWith( "set" ) )
        {
            String s = name.substring( 3 );
            String first = s.substring( 0, 1 ).toLowerCase();
            String remainder = s.substring( 1 );
            String key = first + remainder;
            if( args.length < 1 )
            {
                final String error = 
                  "Set method must have at least one parameter.";
                throw new IllegalStateException( error );
            }
            Object value = args[0];
            getComponentHandler().getContextMap().put( key, value );
            return null;
        }
        else
        {
            return super.invoke( proxy, method, args );
        }
    }
}

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

package net.dpml.composition.engine;

import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.dpml.component.info.EntryDescriptor;

import net.dpml.part.Component;


/**
 * Invoication handler for the Context inner class.  The invocation handler is 
 * responsible for the supply of values based on request invocations applied to 
 * a #Context inner-class.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ApplianceInvocationHandler.java 2106 2005-03-21 18:46:10Z mcconnell@dpml.net $
 */
class PartsInvocationHandler implements InvocationHandler
{
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

   /**
    * The component.
    */
    private final ComponentHandler m_handler;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a context invocation handler.
    *
    * @param handler the component handler
    */
    PartsInvocationHandler( ComponentHandler handler )
    {
        m_handler = handler;
    }

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

    private ComponentHandler getComponentHandler()
    {
        return m_handler;
    }

   /**
    * Invoke the specified method on underlying object.
    * This is called by the proxy object.
    *
    * @param proxy the proxy object
    * @param method the method invoked on proxy object
    * @param args the arguments supplied to method
    * @return the return value of method
    * @throws Throwable if an error occurs
    */
    public Object invoke( final Object proxy, final Method method, final Object[] args ) throws Throwable
    {
        Class source = method.getDeclaringClass();
        if( Object.class == source )
        {
            return method.invoke( this, args );
        }
        
        int semantic = getPartSemantic( method );
        String postfix = getPartPostfix( method );
        String key = getPartKey( method, semantic );
        
        Component handler = getComponentHandler().getPartHandler( key );
        handler.activate();
        
        if( GET == semantic )
        {
            if( null == postfix )
            {
                if( null == args || args.length == 0 )
                {
                    try
                    {
                        return handler.getInstance().getValue( false );
                    }
                    catch( ClassCastException e )
                    {
                        final String error = 
                          "Component handler ["
                          + handler
                          + "] could not be case to an approriate service class.";
                        throw new IllegalStateException( error );
                    }
                }
                else if( args.length == 1 )
                {
                    Object arg = args[0];
                    Class argClass = arg.getClass();
                    if( ( Boolean.TYPE == argClass ) || ( Boolean.class == argClass ) )
                    {
                        boolean policy = getBooleanValue( arg );
                        return handler.getInstance().getValue( policy );
                    }
                    else
                    {
                        final String error = 
                          "Part accessor parameter type not supported."
                          + "\nMethod: " + method.getDeclaringClass().getName() + "#" + method.getName()
                          + "\nParameter: " + arg.getClass();
                        throw new IllegalArgumentException( error );
                    }
                }
                else
                {
                    final String error = 
                      "Illegal number of parameters in ["
                      + method.getName()
                      + "].";
                    throw new IllegalArgumentException( error );
                }
            }
            else if( COMPONENT_KEY.equals( postfix ) )
            {
                if( handler instanceof Component )
                {
                    return (Component) handler; // TODO: wrap in a proxy
                }
                else
                {
                    final String error = 
                      "handler asssigned to the part key [" 
                      + key 
                      + "] is not a assignable to the Component class.";
                    throw new IllegalStateException( error );
                }
            }
            else
            {
                final String error = 
                  "Unrecognized postfix in the part accessor method ["
                  + method.getName()
                  + "] for the key ["
                  + key
                  + "].";
                throw new IllegalStateException( error );
            }
        }
        else if( RELEASE == semantic )
        {
            if( args.length == 1 )
            {
                Object instance = args[0];
                // Release processing not implemented.
                return null;
            }
            else
            {
                final String error = 
                  "Illegal number of parameters supplied under the 'release' method ["
                  + method.getName()
                  + "].";
                throw new IllegalStateException( error );
            }
        }
        else
        {
            final String error = 
              "Unrecognized semantic [" 
              + semantic 
              + "].";
            throw new IllegalArgumentException( error );
        }
    }

    private static int getPartSemantic( Method method )
    {
        String name = method.getName();
        if( name.startsWith( GET_KEY ) )
        {
            return GET;
        }
        else if( name.startsWith( RELEASE_KEY ) )
        {
            return RELEASE;
        }
        else
        {
            final String error = 
              "Unrecognized part accessor method signature ["
              + name 
              + "]";
            throw new IllegalArgumentException( error );
        }
    }
    
    public static String getPartPostfix( Method method )
    {
        String name = method.getName();
        if( name.endsWith( COMPONENT_KEY ) )
        {
            return COMPONENT_KEY;
        }
        else
        {
            return null;
        }
    }
    
    public static String getPartKey( Method method, int semantic )
    {
        String name = method.getName();
        if( GET == semantic )
        {
            if( name.endsWith( COMPONENT_KEY ) )
            {
                int n = COMPONENT_KEY.length();
                int j = name.length() - n;
                String substring = name.substring( 0, j );
                return formatKey( substring, 3 );
            }
            else
            {
               return formatKey( name, 3 );
            }
        }
        else if( RELEASE == semantic )
        {
            return formatKey( name, 7 );
        }
        else
        {
            final String error = 
              "Unrecognized part accessor method signature ["
              + name 
              + "]";
            throw new IllegalArgumentException( error );
        }
    }

    private static String formatKey( String method, int offset )
    {
        String string = method.substring( offset );
        return formatKey( string );
    }

    private static String formatKey( String key )
    {
        return Introspector.decapitalize( key );
    }

    private boolean getBooleanValue( Object object ) throws IllegalArgumentException
    {
        if( object instanceof Boolean )
        {
            Boolean value = (Boolean) object;
            return value.booleanValue();
        }
        else if( object instanceof String )
        {
            return Boolean.getBoolean( (String) object );
        }
        else
        {
            final String error = 
              "Cannot convert supplied parameter ["
              + object.getClass().getName()
              + "] to a boolean value.";
            throw new IllegalArgumentException( error );
        }
    }
    
    public static final int GET = 1;
    public static final int RELEASE = -1;
    public static final String GET_KEY = "get";
    public static final String RELEASE_KEY = "release";
    public static final String COMPONENT_KEY = "Component";
}

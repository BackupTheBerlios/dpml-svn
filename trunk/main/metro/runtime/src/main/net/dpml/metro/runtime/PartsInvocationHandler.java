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

package net.dpml.metro.runtime;

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import net.dpml.part.Manager;
import net.dpml.part.Component;
import net.dpml.part.Provider;
import net.dpml.part.Parts;

/**
 * Invoication handler for the Context inner class.  The invocation handler is 
 * responsible for the supply of values based on request invocations applied to 
 * a #Context inner-class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class PartsInvocationHandler implements InvocationHandler
{
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

   /**
    * The component.
    */
    private final PartsManager m_manager;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a context invocation handler.
    *
    * @param handler the component handler
    */
    PartsInvocationHandler( PartsManager manager )
    {
        m_manager = manager;
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
    */
    public Object invoke( final Object proxy, final Method method, final Object[] args ) throws Throwable
    {
        Class source = method.getDeclaringClass();
        if( Object.class == source )
        {
            return method.invoke( this, args );
        }
        else if( Parts.class == source )
        {
            return method.invoke( m_manager, args );
        }
        
        int semantic = getPartSemantic( method );
        String postfix = getPartPostfix( method );
        String key = getPartKey( method, semantic );
        
        Manager manager = m_manager.getManager( key );
        
        if( GET == semantic )
        {
            if( null == postfix )
            {
                if( null == args || args.length == 0 )
                {
                    try
                    {
                        return manager.getProvider().getValue( false );
                    }
                    catch( ClassCastException e )
                    {
                        final String error = 
                          "Component handler ["
                          + manager
                          + "] could not be cast to an approriate service class.";
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
                        return manager.getProvider().getValue( policy );
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
                if( manager instanceof Component )
                {
                    return (Component) manager;
                }
                else
                {
                    final String error = 
                      "Manager implementation ["
                      + manager.getClass().getName() 
                      + "] does not implement "
                      + Component.class.getName()
                      + ".";
                    throw new IllegalStateException( error );
                }
            }
            else if( MANAGER_KEY.equals( postfix ) )
            {
                return manager;
            }
            else if( MAP_KEY.equals( postfix ) )
            {
                return manager.getContextMap();
            }
            else if( PROVIDER_KEY.equals( postfix ) )
            {
                return manager.getProvider();
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
        if( name.endsWith( MAP_KEY ) )
        {
            return MAP_KEY;
        }
        if( name.endsWith( PROVIDER_KEY ) )
        {
            return PROVIDER_KEY;
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
            else if( name.endsWith( MAP_KEY ) )
            {
                int n = MAP_KEY.length();
                int j = name.length() - n;
                String substring = name.substring( 0, j );
                return formatKey( substring, 3 );
            }
            else if( name.endsWith( PROVIDER_KEY ) )
            {
                int n = PROVIDER_KEY.length();
                int j = name.length() - n;
                String substring = name.substring( 0, j );
                return formatKey( substring, 3 );
            }
            else if( name.endsWith( MANAGER_KEY ) )
            {
                int n = MANAGER_KEY.length();
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
    public static final String MAP_KEY = "Map";
    public static final String PROVIDER_KEY = "Provider";
    public static final String MANAGER_KEY = "Manager";
}
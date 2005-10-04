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

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.rmi.RemoteException;

import net.dpml.composition.control.CompositionController;

import net.dpml.component.control.Controller;
import net.dpml.component.runtime.Component;

import net.dpml.part.DelegationException;
import net.dpml.part.Control;

/**
 * The parts invocation handler maps client request for 'get', 'create' and 
 * 'release' relative to keys identified by the method name.  Method name
 * to key mapping is based on a [function][key] breakout where [function] 
 * is one of the recognized semantic keyworks:
 * 
 * <ol>
 *  <li>get - return a proxied annonomouse instance of the model identified by key</li>
 *  <li>create - creates a non-proxies instance</li>
 *  <li>release - releases the instance</li>
 * </ol>
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ApplianceInvocationHandler.java 2106 2005-03-21 18:46:10Z mcconnell@dpml.net $
 */
final class PartsInvocationHandler
  implements InvocationHandler
{
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

   /**
    * The component model.
    */
    private final ComponentHandler m_component;

    private final CompositionController m_controller;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Create a dependency invocation handler.
    *
    * @param model the component model 
    */
    PartsInvocationHandler( ComponentHandler component )
        throws NullPointerException
    {
        m_component = component;
        m_controller = component.getController();
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
        try
        {
            return execute( proxy, method, args );
        }
        catch( InvocationTargetException e )
        {
            throw e.getTargetException();
        }
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
    * @exception NullPointerException if any one of the proxy or method arguments
    *            is null, or if the object instance has been destroyed earlier.
    */
    private Object execute( final Object proxy, final Method method, final Object[] args )
        throws Throwable, NullPointerException, RemoteException
    {
        Class source = method.getDeclaringClass();
        if( Object.class == method.getDeclaringClass() )
        {
            return method.invoke( this, args );
        }

        int semantic = getPartSemantic( method );
        String postfix = getPartPostfix( method );
        String key = getPartKey( method, semantic );

        //
        // This is the point wyhere we have a reference to the consumer (the part proxy)
        // and we are about to reference a component.  We should be able to resolve
        // the component profile using the part descriptor key and then use the 
        // lifestyle logic to establish the instance and in doing so, register the 
        // consumer in a weak hashmap.  We can then use the consumer as the argument to 
        // the release operation or fall back to garbage collection of consumers.
        //

        Component provider = m_component.getPartsTable().getComponent( key );

        if( GET == semantic )
        {
            if( null == postfix )
            {
                //if( provider instanceof Control )
                //{
                    //Control resolvable = (Control) provider;
                    if( null == args || args.length == 0 )
                    {
                        return provider.resolve();
                    }
                    else if( args.length == 1 )
                    {
                        Object arg = args[0];
                        Class argClass = arg.getClass();
                        if( ( Boolean.TYPE == argClass ) || ( Boolean.class == argClass ) )
                        {
                            boolean policy = getBooleanValue( arg );
                            return provider.resolve( policy );
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
                //}
                //else
                //{
                //    final String error = 
                //      "Cannot resolve a value from a non-resolvable provider."
                //      + "\nProvider: " + provider.getURI()
                //      + "\nComponent: " + m_component.getURI();
                //    throw new IllegalStateException( error );
                //}
            }
            else if( CONTEXT_MANAGER_KEY.equals( postfix )
              || CONTEXT_MAP_KEY.equals( postfix ) )
            {
                Class clazz = method.getReturnType();
                if( !clazz.isInterface() )
                {
                    final String error = 
                      "The request for the context manager for the key ["
                      + key 
                      + "] in the component ["
                      + provider.getURI()
                      + "] could not be completed as the requested return type ["
                      + clazz.getName()
                      + "] is not an interface.";
                    throw new IllegalArgumentException( error );
                }
                else if( false == ( provider instanceof ComponentHandler ) )
                {
                    //
                    // TODO: we could construct a context manager with no 
                    // visible context entries - but let's leave that till
                    // later
                    //

                    final String error = 
                      "Dynamic creation of context managers for foreign component types is not supported."
                      + "\nClass: " + provider.getClass().getName()
                      + "\nProvider: " + provider.getURI();
                    throw new UnsupportedOperationException( error );
                }
                else
                {
                    ClassLoader classloader = provider.getClass().getClassLoader();
                    ContextManagerInvocationHandler handler = 
                      new ContextManagerInvocationHandler( (ComponentHandler) provider );
                    return Proxy.newProxyInstance( classloader, new Class[]{ clazz }, handler );
                }
            }
            else if( COMPONENT_KEY.equals( postfix ) )
            {
                return provider; // TODO: wrap in a proxy
            }
            else
            {
                final String error = 
                  "Unrecognized select postfix in the part accessor method ["
                  + method.getName()
                  + "] for the key ["
                  + key
                  + "].";
                throw new IllegalStateException( error );
            }
        }
        else if( RELEASE == semantic )
        {
            //if( provider instanceof Control )
            //{
                //Control resolvable = (Control) provider;
                if( args.length == 1 )
                {
                    provider.release( args[0] );
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
            //}
            //else
            //{
                //return null;
            //}
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

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

    private void assertNotNull( Object object, String key )
        throws NullPointerException
    {
        if( null == object )
        {
            throw new NullPointerException( key );
        }
    }

    /*
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "[parts {" );
        PartDescriptor[] entries = m_component.getType().getPartDescriptors();
        for( int i=0; i<entries.length; i++ )
        {
            PartDescriptor p = entries[i];
            buffer.append( p.toString() );
        }
        buffer.append( "}]" );
        return buffer.toString();
    }
    */

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
    
    // PartDescriptor stuff
    
    public static final String GET_KEY = "get";
    public static final String RELEASE_KEY = "release";
    public static final int GET = 1;
    public static final int RELEASE = -1;
    public static final String CONTEXT_MAP_KEY = "ContextMap";
    public static final String CONTEXT_MANAGER_KEY = "ContextManager";
    public static final String COMPONENT_KEY = "Component";

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
        if( name.endsWith( CONTEXT_MANAGER_KEY ) )
        {
            return CONTEXT_MANAGER_KEY;
        }
        else if( name.endsWith( CONTEXT_MAP_KEY ) )
        {
            return CONTEXT_MAP_KEY;
        }
        else if( name.endsWith( COMPONENT_KEY ) )
        {
            return COMPONENT_KEY;
        }
        else
        {
            return null;
        }
    }

    public static String getPartKey( Method method )
    {
        int semantic = getPartSemantic( method );
        return getPartKey( method, semantic );
    }

    public static String getPartKey( Method method, int semantic )
    {
        String name = method.getName();
        if( GET == semantic )
        {
            if( name.endsWith( CONTEXT_MANAGER_KEY ) )
            {
                int n = CONTEXT_MANAGER_KEY.length();
                int j = name.length() - n;
                String substring = name.substring( 0, j );
                return formatKey( substring, 3 );
            }
            else if( name.endsWith( CONTEXT_MAP_KEY ) )
            {
                int n = CONTEXT_MAP_KEY.length();
                int j = name.length() - n;
                String substring = name.substring( 0, j );
                return formatKey( substring, 3 );
            }
            else if( name.endsWith( COMPONENT_KEY ) )
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

}

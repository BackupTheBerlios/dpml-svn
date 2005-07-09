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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;

import net.dpml.logging.Logger;

import net.dpml.composition.control.CompositionController;

import net.dpml.configuration.Configuration;

import net.dpml.parameters.Parameters;

import net.dpml.part.control.LifecycleException;


/**
 * A lifestyle handler provides support for the aquisition and release
 * of component instances.  An implementation is responsible for the  
 * handling of new instance creation based on lifestyle policy declared
 * by a component.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: LifestyleManager.java 259 2004-10-30 07:24:40Z mcconnell $
 */
class LifecycleHandler extends LoggingHandler
{
    public static Class getInnerClass( Class subject, String postfix )
    {
        Class[] classes = subject.getClasses();
        return locateClass( postfix, classes );
    }

    public static Class locateClass( String postfix, Class[] classes )
    {
        for( int i=0; i<classes.length; i++ )
        {
            Class inner = classes[i];
            String name = inner.getName();
            if( name.endsWith( postfix ) )
            {
                return inner;
            }
        }
        return null;
    }

    private final Logger m_logger;
    private final CompositionController m_controller;

    LifecycleHandler( Logger logger, CompositionController controller )
    {
        m_logger = logger;
        m_controller = controller;
    }

    /**
     * Execute the incarnation sequence and return a new 
     * component instance. 
     *
     * @return a new component instance
     */
    public Object incarnate( ComponentHandler component ) throws LifecycleException, InvocationTargetException
    {
        if( getLogger().isDebugEnabled() )
        {
            final String message = 
              "Incarnating ["
              + component.getURI() 
              + "].";
            getLogger().debug( message );
        }

        URI uri = component.getURI();
        Class subject = component.getDeploymentClass();
        ClassLoader classloader = subject.getClassLoader();
        Constructor constructor = getConstructor( subject );
        Class parts = getInnerClass( subject, "$Parts" );
        Class context = getInnerClass( subject, "$Context" );
        Class[] classes = constructor.getParameterTypes();
        Object[] args = new Object[ classes.length ];

        for( int i=0; i<classes.length; i++ )
        {
            Class c = classes[i];
            if( java.util.logging.Logger.class.isAssignableFrom( c ) )
            {
                args[i] = getJavaLogger( component );
            }
            else if( Logger.class.isAssignableFrom( c ) )
            {
                args[i] = getLoggingChannel( component );
            }
            else if( ( null != parts ) && parts.isAssignableFrom( c ) )
            {
                args[i] = newPartsInvocationHandler( component, parts );
            }
            else if( ( null != context ) && context.isAssignableFrom( c ) )
            {
                args[i] = newContextInvocationHandler( component, context );
            }
            else if( Configuration.class.isAssignableFrom( c ) )
            {
                Configuration config = component.getConfiguration();
                args[i] = newConfigurationInvocationHandler( config );
            }
            else if( Parameters.class.isAssignableFrom( c ) )
            {
                Parameters params = component.getParameters();
                args[i] = newParametersInvocationHandler( params );
            }
            else
            {
                final String error =
                  "The component class ["
                  + subject.getName()
                  + "] decares an unsupported constructor parameter type ["
                  + c.getName()
                  + "].";
                throw new LifecycleException( error );
            }
        }

        try
        {
            return constructor.newInstance( args );
        }
        catch( Throwable e )
        {
            final String error = 
              "Instantiation failure."
              + "\nComponent class: " + constructor.getDeclaringClass();
            throw new LifecycleException( error, e );
        }
    }

    /**
     * Apply the etherialization process to the supplied instance.
     *
     * @param object the object to etherialize
     */
    public void etherialize( Object object )
    {
        if( null == object )
        {
            return;
        }

        Class subject = object.getClass();
        if( getLogger().isDebugEnabled() )
        {
            final String message = 
              "Initiating etherialization on an instance of the class ["
              + subject.getName() 
              + "].";
            getLogger().debug( message );
        }

        try
        {
            Method method = subject.getDeclaredMethod( "dispose", new Class[0] ); 
            method.invoke( object, new Object[0] );
        }
        catch( NoSuchMethodException e )
        {
            // ignore
        }
        catch( Exception e )
        {
            // ignore
        }
    }

    private Constructor getConstructor( Class subject ) throws LifecycleException
    {
        Constructor[] constructors = subject.getConstructors();
        if( constructors.length < 1 )
        {
            final String error =
              "The component class ["
              + subject.getName()
              + "] does not declare a public constructor.";
            throw new LifecycleException( error );
        }
        else if( constructors.length > 1 )
        {
            final String error =
              "The component class ["
              + subject.getName()
              + "] declares more than one public constructor.";
            throw new LifecycleException( error );
        }
        else
        {
            return constructors[0];
        }
    }

    private Object newParametersInvocationHandler( Parameters params ) throws LifecycleException
    {
        try
        {
            InvocationHandler handler = new DefaultInvocationHandler( params );
            ClassLoader classloader = params.getClass().getClassLoader();
            return Proxy.newProxyInstance( classloader, new Class[]{ Parameters.class }, handler );
        }
        catch( Throwable e )
        {
            final String error =
              "Unexpected error while attempting to construct the parameters invocation handler.";
            throw new LifecycleException( error, e );
        }
    }

    private Object newConfigurationInvocationHandler( Configuration config ) throws LifecycleException
    {
        try
        {
            InvocationHandler handler = new DefaultInvocationHandler( config );
            ClassLoader classloader = config.getClass().getClassLoader();
            return Proxy.newProxyInstance( classloader, new Class[]{ Configuration.class }, handler );
        }
        catch( Throwable e )
        {
            final String error =
              "Unexpected error while attempting to construct the configuration invocation handler.";
            throw new LifecycleException( error, e );
        }
    }

    private Object newContextInvocationHandler( 
      ComponentHandler component, Class clazz ) throws LifecycleException
    {
        try
        {
            InvocationHandler handler = new ContextInvocationHandler( component );
            ClassLoader classloader = component.getDeploymentClass().getClassLoader();
            return Proxy.newProxyInstance( classloader, new Class[]{ clazz }, handler );
        }
        catch( Throwable e )
        {
            final String error =
              "Unexpected error while attempting to construct the context invocation handler.";
            throw new LifecycleException( error, e );
        }
    }

    private Object newPartsInvocationHandler( ComponentHandler component, Class clazz ) throws LifecycleException
    {
        try
        {
            InvocationHandler handler = new PartsInvocationHandler( component );
            ClassLoader classloader = component.getDeploymentClass().getClassLoader();
            return Proxy.newProxyInstance( classloader, new Class[]{ clazz }, handler );
        }
        catch( Throwable e )
        {
            final String error =
              "Unexpected error while attempting to construct the parts invocation handler.";
            throw new LifecycleException( error, e );
        }
    }

    private Logger getLogger()
    {
        return m_logger;
    }
}

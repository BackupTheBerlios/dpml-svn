/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.runtime;

import dpml.util.DefaultLogger;
import dpml.util.StandardClassLoader;
import dpml.lang.Disposable;

import dpml.lang.LoggingInvocationHandler;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ServiceLoader;

import net.dpml.annotation.Services;
import net.dpml.annotation.LifestylePolicy;
import net.dpml.annotation.ActivationPolicy;

import net.dpml.lang.ServiceRegistry;
import net.dpml.lang.StandardServiceRegistry;
import net.dpml.lang.TypeCastException;

import net.dpml.state.State;
import net.dpml.state.StateListener;
import net.dpml.state.StateMachine;

import net.dpml.util.Logger;

import static net.dpml.runtime.Status.CREATION;
import static net.dpml.runtime.Status.INCARNATION;
import static net.dpml.runtime.Status.ETHERIALIZATION;
import static net.dpml.runtime.Status.TERMINATION;

import static net.dpml.annotation.ActivationPolicy.STARTUP;


/**
 * Abstract lifestyle handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class StandardProvider implements Provider, Disposable
{
    private static final EventQueue QUEUE = 
      new EventQueue( 
        new DefaultLogger( "dpml.metro" ), 
        "DPML Provider Event Queue" );

    private static final ServiceRegistry REGISTRY = 
      new StandardServiceRegistry();

    private final ComponentStrategy m_strategy;
    private final StateMachine m_machine;
    private final Class<?> m_class;
    private final String m_category;
    private final Logger m_logger;
    
    private Object m_instance = null;
    
    StandardProvider( ComponentStrategy strategy )
    {
        m_strategy = strategy;
        m_class = m_strategy.getComponentClass();
        final Logger logger = strategy.getComponentLogger();
        final State graph = strategy.getStateGraph();
        LifestylePolicy lifestyle = m_strategy.getLifestylePolicy();
        m_category = getLoggingCategory( lifestyle );
        m_logger = new DefaultLogger( m_category );
        m_machine = getStateMachine( m_logger, graph );
        
        ComponentEvent event = new ProviderEvent( m_strategy, this, CREATION );
        strategy.processEvent( event );

        ActivationPolicy activation = m_strategy.getActivationPolicy();
        if( STARTUP.equals( activation ) )
        {
            m_instance = newInstance();
        }
    }

   /**
    * Add a state change listener to the state machine.
    * @param listener the state listener
    */
    public void addStateListener( final StateListener listener )
    {
        if( m_logger.isTraceEnabled() )
        {
            m_logger.trace( "adding state listener" );
        }
        m_machine.addStateListener( listener );
    }
    
   /**
    * Remove a state listener from the state machine.
    * @param listener the state listener
    */
    public void removeStateListener( final StateListener listener )
    {
        if( m_logger.isTraceEnabled() )
        {
            m_logger.trace( "removing state listener" );
        }
        m_machine.removeStateListener( listener );
    }
    
    public <T>T getInstance( Class<T> type )
    {
        synchronized( this )
        {
            Object instance = getCachedInstance();
            if( type.isInterface() )
            {
                if( m_class.isAnnotationPresent( Services.class ) )
                {
                    InvocationHandler handler = new ComponentInvocationHandler( instance );
                    ClassLoader classloader = m_class.getClassLoader();
                    Services annotation = m_class.getAnnotation( Services.class );
                    Class[] services = annotation.value();
                    Object proxy = Proxy.newProxyInstance( classloader, services, handler );
                    try
                    {
                        return type.cast( proxy );
                    }
                    catch( ClassCastException cce )
                    {
                        StringBuffer buffer = new StringBuffer();
                        buffer.append( "Cannot assign component class [" );
                        buffer.append( instance.getClass().getName() + "]" );
                        buffer.append( " to the return type [" );
                        buffer.append( type.getName() + "] " );
                        buffer.append( "because the requested service type is not " );
                        buffer.append( "included in the public services published by the component." );
                        String error = buffer.toString();
                        throw new TypeCastException( error, m_class, type );
                    }
                }
                else
                {
                    try
                    {
                        return type.cast( instance );
                    }
                    catch( ClassCastException cce )
                    {
                        StringBuffer buffer = new StringBuffer();
                        buffer.append( "Cannot assign component class [" );
                        buffer.append( instance.getClass().getName() + "]" );
                        buffer.append( " to the return type [" );
                        buffer.append( type.getName() + "]" );
                        String error = buffer.toString();
                        throw new TypeCastException( error, m_class, type );
                    }
                }
            }
            else if( type.isAssignableFrom( m_class ) )
            {
                return type.cast( instance );
            }
            else
            {
                StringBuffer buffer = new StringBuffer();
                buffer.append( "Cannot assign component class [" );
                buffer.append( instance.getClass().getName() + "]" );
                buffer.append( " to the return type [" );
                buffer.append( type.getName() + "]" );
                String error = buffer.toString();
                throw new TypeCastException( error, m_class, type );
            }
        }
    }
    
    private Object getCachedInstance()
    {
        if( null == m_instance )
        {
            m_instance = newInstance();
        }
        return m_instance;
    }
    
    private Object newInstance()
    {
        if( m_logger.isTraceEnabled() )
        {
            m_logger.trace( "incarnation [" + m_class + "]" );
        }
        
        ComponentEvent event = new ProviderEvent( m_strategy, this, INCARNATION );
        m_strategy.processEvent( event );
        
        final String path = m_strategy.getComponentPath();
        final Constructor<?> constructor = getConstructor( m_class );
        final Class<?>[] types = constructor.getParameterTypes();
        final Object[] args = new Object[ types.length ];
        
        for( int i=0; i<types.length; i++ )
        {
            Class<?> type = types[i];
            if( java.util.logging.Logger.class.isAssignableFrom( type ) )
            {
                String category = getLoggingCategory();
                java.util.logging.Logger logger = 
                  java.util.logging.Logger.getLogger( category );
                args[i] = logger;
            }
            else if( Logger.class.isAssignableFrom( type ) )
            {
                args[i] = getTargetLogger();
            }
            else if( ContextInvocationHandler.isaContext( type, false ) )
            {
                ContextModel context = m_strategy.getContextModel();
                args[i] = ContextInvocationHandler.getProxiedInstance( type, m_strategy, context );
            }
            else if( PartsInvocationHandler.isaParts( type, false ) )
            {
                PartsDirective directive = m_strategy.getPartsDirective();
                args[i] = PartsInvocationHandler.getProxiedInstance( type, directive );
            }
            else if( "net.dpml.logging.Logger".equals( type.getName() ) ) // legacy
            {
                Logger logger = getTargetLogger();
                args[i] = LoggingInvocationHandler.getProxiedInstance( type, logger );
            }
            else
            {
                Object service = m_strategy.lookup( type );
                if( null != service )
                {
                    args[i] = service;
                }
                else
                {
                    service = REGISTRY.lookup( type );
                    if( null != service )
                    {
                        args[i] = service;
                    }
                    else if( type.isArray() )
                    {
                        args[i] = Array.newInstance( type.getComponentType(), 0 );
                    }
                    else
                    {
                        String stack = StandardClassLoader.toString( getClass(), m_class );
                        System.out.println( stack );
                        final String error = 
                          "No solution for the constructor parameter ["
                          + type.getName()
                          + "] at position " + i 
                          + " for the component ["
                          + path
                          + "] ("
                          + m_class.getName() 
                          + ").";
                        throw new ComponentError( error );
                    }
                }
            }
        }
        
        try
        {
            Object instance = constructor.newInstance( args );
            m_machine.initialize( instance );
            return instance;
        }
        catch( Throwable e )
        {
            ComponentError error = getComponentError( path, e );
            throw error;
        }
    }
    
    private Logger getTargetLogger()
    {
        String category = getLoggingCategory();
        return new DefaultLogger( category );
    }
    
    public void dispose()
    {
        if( null != m_instance )
        {
            ComponentEvent etherialization = new ProviderEvent( m_strategy, this, ETHERIALIZATION );
            m_strategy.processEvent( etherialization );
            if( m_logger.isTraceEnabled() )
            {
                m_logger.trace( "etherialization" );
            }
            Object instance = getUnwrappedInstance( m_instance );
            m_machine.terminate( instance );
            m_instance = null;
        }
        ComponentEvent termination = new ProviderEvent( m_strategy, this, TERMINATION );
        m_strategy.processEvent( termination );
    }
    
    private ComponentError getComponentError( String path, Throwable e )
    {
        if( e instanceof ComponentError )
        {
            return (ComponentError) e;
        }
        if( e instanceof UndeclaredThrowableException )
        {
            Throwable cause = e.getCause();
            if( null != cause )
            {
                return getComponentError( path, cause );
            }
        }
        if( e instanceof InvocationTargetException )
        {
            Throwable cause = e.getCause();
            if( null != cause )
            {
                return getComponentError( path, cause );
            }
        }
        final String error = 
          "Component '" 
          + path 
          + "' could not be instantiated ["
          + m_class.getName()
          + "]";
        return new ComponentError( error, e );
    }
    
   /**
    * Loacate and return the single public constructor declared by the component class.
    * @return the constructor
    * @exception ControlException if the class does not declare a unique public constructor
    */
    private Constructor getConstructor( Class c )
    {
        return ComponentStrategyHandler.getSingleConstructor( c );
    }
    
    private StateMachine getStateMachine( Logger logger, State graph )
    {
        return new DefaultStateMachine( QUEUE, logger, graph );
    }
    
    private Object getUnwrappedInstance( Object instance )
    {
        Class c = instance.getClass();
        if( Proxy.isProxyClass( c ) )
        {
            InvocationHandler handler = Proxy.getInvocationHandler( instance );
            if( handler instanceof ComponentInvocationHandler )
            {
                ComponentInvocationHandler h = (ComponentInvocationHandler) handler;
                return h.getInstance();
            }
            else
            {
                final String error = 
                  "Invocation handler class ["
                  + handler.getClass().getName() 
                  + "] is not recognized.";
                throw new IllegalArgumentException( error );
            }
        }
        else
        {
            return instance;
        }
    }
    
    private String getLoggingCategory()
    {
        return m_category;
    }
    
    private String getLoggingCategory( LifestylePolicy lifestyle )
    {
        String category = getBaseLoggingCategory();
        if( LifestylePolicy.SINGLETON.equals( lifestyle ) )
        {
            return category;
        }
        else
        {
            int id = System.identityHashCode( this );
            return category + "." + id;
        }
    }
    
    private String getBaseLoggingCategory()
    {
        String path = m_strategy.getComponentPath();
        if( path.startsWith( "/" ) )
        {
            return path.substring( 1 );
        }
        else
        {
            return path;
        }
    }
    
    public String toString()
    {
        String lifestyle = m_strategy.getLifestylePolicy().toString().toLowerCase();
        String path = m_strategy.getComponentPath();
        int id = System.identityHashCode( this );
        return lifestyle + ":" + path + "#" + id;
    }
}


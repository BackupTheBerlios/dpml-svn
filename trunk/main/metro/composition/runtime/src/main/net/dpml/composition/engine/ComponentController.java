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

package net.dpml.composition.engine;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.Map;
import java.util.ArrayList;
import java.util.EventObject;

import net.dpml.activity.Executable;
import net.dpml.activity.Startable;

import net.dpml.composition.control.CompositionController;
import net.dpml.composition.event.EventProducer;

import net.dpml.component.info.Type;
import net.dpml.component.info.ServiceDescriptor;
import net.dpml.component.data.Directive;
import net.dpml.component.data.ClasspathDirective;
import net.dpml.component.data.ClassLoaderDirective;
import net.dpml.component.data.ComponentDirective;
import net.dpml.component.model.ComponentModel;
import net.dpml.component.model.ContextModel;

import net.dpml.configuration.Configuration;

import net.dpml.logging.Logger;

import net.dpml.parameters.Parameters;

import net.dpml.part.Context;
import net.dpml.part.ControlException;
import net.dpml.part.ControlRuntimeException;
import net.dpml.part.Handler;
import net.dpml.part.HandlerException;
import net.dpml.part.PartException;

import net.dpml.state.State;
import net.dpml.state.impl.DefaultState;
import net.dpml.state.StateMachine;
import net.dpml.state.impl.DefaultStateMachine;

import net.dpml.transit.Category;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.Value;

/**
 * The ComponentController class is a controller of a component instance.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ComponentController
{
    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final Logger m_logger;
    private final CompositionController m_controller;

    //--------------------------------------------------------------------------
    // contructor
    //--------------------------------------------------------------------------

    public ComponentController( Logger logger, CompositionController controller )
    {
        m_logger = logger;
        m_controller = controller;
    }

    //--------------------------------------------------------------------------
    // ComponentController
    //--------------------------------------------------------------------------

   /**
    * Create a new remotely manageable component model.
    * @param directive the component definition
    * @return the managable component model
    */
    public ComponentModel createComponentModel( ComponentDirective directive ) throws PartException
    {
        ClassLoader anchor = Thread.currentThread().getContextClassLoader();
        String partition = Context.PARTITION_SEPARATOR;
        return createComponentModel( anchor, partition, directive );
    }
    
   /**
    * Create a new runtime handler using a supplied context.
    * @param context the managed context
    * @return the runtime handler
    */
    public ComponentHandler createComponentHandler( ComponentModel context )
    {
        ClassLoader anchor = Thread.currentThread().getContextClassLoader();
        return createComponentHandler( anchor, context );
    }
    
    //--------------------------------------------------------------------------
    // ComponentController
    //--------------------------------------------------------------------------

   /**
    * Create a new remotely manageable component model.
    * @param classloader the parent classloader
    * @param partition the enclosing partition
    * @param directive the component definition
    * @return the managable component model
    */
    ComponentModel createComponentModel( ClassLoader anchor, String partition, ComponentDirective directive ) throws PartException
    {
        try
        {
            ClassLoader classloader = createClassLoader( anchor, directive );
            return new DefaultComponentModel( classloader, this, directive, partition );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Creation of a new component model failed due to an remote exception.";
            throw new ControlRuntimeException( error, e );
        }
    }

   /**
    * Create a new runtime handler using a supplied anchor classloader and context.
    * @param anchor the anchor classloader
    * @param context the managed context
    * @return the runtime handler
    */
    ComponentHandler createComponentHandler( ClassLoader anchor, ComponentModel context )
    {
        try
        {
            final String name = context.getName();
            final String path = context.getContextPath();
            Logger logger = new DefaultLogger( path.substring( 1 ).replace( '/', '.' ) );
            final ClassLoaderDirective directive = context.getClassLoaderDirective();
            ClassLoader classloader = createClassLoader( anchor, directive, name );
            return new ComponentHandler( classloader, logger, this, context );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Creation of a new component handler failed due to an remote exception.";
            throw new ControlRuntimeException( error, e );
        }
    }
    
    Object createInstance( ComponentHandler handler ) throws ControlException, InvocationTargetException
    {
        Class subject = handler.getImplementationClass();
        Constructor constructor = getConstructor( subject );
        Class parts = getInnerClass( subject, "$Parts" );
        Class contextInnerClass = getInnerClass( subject, "$Context" );
        Class[] classes = constructor.getParameterTypes();
        Object[] args = new Object[ classes.length ];
        
        //
        // A component class may declare any of the following constructor parameter
        // types:
        // 1. net.dpml.logging.Logger;
        // 2. java.util.logging.Logger;
        // 3. net.dpml.configuration.Configuration;
        // 4. net.dpml.parameters.Parameters
        // 5. #Context
        // 6. #Parts
        //
        
        for( int i=0; i<classes.length; i++ )
        {
            Class c = classes[i];
            if( java.util.logging.Logger.class.isAssignableFrom( c ) )
            {
                String spec = getPathForLogger( handler );
                args[i] = java.util.logging.Logger.getLogger( spec );
            }
            else if( Logger.class.isAssignableFrom( c ) )
            {
                String spec = getPathForLogger( handler );
                args[i] = new DefaultLogger( spec );
            }
            else if( Parameters.class.isAssignableFrom( c ) )
            {
                args[i] = createParametersArgument( handler );
            }
            else if( Configuration.class.isAssignableFrom( c ) )
            {
                args[i] = createConfigurationArgument( handler );
            }
            else if( ( null != contextInnerClass ) && contextInnerClass.isAssignableFrom( c ) )
            {
                args[i] = createContextInvocationHandler( handler, contextInnerClass );
            }
            else
            {
                final String error = 
                  "Unable to resolve a solution for the component constructor parameter ["
                  + c.getName()
                  + "] at position " + i 
                  + " for the component ["
                  + handler.getPath()
                  + "].";
                throw new ControlException( error );
            }
        }

        try
        {
            return constructor.newInstance( args );
        }
        catch( InstantiationException e )
        {
            final String error = 
              "Instantiation failure within the component ["
              + handler
              + "].";
            throw new ControlException( error, e );
        }
        catch( IllegalAccessException e )
        {
            final String error = 
              "Cannot access component constructor in ["
              + handler
              + "].";
            throw new ControlException( error, e );
        }
    }
    
   /**
    * Load the component type for the supplied class.
    * @param the component class
    * @return the type defintion
    * @exception ControlException if an error occurs during type decoding
    */
    Type loadType( Class subject ) throws ControlException
    {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        try
        {
            Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
            return Type.decode( getClass().getClassLoader(), subject );
        }
        catch( Throwable e )
        {
            final String error =
              "Cannot load component type defintion: " + subject.getName();
            throw new ControlException( error, e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( context );
        }
    }
    
   /**
    * Load a state graph for a supplied class.  If the cass has a colocated
    * 'xgraph' resource then that resource will be used to construct the graph
    * instance, otherwise, if the class is assignable from Startable or Executable
    * the state graph will be resolved using the respective xgraph resources.  In 
    * neither case holds, an empty state graph will be returned.
    *
    * @param subject the subject class
    * @return a state graph instance
    */
    State loadStateGraph( Class subject ) throws ControlException
    {
        State state = loadStateFromResource( subject );
        if( null == state )
        {
            if( Executable.class.isAssignableFrom( subject ) )
            {
                return loadStateFromResource( Executable.class );
            }
            else if( Startable.class.isAssignableFrom( subject ) )
            {
                return loadStateFromResource( Startable.class );
            }
            else
            {
                return new DefaultState( "" );
            }
        }
        else
        {
            return state;
        }
    }
    
    Class loadComponentClass( ClassLoader classloader, String classname ) throws ControlException
    {
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
    
    File getWorkDirectory( ComponentHandler handler )
    {
        return m_controller.getControllerContext().getWorkingDirectory();
    }
    
    File getTempDirectory( ComponentHandler handler )
    {
        return m_controller.getControllerContext().getTempDirectory();
    }
    
   /**
    * Return an array of all of the classes representing service exported by
    * the component.
    * @param handler the component handler
    * @return the service class array
    * @exception ControlException if an error occurs while attempting to load a 
    *   declared service class
    */
    Class[] loadServiceClasses( ComponentHandler handler ) throws ControlException
    {
        Type type = handler.getType();
        ClassLoader classloader = handler.getClassLoader();
        return loadServiceClasses( type, classloader );
    }
    
    private ClassLoader createClassLoader( ClassLoader anchor, ComponentDirective profile )
    {
        final String name = profile.getName();
        final ClassLoaderDirective directive = profile.getClassLoaderDirective();
        return createClassLoader( anchor, directive, name );
    }
    
    private ClassLoader createClassLoader( ClassLoader anchor, ClassLoaderDirective directive, String name )
    {
        ClassLoader parent = anchor;
        final ClassLoader base = getClass().getClassLoader();
        final ClasspathDirective[] cpds = directive.getClasspathDirectives();
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
    
    private State loadStateFromResource( Class subject ) throws ControlException
    {
        String resource = subject.getName().replace( '.', '/' ) + ".xgraph";
        try
        {
            URL url = subject.getClassLoader().getResource( resource );
            if( null == url )
            {
                return null;
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

    private Logger getLogger()
    {
        return m_logger;
    }
    
   /**
    * Loacate and return the single public constructor declared by the component class.
    * @param subject the component class
    * @return the constructor
    * @exception ControlException if the class does not declare a unique public constructor
    */
    private Constructor getConstructor( Class subject ) throws ControlException
    {
        Constructor[] constructors = subject.getConstructors();
        if( constructors.length < 1 )
        {
            final String error =
              "The component class ["
              + subject.getName()
              + "] does not declare a public constructor.";
            throw new ControlException( error );
        }
        else if( constructors.length > 1 )
        {
            final String error =
              "The component class ["
              + subject.getName()
              + "] declares more than one public constructor.";
            throw new ControlException( error );
        }
        else
        {
            return constructors[0];
        }
    }

    private Class getInnerClass( Class subject, String postfix )
    {
        Class[] classes = subject.getClasses();
        return locateClass( postfix, classes );
    }

    private Class locateClass( String postfix, Class[] classes )
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

    private Object createParametersArgument( ComponentHandler handler ) throws ControlException
    {
        try
        {
            Parameters params = handler.getComponentModel().getParameters();
            InvocationHandler invocationHandler = new ParametersInvocationHandler( params );
            ClassLoader classloader = params.getClass().getClassLoader();
            return Proxy.newProxyInstance( 
              classloader, new Class[]{ Parameters.class }, invocationHandler );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Unable to construct the Parameters invocation handler due to a remote exception.";
            throw new ControlException( error, e );
        }
    }

    private Object createConfigurationArgument( ComponentHandler handler ) throws ControlException
    {
        try
        {
            Configuration config = handler.getComponentModel().getConfiguration();
            InvocationHandler invocationHandler = new ConfigurationInvocationHandler( config );
            ClassLoader classloader = config.getClass().getClassLoader();
            return Proxy.newProxyInstance( 
              classloader, new Class[]{ Configuration.class }, invocationHandler );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Unable to construct the Configuration invocation handler due to a remote exception.";
            throw new ControlException( error, e );
        }
    }
    
    private Object createContextInvocationHandler( ComponentHandler handler, Class clazz ) throws ControlException
    {
        try
        {
            InvocationHandler invocationHandler = new ContextInvocationHandler( handler );
            ClassLoader classloader = clazz.getClassLoader();
            return Proxy.newProxyInstance( classloader, new Class[]{ clazz }, invocationHandler );
        }
        catch( Throwable e )
        {
            final String error =
              "Unexpected error while attempting to construct the context invocation handler.";
            throw new ControlException( error, e );
        }
    }

    private String getPathForLogger( ComponentHandler handler )
    {
        String path = handler.getPath();
        return path.substring( 1 ).replace( '/', '.' );
    }
    
    Object getContextValue( ComponentHandler handler, String key ) throws ControlException
    {
        try
        {
            ComponentModel model = handler.getComponentModel();
            ContextModel context = model.getContextModel();
            Directive directive = context.getEntryDirective( key );
            if( null == directive )
            {
                if( handler.getType().getContextDescriptor().getEntryDescriptor( key ).isOptional() )
                {
                    return null;
                }
                else
                {
                    final String error =
                      "No solution defined for the context entry [" 
                      + key
                      + "].";
                    throw new ControlException( error );
                }
            }
            else
            {
                //
                // evaluate the directive
                //
                
                if( directive instanceof Value )
                {
                    Map map = handler.getContextMap();
                    ClassLoader classloader = handler.getClassLoader();
                    Value value = (Value) directive;
                    ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    try
                    {
                        Thread.currentThread().setContextClassLoader( classloader );
                        return value.resolve( map, false );
                    }
                    catch( Exception ve )
                    {
                        final String error = 
                          "Unexpect error while attempting to resolve a value from the context directive class ["
                          + directive.getClass().getName() 
                          + "] assigned to context key ["
                          + key
                          + "] in the component ["
                          + handler.getPath()
                          + "].";
                        throw new ControlException( error, ve );
                    }
                    finally
                    {
                        Thread.currentThread().setContextClassLoader( loader );
                    }
                }
                else
                {
                    final String error =
                      "Unsuppored context directive argument class: " + directive;
                    throw new ControlException( error );
                }
            }
        }
        catch( UnknownKeyException e )
        {
            final String error = 
              "Internal error in controller due to a reference to an unknown context key.";
            throw new ControlException( error, e );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Resolution of the context entry [" 
              + key
              + "] failed due to a remote exception.";
            throw new ControlException( error, e );
        }
    }
    
    private Class[] loadServiceClasses( Type type, ClassLoader classloader ) throws ControlException
    {
        ServiceDescriptor[] services = type.getServiceDescriptors();
        Class[] interfaces = new Class[ services.length ];
        for( int i=0; i<interfaces.length; i++ )
        {
            ServiceDescriptor service = services[i];
            interfaces[i] = loadServiceClass( type, classloader, service );
        }
        return interfaces;
    }

    private Class loadServiceClass( 
      Type type, ClassLoader classloader, ServiceDescriptor service ) throws ControlException
    {
        final String classname = service.getClassname();
        try
        {
            return classloader.loadClass( classname );
        }
        catch( ClassNotFoundException e )
        {
            final String error = 
              "Service class ["
              + classname
              + "] declared by the component class ["
              + type.getInfo().getClassname()
              + "] not found.";
            throw new ControlException( error, e );
        }
    }
}

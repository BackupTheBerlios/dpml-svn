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

package net.dpml.metro.runtime;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.ArrayList;

import net.dpml.metro.info.Type;
import net.dpml.metro.info.ServiceDescriptor;
import net.dpml.metro.data.ReferenceDirective;
import net.dpml.metro.data.ClasspathDirective;
import net.dpml.metro.data.ClassLoaderDirective;
import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.model.ComponentModel;
import net.dpml.metro.model.ContextModel;
import net.dpml.metro.ContextManager;

import net.dpml.configuration.Configuration;

import net.dpml.logging.Logger;

import net.dpml.parameters.Parameters;

import net.dpml.part.Directive;
import net.dpml.part.ControlException;
import net.dpml.part.Version;
import net.dpml.metro.PartsManager;
import net.dpml.part.remote.Component;
import net.dpml.part.remote.Model;
import net.dpml.part.ServiceNotFoundException;

import net.dpml.transit.Category;
import net.dpml.transit.Value;

import net.dpml.lang.UnknownKeyException;

/**
 * The ComponentController class is a controller of a component instance.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class ComponentController
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
    public ComponentModel createComponentModel( ComponentDirective directive ) throws ControlException
    {
        ClassLoader anchor = Thread.currentThread().getContextClassLoader();
        String partition = Model.PARTITION_SEPARATOR;
        return createComponentModel( anchor, partition, directive );
    }
    
   /**
    * Create a new runtime handler using a supplied context.
    * @param model the managed context
    * @param flag if true the component model is responsible for model lifecycle
    * @return the runtime handler
    */
    public DefaultComponentHandler createDefaultComponentHandler( 
      ComponentModel model, boolean flag ) throws ControlException
    {
        ClassLoader anchor = Thread.currentThread().getContextClassLoader();
        return createDefaultComponentHandler( anchor, model, flag );
    }
    
    public ClassLoader createClassLoader( 
      ClassLoader anchor, ComponentModel model ) throws ControlException
    {
        try
        {
            String name = model.getName();
            ClassLoaderDirective directive = model.getClassLoaderDirective();
            return createClassLoader( anchor, directive, name );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Classloader creation failed due to an remote exception.";
            throw new ControllerException( error, e );
        }
    }
    
    public ClassLoader createClassLoader( ClassLoader anchor, ComponentDirective profile )
    {
        final String name = profile.getName();
        final ClassLoaderDirective directive = profile.getClassLoaderDirective();
        return createClassLoader( anchor, directive, name );
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
    ComponentModel createComponentModel( 
      ClassLoader anchor, String partition, ComponentDirective directive ) throws ControlException
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
            throw new ControllerException( error, e );
        }
    }

   /**
    * Create a new top-level runtime handler using a supplied anchor classloader and context.
    * @param anchor the anchor classloader
    * @param context the managed context
    * @return the runtime handler
    */
    DefaultComponentHandler createDefaultComponentHandler( 
      ClassLoader anchor, ComponentModel context, boolean flag ) throws ControlException
    {
        return createDefaultComponentHandler( null, anchor, context, flag );
    }
    
   /**
    * Create a new runtime handler using a supplied parent, anchor and context.
    * @param parent the parent handler
    * @param anchor the anchor classloader
    * @param context the managed context
    * @return the runtime handler
    */
    DefaultComponentHandler createDefaultComponentHandler( 
      Component parent, ClassLoader anchor, ComponentModel context, boolean flag ) 
      throws ControlException
    {
        try
        {
            final String name = context.getName();
            final String path = context.getContextPath();
            Logger logger = new StandardLogger( path.substring( 1 ).replace( '/', '.' ) );
            final ClassLoaderDirective directive = context.getClassLoaderDirective();
            ClassLoader classloader = createClassLoader( anchor, directive, name );
            return new DefaultComponentHandler( parent, classloader, logger, this, context, flag );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Creation of a new component handler failed due to an remote exception.";
            throw new ControllerException( error, e );
        }
    }
    
    Object createInstance( DefaultProvider provider ) throws ControlException, InvocationTargetException
    {
        DefaultComponentHandler handler = provider.getDefaultComponentHandler();
        Class subject = handler.getImplementationClass();
        Constructor constructor = getConstructor( subject );
        Class parts = getPartsClass( subject );
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
                args[i] = new StandardLogger( spec );
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
                args[i] = createContextInvocationHandler( provider, contextInnerClass );
            }
            else if( parts.isAssignableFrom( c ) )
            {
                args[i] = createPartsInvocationHandler( handler, parts );
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
                throw new ControllerException( error );
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
            throw new ControllerException( error, e );
        }
        catch( IllegalAccessException e )
        {
            final String error = 
              "Cannot access component constructor in ["
              + handler
              + "].";
            throw new ControllerException( error, e );
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
            throw new ControllerException( error, e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( context );
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
            throw new ControllerException( error, e );
        }
    }
    
    File getWorkDirectory( DefaultComponentHandler handler )
    {
        return m_controller.getControllerContext().getWorkingDirectory();
    }
    
    File getTempDirectory( DefaultComponentHandler handler )
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
    DefaultService[] loadServices( DefaultComponentHandler handler ) throws ControlException
    {
        Type type = handler.getType();
        ClassLoader classloader = handler.getClassLoader();
        return loadServices( type, classloader );
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
        for( int i = ( uris.length - 1 ); i>-1; i-- )
        {
            URI uri = uris[i];
            String path = uri.toString();
            if( !exists( uri, parent ) )
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
            throw new ControllerException( error );
        }
        else if( constructors.length > 1 )
        {
            final String error =
              "The component class ["
              + subject.getName()
              + "] declares more than one public constructor.";
            throw new ControllerException( error );
        }
        else
        {
            return constructors[0];
        }
    }

    private Class getPartsClass( Class subject )
    {
        Class clazz = getInnerClass( subject, "$Parts" );
        if( null == clazz )
        {
            return PartsManager.class;
        }
        else
        {
            return clazz;
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

    private Object createParametersArgument( DefaultComponentHandler handler ) throws ControlException
    {
        try
        {
            Parameters params = handler.getComponentModel().getParameters();
            InvocationHandler invocationHandler = new ParametersInvocationHandler( params );
            ClassLoader classloader = params.getClass().getClassLoader();
            return Proxy.newProxyInstance( 
              classloader, new Class[]{Parameters.class}, invocationHandler );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Unable to construct the Parameters invocation handler due to a remote exception.";
            throw new ControllerException( error, e );
        }
    }

    private Object createConfigurationArgument( DefaultComponentHandler handler ) throws ControlException
    {
        try
        {
            Configuration config = handler.getComponentModel().getConfiguration();
            InvocationHandler invocationHandler = new ConfigurationInvocationHandler( config );
            ClassLoader classloader = config.getClass().getClassLoader();
            return Proxy.newProxyInstance( 
              classloader, new Class[]{Configuration.class}, invocationHandler );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Unable to construct the Configuration invocation handler due to a remote exception.";
            throw new ControllerException( error, e );
        }
    }
    
    private Object createContextInvocationHandler( DefaultProvider provider, Class clazz ) 
      throws ControlException
    {
        try
        {
            InvocationHandler invocationHandler = new ContextInvocationHandler( provider );
            ClassLoader classloader = clazz.getClassLoader();
            return Proxy.newProxyInstance( classloader, new Class[]{clazz}, invocationHandler );
        }
        catch( Throwable e )
        {
            final String error =
              "Unexpected error while attempting to construct the context invocation handler.";
            throw new ControllerException( error, e );
        }
    }

    private Object createPartsInvocationHandler( DefaultComponentHandler handler, Class clazz ) 
      throws ControlException
    {
        PartsManager manager = handler.getPartsManager();
        try
        {
            InvocationHandler invocationHandler = new PartsInvocationHandler( manager );
            ClassLoader classloader = clazz.getClassLoader();
            return Proxy.newProxyInstance( classloader, new Class[]{clazz}, invocationHandler );
        }
        catch( Throwable e )
        {
            final String error =
              "Unexpected error while attempting to construct the parts invocation handler.";
            throw new ControllerException( error, e );
        }
    }
    
    String getPathForLogger( DefaultComponentHandler handler )
    {
        String path = handler.getPath();
        return path.substring( 1 ).replace( '/', '.' );
    }
    
    Object getContextValue( DefaultComponentHandler handler, String key ) throws ControlException
    {
        try
        {
            ComponentModel model = handler.getComponentModel();
            ContextModel context = model.getContextModel();
            
            // check for an overriding locally assigned value
            
            Map map = handler.getContextMap();
            if( map.containsKey( key ) )
            {
                return map.get( key );
            }
            
            // resolve using defaults
            
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
                    throw new ControllerException( error );
                }
            }
            else
            {
                //
                // evaluate the directive
                //
                
                if( directive instanceof Value )
                {
                    Map symbols = handler.getSymbolMap();
                    ClassLoader classloader = handler.getClassLoader();
                    Value value = (Value) directive;
                    ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    try
                    {
                        Thread.currentThread().setContextClassLoader( classloader );
                        return value.resolve( symbols, false );
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
                        throw new ControllerException( error, ve );
                    }
                    finally
                    {
                        Thread.currentThread().setContextClassLoader( loader );
                    }
                }
                else if( directive instanceof ReferenceDirective )
                {
                    ReferenceDirective ref = (ReferenceDirective) directive;
                    URI uri = ref.getURI();
                    String scheme = uri.getScheme();
                    if( "service".equals( scheme ) || "lookup".equals( scheme ) )
                    {
                        String spec = uri.getSchemeSpecificPart();
                        ServiceDescriptor request = new ServiceDescriptor( spec );
                        DefaultService service = loadService( handler, request );
                        try
                        {
                            return executeLookup( handler, service );
                        }
                        catch( Exception ee )
                        {
                            final String error = 
                              "Unable to resolve a service provider for the class ["
                              + request.getClassname()
                              + "] requested in component ["
                              + handler.getPath()
                              + "] under the context key ["
                              + key
                              + "].";
                            throw new ControllerException( error, ee );
                        }
                    }
                    else if( "registry".equals( scheme ) )
                    {
                        try
                        {  
                            return uri.toURL().getContent();
                        }
                        catch( Exception ee )
                        {
                            final String error = 
                              "Unable to dereference rmi registry reference."
                              + "\nHandler: " + handler
                              + "\nKey: " + key
                              + "\nURI: " + uri;
                            throw new ControllerException( error, ee );
                        }
                    }
                    else
                    {
                        final String error = 
                        "Service lookup scheme [" + scheme + "] not recognized.";
                        throw new ControllerException( error );
                    }
                }
                else
                {
                    final String error =
                      "Unsuppored context directive argument class: " + directive;
                    throw new ControllerException( error );
                }
            }
        }
        catch( UnknownKeyException e )
        {
            final String error = 
              "Internal error in controller due to a reference to an unknown context key.";
            throw new ControllerException( error, e );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Resolution of the context entry [" 
              + key
              + "] failed due to a remote exception.";
            throw new ControllerException( error, e );
        }
    }
    
    private Object executeLookup( DefaultComponentHandler handler, DefaultService service ) 
      throws Exception
    {
        Component parent = handler.getParentHandler();
        if( null != parent )
        {
            try
            {
                Component component = parent.lookup( service );
                component.activate();
                return component.getProvider().getValue( false );
            }
            catch( RemoteException e )
            {
                final String error = 
                "Resolution of the service lookup for [" 
                  + service.getServiceClass().getName()
                  + "] failed due to a remote exception.";
                throw new ControllerException( error, e );
            }
        }
        else
        {
            String classname = service.getServiceClass().getName();
            throw new ServiceNotFoundException( 
              CompositionController.CONTROLLER_URI, classname );
        }
    }
    
    private DefaultService[] loadServices( Type type, ClassLoader classloader ) throws ControlException
    {
        ServiceDescriptor[] descriptors = type.getServiceDescriptors();
        DefaultService[] services = new DefaultService[ descriptors.length ];
        for( int i=0; i<descriptors.length; i++ )
        {
            ServiceDescriptor descriptor = descriptors[i];
            Class clazz = loadServiceClass( type, classloader, descriptor );
            Version version = descriptor.getVersion();
            services[i] = new DefaultService( clazz, version );
        }
        return services;
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
            throw new ControllerException( error, e );
        }
    }
    
    private DefaultService loadService( 
      DefaultComponentHandler handler, ServiceDescriptor service ) throws ControlException
    {
        ClassLoader classloader = handler.getClassLoader();
        final String classname = service.getClassname();
        try
        {
            Class c = classloader.loadClass( classname );
            Version version = service.getVersion();
            return new DefaultService( c, version );
        }
        catch( ClassNotFoundException e )
        {
            final String error = 
              "Service class ["
              + classname
              + "] declared as a dependency the component ["
              + handler.getPath()
              + "] not found.";
            throw new ControllerException( error, e );
        }
    }
}

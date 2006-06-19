/* 
 * Copyright 2005-2006 Stephen J. McConnell.
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
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.Map;

import net.dpml.component.Directive;
import net.dpml.component.ControlException;
import net.dpml.component.Component;
import net.dpml.component.Model;
import net.dpml.component.ServiceNotFoundException;
import net.dpml.component.Provider;

import net.dpml.lang.StandardClassLoader;
import net.dpml.lang.Version;
import net.dpml.lang.Classpath;
import net.dpml.lang.UnknownKeyException;
import net.dpml.lang.Value;

import net.dpml.metro.info.Type;
import net.dpml.metro.info.EntryDescriptor;
import net.dpml.metro.info.ServiceDescriptor;
import net.dpml.metro.data.LookupDirective;
import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.ComponentModel;
import net.dpml.metro.ContextModel;
import net.dpml.metro.PartsManager;
import net.dpml.metro.builder.ComponentTypeDecoder;

import net.dpml.util.Logger;
import net.dpml.util.DefaultLogger;
import net.dpml.util.EventQueue;

/**
 * The ComponentController class is a controller of a component instance.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class ComponentController
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------
    
    private static final ComponentTypeDecoder BUILDER = new ComponentTypeDecoder();
    
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
    
    CompositionController getCompositionController()
    {
        return m_controller;
    }

   /**
    * Build a classloader stack.
    * @param anchor the anchor classloader to server as the classloader chain root
    * @param classpath the part classpath definition
    * @exception IOException if an IO error occurs during classpath evaluation
    */
    public ClassLoader getClassLoader( ClassLoader anchor, Classpath classpath ) throws IOException
    {
        return getCompositionController().getClassLoader( anchor, classpath );
    }

   /**
    * Creation of a new component model using a supplied composition datatstructure
    * from which the classloader and deplyment strategy can be resolved.
    * @param composition a composition datastructure
    * @exception ControlException if an error occurs during model creation
    */
    public ComponentModel createComponentModel( DefaultComposition composition ) throws ControlException
    {
        try
        {
            String partition = m_controller.getPartition() + Model.PARTITION_SEPARATOR;
            ClassLoader classloader = composition.getClassLoader();
            Classpath classpath = composition.getClasspath();
            ComponentDirective directive = composition.getComponentDirective();
            String name = directive.getName();
            String path = partition + name;
            Logger logger = new DefaultLogger( path );
            EventQueue queue = m_controller.getEventQueue();
            return new DefaultComponentModel( queue, logger, classloader, classpath, this, directive, partition );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Creation of a new component model failed due to an remote exception.";
            throw new ControllerException( error, e );
        }
        catch( IOException e )
        {
            final String error = 
              "Creation of a new component model failed due to an IO exception.";
            throw new ControllerException( error, e );
        }
    }

    //--------------------------------------------------------------------------
    // ComponentController
    //--------------------------------------------------------------------------

   /**
    * Create a new remotely manageable component model.
    * @param anchor the parent classloader
    * @param classpath the classpath definition
    * @param partition the enclosing partition
    * @param directive the component definition
    * @return the managable component model
    */
    ComponentModel createComponentModel( 
      ClassLoader anchor, Classpath classpath, String partition, ComponentDirective directive ) 
      throws ControlException
    {
        try
        {
            Logger logger = new DefaultLogger( partition );
            ClassLoader classloader = getClassLoader( anchor, classpath );
            EventQueue queue = m_controller.getEventQueue();
            return new DefaultComponentModel( queue, logger, classloader, classpath, this, directive, partition );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Creation of a new component model failed due to an remote exception.";
            throw new ControllerException( error, e );
        }
        catch( IOException e )
        {
            final String error = 
              "Creation of a new component model failed due to an IO exception.";
            throw new ControllerException( error, e );
        }
    }

   /**
    * Create a new top-level runtime handler using a supplied anchor classloader and context.
    * @param classloader the anchor classloader
    * @param context the managed context
    * @param flag TRUE if this is a managed model
    * @return the runtime handler
    */
    DefaultComponentHandler createDefaultComponentHandler( 
      ClassLoader classloader, ComponentModel context, boolean flag ) throws ControlException
    {
        return createDefaultComponentHandler( null, classloader, context, flag );
    }
    
   /**
    * Create a new runtime handler using a supplied parent, anchor and context.
    * @param parent the parent handler
    * @param anchor the anchor classloader
    * @param context the managed context
    * @param flag TRUE if the supplied model is managed by the handler
    * @return the runtime handler
    */
    DefaultComponentHandler createDefaultComponentHandler( 
      Provider parent, ClassLoader anchor, ComponentModel context, boolean flag ) 
      throws ControlException
    {
        try
        {
            final String name = context.getName();
            final String path = context.getContextPath();
            Logger logger = new DefaultLogger( path );
            Classpath classpath = context.getClasspath();
            ClassLoader classloader = resolveClassLoader( anchor, context );
            EventQueue queue = m_controller.getEventQueue();
            return new DefaultComponentHandler( queue, parent, classloader, logger, this, context, flag );
        }
        catch( RemoteException e )
        {
            final String error = 
              "Creation of a new component handler failed due to an remote exception.";
            throw new ControllerException( error, e );
        }
        catch( ControlException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error = 
              "Creation of a new component handler failed due to an unexpected error.";
            throw new ControllerException( error, e );
        }
    }
    
    private ClassLoader resolveClassLoader( ClassLoader anchor, ComponentModel model ) throws IOException
    {
        if( model instanceof DefaultComponentModel )
        {
            getLogger().debug( "using model classloader (local mode) for " + model.getContextPath() );
            DefaultComponentModel impl = (DefaultComponentModel) model;
            return impl.getClassLoader();
        }
        else
        {
            getLogger().debug( "building new classloader (remote mode) for " + model.getContextPath() );
            Classpath classpath = model.getClasspath();
            return getClassLoader( anchor, classpath );
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
        // 3. #Context
        // 4. #Parts
        //
        
        for( int i=0; i<classes.length; i++ )
        {
            Class c = classes[i];
            if( java.util.logging.Logger.class.isAssignableFrom( c ) )
            {
                String spec = getPathForLogger( handler );
                args[i] = java.util.logging.Logger.getLogger( spec );
            }
            else if( net.dpml.logging.Logger.class.isAssignableFrom( c ) )
            {
                String spec = getPathForLogger( handler );
                args[i] = new StandardLogger( spec );
            }
            else if( Logger.class.isAssignableFrom( c ) )
            {
                String spec = getPathForLogger( handler );
                args[i] = new DefaultLogger( spec );
            }
            else if( ( null != contextInnerClass ) && contextInnerClass.isAssignableFrom( c ) )
            {
                args[i] = createContextInvocationHandler( provider, contextInnerClass );
            }
            else if( parts.isAssignableFrom( c ) )
            {
                args[i] = createPartsInvocationHandler( provider, parts );
            }
            else
            {
                final String error = 
                  "Unable to resolve a solution for the component constructor parameter ["
                  + c.getName()
                  + "] at position " + i 
                  + " for the component ["
                  + handler.getPath()
                  + "] ("
                  + subject.getName() 
                  + ")."
                  + StandardClassLoader.toString( getClass().getClassLoader(), c.getClassLoader() );
                throw new ControllerException( error );
            }
        }

        try
        {
            Object instance = constructor.newInstance( args );
            return instance;
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
    * @param subject the component class
    * @return the type definition
    * @exception ControlException if an error occurs during type decoding
    */
    Type loadType( Class subject ) throws ControlException
    {
        try
        {
            return BUILDER.loadType( subject );
        }
        catch( Throwable e )
        {
            final String error =
              "An error occured while attempting to load component type definition for the class: " 
              + subject.getName();
            throw new ControllerException( error, e );
        }
    }
    
    Class loadComponentClass( ClassLoader classloader, String classname ) throws ControlException
    {
        if( null == classloader )
        {
            throw new NullPointerException( "classloader" );
        }
        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }
        
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

    private Object createPartsInvocationHandler( DefaultProvider provider, Class clazz ) 
      throws ControlException
    {
        try
        {
            InvocationHandler invocationHandler = new PartsInvocationHandler( provider );
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
        String category = path.replace( '/', '.' );
        return trimCategoryPath( category );
    }
    
    String trimCategoryPath( String path )
    {
        if( null == path )
        {
            return "";
        }
        else
        {
            if( path.startsWith( "." ) )
            {
                String substring = path.substring( 1 );
                return trimCategoryPath( substring );
            }
            else if( path.endsWith( "." ) )
            {
                String substring = path.substring( 0, path.length() - 1 );
                return trimCategoryPath( substring );
            }
            else
            {
                return path;
            }
        }
    }
    
    Object getContextValue( DefaultProvider provider, String key ) throws ControlException
    {
        DefaultComponentHandler handler = provider.getDefaultComponentHandler();
        
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
            
            // resolve using entry directive
            
            EntryDescriptor descriptor = handler.getType().getContextDescriptor().getEntryDescriptor( key );
            Directive directive = context.getEntryDirective( key );
            if( null == directive )
            {
                if( descriptor.isOptional() )
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
                    String defaultTarget = descriptor.getClassname();
                    try
                    {
                        Thread.currentThread().setContextClassLoader( classloader );
                        return value.resolve( defaultTarget, symbols, false );
                    }
                    catch( Exception ve )
                    {
                        final String error = 
                          "Unexpected error while attempting to resolve a value from a context directive ["
                          + directive.getClass().getName() 
                          + "] assigned to key ["
                          + key
                          + "] in component ["
                          + handler.getPath()
                          + "].";
                        throw new ControllerException( error, ve );
                    }
                    finally
                    {
                        Thread.currentThread().setContextClassLoader( loader );
                    }
                }
                else if( directive instanceof LookupDirective )
                {
                    LookupDirective ref = (LookupDirective) directive;
                    String spec = ref.getServiceClassname();
                    ServiceDescriptor request = new ServiceDescriptor( spec );
                    DefaultService service = loadService( handler, request );
                    try
                    {
                        return executeLookup( provider, service );
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
                else if( directive instanceof ComponentDirective )
                {
                    ClassLoader classloader = handler.getClassLoader();
                    ComponentDirective componentDirective = (ComponentDirective) directive;
                    Classpath classpath = new Classpath();
                    String partition = handler.getPath();
                    ComponentModel componentModel = 
                      createComponentModel( classloader, classpath, partition, componentDirective );
                    try
                    {
                        Component component = m_controller.createComponent( componentModel );
                        return component.getProvider().getValue( false );
                    }
                    catch( Exception ee )
                    {
                        final String error = 
                          "Unable to resolve context entry component directive ["
                          + componentDirective
                          + "] requested in component ["
                          + handler.getPath()
                          + "] under the context key ["
                          + key
                          + "].";
                        throw new ControllerException( error, ee );
                    }
                }
                else
                {
                    final String error =
                      "Unsuppored context directive class: " 
                      + directive.getClass();
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
    
    private Object executeLookup( DefaultProvider provider, DefaultService service ) 
      throws Exception
    {
        Provider parent = provider.getParent();
        if( null != parent )
        {
            try
            {
                Provider p = parent.lookup( service );
                return p.getValue( false );
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

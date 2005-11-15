/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.composition.control;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLClassLoader;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Properties;

import net.dpml.logging.Logger;

import net.dpml.component.data.ClassLoaderDirective;
import net.dpml.component.data.ClasspathDirective;
import net.dpml.component.data.ComponentDirective;
import net.dpml.component.data.ValueDirective;
import net.dpml.component.data.Directive;
import net.dpml.component.info.InfoDescriptor;
import net.dpml.component.info.Type;
import net.dpml.component.model.ComponentModel;

import net.dpml.composition.runtime.ComponentHandler;
import net.dpml.composition.runtime.ValueHandler;
import net.dpml.composition.runtime.ValueController;
import net.dpml.composition.runtime.ComponentController;
import net.dpml.composition.runtime.CompositionHandler;
import net.dpml.composition.runtime.DefaultLogger;

import net.dpml.component.control.ClassLoaderManager;
import net.dpml.component.control.ControllerContext;
import net.dpml.component.control.ControllerException;
import net.dpml.component.control.ControllerRuntimeException;
import net.dpml.component.control.Controller;
import net.dpml.component.control.Disposable;
import net.dpml.component.control.LifecycleException;
import net.dpml.component.control.UnsupportedPartTypeException;
import net.dpml.component.runtime.Component;
import net.dpml.component.runtime.ClassLoadingContext;
import net.dpml.component.runtime.ComponentException;
import net.dpml.component.runtime.Container;
import net.dpml.component.runtime.Service;

import net.dpml.part.DelegationException;
import net.dpml.part.Part;
import net.dpml.part.PartException;
import net.dpml.part.PartHandlerNotFoundException;
import net.dpml.part.PartNotFoundException;
import net.dpml.part.Context;
import net.dpml.part.Handler;

import net.dpml.transit.Plugin;
import net.dpml.transit.Category;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.model.Value;

/**
 * The composition controller is the controller used to establish remotely accessible
 * component controls.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class CompositionController extends CompositionPartHandler implements Controller, ClassLoaderManager
{
    //--------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------

    public static final String SELF = "self";

    //--------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------

    private final net.dpml.composition.engine.ComponentController m_controller;
    
    private final Logger m_logger;
    private final ValueController m_valueController;
    private final ComponentController m_componentController;
    private final ControllerContext m_context;

    //private final CompositionHandler m_root;
    //private final LifestyleHandler m_lifestyleHandler;

    //--------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------

    public CompositionController( net.dpml.transit.Logger logger )
       throws ControllerException
    {
        this( new CompositionContext( logger, null, null ) );
    }

    protected CompositionController( ControllerContext context )
       throws ControllerException
    {
        super( context );

        m_context = context;
        m_logger = new StandardLogger( context.getLogger() );
        m_valueController = new ValueController( this );
        m_componentController = new ComponentController( m_logger, this );
        //m_lifestyleHandler = new LifestyleHandler( m_logger, m_componentController );
        m_logger.debug( "controller: " + CONTROLLER_URI );
        
        m_controller = new net.dpml.composition.engine.ComponentController( m_logger, this );
    }
    
    //--------------------------------------------------------------------
    // PartHandler
    //--------------------------------------------------------------------
    
   /**
    * Create and return a new management context using the supplied part
    * as the inital management state.
    *
    * @param part the part data structure
    * @return the management context instance
    */
    public Context createContext( Part part ) throws PartException
    {
        if( part instanceof ComponentDirective )
        {
            ComponentDirective directive = (ComponentDirective) part;
            return m_controller.createComponentModel( directive );
        }
        else
        {
            //
            // TODO delegate to foreign controller
            //
            
            final String error =
              "Construction of a managment context for the part class ["
              + part.getClass().getName() 
              + "] is not supported.";
            throw new PartException( error );
        }
    }
    
   /**
    * Create and return a remote reference to a component handler.
    * @return the component handler
    */
    public Handler createHandler( Context context ) throws Exception
    {
        if( context instanceof ComponentModel )
        {
            ComponentModel model = (ComponentModel) context;
            return m_controller.createComponentHandler( model );
        }
        else
        {
            //
            // TODO delegate to foreign controller
            //
            
            final String error =
              "Construction of a handler for the context class ["
              + context.getClass().getName() 
              + "] is not supported.";
            throw new PartException( error );
        }
    }

   /**
    * Return the controller for the supplied context.
    * @return the context handler
    */
    /*
    public Control getController( Context context ) throws Exception
    {
        if( context instanceof ComponentModel )
        {
            return m_controller;
        }
        else
        {
            //
            // TODO delegate to foreign controller
            //
            
            final String error =
              "Construction of a control for the context class ["
              + context.getClass().getName() 
              + "] is not supported.";
            throw new PartException( error );
        }
    }
    */
    
    //--------------------------------------------------------------------
    // stuff
    //--------------------------------------------------------------------

   /**
    * Return the controllers runtime context. The runtime context holds infromation 
    * about the working and temporary directories and a uri identifying the execution 
    * domain.
    * 
    * @return the runtime context
    */
    public ControllerContext getControllerContext()
    {
        return m_context;
    }

    public ComponentController getComponentController()
    {
        return m_componentController;
    }

    public ValueController getValueController()
    {
        return m_valueController;
    }

    public Logger getLogger()
    {
        return m_logger;
    }

    //--------------------------------------------------------------------
    // Controller
    //--------------------------------------------------------------------

    public Object getContent( URLConnection connection, Class[] classes ) throws IOException
    {
        Object content = super.getContent( connection, classes );
        if( null != content )
        {
            return content;
        }

        URL url = connection.getURL();   
        try
        {
            if( classes.length == 0 )
            {
                return loadPart( url );
            }
            else
            {
                for( int i=0; i<classes.length; i++ )
                {
                    Class c = classes[i];
                    if( Component.class.isAssignableFrom( c ) )
                    {
                        URI uri = new URI( url.toString() );
                        return newComponent( uri );
                    }
                    else if( Object.class.equals( c ) )
                    {
                        URI uri = new URI( url.toString() );
                        Component component = newComponent( uri );
                        return component.resolve( false );
                    }
                }
            }
            return null;
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to establish content for the : " + url;
            IOException cause = new IOException( error );
            cause.initCause( e );
            throw cause;
        }
    }

    //-------------------------------------------------------------------------------
    // ClassLoaderManager
    //-------------------------------------------------------------------------------

   /**
    * Load a classloader using the supplied anchor classloader and 
    * component directive.
    * 
    * @param anchor the anchor classloader
    * @param part a component directive
    */
    public ClassLoader createClassLoader( ClassLoader anchor, Part part )
    {
        if( part instanceof ComponentDirective )
        {
            ComponentDirective profile = (ComponentDirective) part;
            URI partition = getPartition();
            return getClassLoader( anchor, partition, profile );
        }
        else
        {
            return anchor;
        }
    }
    
    //-------------------------------------------------------------------------------
    // Controller
    //-------------------------------------------------------------------------------
    
   /**
    * Returns an control object using the supplied part as the construction template.
    * @param uri the part construction template
    * @return the control instance
    */
    public Value resolve( URI uri ) 
      throws Exception
    {
        return newComponent( uri );
    }

   /**
    * Construct a new top-level component.
    *
    * @param uri a uri identifying a part from which the component will be created.
    * @return the new component
    * @exception ComponentException is an error occurs during component establishment
    * @exception IOException if an error occurs while attempting to resolve the component part uri
    * @exception PartNotFoundException if the uri could not be resolved to a physical resource
    * @exception PartHandlerNotFoundException if the part references a handler but the handler could not be found
    * @exception DelegationException if an error occurs following handover of control to a foreign controller
    */
    public Component newComponent( URI uri )
      throws IOException, PartException, RemoteException
    {
        getLogger().debug( "loading part " + uri );
        Part part = loadPart( uri );
        Component component = newComponent( null, part, "" );
        if( component instanceof CompositionHandler )
        {
            CompositionHandler handler = (CompositionHandler) component;
            addShutdownHook( getLogger(), handler );
        }
        return component;
    }

   /**
    * Construct a new component using the supplied part as the defintion of the 
    * component type and deployment criteria.
    *
    * @param container the enclosing parent container
    * @param part component definition including type and deployment data
    * @param name the name to assign to the new component
    * @return a new component
    * @exception ComponentException is an error occurs during component establishment
    * @exception PartHandlerNotFoundException if the part references a handler but the handler could not be found
    * @exception DelegationException if an error occurs following handover of control to a foreign controller
    * @exception UnsupportedPartTypeException if the component type is recognized but not supported
    */
    public Component newComponent( Container container, Part part, String name )
      throws PartException, RemoteException
    {
        URI partition = getPartition( container );
        if( isRecognizedPart( part ) )
        {
            ClassLoader anchor = getAnchorClassLoader( container );
            return newComponent( container, anchor, part, name );
        }
        else
        {
            URI handlerUri = part.getPartHandlerURI();
            getLogger().info( "delegating to: " + handlerUri );
            Controller controller = (Controller) getPrimaryController( handlerUri );
            getLogger().info( "delegate established: " + controller );
            return controller.newComponent( container, part, name );
        }
    }

   /**
    * Construct a new component using the supplied part as the defintion of the 
    * component type and deployment criteria.
    *
    * @param container the enclosing container
    * @param part component definition including type and deployment data
    * @param name the name to assign to the new component
    * @return a new component
    * @exception ComponentException is an error occurs during component establishment
    * @exception PartHandlerNotFoundException if the part references a handler but the handler could not be found
    * @exception DelegationException if an error occurs following handover of control to a foreign controller
    * @exception UnsupportedPartTypeException if the component type is recognized but not supported
    */
    public Component newComponent( Container container, ClassLoader classloader, Part part, String name )
      throws PartException, RemoteException
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }

        URI partition = getPartition( container );
        if( isRecognizedPart( part ) )
        {
            if( part instanceof ValueDirective )
            {
                ValueDirective directive = (ValueDirective) part;
                URI id = createURI( partition, name );
                Logger logger = getLogger().getChildLogger( name );
                logger.debug( "constructing value [" + name + "] as [" + id + "]" );
                return new ValueHandler( logger, this, classloader, id, name, directive, container );
            }
            else if( part instanceof ComponentDirective )
            {
                ComponentDirective profile = (ComponentDirective) part;
                String defaultName = profile.getName();
                URI id = createURI( partition, name );
                getLogger().debug( "constructing component: " + name + " as [" + id + "]" );
                Logger logger = getLogger().getChildLogger( name );
                ClassLoader loader = getClassLoader( classloader, id, profile );
                return new CompositionHandler( logger, this, loader, id, profile, container );
            }
            else
            {
                String classname = part.getClass().getName();
                final String error = 
                  "Unsupported part implementation class ["
                  + classname
                  + "] passed to newComponent/3.";
                throw new UnsupportedPartTypeException( CONTROLLER_URI, classname, error );
            }
        }
        else
        {
            URI handlerUri = part.getPartHandlerURI();
            getLogger().info( "delegating to: " + handlerUri );
            Controller controller = (Controller) getPrimaryController( handlerUri );
            getLogger().info( "delegate established: " + controller );
            return controller.newComponent( container, part, name );
        }
    }

   /**
    * Construct a new container using the supplied part as the defintion of the 
    * component type and deployment criteria.  This method is typically used by
    * buildtime tools where the buildtime classloader is establised prior to 
    * component deployment.
    *
    * @param classloader the root classloader
    * @param part component definition including type and deployment data
    * @return a new component
    * @exception ComponentException is an error occurs during component establishment
    * @exception PartHandlerNotFoundException if the part references a handler but the handler could not be found
    * @exception DelegationException if an error occurs following handover of control to a foreign controller
    * @exception UnsupportedPartTypeException if the component type is recognized but not supported
    */
    public Container newContainer( ClassLoader classloader, Part part )
      throws PartException, RemoteException
    {
        URI partition = getPartition();
        if( isRecognizedPart( part ) )
        {
            if( part instanceof ComponentDirective )
            {
                Component parent = null;
                ComponentDirective profile = (ComponentDirective) part;
                String name = profile.getName();
                URI id = createURI( partition, name );
                getLogger().debug( "creating component: " + name + " as [" + id + "]" );
                Logger logger = getLogger().getChildLogger( name );
                ClassLoader loader = getClassLoader( classloader, id, profile );
                return new CompositionHandler( logger, this, loader, id, profile, parent );
            }
            else
            {
                System.out.println( "THIS CL: " + getClass().getClassLoader() );
                String classname = part.getClass().getName();
                final String error = 
                  "Unsupported part implementation class ["
                  + classname
                  + "] passed to newComponent/3.";
                throw new UnsupportedPartTypeException( CONTROLLER_URI, classname, error );
            }
        }
        else
        {
            String classname = part.getClass().getName();
            final String error = 
              "Method does not support foreign part handlers."
              + "\nController: " + part.getPartHandlerURI()
              + "\nPart classname: " + classname;
            throw new UnsupportedPartTypeException( CONTROLLER_URI, classname, error );
        }
    }

    private ClassLoader getAnchorClassLoader( Container container )
    {
        if( container instanceof ClassLoadingContext )
        {
            ClassLoadingContext context = (ClassLoadingContext) container;
            ClassLoader classloader = context.getClassLoader();
            if( classloader != null )
            {
                if( classloader instanceof CompositionClassLoader )
                {
                    return classloader;
                }
                else
                {
                    URI partition = getPartition( container );
                    return new CompositionClassLoader( 
                      partition, Category.PROTECTED, getClass().getClassLoader(), new URI[0], classloader );
                }
            }
        }
        return Logger.class.getClassLoader();
    }

    //--------------------------------------------------------------------
    // CompositionController
    //--------------------------------------------------------------------

    private URI getPartition()
    {
        return ROOT_URI;
    }

    private URI getPartition( Container container )
    {
        if( null == container )
        {
            return getPartition();
        }
        else
        {
            try
            {
                return container.getURI();
            }
            catch( RemoteException e )
            {
                final String error =
                  "Container raised a remote exception in response to uri request."
                  + "Container class: " + container.getClass().getName();
                throw new ControllerRuntimeException( CONTROLLER_URI, error, e );
            }
        }
    }

    private String getName( String defaultValue, String preferredValue )
    {
        if( null != preferredValue ) 
        {
            return preferredValue;
        }
        else
        {
            return defaultValue;
        }
    }

    //--------------------------------------------------------------------
    // private
    //--------------------------------------------------------------------

    private ClassLoader getClassLoader( ClassLoader anchor, URI partition, ComponentDirective profile )
    {
        ClassLoader parent = anchor;
        final ClassLoader base = getClass().getClassLoader();
        final String name = profile.getName();
        final ClassLoaderDirective cld = profile.getClassLoaderDirective();
        final ClasspathDirective[] cpds = cld.getClasspathDirectives();
        for( int i=0; i<cpds.length; i++ )
        {
            ClasspathDirective cpd = cpds[i];
            Category tag = cpd.getCategory();
            URI[] uris = filter( cpd.getURIs(), parent );
            if( uris.length > 0 )
            {
                Logger logger = getLogger();
                logger.debug( "creating " + tag + " classloader with " + uris.length + " entries" );
                parent = new CompositionClassLoader( partition, tag, base, uris, parent );
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


    private URI getLocalPartition( URI base, String name )
    {
        String scheme = base.getScheme();
        String spec = base.getSchemeSpecificPart();
        String path = getPathWithTrailingSlash( spec );
        String cleanName = getNameWithoutLeadingSlash( name );
        return createURI( scheme, path, cleanName );
    }

    private String getNameWithoutLeadingSlash( String path )
    {
        String spec = path.trim();
        if( spec.startsWith( "/" ) )
        {
            return spec.substring( 1 );
        }
        else
        {
            return spec;
        }
    }

    private String getPathWithTrailingSlash( String path )
    {
        String spec = path.trim();
        if( spec.endsWith( "/" ) )
        {
            return spec;
        }
        else
        {
            return spec + "/";
        }
    }

    private boolean isRecognizedPart( Part part )
    {
        return CONTROLLER_URI.equals( part.getPartHandlerURI() );
    }

    //--------------------------------------------------------------------
    // static utilities
    //--------------------------------------------------------------------

    public static URI createURI( URI base, String name )
    {
        String scheme = base.getScheme();
        String partition = base.getSchemeSpecificPart();
        if( partition.endsWith( "/" ) )
        {
            final String path = partition + name;
            return createURI( scheme, path );
        }
        else
        {
            final String path = partition + "/" + name;
            return createURI( scheme, path );
        }
    }

    public static URI createURI( String scheme, String path )
    {
        return createURI( scheme, path, null );
    }

    private static URI createURI( String scheme, String path, String fragment )
    {
        try
        {
            return new URI( scheme, path, fragment );
        }
        catch( URISyntaxException e )
        {
            final String error = 
              "Internal exception while attempting to construct a part uri using the schme ["
              + scheme
              + "] and path ["
              + path
              + "].";
            throw new ControllerRuntimeException( CONTROLLER_URI, error, e );
        }
    }

    protected static URI createURI( String spec )
    {
        try
        {
            return new URI( spec );
        }
        catch( URISyntaxException e )
        {
            final String error = 
              "Invalid URI specification [" + spec + "].";
            throw new ControllerRuntimeException( CONTROLLER_URI, error, e );
        }
    }

    static final URI CONTROLLER_URI = setupURI( "@PART-HANDLER-URI@" );
    static final URI ROOT_URI = setupURI( "metro:/" );

    protected static URI setupURI( String spec )
    {
        try
        {
            return new URI( spec );
        }
        catch( URISyntaxException ioe )
        {
            return null;
        }
    }

   /**
    * Create a shutdown hook that will trigger termination of the root component.
    * @param component the root component
    */
    private void addShutdownHook( final Logger logger, final ComponentHandler component )
    {
        //
        // Create a shutdown hook to trigger clean disposal of the
        // component.
        //
        
        Runtime.getRuntime().addShutdownHook(
          new Thread()
          {
              public void run()
              {
                  try
                  {
                      logger.info( "initiating controller shutdown" );
                      component.terminate();
                  }
                  catch( Throwable e )
                  {
                      logger.warn( "ignoring shutdown error", e );
                  }
                  finally
                  {
                      logger.info( "controller shutdown complete" );
                      System.runFinalization();
                  }
              }
          }
        );
    }

}

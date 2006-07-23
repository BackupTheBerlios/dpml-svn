/*
 * Copyright (c) 2005-2006 Stephen J. McConnell
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
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import net.dpml.component.Controller;
import net.dpml.component.ControlException;
import net.dpml.component.ControllerContext;
import net.dpml.component.ControllerContextListener;
import net.dpml.component.ControllerContextEvent;
import net.dpml.component.Model;
import net.dpml.component.Component;
import net.dpml.component.Composition;

import net.dpml.lang.Part;
import net.dpml.lang.Builder;
import net.dpml.lang.StandardClassLoader;
import net.dpml.lang.Info;
import net.dpml.lang.Category;
import net.dpml.lang.Classpath;

import net.dpml.metro.ComponentModel;
import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.data.DefaultComposition;
import net.dpml.metro.builder.ComponentDecoder;

import net.dpml.util.Logger;
import net.dpml.util.DefaultLogger;
import net.dpml.util.EventQueue;
import net.dpml.util.Resolver;

import org.w3c.dom.Element;

/**
 * The composition controller is the controller used to establish remotely accessible
 * component controls.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CompositionController implements Controller, Builder
{
    //--------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------
        
   /**
    * Static URI of this controller.
    */
    public static final URI CONTROLLER_URI = createStaticURI( "@PART-HANDLER-URI@" );
    
    private static final String BASEPATH = setupBasePathSpec();
    
    static final URI ROOT_URI = createStaticURI( "metro:/" );

    //--------------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------------

    private final Logger m_logger;
    private final ControllerContext m_context;
    private final ComponentController m_controller;
    private final InternalControllerContextListener m_listener;
    private final String m_partition;
    private final EventQueue m_events;
    
    //--------------------------------------------------------------------
    // mutable state
    //--------------------------------------------------------------------
    
    private boolean m_disposed = false;
    
    //--------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------

   /**
    * Creation of a new controller.
    * @param context the control context
    * @exception ControlException if an error occurs during controller creation
    */
    public CompositionController( ControllerContext context )
       throws ControlException
    {
        super();
        
        m_context = context;
        m_partition = context.getPartition();
        Logger root = new DefaultLogger( m_partition );
        m_logger = root.getChildLogger( "dpml.metro" );
        m_listener = new InternalControllerContextListener();
        m_context.addControllerContextListener( m_listener );
        m_controller = new ComponentController( m_logger, this );
        m_events = new EventQueue( m_logger );
        
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( 
              "loaded controller [" 
              + getClass().getName() 
              + "#"
              + System.identityHashCode( this ) 
              + "]" );
        }
    }
    
    EventQueue getEventQueue()
    {
        return m_events;
    }
    
   /**
    * Controller finalization.
    * @exception Throwable if a finalization error occurs
    */
    protected void finalize() throws Throwable
    {
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( 
              "finalizing controller [" 
              + getClass().getName() 
              + "#"
              + System.identityHashCode( this ) 
              + "]" );
        }
        dispose();
    }
    
    //--------------------------------------------------------------------
    // Builder
    //--------------------------------------------------------------------
    
   /**
    * Construct the deployment information from a part definition.
    * @param info the part info definition
    * @param classpath the part classpath definition
    * @param strategy the DOM element definining the deplyment streategy
    * @param resolver build-time uri resolver
    * @return the part definition
    * @exception IOException if an I/O error occurs
    */
    public Part build( 
      Info info, Classpath classpath, Element strategy, Resolver resolver ) throws IOException
    {
        if( getLogger().isTraceEnabled() )
        {
            String path = getPartSpec( info.getURI() );
            getLogger().trace( "new composition: " + path );
        }
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        try
        {
            ClassLoader anchor = Component.class.getClassLoader();
            Thread.currentThread().setContextClassLoader( anchor );
            ComponentDecoder decoder = new ComponentDecoder();
            ComponentDirective directive = decoder.buildComponent( strategy, resolver );
            Logger logger = getLogger();
            return new DefaultComposition( logger, info, classpath, this, directive );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( context );
        }
    }

    //--------------------------------------------------------------------
    // PartHandler
    //--------------------------------------------------------------------
    
   /**
    * Build a classloader stack.
    * @param anchor the anchor classloader to server as the classloader chain root
    * @param classpath the part classpath definition
    * @return the new classloader
    * @exception IOException if an IO error occurs during classpath evaluation
    */
    public ClassLoader getClassLoader( String name, ClassLoader anchor, Classpath classpath ) throws IOException
    {
        if( null == classpath )
        {
            return anchor;
        }
        
        ClassLoader base = anchor;
        Logger logger = getLogger();
        
        try
        {
            base.loadClass( ComponentDirective.class.getName() );
        }
        catch( ClassNotFoundException e )
        {
            ClassLoader management = ComponentDirective.class.getClassLoader();
            base = new CompositionClassLoader( logger, name, Category.PROTECTED, management, anchor );
        }
        
        URI[] apis = classpath.getDependencies( Category.PUBLIC );
        ClassLoader api = StandardClassLoader.buildClassLoader( logger, name, Category.PUBLIC, base, apis );
        URI[] spis = classpath.getDependencies( Category.PROTECTED );
        ClassLoader spi = StandardClassLoader.buildClassLoader( logger, name, Category.PROTECTED, api, spis );
        URI[] imps = classpath.getDependencies( Category.PRIVATE );
        ClassLoader impl = StandardClassLoader.buildClassLoader( logger, name, Category.PRIVATE, spi, imps );
        return impl;
    }

   /**
    * Handle notification of the creation of a new classloader.
    * @param category the classloader category (public, protected or private)
    * @param classloader the new classloader 
    */
    public void classloaderConstructed( String name, Category category, ClassLoader classloader )
    {
        if( getLogger().isDebugEnabled() )
        {
            int id = System.identityHashCode( classloader );
            StringBuffer buffer = new StringBuffer();
            buffer.append( "created " );
            buffer.append( category.toString() );
            buffer.append( " classloader for " + name );
            buffer.append( "\n  ID: " + id );
            if( classloader instanceof CompositionClassLoader )
            {
                CompositionClassLoader loader = (CompositionClassLoader) classloader;
                ClassLoader interceptor = loader.getInterceptionClassLoader();
                int pid = System.identityHashCode( interceptor );
                buffer.append( "\n  interceptor: " + pid );
            }
            ClassLoader parent = classloader.getParent();
            if( null != parent )
            {
                int pid = System.identityHashCode( parent );
                buffer.append( "\n  extends: " + pid );
            }
            if( classloader instanceof URLClassLoader )
            {
                URLClassLoader loader = (URLClassLoader) classloader;
                URL[] urls = loader.getURLs();
                for( int i=0; i < urls.length; i++ )
                {
                    URL url = urls[i];
                    buffer.append( "\n  [" + i + "] " + url.toString() );
                }
            }
            getLogger().debug( buffer.toString() );
        }
    }
    
    //--------------------------------------------------------------------
    // Controller
    //--------------------------------------------------------------------
    
   /**
    * Returns the uri of this controller.
    * @return the controller uri
    */
    public URI getURI()
    {
        return CONTROLLER_URI;
    }
    
   /**
    * Create and return a new management context using the supplied directive uri.
    *
    * @param uri a uri identifying a deployment directive
    * @return the management context model
    * @exception ControlException if an error occurs
    * @exception IOException if an error occurs reading the identified resource
    */
    public Model createModel( URI uri ) throws ControlException, IOException
    {
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( 
              "creating new model from URI" 
              + "\n  URI: " + uri );
        }
        Part part = Part.load( uri, false );
        if( part instanceof Composition )
        {
            Composition composition = (Composition) part;
            return createModel( composition );
        }
        else
        {
            final String error = 
              "Part class [" 
              + part.getClass().getName() 
              + "] not recognized.";
            throw new ControllerException( error );
        }
    }

   /**
    * Create and return a new management context using the supplied directive uri.
    *
    * @param composition a composition directive
    * @return the management model
    * @exception ControlException if an error occurs
    * @exception IOException if an I/O error occurs
    */
    public Model createModel( Composition composition ) throws ControlException, IOException
    {
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( 
              "creating new model from part" 
                + "\n  URI: " + composition.getInfo().getURI() );
        }
        if( composition instanceof DefaultComposition )
        {
            DefaultComposition data = (DefaultComposition) composition;
            return m_controller.createComponentModel( data );
        }
        else
        {
            final String error = 
              "Composition class [" 
              + composition.getClass().getName() 
              + "] not recognized.";
            throw new ControllerException( error );
        }
    }
    
   /**
    * Create and return a remote reference to a component handler.
    * @param uri a uri identifying a deployment directive
    * @return the component handler
    * @exception Exception if an error occurs
    */
    public Component createComponent( URI uri ) throws Exception
    {
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( 
              "creating new component from URI"
               + "\nURI: " + uri );
        }
        Part part = Part.load( uri, false );
        if( part instanceof DefaultComposition )
        {
            DefaultComposition composition = (DefaultComposition) part;
            Model model = m_controller.createComponentModel( composition );
            return createComponent( model, true );
        }
        else
        {
            final String error = 
              "Part class [" 
              + part.getClass().getName() 
              + "] is not recognized.";
            throw new ControllerException( error );
        }
    }

   /**
    * Create and return a remote reference to a component handler.
    * @param model the component model
    * @return the component handler
    * @exception Exception if an error occurs during component creation
    */
    public Component createComponent( Model model ) throws Exception
    {
        return createComponent( model, false );
    }
    
   /**
    * Create and return a remote reference to a component handler.
    * @param model the component model
    * @param flag if true the component is responsible for model retraction
    * @return the component handler
    * @exception Exception if an error occurs during component creation
    */
    private Component createComponent( Model model, boolean flag ) throws Exception
    {
        if( getLogger().isTraceEnabled() )
        {
            if( flag )
            {
                getLogger().trace( "creating new locally controlled component" );
            }
            else
            {
                getLogger().trace( "creating new managed component" );
            }
        }
        if( model instanceof ComponentModel )
        {
            ClassLoader anchor = Logger.class.getClassLoader();
            ComponentModel componentModel = (ComponentModel) model;
            Classpath classpath = componentModel.getClasspath();
            String name = componentModel.getName();
            ClassLoader classloader = getClassLoader( name, anchor, classpath );
            return m_controller.createDefaultComponentHandler( classloader, componentModel, flag );
        }
        else
        {
            //
            // TODO delegate to foreign controller
            //
            
            final String error =
              "Construction of a handler for the context model class ["
              + model.getClass().getName() 
              + "] is not supported.";
            throw new ControllerException( error );
        }
    }
    
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

   /**
    * Return the assigned logging channel.
    * @return the logging channel
    */
    Logger getLogger()
    {
        return m_logger;
    }

    String getPartition()
    {
        return m_partition;
    }
    
    void dispose()
    {
        if( !m_disposed )
        { 
            getLogger().debug( "initating shutdown" );
            m_context.removeControllerContextListener( m_listener );
            m_events.terminateDispatchThread();
            m_disposed = true;
            getLogger().debug( "shutdown complete" );
        }
    }
    
    private static URI createStaticURI( String path )
    {
        try
        {
            return new URI( path );
        }
        catch( Throwable e )
        {
            return null;
        }
    }

    private URI getURIFromURL( URL url )
    {
        try
        {
            return new URI( url.toExternalForm() );
        }
        catch( Throwable e )
        {
            throw new RuntimeException( e );
        }
    }
    
   /**
    * Controller context listener.
    */
    private class InternalControllerContextListener implements ControllerContextListener
    {
       /**
        * Notify the listener that the working directory has changed.
        *
        * @param event the change event
        */
        public void workingDirectoryChanged( ControllerContextEvent event )
        {
        }

       /**
        * Notify the listener that the temporary directory has changed.
        *
        * @param event the change event
        */
        public void tempDirectoryChanged( ControllerContextEvent event )
        {
        }
    
       /**
        * Notify listeners of the disposal of the controller.
        * @param event the context event
        */
        public void controllerDisposal( ControllerContextEvent event )
        {
            dispose();
        }
    }
    
    static String setupBasePathSpec()
    {
        try
        {
            String path = System.getProperty( "user.dir" );
            File file = new File( path );
            URI uri = file.toURI();
            URL url = file.toURL();
            return url.toString();
        }
        catch( Exception e )
        {   
            return e.toString();
        }
    }
    
    private static String getPartSpec( URI uri )
    {
        String path = uri.toASCIIString();
        if( path.startsWith( BASEPATH ) )
        {
            return "./" + path.substring( BASEPATH.length() );
        }
        else
        {
            return path;
        }
    }
}

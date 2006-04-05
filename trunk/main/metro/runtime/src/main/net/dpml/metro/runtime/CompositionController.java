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

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

import net.dpml.metro.ComponentModel;
import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.builder.ComponentDecoder;

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
import net.dpml.util.Logger;
import net.dpml.util.DefaultLogger;

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
    
    private static final String COMPONENT_NAMESPACE_URI = "@COMPONENT-NAMESPACE-URI@";
    
   /**
    * Static URI of this controller.
    */
    public static final URI CONTROLLER_URI = createStaticURI( "@PART-HANDLER-URI@" );
    
    static final URI ROOT_URI = createStaticURI( "metro:/" );

    //--------------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------------

    private final Logger m_logger;
    private final ControllerContext m_context;
    private final ComponentController m_controller;
    private final HashMap m_handlers = new HashMap(); // foreign controllers
    private final InternalControllerContextListener m_listener;
    private final String m_partition;
    
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
        m_logger = root.getChildLogger( "control" );
        m_listener = new InternalControllerContextListener( this );
        m_context.addControllerContextListener( m_listener );
        m_logger.debug( "controller: " + CONTROLLER_URI );
        m_controller = new ComponentController( m_logger, this );
        
        startEventDispatchThread();
    }
    
    //--------------------------------------------------------------------
    // Builder
    //--------------------------------------------------------------------
    
   /**
    * Construct the deployment infomation for a part definition.
    * @param info the part info definition
    * @param classpath the part classpath definition
    * @param strategy the DOM element definining the deplyment streategy
    * @return the part definition
    * @exception IOException if an I/O error occurs
    */
    public Part build( Info info, Classpath classpath, Element strategy ) throws IOException
    {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        try
        {
            ClassLoader anchor = Component.class.getClassLoader();
            Thread.currentThread().setContextClassLoader( anchor );
            ComponentDecoder decoder = new ComponentDecoder();
            ComponentDirective directive = decoder.buildComponent( strategy );
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
    public ClassLoader getClassLoader( ClassLoader anchor, Classpath classpath ) throws IOException
    {
        ClassLoader management = ComponentDirective.class.getClassLoader();
        
        ClassLoader composer = 
          new CompositionClassLoader( null, Category.PROTECTED, management, anchor );
        
        URI[] apis = classpath.getDependencies( Category.PUBLIC );
        ClassLoader api = StandardClassLoader.buildClassLoader( null, Category.PUBLIC, composer, apis );
        if( api != composer )
        {
            classloaderConstructed( Category.PUBLIC, api );
        }
        URI[] spis = classpath.getDependencies( Category.PROTECTED );
        ClassLoader spi = StandardClassLoader.buildClassLoader( null, Category.PROTECTED, api, spis );
        if( spi != api )
        {
            classloaderConstructed( Category.PROTECTED, spi );
        }
        URI[] imps = classpath.getDependencies( Category.PRIVATE );
        ClassLoader impl = StandardClassLoader.buildClassLoader( null, Category.PRIVATE, spi, imps );
        if( impl != spi )
        {
            classloaderConstructed( Category.PRIVATE, impl );
        }
        return impl;
    }

   /**
    * Handle notification of the creation of a new classloader.
    * @param category the classloader category (public, protected or private)
    * @param classloader the new classloader 
    */
    public void classloaderConstructed( Category category, ClassLoader classloader )
    {
        if( getLogger().isDebugEnabled() )
        {
            int id = System.identityHashCode( classloader );
            StringBuffer buffer = new StringBuffer();
            buffer.append( "created " );
            buffer.append( category.toString() );
            buffer.append( " classloader: " + id );
            if( classloader instanceof URLClassLoader )
            {
                URLClassLoader loader = (URLClassLoader) classloader;
                URL[] urls = loader.getURLs();
                for( int i=0; i < urls.length; i++ )
                {
                    URL url = urls[i];
                    buffer.append( "\n  [" + i + "] \t" + url.toString() );
                }
            }
            getLogger().debug( buffer.toString() );
        }
    }
    
   /**
    * Instantiate a value.
    * @param anchor the anchor classloader
    * @param classpath the part classpath definition
    * @param data the part deployment data
    * @param args supplimentary arguments
    * @return the instantiated service
    * @exception Exception if a deployment error occurs
    */
    /*
    public Object getInstance( 
      ClassLoader anchor, Classpath classpath, Object data, Object[] args ) throws Exception
    {
        if( data instanceof ComponentDirective )
        {
            ComponentDirective directive = (ComponentDirective) data;
            ComponentModel model = m_controller.createComponentModel( classpath, directive );
            ClassLoader classloader = getClassLoader( anchor, classpath );
            Component component = m_controller.createDefaultComponentHandler( classloader, model, true );
            return component.getProvider().getValue( true );
        }
        else
        {
            final String datatype = data.getClass().getName();
            final String error = 
              "Datatype not recognized."
              + "\nClass: " + datatype;
            final String report = 
              error
              + "\n" 
              + StandardClassLoader.toString( 
                  ComponentDirective.class.getClassLoader(),
                  data.getClass().getClassLoader() );
            getLogger().error( report );
            throw new IllegalArgumentException( error );
        }
    }
    */
    
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
        Part part = Part.load( uri );
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
        Part part = Part.load( uri );
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
        if( model instanceof ComponentModel )
        {
            ClassLoader anchor = Logger.class.getClassLoader();
            ComponentModel componentModel = (ComponentModel) model;
            Classpath classpath = componentModel.getClasspath();
            ClassLoader classloader = getClassLoader( anchor, classpath );
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
        getLogger().debug( "initating controller disposal" );
        m_context.removeControllerContextListener( m_listener );
        m_dispatch.dispose();
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
     * Queue of pending notification events.  When an event for which 
     * there are one or more listeners occurs, it is placed on this queue 
     * and the queue is notified.  A background thread waits on this queue 
     * and delivers the events.  This decouples event delivery from 
     * the application concern, greatly simplifying locking and reducing 
     * opportunity for deadlock.
     */
    private static final List EVENT_QUEUE = new LinkedList();

   /**
    * Enqueue an event for delivery to registered
    * listeners unless there are no registered
    * listeners.
    * @param event the event to enqueue
    */
    static void enqueueEvent( EventObject event )
    {
        synchronized( EVENT_QUEUE ) 
        {
            EVENT_QUEUE.add( event );
            EVENT_QUEUE.notify();
        }
    }
    
    /**
     * A single background thread ("the event notification thread") monitors
     * the event queue and delivers events that are placed on the queue.
     */
    private static class EventDispatchThread extends Thread 
    {
        private final Logger m_logger;
        
        private boolean m_continue = true;
        
        EventDispatchThread( Logger logger )
        {
            m_logger = logger;
            m_logger.debug( "starting event dispatch thread" );
        }
        
        void dispose()
        {
            synchronized( EVENT_QUEUE )
            {
                m_logger.debug( "stopping event dispatch thread" );
                m_continue = false;
                EVENT_QUEUE.notify();
            }
        }
        
        public void run() 
        {
            while( m_continue ) 
            {
                // Wait on EVENT_QUEUE till an event is present
                EventObject event = null;
                synchronized( EVENT_QUEUE ) 
                {
                    try
                    {
                        while( EVENT_QUEUE.isEmpty() )
                        {
                            EVENT_QUEUE.wait();
                        }
                        Object object = EVENT_QUEUE.remove( 0 );
                        try
                        {
                            event = (EventObject) object;
                        }
                        catch( ClassCastException cce )
                        {
                            final String error = 
                              "Unexpected class cast exception while processing an event." 
                              + "\nEvent: " + object;
                            throw new IllegalStateException( error );
                        }
                    }
                    catch( InterruptedException e )
                    {
                        return;
                    }
                }
                
                Object source = event.getSource();
                if( source instanceof UnicastEventSource )
                {
                    UnicastEventSource producer = (UnicastEventSource) source;
                    try
                    {
                        producer.processEvent( event );
                    }
                    catch( Throwable e )
                    {
                        final String error = 
                          "Unexpected error while processing event."
                          + "\nEvent: " + event
                          + "\nSource: " + source;
                        m_logger.warn( error, e );
                    }
                }
                else
                {
                    final String error = 
                      "Event source [" 
                      + source.getClass().getName()
                      + "] is not an instance of " + UnicastEventSource.class.getName();
                    throw new IllegalStateException( error );
                }
            }
            
            m_logger.info( "Controller event queue terminating." );
        }
    }

    private EventDispatchThread m_dispatch = null;

    /**
     * This method starts the event dispatch thread the first time it
     * is called.  The event dispatch thread will be started only
     * if someone registers a listener.
     */
    private synchronized void startEventDispatchThread()
    {
        if( m_dispatch == null )
        {
            Logger logger = getLogger().getChildLogger( "event" );
            m_dispatch = new EventDispatchThread( logger );
            m_dispatch.setDaemon( true );
            m_dispatch.start();
        }
    }
    
   /**
    * Controller context listener.
    */
    private class InternalControllerContextListener implements ControllerContextListener
    {
        private final CompositionController m_controller;
        
       /**
        * Creation of a new controller context listener.
        * @param controller the controller to which change event actions are applied
        */
        InternalControllerContextListener( final CompositionController controller )
        {
            m_controller = controller;
        }
        
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
            m_controller.dispose();
        }
    }
}

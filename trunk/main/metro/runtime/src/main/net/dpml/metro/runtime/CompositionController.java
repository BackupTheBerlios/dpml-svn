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
import java.util.HashMap;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

import net.dpml.logging.Logger;

import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.model.ComponentModel;

import net.dpml.part.local.Controller;
import net.dpml.part.ControlException;
import net.dpml.part.DelegationException;
import net.dpml.part.Directive;
import net.dpml.part.Part;
import net.dpml.part.PartBuilder;
import net.dpml.part.PartHeader;
import net.dpml.part.local.ControllerContext;
import net.dpml.part.local.ControllerContextListener;
import net.dpml.part.local.ControllerContextEvent;
import net.dpml.part.remote.Model;
import net.dpml.part.remote.Component;

import net.dpml.transit.Repository;
import net.dpml.transit.Transit;

/**
 * The composition controller is the controller used to establish remotely accessible
 * component controls.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CompositionController implements Controller
{
    //--------------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------------

    private final Logger m_logger;
    private final ControllerContext m_context;
    private final ComponentController m_controller;
    private final HashMap m_handlers = new HashMap(); // foreign controllers
    private final Repository m_loader;
    private final InternalControllerContextListener m_listener;
    
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
        Logger root = new StandardLogger( context.getLogger() );
        m_logger = root.getChildLogger( "control" );
        m_listener = new InternalControllerContextListener( this );
        m_context.addControllerContextListener( m_listener );
        m_loader = Transit.getInstance().getRepository();
        m_logger.debug( "controller: " + CONTROLLER_URI );
        m_controller = new ComponentController( m_logger, this );
        startEventDispatchThread();
    }
    
    //--------------------------------------------------------------------
    // Controller
    //--------------------------------------------------------------------
    
   /**
    * Create a classloader using the supplied anchor classloader and 
    * component directive.
    * 
    * @param anchor the anchor classloader
    * @param model a component model 
    * @return the new classloader
    * @exception ControlException if a classloader creation error occurs
    */
    public ClassLoader createClassLoader( ClassLoader anchor, Model model ) throws ControlException
    {
        if( model instanceof ComponentModel )
        {
            ComponentModel componentModel = (ComponentModel) model;
            return m_controller.createClassLoader( anchor, componentModel );
        }
        else
        {
            final String error =
              "Construction of a classloader from the context model class ["
              + model.getClass().getName() 
              + "] is not supported.";
            throw new ControllerException( error );
        }
    }
    
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
        Directive directive = loadDirective( uri );
        return createModel( directive );
    }
    
   /**
    * Create and return a new management context using the supplied part
    * as the inital management state.
    *
    * @param directive the part data structure
    * @return the management context model
    * @exception ControlException if an error occurs during model construction
    */
    public Model createModel( Directive directive ) throws ControlException
    {
        if( directive instanceof ComponentDirective )
        {
            ComponentDirective component = (ComponentDirective) directive;
            return m_controller.createComponentModel( component );
        }
        else
        {
            final String error =
              "Construction of a managment context for the part class ["
              + directive.getClass().getName() 
              + "] is not supported.";
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
        Model model = createModel( uri );
        return createComponent( model, true );
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
            ComponentModel componentModel = (ComponentModel) model;
            return m_controller.createDefaultComponentHandler( componentModel, flag );
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

   /**
    * Load a directive from serialized form.
    *
    * @param uri the directive uri
    * @return the directive
    * @exception ControlException if an error is raised related to direction construction
    * @exception IOException if an I/O error occurs while reading the directive
    */
    public Directive loadDirective( URI uri ) throws ControlException, IOException
    {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        try
        {
            PartHeader header = PartBuilder.readPartHeader( uri );
            URI controllerURI = header.getControllerURI();
            if( CONTROLLER_URI.equals( controllerURI ) )
            {
                ClassLoader loader = ComponentDirective.class.getClassLoader();
                Thread.currentThread().setContextClassLoader( loader );
                Part part =  PartBuilder.readPart( uri );
                return part.getDirective();
            }
            else
            {
                Controller controller = getPrimaryController( controllerURI );
                return controller.loadDirective( uri );
            }
            
            /*
            ClassLoader loader = ComponentDirective.class.getClassLoader();
            Thread.currentThread().setContextClassLoader( loader );
            URL url = uri.toURL();
            InputStream input = url.openStream();
            XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( input ) );
            return (Directive) decoder.readObject();
            */
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while loading directive: "
              + uri;
            throw new ControllerException( error, e );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( current );
        }
    }
    
   /**
    * Load a controller given a uri.
    * @param uri the forign controller uri
    * @return the part controller
    * @exception ControllerNotFoundException if the controller could not be found
    * @exception DelegationException if an error occured in the foreign controller
    */
    public Controller getPrimaryController( URI uri ) 
       throws ControllerNotFoundException, DelegationException
    {
        if( !getURI().equals( uri ) )
        {
            return resolveController( uri );
        }
        else
        {
            return this;
        }
    }

    private Controller resolveController( URI uri ) throws ControllerNotFoundException
    {
        if( getURI().equals( uri ) )
        {
            return this;
        }
        
        synchronized( m_handlers )
        {
            Controller controller = (Controller) m_handlers.get( uri );
            if( null != controller )
            {
                return controller;
            }
            else
            {
                controller = loadForeignController( uri );
                m_handlers.put( uri, controller );
                return controller;
            }
        }
    }

    private Controller loadForeignController( URI uri ) throws ControllerNotFoundException
    {
        ClassLoader classloader = Controller.class.getClassLoader();
        try
        {
            return (Controller) m_loader.getPlugin( 
              classloader, uri, 
              new Object[]{m_context} );
        }
        catch( IOException e )
        {
            throw new ControllerNotFoundException( CONTROLLER_URI, uri, e );
        }
    }
    
    void dispose()
    {
        getLogger().debug( "initating controller disposal" );
        m_context.removeControllerContextListener( m_listener );
        m_dispatch.dispose();
    }
    
   /**
    * Static URI of this controller.
    */
    public static final URI CONTROLLER_URI = createStaticURI( "@PART-HANDLER-URI@" );
    
    static final URI ROOT_URI = createStaticURI( "metro:/" );

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

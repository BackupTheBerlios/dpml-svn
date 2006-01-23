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

package net.dpml.part.local;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.part.Disposable;

import net.dpml.transit.Logger;
import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.monitor.LoggingAdapter;

/**
 * The CompositionControllerContext class wraps a ContentModel and supplies convinience
 * operations that translate ContentModel properties and events to type-safe values used 
 * in the conposition controller.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class InitialContext extends LocalEventProducer 
  implements ControllerContext, Disposable
{
    //----------------------------------------------------------------------------
    // static
    //----------------------------------------------------------------------------
    
   /**
    * Construct a controller.
    * @param context the controller context
    * @return the controller
    */
    public static Controller newController( final ControllerContext context )
    {
        if( null == context )
        {
            throw new NullPointerException( "context" );
        }
        try
        {
            URI uri = getControllerURI();
            ClassLoader classloader = InitialContext.class.getClassLoader();
            Repository repository = Transit.getInstance().getRepository();
            Class c = repository.getPluginClass( classloader, uri );
            Constructor constructor = c.getConstructor( new Class[]{ControllerContext.class} );
            Controller controller = (Controller) constructor.newInstance( new Object[]{context} );
            return controller;
        }
        catch( Throwable e )
        {
            final String error =
              "Internal error while attempting to establish the default part handler.";
            throw new RuntimeException( error, e );
        }
    }
    
    private static URI getControllerURI() throws Exception
    {
        String spec = 
          System.getProperty( 
            "dpml.part.controller.uri", 
            "@PART-HANDLER-URI@" );
        return new URI( spec );
    }
    
    //----------------------------------------------------------------------------
    // state
    //----------------------------------------------------------------------------
    
   /**
    * Working directory.
    */
    private File m_work;
  
   /**
    * Temp directory.
    */
    private File m_temp;

   /**
    * The assigned logging channel.
    */
    private Logger m_logger;

    //----------------------------------------------------------------------------
    // constructor
    //----------------------------------------------------------------------------
    
   /**
    * Creation of a new <tt>CompositionContext</tt>.
    * @param logger the assigned logging channel
    */
    public InitialContext()
    {
        this( new LoggingAdapter( "metro" ), null, null );
    }

   /**
    * Creation of a new <tt>CompositionContext</tt>.
    * @param logger the assigned logging channel
    */
    public InitialContext( Logger logger )
    {
        this( logger, null, null );
    }
    
   /**
    * Creation of a new <tt>CompositionContext</tt>.
    * @param logger the assigned logging channel
    * @param work the working directory
    * @param temp the temporary directory
    */
    public InitialContext( Logger logger, File work, File temp )
    {
        super( new LoggingAdapter( "metro.context" ) );

        m_logger = logger;

        if( null == work )
        {
            String path = System.getProperty( "user.dir" );
            m_work = new File( path );
        }
        else
        {
            m_work = work;
        }
        
        if( null == temp )
        {
            String path = System.getProperty( "java.io.tmpdir" );
            m_temp = new File( path );
        }
        else
        {
            m_temp = temp;
        }
    }

    //----------------------------------------------------------------------------
    // Disposable
    //----------------------------------------------------------------------------
    
   /**
    * Initiate disposal.
    */
    public void dispose()
    {
        m_logger.debug( "context disposal" );
        ControllerDisposalEvent event = new ControllerDisposalEvent( this );
        enqueueEvent( event, false );
        super.dispose();
    }

    //----------------------------------------------------------------------------
    // ControllerContext
    //----------------------------------------------------------------------------

   /**
    * Return the assigned logging channel
    * @return the logging channel
    */
    public Logger getLogger()
    {
        return m_logger;
    }

   /**
    * Return the root working directory.
    *
    * @return directory representing the root of the working directory hierachy
    */
    public File getWorkingDirectory()
    {
        synchronized( getLock() )
        {
            return m_work;
        }
    }

   /**
    * Set the root working directory.
    *
    * @param dir the root of the working directory hierachy
    */
    public void setWorkingDirectory( File dir )
    {
        synchronized( getLock() )
        {
            m_work = dir;
        }
    }

   /**
    * Return the root temporary directory.
    *
    * @return directory representing the root of the temporary directory hierachy.
    */
    public File getTempDirectory()
    {
        synchronized( getLock() )
        {
            return m_temp;
        }
    }

   /**
    * Add the supplied controller context listener to the controller context.  A 
    * controller implementation should not maintain strong references to supplied 
    * listeners.
    *
    * @param listener the controller context listener to add
    */
    public void addControllerContextListener( ControllerContextListener listener ) 
    {
        addListener( listener );
    }

   /**
    * Remove the supplied controller context listener from the controller context.
    *
    * @param listener the controller context listener to remove
    */
    public void removeControllerContextListener( ControllerContextListener listener )
    {
        removeListener( listener );
    }

    //----------------------------------------------------------------------------
    // impl
    //----------------------------------------------------------------------------

   /**
    * Process a context related event.
    * @param event the event to process
    */
    protected void processEvent( EventObject event )
    {
        if( event instanceof WorkingDirectoryChangeEvent )
        {
            processWorkingDirectoryChangeEvent( (WorkingDirectoryChangeEvent) event );
        }
        else if( event instanceof TempDirectoryChangeEvent )
        {
            processTempDirectoryChangeEvent( (TempDirectoryChangeEvent) event );
        }
        else if( event instanceof ControllerDisposalEvent )
        {
            processControllerDisposalEvent( (ControllerDisposalEvent) event );
        }
    }

    private void processWorkingDirectoryChangeEvent( WorkingDirectoryChangeEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener eventListener = listeners[i];
            if( eventListener instanceof ControllerContextListener )
            {
                ControllerContextListener listener = (ControllerContextListener) eventListener;
                try
                {
                    listener.workingDirectoryChanged( event );
                }
                catch( Throwable e )
                {
                    final String error =
                      "ControllerContextListener working dir change notification error.";
                    getLogger().error( error, e );
                }
            }
        }
    }

    private void processTempDirectoryChangeEvent( TempDirectoryChangeEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener eventListener = listeners[i];
            if( eventListener instanceof ControllerContextListener )
            {
                ControllerContextListener listener = (ControllerContextListener) eventListener;
                try
                {
                    listener.tempDirectoryChanged( event );
                }
                catch( Throwable e )
                {
                    final String error =
                      "ControllerContextListener temp dir change notification error.";
                    getLogger().error( error, e );
                }
            }
        }
    }

    private void processControllerDisposalEvent( ControllerDisposalEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i<listeners.length; i++ )
        {
            EventListener eventListener = listeners[i];
            if( eventListener instanceof ControllerContextListener )
            {
                ControllerContextListener listener = (ControllerContextListener) eventListener;
                try
                {
                    listener.controllerDisposal( event );
                }
                catch( Throwable e )
                {
                    final String error =
                      "ControllerContextListener disposal notification error.";
                    getLogger().error( error, e );
                }
            }
        }
    }

    //----------------------------------------------------------------------------
    // static (private)
    //----------------------------------------------------------------------------

   /**
    * Working directory change event impl.
    */
    private static class WorkingDirectoryChangeEvent extends ControllerContextEvent
    {
        public WorkingDirectoryChangeEvent( 
          ControllerContext source, File oldDir, File newDir )
        {
            super( source, oldDir, newDir );
        }
    }

   /**
    * Temp directory change event impl.
    */
    private static class TempDirectoryChangeEvent extends ControllerContextEvent
    {
        public TempDirectoryChangeEvent( 
          ControllerContext source, File oldDir, File newDir )
        {
            super( source, oldDir, newDir );
        }
    }

   /**
    * Disposal event.
    */
    private static class ControllerDisposalEvent extends ControllerContextEvent
    {
        public ControllerDisposalEvent( ControllerContext source )
        {
            super( source, null, null );
        }
    }

}

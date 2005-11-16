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

package net.dpml.metro.runtime;

import java.io.File;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;
import java.util.EventObject;
import java.util.EventListener;
import java.util.WeakHashMap;
import java.util.Properties;

import net.dpml.part.ControllerContext;
import net.dpml.part.ControllerContextListener;
import net.dpml.part.ControllerContextEvent;

import net.dpml.transit.Logger;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.util.PropertyResolver;

/**
 * The CompositionControllerContext class wraps a ContentModel and supplies convinience
 * operations that translate ContentModel properties and events to type-safe values used 
 * in the conposition controller.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class CompositionContext extends LocalWeakEventProducer implements ControllerContext
{
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

    private Logger m_logger;

    //----------------------------------------------------------------------------
    // constructor
    //----------------------------------------------------------------------------

    public CompositionContext( Logger logger )
    {
        this( logger, null, null );
    }

    public CompositionContext( Logger logger, File work, File temp )
    {
        super();

        if( null == logger )
        {
            throw new NullPointerException( "logger" );
        }

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
    // ControllerContext
    //----------------------------------------------------------------------------

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
        synchronized( m_lock )
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
        synchronized( m_lock )
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
        synchronized( m_lock )
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
        else
        {
            super.processEvent( event );
        }
    }

    public void processWorkingDirectoryChangeEvent( WorkingDirectoryChangeEvent event )
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

    public void processTempDirectoryChangeEvent( TempDirectoryChangeEvent event )
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

    //----------------------------------------------------------------------------
    // static (private)
    //----------------------------------------------------------------------------

    static class WorkingDirectoryChangeEvent extends ControllerContextEvent
    {
        public WorkingDirectoryChangeEvent( 
          ControllerContext source, File oldDir, File newDir )
        {
            super( source, oldDir, newDir );
        }
    }

    static class TempDirectoryChangeEvent extends ControllerContextEvent
    {
        public TempDirectoryChangeEvent( 
          ControllerContext source, File oldDir, File newDir )
        {
            super( source, oldDir, newDir );
        }
    }

}

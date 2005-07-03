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
import java.net.URI;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;
import java.util.EventObject;
import java.util.EventListener;
import java.util.WeakHashMap;
import java.util.Properties;

import net.dpml.logging.Logger;

import net.dpml.composition.event.WeakEventProducer;
import net.dpml.composition.runtime.DefaultLogger;

import net.dpml.part.control.ControllerContext;
import net.dpml.part.control.ControllerContextListener;
import net.dpml.part.control.ControllerContextEvent;

import net.dpml.transit.model.DefaultModel;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.model.DefaultContentModel;
import net.dpml.transit.util.PropertyResolver;

/**
 * The CompositionControllerContext class wraps a ContentModel and supplies convinience
 * operations that translate ContentModel properties and events to type-safe values used 
 * in the conposition controller.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class CompositionControllerContext extends WeakEventProducer implements ControllerContext
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

    private final ContentModel m_model;

    private URI m_uri;

    private Logger m_logger;

    //----------------------------------------------------------------------------
    // constructor
    //----------------------------------------------------------------------------

    public CompositionControllerContext( ContentModel model ) throws RemoteException
    {
        m_model = model;

        // TODO: add listeners to changes to the content model properties

        String work = getWorkingDirectoryPath();
        String temp = getTempDirectoryPath();

        String domain = model.getProperty( "domain.name", "local" );
        m_uri = setupContextURI( domain );
        m_work = getWorkingDirectory( work );
        m_temp = getTempDirectory( temp );
        m_logger = getLoggerForURI( m_uri );
    }

    private Logger getLoggerForURI( URI uri )
    {
        String path = uri.getSchemeSpecificPart();
        if( path.endsWith( "/" ) )
        {
            path = path.substring( 0, path.length() - 1 );
        }
        path.replace( '/', '.' );
        return new DefaultLogger( path );
    }

    //----------------------------------------------------------------------------
    // ControllerContext
    //----------------------------------------------------------------------------

    public Logger getLogger()
    {
        return m_logger;
    }

   /**
    * Return the root working directory path.
    *
    * @return directory path representing the root of the working directory hierachy
    */
    public String getWorkingDirectoryPath()
    {
        try
        {
            return m_model.getProperty( "work.dir", "${dpml.data}/work" );
        }
        catch( Exception e )
        {
            final String error = 
              "Remote error while attempting to reslve working directory.";
            throw new RuntimeException( error, e );
        }
    }

   /**
    * Return the root temporary directory path.
    *
    * @return directory path representing the root of the temporary directory hierachy.
    */
    public String getTempDirectoryPath()
    {
        try
        {
            return m_model.getProperty( "temp.dir", "${java.io.tmpdir}" );
        }
        catch( Exception e )
        {
            final String error = 
              "Remote error while attempting to reslve temp directory.";
            throw new RuntimeException( error, e );
        }
    }

   /**
    * Return the context uri.
    *
    * @return the uri for this context.
    */
    public URI getURI()
    {
        return m_uri;
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
    public void addControllerContextlistener( ControllerContextListener listener ) 
    {
        addListener( listener );
    }

   /**
    * Remove the supplied controller context listener from the controller context.
    *
    * @param listener the controller context listener to remove
    */
    public void removeControllerContextlistener( ControllerContextListener listener )
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

    private File getWorkingDirectory( String value )
    {
        Properties properties = System.getProperties();
        String path = PropertyResolver.resolve( properties, value );
        return new File( path );
    }

    private File getTempDirectory( String value )
    {
        Properties properties = System.getProperties();
        String path = PropertyResolver.resolve( properties, value );
        return new File( path );
    }

    //----------------------------------------------------------------------------
    // static (private)
    //----------------------------------------------------------------------------

    private static URI setupContextURI( String domain )
    {
        try
        {
            return new URI( "metro:" + domain );
        }
        catch( Throwable e )
        {
            return null;
        }
    }

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

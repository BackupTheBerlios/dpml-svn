
package net.dpml.station.impl; 

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.server.ObjID;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Properties;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.station.Application;
import net.dpml.station.Application.State;
import net.dpml.station.ApplicationListener;
import net.dpml.station.ApplicationEvent;

import net.dpml.profile.ApplicationProfile;

import net.dpml.part.Part;
import net.dpml.part.PartHandler;
import net.dpml.part.PartContentHandler;
import net.dpml.part.Context;

import net.dpml.transit.Transit;
import net.dpml.transit.Logger;
import net.dpml.transit.model.Connection;
import net.dpml.transit.model.DefaultModel;
import net.dpml.transit.Environment;
import net.dpml.transit.PID;

/**
 * Default implementation of a remote controlled application.
 */
public class DefaultApplication extends EventProducer implements Application
{
    private final ApplicationProfile m_profile;
    private final String m_path;

    private Process m_process;
    private State m_state = Application.READY;
    private PID m_pid;
    private String m_error;
    
    private final Context m_context;

    public DefaultApplication( 
      Logger logger, ApplicationProfile profile, String path ) throws Exception
    {
        super( logger );

        m_profile = profile;
        m_path = path;

        PartHandler handler = PartContentHandler.newPartHandler( logger );
        URI uri = profile.getCodeBaseURI();
        Part part = handler.loadPart( uri );
        m_context = handler.createContext( part );
    }

    public void addApplicationListener( ApplicationListener listener )
    {
        super.addListener( listener );
    }
 
    public void removeApplicationListener( ApplicationListener listener )
    {
        super.removeListener( listener );
    }

    public Context getContext()
    {
        return m_context;
    }

    public ApplicationProfile getProfile()
    {
        return m_profile;
    }

    private long getStartupTimeout()
    {
        try
        {
            return m_profile.getStartupTimeout() * 1000000;
        }
        catch( RemoteException e )
        {
            return ApplicationProfile.DEFAULT_STARTUP_TIMEOUT * 1000000;
        }
    }

    private long getShutdownTimeout()
    {
        try
        {
            return m_profile.getShutdownTimeout() * 1000000;
        }
        catch( RemoteException e )
        {
            return ApplicationProfile.DEFAULT_SHUTDOWN_TIMEOUT * 1000000;
        }
    }

    public PID getPID()
    {
        return m_pid;
    }

    public void handleCallback( PID pid )
    {
        getLogger().info( "incomming callback from process " + pid );
        setState( Application.RUNNING, pid );
    }

    public synchronized void start() throws RemoteException
    {
        if( m_profile.getStartupPolicy() == ApplicationProfile.DISABLED ) 
        {
            final String key = m_profile.getID();
            final String error = 
              "Application [" + key + "] is disabled.";
            throw new ServerException( error );              
        }

        if( null != getPID() )
        {
            final String key = m_profile.getID();
            final String error = 
              "Application [" + key + "] is already started.";
            throw new ServerException( error );              
        }
        
        setState( Application.STARTING );
        try
        {
            String[] command = getProcessCommand();
            m_process = Runtime.getRuntime().exec( command, null );
        }
        catch( Exception e )
        {
            final String error = 
              "Process establishment failure.";
            throw new ServerException( error, e );
        }

        OutputStreamReader output = new OutputStreamReader( m_process.getInputStream() );
        ErrorStreamReader err = new ErrorStreamReader( m_process.getErrorStream() );

        output.start();
        err.start();

        long timestamp = System.currentTimeMillis();
        long timeout = timestamp + getStartupTimeout();

        while( ( getState() == Application.STARTING ) 
           && ( System.currentTimeMillis() < timeout )
           && ( null == m_error ) )
        {
            try
            {
                wait( 600 );
            }
            catch( InterruptedException e )
            {
            }
        }

        if( null != m_error )
        {
            handleStop( false );
            String error = m_error;
            m_error = null;
            throw new ServerException( error );
        }

        synchronized( getState() )
        {
            final String key = m_profile.getID();
            if( getState() == Application.STARTING )
            {
                final String message = 
                  "application [" 
                  + key 
                  + "] failed to start within the allocated startup time of "
                  + timestamp
                  + " miliseconds";
                getLogger().warn( message );
                handleStop( false );
                throw new ServerException( message );
            }
            else
            {
                long k = System.currentTimeMillis() - timestamp;
                final String notice = 
                  "startup of process [" 
                  + key 
                  + "] completed in " 
                  + k 
                  + " milliseconds";
                getLogger().info( notice );
            }
        }
    }

    // TODO: change this to an elegent shutdown request and only 
    // use Process.destroy as a result of a request timeout

    public synchronized void stop()
    {
        handleStop( true );
    }

    private void handleStop( boolean external )
    {
        setState( Application.STOPPING );
        if( null != m_process )
        {
            m_process.destroy();
        }
        setState( Application.READY );
    }

    public synchronized void restart() throws RemoteException
    {
        handleStop( true );
        start();
    }

    public State getState()
    {
        return m_state;
    }

    // TODO: add state listeners

    private void setState( State state )
    {
        setState( state, null );
    }

    private void setState( State state, PID pid )
    {
        synchronized( m_state )
        {
            if( m_state != state )
            {
                m_state = state;
                ApplicationEvent event = new ApplicationEvent ( this, state );
                super.enqueueEvent( event );
                if( null == pid )
                {
                    getLogger().info( "state [" + state.key() + "]" );
                }
                else
                {
                    m_pid = pid;
                    getLogger().info( "state [" + state.key() + "] in " + m_pid );
                }
                if( m_state == Application.READY )
                {
                    m_pid = null;
                    m_process = null;
                    //m_control = null;
                }
            }
        }
    }

   /**
    * If the underlying OS is Windows then then depot translates to depot.exe
    * and everyting works fine.  If the OSM is not windows then return a command
    * using the java exec.
    * 
    * @param arg a single commandline option
    * @return the command array to use to execute a depot sub-process
    * @exception IOException if an IO error occurs
    */
    private String[] getProcessCommand() throws IOException
    {
        ArrayList list = new ArrayList();
        String key = m_profile.getID();
        boolean useJava = "java".equals( System.getProperty( "dpml.station.subprocess", "" ) );
        if( !useJava && Environment.isWindows() )
        {
            getLogger().info( "launching native subprocess" );
            list.add( "depot" );
            list.add( STARTUP_SYSPROPERTY_ARGUMENT );
            list.add( SHUTDOWN_SYSPROPERTY_ARGUMENT );
            list.add( ERROR_SYSPROPERTY_ARGUMENT );
            list.add( "-exec" );
            list.add( m_path );
        }
        else
        {
            getLogger().info( "launching java subprocess" );
            File bin = new File( Transit.DPML_SYSTEM, "bin" );
            String policy = new File( bin, "security.policy" ).getCanonicalPath();
            String lib = new File( Transit.DPML_DATA, "lib" ).getCanonicalPath();
            
            list.add( "java" );
            list.add( STARTUP_SYSPROPERTY_ARGUMENT );
            list.add( SHUTDOWN_SYSPROPERTY_ARGUMENT );
            list.add( ERROR_SYSPROPERTY_ARGUMENT );
            list.add( "-Djava.ext.dirs=" + lib );
            list.add( "-Djava.security.policy=" + policy );
            list.add( "net.dpml.depot.Main" );
            list.add( "-exec" );
            list.add( "-m_path" );
        }

        Properties properties = m_profile.getSystemProperties();

        //
        // unless otherwise defined, set the loging configuration for the 
        // process to use the logging service established by the Station
        //

        if( null == properties.getProperty( "java.util.logging.config.class" ) )
        {
            properties.setProperty( 
              "java.util.logging.config.class", 
              "net.dpml.depot.DepotLoggingConfiguration" );
        }
        Enumeration names = properties.propertyNames();
        while( names.hasMoreElements() )
        {
            String name = (String) names.nextElement();
            String value = properties.getProperty( name );
            list.add( "-D" + name + "=" + value );
        }
        return (String[]) list.toArray( new String[0] );
    }

   /**
    * Internal event handler.
    * @param eventObject the event
    */
    protected void processEvent( EventObject eventObject )
    {
        if( eventObject instanceof ApplicationEvent )
        {
            ApplicationEvent event = (ApplicationEvent) eventObject;
            processApplicationEvent( event );
        }
        else
        {
            final String error = 
              "Event class not recognized: " + eventObject.getClass().getName();
            throw new IllegalArgumentException( error );
        }
    }

    private void processApplicationEvent( ApplicationEvent event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i < listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof ApplicationListener )
            {
                try
                {
                    ApplicationListener applicationListener = (ApplicationListener) listener;
                    applicationListener.applicationStateChanged( event );
                }
                catch( ConnectException e )
                {
                    super.removeListener( listener );
                }
                catch( Throwable e )
                {
                    final String error =
                      "ApplicationListener notification error.";
                    getLogger().error( error, e );
                }
            }
        }
    }

   /**
    * Internal abstract class to handle reading of subprocess output and error streams.
    */
    private abstract class StreamReader extends Thread
    {
        private final InputStream m_input;
        
       /**
        * Creation of a new reader.
        * @param input the subprocess input stream
        */
        public StreamReader( InputStream input )
        {
            m_input = input;
        }

        protected InputStream getInputStream()
        {
            return m_input;
        }
    }

   /**
    * Internal class to handle reading of subprocess output streams.
    */
    private class OutputStreamReader extends StreamReader
    {
       /**
        * Creation of a process output reader.
        * @param input the subprocess input stream
        */
        public OutputStreamReader( InputStream input )
        {
            super( input );
        }
  
       /**
        * Start the stream reader.
        */
        public void run()
        {
            try
            {
                InputStreamReader isr = new InputStreamReader( getInputStream() );
                BufferedReader reader = new BufferedReader( isr );
                String line = null;
                while( ( line = reader.readLine() ) != null )
                {
                    if( line.startsWith( STARTUP_MESSAGE ) )
                    {
                        //
                        // We are receiving notification from the process that 
                        // process startup has been completed.  The message also
                        // includes the process identifier.
                        //

                        String lead = line.substring( STARTUP_MESSAGE.length() + 2 );
                        int j = lead.indexOf( "]" );
                        String value = lead.substring( 0, j );
                        PID pid = new PID( Integer.parseInt( value ) );
                        setState( Application.RUNNING, pid );
                    }
                    else if( line.startsWith( SHUTDOWN_MESSAGE ) )
                    {
                        //
                        // we are receiving notification from the process that 
                        // a shutdown has been initiated - the following implementation
                        // waits for process exit before changing the application state
                        // to READY
                        //

                        setState( Application.STOPPING );
                        boolean terminated = false;
                        while( !terminated )
                        {
                            try
                            {
                                m_process.exitValue();
                                terminated = true;
                            }
                            catch( IllegalThreadStateException e )
                            {
                                try
                                {
                                    wait( 1000 );
                                }
                                catch( InterruptedException ie )
                                {
                                }
                            }
                        }
                        setState( Application.READY );
                    }
                    else
                    {
                        System.out.println( line );
                    }
                }
            }
            catch( IOException e )
            {
                 getLogger().error( "Process read error.", e );
            }
        }
    }

   /**
    * Internal class to handle reading of subprocess output and error streams.
    */
    private class ErrorStreamReader extends StreamReader
    {
       /**
        * Creation of a process output reader.
        * @param input the subprocess input stream
        */
        public ErrorStreamReader( InputStream input )
        {
            super( input );
        }
  
       /**
        * Start the stream reader.
        */
        public void run()
        {
            try
            {
                InputStreamReader isr = new InputStreamReader( getInputStream() );
                BufferedReader reader = new BufferedReader( isr );
                String line = null;
                while( ( line = reader.readLine() ) != null )
                {
                    if( line.startsWith( ERROR_MESSAGE ) )
                    {
                        m_error = line.substring( ERROR_MESSAGE.length() );

                        handleStop( false );

                        // TODO: make sure the subprocess has terminated by waiting
                        // for some timeout period then killing if necessary
                    }
                    else
                    {
                        System.out.println( line );
                    }
                }
            }
            catch( IOException e )
            {
                 getLogger().error( "Process read error.", e );
            }
        }
    }

    private static final String NOTIFY_HEADER = "#";
    private static final String STARTUP_MESSAGE = NOTIFY_HEADER + "startup";
    private static final String SHUTDOWN_MESSAGE = NOTIFY_HEADER + "shutdown";
    private static final String ERROR_MESSAGE = NOTIFY_HEADER + "error";
    private static final String PID_DELIMITER = ":";

    private static final String STARTUP_SYSPROPERTY_ARGUMENT = 
      "-Ddpml.station.notify.startup=" + STARTUP_MESSAGE;
    private static final String SHUTDOWN_SYSPROPERTY_ARGUMENT = 
      "-Ddpml.station.notify.shutdown=" + SHUTDOWN_MESSAGE;
    private static final String ERROR_SYSPROPERTY_ARGUMENT = 
      "-Ddpml.station.notify.error=" + ERROR_MESSAGE;

}

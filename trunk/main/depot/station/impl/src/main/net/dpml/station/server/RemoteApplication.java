
package net.dpml.station.server; 

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Properties;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.profile.info.StartupPolicy;
import net.dpml.profile.info.ApplicationDescriptor;

import net.dpml.part.Component;

import net.dpml.station.Station;
import net.dpml.station.Callback;
import net.dpml.station.ProcessState;
import net.dpml.station.Application;
import net.dpml.station.ApplicationException;
import net.dpml.station.ApplicationListener;
import net.dpml.station.ApplicationEvent;

import net.dpml.transit.Environment;
import net.dpml.transit.Logger;
import net.dpml.transit.Transit;
import net.dpml.transit.PID;
import net.dpml.transit.model.UnknownKeyException;

/**
 */
public class RemoteApplication extends EventChannel implements Callback, Application
{
    private final Logger m_logger;
    private final ApplicationDescriptor m_descriptor;
    private final String m_id;
    private final int m_port;
    
    private ProcessState m_state = ProcessState.IDLE;
    private PID m_pid = null;
    private Component m_handler = null;
    private Process m_process = null;
    private Exception m_error = null;

   /**
    * Creation of an application instance.
    *
    * @param logger the assigned logging channel
    * @param args supplimentary command line arguments
    * @exception Exception if a exception occurs during establishment
    */
    public RemoteApplication( Logger logger, ApplicationDescriptor descriptor, String id, int port ) throws RemoteException
    {
        super( logger );
        
        m_logger = logger;
        m_descriptor = descriptor;
        m_id = id;
        m_port = port;
    }
    
    //-------------------------------------------------------------------------------
    // Callback
    //-------------------------------------------------------------------------------
    
   /**
    * Method invoked by a process to signal that the process has 
    * commenced startup.
    *
    * @param pid the process identifier
    */
    public void started( PID pid, Component handler ) throws ApplicationException
    {
        if( null != m_pid ) 
        {
            final String error = 
              "PID already assigned.";
            throw new ApplicationException( error );
        }
        synchronized( m_state )
        {
            m_pid = pid;
            m_handler = handler;
            try
            {
                handler.activate();
                setProcessState( ProcessState.STARTED );
            }
            catch( Exception e )
            {
                try
                {
                    handler.deactivate();
                }
                catch( Exception ee )
                {
                }
            }
        }
    }
    
   /**
    * Method invoked by a process to signal that the process has 
    * encounter an error condition.
    *
    * @param throwable the error condition
    * @param fatal if true the process is requesting termination
    */
    public void error( Throwable throwable, boolean fatal )
    {
        if( fatal )
        {
            getLogger().error( "Process raised an fatal error.", throwable );
        }
        else
        {
            getLogger().warn( "Process raised an non-fatal error.", throwable );
        }
    }
    
   /**
    * Method invoked by a process to send a arbitary message to the 
    * the callback handler.
    *
    * @param message the message
    */
    public void info( String message )
    {
        getLogger().info( "[" + m_pid + "]: " + message );
    }
    
   /**
    * Method invoked by a process to signal its imminent termination.
    *
    * @exception RemoteException if a remote error occurs
    */
    public void stopped()
    {
        synchronized( m_state )
        {
            setProcessState( ProcessState.STOPPED );
            m_pid = null;
        }
    }
    
    //-------------------------------------------------------------------------------
    // Application
    //-------------------------------------------------------------------------------
    
   /**
    * Return the process identifier of the process within which the 
    * application is running.  If the application is not running a null
    * value is returned.
    *
    * @return the pid
    */
    public PID getPID()
    {
        return m_pid;
    }

   /**
    * Return the profile associated with this application 
    * @return the application profile
    * @exception RemoteException if a remote error occurs
    */
    public ApplicationDescriptor getApplicationDescriptor()
    {
        return m_descriptor;
    }

   /**
    * Return the current deployment state of the process.
    * @return the current process state
    * @exception RemoteException if a remote error occurs
    */
    public ProcessState getState()
    {
        synchronized( m_state )
        {
            return m_state;
        }
    }

   /**
    * Start the application.
    * @exception RemoteException if a remote error occurs
    */
    public void start() throws RemoteException
    {
        if( m_descriptor.getStartupPolicy() == StartupPolicy.DISABLED ) 
        {
            final String error = 
              "Cannot start the application [" 
              + m_id 
              + "] due to the DISABLED startup status.";
            throw new ApplicationException( error );
        }
        
        synchronized( m_state )
        {
            if( m_state.equals( ProcessState.IDLE ) || m_state.equals( ProcessState.STOPPED ) )
            {
                startProcess();
            }
            else
            {
                final String error = 
                  "Cannot start a process in the ["
                  + m_state
                  + "] state.";
                throw new ApplicationException( error );
            }
        }
    }
    
    private void startProcess() throws ApplicationException
    {
        synchronized( m_state )
        {
            setProcessState( ProcessState.STARTING );
            try
            {
                String[] command = getProcessCommand();
                m_process = Runtime.getRuntime().exec( command, null );
            }
            catch( Exception e )
            {
                setProcessState( ProcessState.IDLE );
                final String error = 
                "Process establishment failure.";
                throw new ApplicationException( error, e );
            }
            
        }
        
        OutputStreamReader output = new OutputStreamReader( m_process.getInputStream() );
        ErrorStreamReader err = new ErrorStreamReader( m_process.getErrorStream() );
        output.start();
        err.start();
        
        /*
        long timestamp = System.currentTimeMillis();
        long timeout = timestamp + getStartupTimeout();
        getLogger().info( "waiting " + getStartupTimeout() );
        
        while( ( getProcessState() == ProcessState.STARTING ) 
          && ( System.currentTimeMillis() < timeout )
          && ( null == m_error ) )
        {
            try
            {
                Thread.currentThread().sleep( 600 );
            }
            catch( InterruptedException e )
            {
            }
        }
        
        getLogger().info( "review" );
        if( getProcessState().equals( ProcessState.STARTING ) )
        {
            final String error = 
              "Process failed to start within the timeout period.";
            getLogger().error( error );
            handleStop( false );
        }
        else if( null != m_error )
        {
            final String error = 
              "Application deployment failure.";
            handleStop( false );
            throw new ApplicationException( error, m_error );
        }
        */
    }

   /**
    * @exception IOException if an IO error occurs
    */
    private String[] getProcessCommand() throws IOException
    {
        ArrayList list = new ArrayList();
        
        String path = m_descriptor.getCodeBaseURISpec();
        list.add( "metro" );

        //
        // add system properties
        //
        
        Properties properties = m_descriptor.getSystemProperties();
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
        
        //
        // add the deployment target
        //
        
        list.add( "-uri" );
        list.add( path );
        
        //
        // add options necessary for the handler to establish a callback
        //
        
        list.add( "-port" );
        list.add( "" + m_port );
        list.add( "-key" );
        list.add( "" + m_id );
        
        return (String[]) list.toArray( new String[0] );
    }
    
   /**
    * Stop the application.
    * @exception RemoteException if a rmote error occurs
    */
    public void stop() throws RemoteException
    {
        handleStop( true );
    }
    
    void shutdown() throws IOException
    {
        try
        {
            handleStop( false );
        }
        catch( Throwable e )
        {
            final String error = 
              "Application shutdown error.";
            getLogger().warn( error, e );
        }
        finally
        {
            UnicastRemoteObject.unexportObject( this, true );
        }
    }
    
    private void handleStop( boolean check ) throws ApplicationException
    {
        synchronized( m_state )
        {
            if( m_state.equals( ProcessState.IDLE ) )
            {
                return;
            }
            else if( m_state.equals( ProcessState.STOPPED ) )
            {
                return;
            }
            else
            {
                setProcessState( ProcessState.STOPPING );
                if( m_handler != null )
                {
                    try
                    {
                        m_handler.deactivate();
                    }
                    catch( Throwable e )
                    {
                        final String error = 
                          "Component deactivation error reported.";
                        getLogger().warn( error, e );
                    }
                }
                setProcessState( ProcessState.STOPPED );
                if( null != m_process )
                {
                    m_process.destroy();
                    m_process = null;
                }
                m_pid = null;
            }
        }
    }

   /**
    * Restart the application.
    * @exception RemoteException if a rmote error occurs
    */
    public void restart() throws RemoteException
    {
        stop();
        start();
    }

   /**
    * Add an application listener.
    * @param listener the listener to add
    * @exception RemoteException if a rmote error occurs
    */
    public void addApplicationListener( ApplicationListener listener )
    {
        super.addListener( listener );
    }
    
   /**
    * Remove an application listener.
    * @param listener the listener to remove
    * @exception RemoteException if a rmote error occurs
    */
    public void removeApplicationListener( ApplicationListener listener )
    {
        super.removeListener( listener );
    }
    
    //-------------------------------------------------------------------------------
    // private utilities
    //-------------------------------------------------------------------------------
    
    private void setProcessState( ProcessState state )
    {
        synchronized( m_state )
        {
            if( m_state != state )
            {
                m_state = state;
                ApplicationEvent event = new ApplicationEvent ( this, state );
                super.enqueueEvent( event );
                getLogger().info( "state set to [" + state.getName() + "]" );
            }
        }
    }
    
    private ProcessState getProcessState()
    {
        return m_state;
    }
    
    //-------------------------------------------------------------------------------
    // EventChannel
    //-------------------------------------------------------------------------------
    
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
                    applicationListener.stateChanged( event );
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

    private long getStartupTimeout()
    {
        return m_descriptor.getStartupTimeout() * 1000000;
    }

    private long getShutdownTimeout()
    {
        return m_descriptor.getShutdownTimeout() * 1000000;
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
                    System.out.println( line );
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
                    System.out.println( line );
                }
            }
            catch( IOException e )
            {
                 getLogger().error( "Process read error.", e );
            }
        }
    }}
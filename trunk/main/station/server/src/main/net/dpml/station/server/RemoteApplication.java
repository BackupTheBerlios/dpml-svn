/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.station.server; 

import java.io.IOException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Properties;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.station.info.StartupPolicy;
import net.dpml.station.info.ApplicationDescriptor;

import net.dpml.component.Component;
import net.dpml.component.Provider;

import net.dpml.station.Callback;
import net.dpml.station.ProcessState;
import net.dpml.station.Application;
import net.dpml.station.ApplicationException;
import net.dpml.station.ApplicationListener;
import net.dpml.station.ApplicationEvent;

import net.dpml.lang.Logger;
import net.dpml.lang.PID;

/**
 * The RemoteApplication is the default implementation of a remotely 
 * accessible Aplication.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class RemoteApplication extends UnicastEventSource implements Callback, Application
{
    private final Logger m_logger;
    private final ApplicationDescriptor m_descriptor;
    private final String m_id;
    private final int m_port;
    
    private ProcessState m_state = ProcessState.IDLE;
    private PID m_pid = null;
    private Component m_handler = null;
    private Provider m_instance = null;
    private Process m_process = null;
    private Exception m_error = null;

   /**
    * Creation of an application instance.
    *
    * @param logger the assigned logging channel
    * @param descriptor the application descriptor
    * @param id the application key
    * @param port the rmi registry port on which the station is registered
    * @exception RemoteException if a remote exception occurs
    */
    public RemoteApplication( 
      Logger logger, ApplicationDescriptor descriptor, String id, int port ) 
      throws RemoteException
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
    * @param handler the component handler
    * @exception ApplicationException if an application exception occurs
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
                m_instance = handler.getProvider();
                setProcessState( ProcessState.STARTED );
            }
            catch( Exception e )
            {
                try
                {
                    handler.decommission();
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
    * Return the application id.
    *
    * @return the id
    */
    public String getID()
    {
        return m_id;
    }

   /**
    * Return the profile associated with this application 
    * @return the application profile
    */
    public ApplicationDescriptor getApplicationDescriptor()
    {
        return m_descriptor;
    }

   /**
    * Return the current deployment state of the process.
    * @return the current process state
    */
    public ProcessState getProcessState()
    {
        synchronized( m_state )
        {
            return m_state;
        }
    }

   /**
    * Start the application.
    * @exception ApplicationException if an application error occurs
    */
    public void start() throws ApplicationException
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
        
        Logger logger = getLogger();
        OutputStreamReader output = new OutputStreamReader( logger, m_process.getInputStream() );
        ErrorStreamReader err = new ErrorStreamReader( logger, m_process.getErrorStream() );
        output.setDaemon( true );
        err.setDaemon( true );
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
    * Construct the process command parameters sequence.
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
        properties.setProperty( "dpml.subprocess", "true" );
        properties.setProperty( "dpml.station.partition", "depot.station." + m_id );
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
        // add the exec command and codebase parameter
        //
        
        list.add( "exec" );
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
                getLogger().info( "stopping application" );
                setProcessState( ProcessState.STOPPING );
                if( m_handler != null )
                {
                    try
                    {
                        m_handler.decommission();
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
                    m_instance = null;
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
    * Return the component instance handler.
    * @return the instance handler (possibly null)
    */
    public Provider getProvider()
    {
        return m_instance;
    }
    
   /**
    * Add an application listener.
    * @param listener the listener to add
    */
    public void addApplicationListener( ApplicationListener listener )
    {
        super.addListener( listener );
    }
    
   /**
    * Remove an application listener.
    * @param listener the listener to remove
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
}

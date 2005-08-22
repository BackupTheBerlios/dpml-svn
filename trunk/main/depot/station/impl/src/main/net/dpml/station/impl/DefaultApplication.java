
package net.dpml.station.impl; 

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.server.UnicastRemoteObject;

import net.dpml.depot.PID;
import net.dpml.station.Application;

import net.dpml.profile.ApplicationProfile;

import net.dpml.transit.Transit;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.Connection;
import net.dpml.transit.Environment;

/**
 * Default implementation of a remote controlled application.
 */
public class DefaultApplication extends UnicastRemoteObject implements Application
{
    private final Logger m_logger;
    private final ApplicationProfile m_profile;
    private final String m_path;

    private Process m_process;
    private PID m_pid;

    public DefaultApplication( 
      Logger logger, ApplicationProfile profile, String path ) throws RemoteException
    {
        super();

        m_profile = profile;
        m_logger = logger;
        m_path = path;
    }

    public ApplicationProfile getProfile() throws RemoteException
    {
        return m_profile;
    }

    public PID getPID() throws RemoteException
    {
        return m_pid;
    }

    public synchronized PID start() throws RemoteException
    {
        if( null != m_pid )
        {
            final String key = m_profile.getID();
            final String error = 
              "Application [" + key + "] is already started.";
            throw new ServerException( error );              
        }
        
        try
        {
            String[] command = getProcessCommand();
            m_process = Runtime.getRuntime().exec( command, null );
            getLogger().info( "suprocess established: " + m_process );
        }
        catch( Exception e )
        {
            final String error = 
              "Process establishment failure.";
            throw new ServerException( error, e );
        }

        StreamReader out = new StreamReader( m_process.getInputStream() );
        StreamReader err = new StreamReader( m_process.getErrorStream() );
        out.start();
        err.start();
        return null;
    }

    public synchronized void stop() throws RemoteException
    {
        if( null != m_process )
        {
            m_process.destroy();
            m_process = null;
            m_pid = null;
        }
    }

    public synchronized PID restart() throws RemoteException
    {
        stop();
        return start();
    }

    private Logger getLogger()
    {
        return m_logger;
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
        String key = m_profile.getID();
        boolean useJava = "java".equals( System.getProperty( "dpml.station.subprocess", "" ) );
        if( !useJava && Environment.isWindows() )
        {
            getLogger().info( "launching native subprocess" );
            return new String[]
            {
              "depot", "-exec", m_path
            };
        }
        else
        {
            getLogger().info( "launching java subprocess" );
            File bin = new File( Transit.DPML_SYSTEM, "bin" );
            String policy = new File( bin, "security.policy" ).getCanonicalPath();
            String lib = new File( Transit.DPML_DATA, "lib" ).getCanonicalPath();
            
            return new String[]
            {
              "java", 
              "-Ddpml.spawn=true",
              "-Djava.ext.dirs=" + lib,
              "-Djava.security.policy=" + policy,
              "net.dpml.depot.Main", 
              "-exec",
              m_path
            };
        }
    }

   /**
    * Internal class to handle reading of subprocess output and error streams.
    */
    private class StreamReader extends Thread
    {
        private InputStream m_input;
        private boolean m_error = false;
        
       /**
        * Creation of a new reader.
        * @param input the subprocess input stream
        */
        public StreamReader( InputStream input )
        {
            m_input = input;
        }
  
       /**
        * Start thestream reader.
        */
        public void run()
        {
            try
            {
                InputStreamReader isr = new InputStreamReader( m_input );
                BufferedReader reader = new BufferedReader( isr );
                String line = null;
                while( ( line = reader.readLine() ) != null )
                {
                    getLogger().info( line );
                }
            }
            catch( IOException e )
            {
                 getLogger().error( "Process read error.", e );
            }
        }
    }

}

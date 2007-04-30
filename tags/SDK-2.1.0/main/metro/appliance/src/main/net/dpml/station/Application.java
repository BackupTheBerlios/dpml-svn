/*
 * Copyright 2007 Stephen J. McConnell.
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

package net.dpml.station;

import dpml.util.PID;

import dpml.station.connector.LocalConnector;
import dpml.station.info.ApplianceDescriptor;
import dpml.station.util.OutputStreamReader;
import dpml.station.util.LoggingServer;

import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Set;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import net.dpml.appliance.Appliance;
import net.dpml.appliance.ApplianceException;
import net.dpml.appliance.ApplianceListener;

import net.dpml.util.Logger;

/**
 * Process configuration and management support class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
final class Application
{
    static
    {
        LoggingServer.init();
    }
    
    private static final PID PROCESS_ID = new PID();
    
    private final ApplianceDescriptor m_descriptor;
    private final Logger m_logger;
    private final ProcessBuilder m_builder;
    private final LocalConnector m_connector;
    private final String m_address;
    private final String m_key;
    
    Application( Logger logger, String key, ApplianceDescriptor descriptor ) throws IOException
    {
        m_logger = logger;
        m_descriptor = descriptor;
        m_key = key;
        
        int port = getConnectorPort();
        try
        {
            m_connector = new LocalConnector();
            Registry registry = resolveRegistry( port );
            m_address = 
              "dpml/station/" 
              + PROCESS_ID.getValue() 
              + "/"
              + System.identityHashCode( this );
            registry.bind( m_address, m_connector );
        }
        catch( ApplianceException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to bind local station callback to RMI registry.";
            throw new ApplianceException( error, e );
        }
        
        m_builder = newProcessBuilder();
    }
    
    private Registry resolveRegistry( int port )throws ApplianceException
    {
        try
        {
            Registry registry = LocateRegistry.createRegistry( port );
            getLogger().debug( "created local registry on port " + port );
            return registry;
        }
        catch( RemoteException re )
        {
            try
            {
                Registry registry = LocateRegistry.getRegistry( port );
                getLogger().debug( "using local registry on port " + port );
                return registry;
            }
            catch( RemoteException e )
            {
                final String error = 
                  "Unable to locate or create RMI registry to due a remote exeception.";
                throw new ApplianceException( error, e );
            }
        }
    }
    
    private int getConnectorPort()
    {
        String port = System.getProperty( "dpml.appliance.connector.port", null );
        if( null == port )
        {
            return Registry.REGISTRY_PORT;
        }
        else
        {
            return Integer.parseInt( port );
        }
    }
    
   /**
    * Returns the part URI as a string.
    * @return the codebase uri
    */
    //public String getCodebaseURI()
    //{
    //    return m_descriptor.getURI().toASCIIString();
    //}
    
   /**
    * Returns the part title.
    * @return the title
    */
    public String getTitle()
    {
        return m_descriptor.getTitle();
    }
    
   /**
    * Returns the part description.
    * @return the description
    */
    public String getDescription()
    {
        return m_descriptor.getDescription();
    }
    
    public Appliance getAppliance() throws IOException
    {
        int timeout = m_descriptor.getStartupTimeout();
        if( 0 == timeout )
        {
            return getAppliance( TimeUnit.SECONDS, 60 );
        }
        else
        {
            return getAppliance( TimeUnit.SECONDS, timeout );
        }
    }

    public Appliance getAppliance( TimeUnit unit, int timeout ) throws IOException
    {
        final Process process = newProcess();
        long now = new Date().getTime();
        Appliance appliance = m_connector.getAppliance( unit, timeout );
        long then = new Date().getTime();
        long diff = then - now;
        getLogger().info( "process established in " + diff + " ms" );
        return appliance;
    }
    
    Logger getLogger()
    {
        return m_logger;
    }
    
    ApplianceDescriptor getApplianceDescriptor()
    {
        return m_descriptor;
    }
    
    Process newProcess() throws IOException
    {
        Process process = m_builder.start();
        setShutdownHook( process );
        OutputStreamReader output = 
          new OutputStreamReader( m_logger, process.getInputStream() );
        output.setDaemon( true );
        output.start();
        return process;
    }
    
    private ProcessBuilder newProcessBuilder() throws IOException
    {
        List<String> commands = getProcessCommands();
        ProcessBuilder builder = new ProcessBuilder( commands );
        builder.redirectErrorStream( true );
        File dir = getValidatedProcessDirectory();
        builder.directory( dir );
        Map<String,String> environment = builder.environment();
        Map<String,String> env = m_descriptor.getEnvironmentMap();
        environment.putAll( env );
        return builder;
    }
    
    private File getValidatedProcessDirectory() throws IOException
    {
        File file = getProcessDirectory();
        if( null == file )
        {
            return file;
        }
        if( !file.exists() )
        {
            final String error = 
              "Process basedir not found: " 
              + file.getCanonicalPath();
            throw new ApplianceException( error, null, m_descriptor.getElement() );  
            //throw new FileNotFoundException( error );
        }
        if( !file.isDirectory() )
        {
            final String error = 
              "Process basedir is not a directory: " 
              + file.getCanonicalPath();
            throw new ApplianceException( error, null, m_descriptor.getElement() );  
            //throw new IllegalArgumentException( error );
        }
        return file;
    }
    
    private File getProcessDirectory()
    {
        String path = m_descriptor.getPath();
        if( null == path )
        {
            return null;
        }
        else
        {
            File file = new File( path );
            if( file.isAbsolute() )
            {
                return file;
            }
            else
            {
                File base = new File( System.getProperty( "user.dir" ) );
                return new File( base, path );
            }
        }
    }
    
    private List<String> getProcessCommands() throws IOException
    {
        List<String> list = new ArrayList<String>();
        
        //
        // add the metro command
        //
        
        String executable = m_descriptor.getExecutable();
        list.add( executable );
        
        //
        // add partition
        //
        
        if( null != m_key )
        {
            list.add( "-Ddpml.station.partition=" + m_key );
        }
        
        //
        // add connection key
        //
        
        list.add( "-Ddpml.appliance.connector.key=" + m_address );
        
        //
        // add codebase uri
        //
        
        URI codebase = m_descriptor.getCodebaseURI();
        if( null != codebase )
        {
            String spec = codebase.toASCIIString();
            list.add( "-Ddpml.appliance.codebase.uri=" + spec );
        }
        
        //
        // add decommissioning timeout
        //
        
        list.add( "-Ddpml.decommmission.timeout=" + m_descriptor.getShutdownTimeout() );
        
        //
        // add default system properties
        //
        
        list.add( "-Djava.util.logging.config.class=dpml.util.DepotLoggingConfiguration" );
        //list.add( "-Ddpml.logging.config=local:properties:dpml/station/application" );

        //
        // add declared system properties
        //
        
        Properties properties = m_descriptor.getSystemProperties();
        Set<String> names = properties.stringPropertyNames();
        for( String name : names )
        {
            String value = properties.getProperty( name ); // expand symbols?
            list.add( "-D" + name + "=" + value );
        }
        
        //
        // add the codebase uri
        //
        
        URI uri = m_descriptor.getTargetURI();
        String spec = uri.toASCIIString();
        list.add( spec );
        
        return list;
    }
    
    private static void setShutdownHook( final ApplianceListener listener )
    {
        Runtime.getRuntime().addShutdownHook(
          new Thread()
          {
            public void run()
            {
                try
                {
                    UnicastRemoteObject.unexportObject( listener, false );
                }
                catch( RemoteException e )
                {
                }
            }
          }
        );
    }
    
    private static void setShutdownHook( final Process process )
    {
        Runtime.getRuntime().addShutdownHook(
          new Thread()
          {
            public void run()
            {
                try
                {
                    process.destroy();
                }
                catch( Throwable e )
                {
                    e.printStackTrace();
                }
            }
          }
        );
    }
}

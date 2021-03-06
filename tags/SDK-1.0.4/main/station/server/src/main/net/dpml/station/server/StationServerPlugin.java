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

import java.net.URI;
import java.net.URL;
import java.rmi.registry.Registry;

import net.dpml.cli.Option;
import net.dpml.cli.Group;
import net.dpml.cli.CommandLine;
import net.dpml.cli.OptionException;
import net.dpml.cli.commandline.Parser;
import net.dpml.cli.builder.ArgumentBuilder;
import net.dpml.cli.builder.GroupBuilder;
import net.dpml.cli.builder.DefaultOptionBuilder;
import net.dpml.cli.option.PropertyOption;
import net.dpml.cli.validation.URIValidator;
import net.dpml.cli.validation.NumberValidator;

import net.dpml.station.ApplicationRegistry;

import net.dpml.transit.Artifact;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.management.TransitController;

import net.dpml.util.Logger;

/**
 * The RemoteStation is responsible for the establishment of 
 * callback monitors to external processes established by the 
 * station manager.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StationServerPlugin implements Runnable
{
    private final Logger m_logger;
    private final CommandLine m_line;
    private final TransitModel m_model;
    
    private RemoteStation m_station;
    
   /**
    * Creation of a new station server plugin for station commandline
    * handling.
    * @param logger the assigned logging channel
    * @param model the transit model
    * @param args the command line arguments array
    * @exception Exception if an error occurs
    */
    public StationServerPlugin( Logger logger, TransitModel model, String[] args ) throws Exception
    {
        m_logger = logger;
        m_model = model;
        
        // parse the command group model
        
        Parser parser = new Parser();
        parser.setGroup( COMMAND_GROUP );
        try
        {
            m_line = parser.parse( args );
        }
        catch( OptionException e )
        {
            final String message = "Server commandline processing error.";
            StringBuffer buffer = new StringBuffer( message );
            buffer.append( "\nArgument count: " + args.length );
            for( int i=0; i<args.length; i++ )
            {
                buffer.append( "\n  arg (" + i + "): [" + args[i] + "]" );
            }
            String error = buffer.toString();
            throw new Exception( error, e );
        }
    }
    
   /**
    * Start the thread.
    */
    public void run()
    {
        try
        {
            // register MBeans
            
            String jmxEnabled = System.getProperty( "dpml.transit.jmx.enabled" );
            if( "true".equals( jmxEnabled ) )
            {
                try
                {
                    new TransitController( m_logger, m_model );
                    m_logger.info( "Transit controller established." );
                }
                catch( NoClassDefFoundError ncdfe )
                {
                    if( m_logger.isWarnEnabled() )
                    {
                        m_logger.warn( "JMX resources unavailable." );
                    }
                }
                catch( ClassNotFoundException cnfe )
                {
                    if( m_logger.isWarnEnabled() )
                    {
                        m_logger.warn( "JMX resources unavailable." );
                    }
                }
            }
            
            int port = getPortValue( m_line, Registry.REGISTRY_PORT );
            URI uri = (URI) m_line.getValue( URI_OPTION, null );
            if( null == uri )
            {
                URL url = ApplicationRegistry.DEFAULT_STORAGE_URI.toURL();
                m_logger.info( "starting station on port: " + port );
                m_station = new RemoteStation( m_logger, m_model, port, url );
                Thread.currentThread().setContextClassLoader( ApplicationRegistry.class.getClassLoader() );
                setShutdownHook( m_station );
            }
            else
            {
                if( Artifact.isRecognized( uri ) )
                {
                    URL url = Artifact.createArtifact( uri ).toURL();
                    m_logger.info( 
                    "starting station on port: " 
                    + port 
                    + " with registry " 
                    + url );
                    m_station = new RemoteStation( m_logger, m_model, port, url );
                }
                else
                {
                    URL url = uri.toURL();
                    m_logger.info( 
                      "starting station on port: " 
                      + port 
                      + " with registry " 
                      + url );
                    m_station = new RemoteStation( m_logger, m_model, port, url );
                }
            }
        }
        catch( Exception e )
        {
            final String error = 
              "Station startup failure.";
            m_logger.error( error, e );
        }
    }
    
    private int getPortValue( CommandLine line, int defaultPort )
    {
        if( line.hasOption( PORT_OPTION ) )
        {
            Number number = (Number) line.getValue( PORT_OPTION, null );
            return number.intValue();
        }
        else
        {
            return defaultPort;
        }
    }
    
    private URI getRegistryURI( CommandLine line, URI uri )
    {
        if( line.hasOption( URI_OPTION ) )
        {
            return (URI) line.getValue( URI_OPTION, null );
        }
        else
        {
            return uri;
        }
    }

   /**
    * Create a shutdown hook that will trigger shutdown of the station.
    * @param station the station
    */
    public static void setShutdownHook( final RemoteStation station )
    {
        Runtime.getRuntime().addShutdownHook( new ShutdownHandler( station ) );
    }
    
    private static final DefaultOptionBuilder OPTION_BUILDER = new DefaultOptionBuilder();
    private static final ArgumentBuilder ARGUMENT_BUILDER = new ArgumentBuilder();
    private static final GroupBuilder GROUP_BUILDER = new GroupBuilder();

    private static final PropertyOption PROPERTY_OPTION = new PropertyOption();
    private static final NumberValidator NUMBER_VALIDATOR = NumberValidator.getIntegerInstance();
      
    private static final Option PORT_OPTION = 
      ARGUMENT_BUILDER 
        .withDescription( "Port number." )
        .withName( "port" )
        .withMinimum( 0 )
        .withMaximum( 1 )
        .withValidator( NUMBER_VALIDATOR )
        .create();
        
    private static final Option URI_OPTION = 
        OPTION_BUILDER
          .withShortName( "registry" )
          .withDescription( "Application registry store." )
          .withRequired( false )
          .withArgument(
            ARGUMENT_BUILDER 
              .withDescription( "Local or remote artifact reference." )
              .withName( "artifact" )
              .withMinimum( 1 )
              .withMaximum( 1 )
              .withValidator( new URIValidator() )
              .create() )
          .create();
        
    private static final Group COMMAND_GROUP =
      GROUP_BUILDER
        .withName( "options" )
        .withOption( PORT_OPTION )
        .withOption( URI_OPTION )
        .withOption( PROPERTY_OPTION )
        .create();

   /**
    * Shutdown handler implementation.
    */
    private static class ShutdownHandler extends Thread
    {
        private final RemoteStation m_station;
        
       /**
        * Creation of a new shutdown handler.
        * @param station the station to shutdown
        */
        ShutdownHandler( final RemoteStation station )
        {
            m_station = station;
        }
        
        public void run()
        {
            m_station.shutdown();
        }
    }
   
}

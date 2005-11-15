
package net.dpml.station.server; 

import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import net.dpml.cli.Option;
import net.dpml.cli.Group;
import net.dpml.cli.CommandLine;
import net.dpml.cli.commandline.Parser;
import net.dpml.cli.util.HelpFormatter;
import net.dpml.cli.OptionException;
import net.dpml.cli.DisplaySetting;
import net.dpml.cli.builder.ArgumentBuilder;
import net.dpml.cli.builder.GroupBuilder;
import net.dpml.cli.builder.DefaultOptionBuilder;
import net.dpml.cli.option.PropertyOption;
import net.dpml.cli.validation.URIValidator;
import net.dpml.cli.validation.NumberValidator;

import net.dpml.station.Station;
import net.dpml.profile.model.ApplicationRegistry;

import net.dpml.transit.Artifact;
import net.dpml.transit.Logger;

/**
 * The RemoteStation is responsible for the establishment of 
 * callback monitors to external processes established by the 
 * station manager.
 */
public class StationServerPlugin
{
    private final RemoteStation m_station;
    
    public StationServerPlugin( Logger logger, String[] args ) throws Exception
    {
    
        // parse the command group model
        
        Parser parser = new Parser();
        parser.setGroup( COMMAND_GROUP );
        
        CommandLine line = parser.parse( args );
        int port = getPortValue( line, Registry.REGISTRY_PORT );
        URI uri = (URI) line.getValue( URI_OPTION, null );
        if( null == uri )
        {
            URL url = ApplicationRegistry.DEFAULT_STORAGE_URI.toURL();
            logger.info( "starting station on port: " + port );
            m_station = new RemoteStation( logger, port, url );
            setShutdownHook( m_station );
        }
        else
        {
            if( Artifact.isRecognized( uri ) )
            {
                URL url = Artifact.createArtifact( uri ).toURL();
                logger.info( 
                  "starting station on port: " 
                  + port 
                  + " with registry " 
                  + url );
                m_station = new RemoteStation( logger, port, url );
            }
            else
            {
                URL url = uri.toURL();
                logger.info( 
                  "starting station on port: " 
                  + port 
                  + " with registry " 
                  + url );
                m_station = new RemoteStation( logger, port, url );
            }
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
    * @param thread the application thread
    */
    public static void setShutdownHook( final RemoteStation station )
    {
        Runtime.getRuntime().addShutdownHook( new ShutdownHandler( station ) );
    }
    
    private static class ShutdownHandler extends Thread
    {
        private final RemoteStation m_station;
        
        ShutdownHandler( final RemoteStation station )
        {
            m_station = station;
        }
        
        public void run()
        {
            m_station.shutdown();
        }
    }
   
    private static final DefaultOptionBuilder OPTION_BUILDER = new DefaultOptionBuilder();
    private static final ArgumentBuilder ARGUMENT_BUILDER = new ArgumentBuilder();
    private static final GroupBuilder GROUP_BUILDER = new GroupBuilder();

    private static final PropertyOption PROPERTY_OPTION = new PropertyOption();
    private static NumberValidator portValidator = NumberValidator.getIntegerInstance();
      
    private static final Option PORT_OPTION = 
      ARGUMENT_BUILDER 
        .withDescription( "Port number." )
        .withName( "port" )
        .withMinimum( 0 )
        .withMaximum( 1 )
        .withValidator( portValidator )
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
}

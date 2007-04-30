/*
 * Copyright 2005-2007 Stephen McConnell
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

package dpml.metro.console;

import dpml.appliance.ApplianceHelper;

import dpml.cli.Option;
import dpml.cli.Group;
import dpml.cli.CommandLine;
import dpml.cli.OptionException;
import dpml.cli.TooManyArgumentsException;
import dpml.cli.commandline.Parser;
import dpml.cli.util.HelpFormatter;
import dpml.cli.DisplaySetting;
import dpml.cli.builder.ArgumentBuilder;
import dpml.cli.builder.GroupBuilder;
import dpml.cli.builder.DefaultOptionBuilder;
import dpml.cli.builder.CommandBuilder;
import dpml.cli.option.PropertyOption;
import dpml.cli.validation.URIValidator;
import dpml.cli.validation.NumberValidator;

import dpml.util.PID;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;
import java.util.Iterator;
import java.util.Set;
import java.util.EnumSet;
import java.util.Collections;

import javax.tools.Tool;
import javax.lang.model.SourceVersion;

import net.dpml.annotation.Component;

import net.dpml.appliance.Appliance;
import net.dpml.appliance.ApplianceError;
import net.dpml.appliance.ApplianceListener;
import net.dpml.appliance.ApplianceEvent;
import net.dpml.appliance.ApplianceConnector;

import net.dpml.runtime.ComponentListener;
import net.dpml.runtime.ComponentEvent;
import net.dpml.runtime.ProviderEvent;
import net.dpml.runtime.Status;

import net.dpml.transit.Artifact;
import net.dpml.transit.InvalidArtifactException;
import net.dpml.util.Logger;

import static net.dpml.annotation.LifestylePolicy.SINGLETON;

/**
 * Generic application handler that establishes a dedicated handler based 
 * on the type of resource exposed by a codebase uri.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( lifestyle=SINGLETON )
public class Main implements Tool
{
    //------------------------------------------------------------------------------
    // static
    //------------------------------------------------------------------------------

    private static final PID PROCESS_ID = new PID();
    
    //------------------------------------------------------------------------------
    // state
    //------------------------------------------------------------------------------
    
    private final Logger m_logger;
    
   /**
    * The application handler class is a generic component handler that delegates
    * application deployment processes to dedicated handlers based on the codebase 
    * type of the target application.  Normally this plugin is activated as a 
    * consequence of invoking the <tt>metro</tt> commandline handler (which is 
    * the default haviour of the <tt>station</tt> when deployment local processes).
    *
    * @param logger the assigned logging channel
    * @exception Exception if an error occurs
    */
    public Main( Logger logger ) throws Exception
    {
        m_logger = logger;
        Thread.currentThread().setContextClassLoader( 
          Appliance.class.getClassLoader() );
    }
    
   /**
    * Return the source version supported by this tool.
    * @return the supported source versions as a set
    */
    public Set<SourceVersion> getSourceVersions()
    {
        return Collections.unmodifiableSet(
          EnumSet.range(
            SourceVersion.RELEASE_4,
            SourceVersion.latest() ) );
    }
    
   /**
    * Run the Metro tool.
    * @param in the input stream
    * @param out the output stream
    * @param err the error stream
    * @param arguments a sequence of command handler arguments
    * @return the command handler execution result value
    */
    public int run( InputStream in, OutputStream out, OutputStream err, String... arguments )
    {
        for( int i=0; i<arguments.length; i++ )
        {
            String arg = arguments[i];
            if( m_logger.isTraceEnabled() )
            {
                m_logger.trace( "  arg: " + ( i + 1 ) + ": " + arg );
            }
            if( "-help".equals( arg ) )
            {
                printHelpMessage();
                return 0;
            }
            if( "-version".equals( arg ) )
            {
                printVersionMessage();
                return 0;
            }
        }
        
        try
        {
            return execute( arguments );
        }
        catch( Throwable e )
        {
            m_logger.error( "Execution error.", e );
            return -1;
        }
    }
    
    private int execute( String... args ) throws Exception
    {
        CommandLine line = getCommandLine( args );
        if( ( null == line ) || !line.hasOption( CODEBASE ) )
        {
            printHelpMessage();
            return -1;
        }
        
        URI uri = getURI( line );
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
        try
        {
            Appliance appliance = ApplianceHelper.newAppliance( uri );
            boolean flag = line.hasOption( COMMAND_EXECUTION_OPTION );
            return execute( appliance, flag );
        }
        catch( InvalidArtifactException e )
        {
            String error = 
              "Invalid artifact: " 
              + uri
              + "\n" + e.getMessage()
              + " (expected format: 'scheme:type:[group[/group[/...]]/name[#<version>]).";
            m_logger.error( error );
            return -1;
        }
        catch( MalformedURLException e )
        {
            String error = 
              "Invalid uri: [" 
              + uri
              + "] (" + e.getMessage() 
              + "), expected format: 'scheme:type:[group[/group[/...]]/name[#<version>].";
            m_logger.error( error );
            return -1;
        }
        catch( TooManyArgumentsException e )
        {
            final String error = 
              "Error: "
              + e.getMessage();
            m_logger.error( error );
            return -1;
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( current );
        }
    }
    
    private String getType( URI uri )
    {
        if( Artifact.isRecognized( uri ) )
        {
            Artifact artifact = Artifact.createArtifact( uri );
            return artifact.getType();
        }
        return null; 
    }
    
   /**
    * Execute the appliance.
    * @param appliance the appliance
    * @param flag if true we are executing in command mode
    */
    private int execute( Appliance appliance, boolean flag ) throws Exception
    {
        if( flag )
        {
            return executeAsCommand( appliance );
        }
        else
        {
            // 
            // we are running in server mode - just need to check if we are
            // running as a top-level server, or if we are running as a 
            // child of a parent process
            //
            
            String key = System.getProperty( "dpml.appliance.connector.key", null );
            if( null == key )
            {
                //
                // just go ahead and commission (decommissioning will be handled
                // by the shutdown hook)
                //
                
                try
                {
                    appliance.commission();
                    return 1;
                }
                catch( Exception e )
                {
                    getLogger().error( "error commissioning appliance", e );
                    return -1;
                }
            }
            else
            {
                //
                // we need to bind the appliance to a connector so that the 
                // parent process can get a handle to the appliance - any
                // commissioning/decommissioning activity is the responsibity 
                // of the application that established the callback connector
                //
                
                appliance.addApplianceListener( new LocalApplianceListener() );
                ApplianceConnector connector = getApplianceConnector( key );
                connector.connect( appliance );
                return 1;
            }
        }
    }
    
   /**
    * Execute the appliance.
    * @param appliance the appliance
    * @param flag if true we are executing in command mode
    */
    private int executeAsCommand( Appliance appliance ) throws Exception
    {
        try
        {
            appliance.commission();
        }
        catch( Exception e )
        {
            getLogger().error( "error commissioning appliance", e );
            return -1;
        }
        finally
        {
            try
            {
                appliance.decommission();
                return 0;
            }
            catch( Exception re )
            {
                getLogger().error( "error decommissioning appliance", re );
                return -1;
            }
        }
    }
    
    private static int getStationPort()
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
    
    private static long getDecommissionTimeout()
    {
        String value = System.getProperty( "dpml.decommmission.timeout", null );
        if( null == value )
        {
            return 10;
        }
        else
        {
            try
            {
                return Long.parseLong( value );
            }
            catch( Throwable e )
            {
                return 10;
            }
        }
    }
    
    private URI getURI( CommandLine line )
    {
        URI uri = (URI) line.getValue( CODEBASE, null );
        if( null == uri )
        {
            throw new NullPointerException( "uri" ); // will not happen
        }
        return uri;
    }
    
    private CommandLine getCommandLine( String[] args ) throws Exception
    {
        try
        {
            Parser parser = new Parser();
            parser.setGroup( COMMAND_GROUP );
            return parser.parse( args );
        }
        catch( TooManyArgumentsException e )
        {
            String message = e.getMessage();
            m_logger.error( message );
            return null;
        }
        catch( OptionException oe )
        {
            String message = oe.getMessage();
            m_logger.error( message );
            return null;
        }
    }
    
   /**
    * Return a properties instance composed of the <tt>-C&lt;key&gt;=&lt;value&gt;</tt>
    * commandline arguments.
    * @param line the commandline
    * @return the resolved properties
    */
    private Properties getCommandLineProperties( CommandLine line )
    {
        Properties properties = new Properties();
        Set propertyValue = line.getProperties();
        Iterator iterator = propertyValue.iterator();
        while( iterator.hasNext() )
        {
            String name = (String) iterator.next();
            String value = line.getProperty( name );
            properties.setProperty( name, value );
        }
        return properties;
    }

   /**
    * Internal component monitor.
    */
    private class InternalComponentMonitor implements ComponentListener
    {
       /**
        * Notify the listener of a state change.
        *
        * @param event the state change event
        * @exception RemoteException if a remote transport error occurs
        */
        public void componentChanged( final ComponentEvent event )
        {
            if( event instanceof ProviderEvent )
            {
                ProviderEvent e = (ProviderEvent) event;
                String action = e.getStatus().name().toLowerCase();
                if( m_logger.isDebugEnabled() )
                {
                    m_logger.debug( action );
                }
            }
            else
            {
                m_logger.warn( 
                  "Event not recognized: " 
                  + event.toString() );
            }
        }
    }
    
    private void printVersionMessage()
    {
        StringBuffer buffer = new StringBuffer( "version" );
        buffer.append( "\n" );
        buffer.append( "\n  DPML Metro" );
        buffer.append( "\n  Version @PROJECT-VERSION@" );
        buffer.append( "\n  Copyright 2005-2007 Stephen J. McConnell" );
        buffer.append( "\n  Digital Product Management Library" );
        String message = buffer.toString();
        m_logger.info( message );
    }
    
    private void printHelpMessage()
    {
        StringBuffer buffer = new StringBuffer( "help" );
        buffer.append( "\n" );
        buffer.append( "\n  Description:" );
        buffer.append( "\n" );
        buffer.append( "\n    Deploys a component referenced by a part uri." );
        buffer.append( "\n" );
        buffer.append( "\n  Usage: metro [-command] [-debug | -trace] [-D<name>=<value>] <uri> | -help | -version" );
        buffer.append( "\n" );
        buffer.append( "\n  Arguments:" );
        buffer.append( "\n    <uri>              uri referencing a part datatype" );
        buffer.append( "\n" );
        buffer.append( "\n  Options:" );
        buffer.append( "\n    -debug             enables debug level logging" );
        buffer.append( "\n    -trace             enables trace level logging" );
        buffer.append( "\n    -D<name>=<value>   declaration of one or more system properties" );
        buffer.append( "\n    -command           enables command execution mode" );
        buffer.append( "\n    -help              list command help and exit" );
        buffer.append( "\n    -version           list version info and exit" );
        buffer.append( "\n" );
        String message = buffer.toString();
        m_logger.info( message );
    }
    
    //------------------------------------------------------------------------------
    // internals
    //------------------------------------------------------------------------------
    
    private Logger getLogger()
    {
        return m_logger;
    }

    private ApplianceConnector getApplianceConnector( String key ) throws Exception
    {
        if( null == key )
        {
            throw new NullPointerException( "key " );
        }
        else
        {
            int port = getStationPort();
            Registry registry = LocateRegistry.getRegistry( port );
            try
            {
                return (ApplianceConnector) registry.lookup( key );
            }
            catch( NotBoundException e )
            {
                final String error = 
                  "Connector key [" + key + "] is not bound in registry ["
                  + registry
                  + "].";
                throw new ApplianceError( error, e );
            }
        }
    }
    
   /**
    * Local appliance listener.
    */
    private class LocalApplianceListener implements ApplianceListener
    {
        public void applianceChanged( final ApplianceEvent event )
        {
            Status status = event.getStatus();
            String name = status.name().toLowerCase();
            getLogger().getChildLogger( "event" ).info( name );
            if( Status.TERMINATION.equals( status ) )
            {
                try
                {
                    event.getAppliance().removeApplianceListener( this );
                }
                catch( IOException ioe )
                {
                    final String warning = 
                      "Ignoring error on listener removal.";
                    getLogger().warn( warning, ioe );
                }
            }
        }
    }
    
    //-----------------------------------------------------------------------------
    // CLI
    //-----------------------------------------------------------------------------

   /**
    * List general command help to the console.
    * @exception IOException if an I/O error occurs
    */
    @SuppressWarnings( "unchecked" )
    private void processHelp() throws IOException
    {
        HelpFormatter formatter = new HelpFormatter( 
          HelpFormatter.DEFAULT_GUTTER_LEFT, 
          HelpFormatter.DEFAULT_GUTTER_CENTER, 
          HelpFormatter.DEFAULT_GUTTER_RIGHT, 
          100 );
        
        formatter.getDisplaySettings().add( DisplaySetting.DISPLAY_GROUP_OUTER );
        formatter.getDisplaySettings().add( DisplaySetting.DISPLAY_PROPERTY_OPTION );
        formatter.getDisplaySettings().add( DisplaySetting.DISPLAY_ARGUMENT_BRACKETED );
        
        formatter.getFullUsageSettings().add( DisplaySetting.DISPLAY_OPTIONAL );
        formatter.getFullUsageSettings().add( DisplaySetting.DISPLAY_GROUP_OUTER );
        formatter.getFullUsageSettings().add( DisplaySetting.DISPLAY_PROPERTY_OPTION );
        formatter.getFullUsageSettings().add( DisplaySetting.DISPLAY_OPTIONAL );
        formatter.getFullUsageSettings().add( DisplaySetting.DISPLAY_ARGUMENT_BRACKETED );
        formatter.getFullUsageSettings().remove( DisplaySetting.DISPLAY_PARENT_CHILDREN );
        
        formatter.getLineUsageSettings().add( DisplaySetting.DISPLAY_PROPERTY_OPTION );
        formatter.getLineUsageSettings().add( DisplaySetting.DISPLAY_ARGUMENT_BRACKETED );
        formatter.getLineUsageSettings().remove( DisplaySetting.DISPLAY_PARENT_CHILDREN );
        formatter.getLineUsageSettings().remove( DisplaySetting.DISPLAY_GROUP_EXPANDED );
        
        formatter.setGroup( CODEBASE_GROUP );
        formatter.setShellCommand( "metro" );
        formatter.print();
    }
    
    private static final DefaultOptionBuilder OPTION_BUILDER = new DefaultOptionBuilder();
    private static final ArgumentBuilder ARGUMENT_BUILDER = new ArgumentBuilder();
    private static final GroupBuilder GROUP_BUILDER = new GroupBuilder();
    private static final CommandBuilder COMMAND_BUILDER = new CommandBuilder();

    private static final PropertyOption CONTEXT_OPTION = 
      new PropertyOption( "-C", "Set a context entry value.", 'C' );
    private static final NumberValidator PORT_VALIDATOR = NumberValidator.getIntegerInstance();
    private static final URIValidator URI_VALIDATOR = new URIValidator();
    
    private static final Option HELP_COMMAND =
      OPTION_BUILDER
        .withShortName( "help" )
        .withShortName( "h" )
        .withDescription( "Prints command help." )
        .create();

    private static final Option COMMAND_EXECUTION_OPTION =
      OPTION_BUILDER
        .withShortName( "command" )
        .withShortName( "c" )
        .withDescription( "Enables command execution mode." )
        .create();

    private static final Option CODEBASE = 
        ARGUMENT_BUILDER
          .withName( "codebase" )
          .withDescription( "URI referencing a part datastructure." )
          .withValidator( new URIValidator() )
          .create();

    private static final Group CODEBASE_GROUP =
      GROUP_BUILDER
        .withOption( CODEBASE )
        .withOption( CONTEXT_OPTION )
        .create();
        
    private static final Group COMMAND_GROUP =
      GROUP_BUILDER
        .withMinimum( 0 )
        .withMaximum( 1 )
        .withOption( CODEBASE )
        .withOption( CONTEXT_OPTION )
        .withOption( COMMAND_EXECUTION_OPTION )
        .create();
}

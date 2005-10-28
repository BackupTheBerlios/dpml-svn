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

package net.dpml.tools.control;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.dpml.transit.Logger;
import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.util.ExceptionHelper;
import net.dpml.transit.util.CLIHelper;

import net.dpml.tools.info.Scope;
import net.dpml.tools.model.Library;
import net.dpml.tools.model.Module;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Builder;
import net.dpml.tools.model.ResourceNotFoundException;
import net.dpml.tools.model.ModuleNotFoundException;
import net.dpml.tools.control.DefaultLibrary;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;

/**
 * Plugin that handles multi-project builds based on supplied commandline arguments.
 *
 * @author <a href="mailto:dev@dpmlnet">Stephen J. McConnell</a>
 */
public class BuildPlugin 
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final Logger m_logger;
    private final DefaultLibrary m_library;
    private final boolean m_verbose;
    
    // ------------------------------------------------------------------------
    // constructors
    // ------------------------------------------------------------------------

   /**
    * AntPlugin establishment.
    *
    * @param args supplimentary command line arguments
    * @exception Exception if the build fails
    */
    public BuildPlugin( Logger logger, String[] args )
        throws Exception
    {
        m_logger = logger;
        
        Options options = buildActionCommandLineOptions();
        //HelpFormatter formatter = new HelpFormatter();
        //formatter.printHelp( "build ", options, true );
        //CommandLineParser parser = new GnuParser();
        CommandLineParser parser = new BasicParser();
        CommandLine line = parser.parse( options, args, false );
        m_verbose = ( line.hasOption( "verbose" ) || line.hasOption( "v" ) );
        if( line.hasOption( "version" ) )
        {
            String version = line.getOptionValue( "version" );
            System.setProperty( "build.signature", version );
        }
        getLogger().debug( "building library" );
        m_library = new DefaultLibrary( logger );
        String selection = getSelectionArgument( line );
        if( null != selection )
        {
            getLogger().debug( "parsing selection: " + selection );
            Resource[] resources = m_library.select( selection, true );
            process( resources, line );
        }
        else
        {
            String work = System.getProperty( "user.dir" );
            getLogger().debug( "resolving selection in: " + work );
            File file = new File( work ).getCanonicalFile();
            Resource resource = m_library.locate( file );
            process( resource, line );
        }
    }
    
    private String getSelectionArgument( CommandLine line )
    {
        if( line.hasOption( "select" ) )
        {
            return line.getOptionValue( "select" );
        }
        else if( line.hasOption( "s" ) )
        {
            return line.getOptionValue( "s" );
        }
        else
        {
            return null;
        }
    }
    
    private void process( Resource[] resources, CommandLine line ) throws Exception
    {
        if( resources.length == 0 )
        {
            return;
        }
        else if( resources.length == 1 )
        {
            Resource resource = resources[0];
            process( resource, line );
        }
        else
        {
            listResources( resources, line );
        }
    }

    private void process( Resource resource, CommandLine line ) throws Exception
    {
        getLogger().debug( "processing: " + resource );
        if( getListArgument( line ) )
        {
            String[] remainder = line.getArgs();
            if( remainder.length > 0 )
            {
                StringBuffer buffer = 
                  new StringBuffer( 
                    "Ignoring (" 
                    + remainder.length 
                    + ") unrecognized arguments: " );
                for( int i=0; i<remainder.length; i++ )
                {
                    buffer.append( "[" + remainder[i] + "]" );
                    if( i < (remainder.length-1) )
                    {
                        buffer.append( ", " );
                    }
                }
                getLogger().warn( buffer.toString() );
            }
            if( resource instanceof Module )
            {
                listModule( (Module) resource, line );
            }
            else
            {
                listResource( resource, line );
            }
        }
        else
        {
            // TODO: check if LOCAL
            buildProject( resource, line );
        }
    }
    
    private boolean getListArgument( CommandLine line )
    {
        return ( line.hasOption( "list" ) || line.hasOption( "l" ) );
    }
    
    private boolean getConsumersArgument( CommandLine line )
    {
        return ( line.hasOption( "consumers" ) || line.hasOption( "c" ) );
    }
        
    private boolean buildProject( Resource project, CommandLine line ) throws Exception
    {
        String[] targets = line.getArgs();
        Builder builder = createBuilder();
        boolean flag = getConsumersArgument( line );
        if( flag )
        {
            Resource[] consumers = project.getConsumers( true, true );
            StringBuffer buffer = new StringBuffer();
            buffer.append( 
              "building consumers of project [" 
              + project.getResourcePath() 
              + "]\n" );
            for( int i=0; i<consumers.length; i++ )
            {
                int n = i+1;
                Resource consumer = consumers[i];
                buffer.append( "\n" + n + "\t" + consumer.getResourcePath() );
            }
            getLogger().info( buffer.toString() );
            for( int i=0; i<consumers.length; i++ )
            {
                Resource consumer = consumers[i];
                boolean status = builder.build( consumer, targets );
                if( !status )
                {
                    return false;
                }
            }
            return true;
        }
        else
        {
            return builder.build( project, targets );
        }
    }
    
    private void listModule( Module module, CommandLine line ) throws Exception
    {
        StringBuffer buffer = new StringBuffer( "Listing module [" + module.getResourcePath() + "]\n" );
        listModule( buffer, "  ", module, 0 );
        getLogger().info( buffer.toString() + "\n" );
    }
    
    private void listResource( Resource project, CommandLine line ) throws Exception
    {
        boolean flag = getConsumersArgument( line );
        StringBuffer buffer = new StringBuffer();
        if( flag )
        {
            buffer.append( "Listing consumers of project: " + project.getResourcePath() + "\n" );
            Resource[] consumers = project.getConsumers( true, true );
            for( int i=0; i<consumers.length; i++ )
            {
                if( i>0 )
                {
                    buffer.append( "\n" );
                }
                Resource consumer = consumers[i];
                if( consumer instanceof Module )
                {
                    listModule( buffer, "  ", (Module) consumer, (i+1) );
                }
                else
                {
                    listResource( buffer, "  ", consumer, (i+1) );
                }
            }
        }
        else
        {
            buffer.append( "Listing project: " + project.getResourcePath() + "\n" );
            listResource( buffer, "  ", project, 0 );
            buffer.append( "\n" );
        }
        getLogger().info( buffer.toString() );
    }
    
    private void listResources( Resource[] resources, CommandLine line ) throws Exception
    {
        StringBuffer buffer = new StringBuffer( "Selection: " + resources.length + "\n" );
        for( int i=0; i<resources.length; i++ )
        {
            if( i>0 )
            {
                buffer.append( "\n" );
            }
            Resource resource = resources[i];
            if( resource instanceof Module )
            {
                listModule( buffer, "  ", (Module) resource, (i+1) );
            }
            else
            {
                listResource( buffer, "  ", resource, (i+1) );
            }
        }
        getLogger().info( buffer.toString() );
    }
    
    private void listModule( StringBuffer buffer, String pad, Module module, int n ) throws Exception
    {
        listResource( buffer, pad, module, n );
        
        String p = pad + "  ";
        Resource[] providers = module.getAggregatedProviders( Scope.TEST, false, true );
        Resource[] resources = module.getResources();
        List list = Arrays.asList( resources );
        ArrayList stack = new ArrayList();
        for( int i=0; i<providers.length; i++ )
        {
            Resource provider = providers[i];
            if( !list.contains( provider ) )
            {
                stack.add( provider );
            }
        }
        providers = (Resource[]) stack.toArray( new Resource[0] );
        if( providers.length > 0 )
        {
            line( buffer, pad + "imports: (" + providers.length + ")" );
            for( int i=0; i<providers.length; i++ )
            {
                line( buffer, p + providers[i].getResourcePath() );
            }
        }
        if( resources.length > 0 )
        {
            line( buffer, pad + "resources: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                line( buffer, p + resources[i].getName() );
            }
        }
    }
    
    private void listResource( StringBuffer buffer, String pad, Resource resource, int n ) throws Exception
    {
        if( n > 0 )
        {
            buffer.append( "\n[" + n + "] " );
        }
        else
        {
            buffer.append( "\n " );
        }
        buffer.append( resource + "\n" );
        line( buffer, pad + "version: " + resource.getVersion() );
        line( buffer, pad + "basedir: " + resource.getBaseDir() );
        String p = pad + "  ";
        Resource[] resources = resource.getProviders( Scope.BUILD, true, true );
        if( resources.length > 0 )
        {
            line( buffer, pad + "build phase providers: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                line( buffer, p + resources[i].getName() );
            }
        }
        resources = resource.getProviders( Scope.RUNTIME, true, true );
        if( resources.length > 0 )
        {
            line( buffer, pad + "runtime providers: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                line( buffer, p + resources[i].getName() );
            }
        }
        resources = resource.getProviders( Scope.TEST, true, true );
        if( resources.length > 0 )
        {
            line( buffer, pad + "test providers: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                line( buffer, p + resources[i].getName() );
            }
        }
        resources = resource.getConsumers( true, true );
        if( resources.length > 0 )
        {
            line( buffer, pad + "consumers: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                line( buffer, p + resources[i].getName() );
            }
        }
    }
    
    private void line( StringBuffer buffer, String message )
    {
        buffer.append( "\n" + message );
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }
    
    private Options buildActionCommandLineOptions()
    {
        Options options = new Options();
        
        //
        // project or module selection
        //
        
        OptionGroup selection = new OptionGroup();
        Option s = new Option( "s", true, "-select" );
        Option select = new Option( "select", true, "select the named module or project" );
        select.setArgName( "group[/name]" );
        s.setArgName( "group[/name]" );
        selection.addOption( s );
        selection.addOption( select );
        options.addOptionGroup( selection );
        
        OptionGroup verbose = new OptionGroup();
        verbose.addOption( new Option( "v", false, "-verbose" ) );
        verbose.addOption( new Option( "verbose", false, "enable verbose mode" ) );
        options.addOptionGroup( verbose );
        
        //
        // list switch
        //

        OptionGroup listing = new OptionGroup();
        Option l = new Option( "l", false, "-list" );
        Option list = new Option( "list", false, "list the selection" );
        listing.addOption( l );
        listing.addOption( list );
        options.addOptionGroup( listing );
        
        //
        // consumer switch
        //

        OptionGroup consumption = new OptionGroup();
        Option c = new Option( "c", false, "-consumers" );
        Option consumers = new Option( "consumers", false, "select downstream consumer projects" );
        consumption.addOption( c );
        consumption.addOption( consumers );
        options.addOptionGroup( consumption );
        
        //
        // version modifier
        //

        Option version = new Option( "version", true, "build using an explicit version" );
        version.setArgName( "major.minor.micro" );
        options.addOption( version );
        
        //
        // test
        //

        Option test = new Option( "test", false, "internal testing" );
        options.addOption( test );
        
        return options;
    }
    
    private Builder createBuilder() throws Exception
    {
        URI uri = getBuilderURI();
        Object[] params = new Object[]{ m_logger, m_library, new Boolean( m_verbose ) };
        ClassLoader classloader = Builder.class.getClassLoader();
        Class builderClass = Transit.getInstance().getRepository().getPluginClass( classloader, uri );
        return (Builder) Transit.getInstance().getRepository().instantiate( builderClass, params );
    }
    
    private URI getBuilderURI()
    {
        // TODO update the following to select the builder from a property declared on the project
        return ANT_BUILDER_URI;
    }
    
    private static final URI ANT_BUILDER_URI;
    
    static
    {
        try
        {
            ANT_BUILDER_URI = new URI( "@ANT-BUILDER-URI@" );
        }
        catch( Exception e )
        {
            throw new RuntimeException( "will not happen", e );
        }
    }
}


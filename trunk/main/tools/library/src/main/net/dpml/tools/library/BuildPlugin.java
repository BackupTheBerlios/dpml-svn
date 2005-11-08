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

package net.dpml.tools.library;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.dpml.transit.Logger;
import net.dpml.transit.Transit;

import net.dpml.tools.info.Scope;
import net.dpml.tools.info.ResourceDirective.Classifier;
import net.dpml.tools.model.Module;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Builder;
import net.dpml.tools.model.Type;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.BasicParser;
//import org.apache.commons.cli.HelpFormatter;

/**
 * Plugin that handles multi-project builds based on supplied commandline arguments.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class BuildPlugin 
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final Logger m_logger;
    private final DefaultLibrary m_library;
    private final boolean m_verbose;
    private final boolean m_list;
    private final Builder m_builder;
    
    // ------------------------------------------------------------------------
    // constructors
    // ------------------------------------------------------------------------

   /**
    * AntPlugin establishment.
    *
    * @param logger the assigned logging channel
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
        m_library = new DefaultLibrary( logger );
        m_builder = createStandardBuilder();
        
        if( line.hasOption( "version" ) )
        {
            String version = line.getOptionValue( "version" );
            System.setProperty( "build.signature", version );
        }
        
        m_list = getListArgument( line );
        
        getLogger().debug( "building selection" );
        Resource[] resources = getSelection( line );
        
        if( resources.length == 0 )
        {
            getLogger().info( "Empty selection." );
            return;
        }
        
        if( getConsumersArgument( line ) )
        {
            if( resources.length != 1 )
            {
                final String error = 
                  "Consumer resolution against a multi-element selection is not supported.";
                throw new UnsupportedOperationException( error );
            }
            else
            {
                Resource resource = resources[0];
                Resource[] consumers = resource.getConsumers( true, true );
                process( consumers, line );
            }
        }
        else
        {
            process( resources, line );
        }
    }
    
    private Resource[] getSelection( CommandLine line ) throws Exception
    {
        String selection = getSelectionArgument( line );
        if( null != selection )
        {
            getLogger().debug( "parsing selection: " + selection );
            Resource[] resources = m_library.select( selection, false, true );
            getLogger().debug( "selection: " + resources.length );
            return resources;
        }
        else
        {
            String work = System.getProperty( "user.dir" );
            getLogger().debug( "resolving selection in: " + work );
            File file = new File( work ).getCanonicalFile();
            Resource[] resources = m_library.select( file );
            return resources;
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
    
    private boolean getListArgument( CommandLine line )
    {
        return ( line.hasOption( "list" ) || line.hasOption( "l" ) );
    }
    
    private boolean getConsumersArgument( CommandLine line )
    {
        return ( line.hasOption( "consumers" ) || line.hasOption( "c" ) );
    }
    
    private void process( Resource[] resources, CommandLine line ) throws Exception
    {
        if( m_list )
        {
            if( resources.length == 1 )
            {
                Resource resource = resources[0];
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
                listResources( resources, line );
            }
        }
        else
        {
            StringBuffer buffer = new StringBuffer( "Initiating build sequence: (" + resources.length + ")\n" );
            for( int i=0; i<resources.length; i++ )
            {
                Resource resource = resources[i];
                buffer.append( "\n  (" + ( i+1 ) + ")\t" + resource.getResourcePath() );
            }
            buffer.append( "\n" );
            getLogger().info( buffer.toString() );
            for( int i=0; i<resources.length; i++ )
            {
                Resource resource = resources[i];
                boolean status = buildProject( resource, line );
                if( !status )
                {
                    return;
                }
                System.gc();
            }
        }
    }

    private boolean buildProject( Resource project, CommandLine line ) throws Exception
    {
        if( Classifier.LOCAL.equals( project.getClassifier() ) )
        {
            String[] targets = line.getArgs();
            Builder builder = createBuilder( project );
            return builder.build( project, targets );
        }
        else
        {
            return true;
        }
    }
    
    private void listModule( Module module, CommandLine line ) throws Exception
    {
        print( "Listing module [" + module.getResourcePath() + "]\n"  );
        listModule( "  ", module, 0 );
        print( "" );
    }
    
    private void listResource( Resource project, CommandLine line ) throws Exception
    {
        print( "Listing project: " + project.getResourcePath() + "\n"  );
        listResource( "  ", project, 0 );
        print( "" );
    }
    
    private void listResources( Resource[] resources, CommandLine line ) throws Exception
    {
        print( "Selection: [" + resources.length + "]\n"  );
        for( int i=0; i<resources.length; i++ )
        {
            Resource resource = resources[i];
            String label = getLabel( i+1 );
            print( label + resource );
        }
        print( "" );
    }
    
    private void listModule( String pad, Module module, int n ) throws Exception
    {
        if( n > 0 )
        {
            String label = getLabel( n );
            print( "\n" + label + module );
        }
        else
        {
            print( "\n" + module );
        }
        print( "" );
        print( pad + "version: " + module.getVersion() );
        print( pad + "basedir: " + module.getBaseDir() );
        
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
            print( pad + "imports: (" + providers.length + ")"  );
            for( int i=0; i<providers.length; i++ )
            {
                print( p + providers[i] );
            }
        }
        if( resources.length > 0 )
        {
            print( pad + "resources: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                print( p + resources[i] );
            }
        }
    }
    
    private void listResource( String pad, Resource resource, int n ) throws Exception
    {
        if( n > 0 )
        {
            print( "\n[" + n + "] " + resource );
        }
        else
        {
            print( "\n" + resource );
        }
        print( "" );
        print( pad + "version: " + resource.getVersion() );
        print( pad + "basedir: " + resource.getBaseDir() );
        String p = pad + "  ";
        Type[] types = resource.getTypes();
        if( types.length > 0 )
        {
            print( pad + "types: (" + types.length + ")" );
            for( int i=0; i<types.length; i++ )
            {
                print( p + types[i].getName() );
            }
        }
        Resource[] resources = resource.getProviders( Scope.BUILD, true, true );
        if( resources.length > 0 )
        {
            print( pad + "build phase providers: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                print( p + resources[i] );
            }
        }
        resources = resource.getProviders( Scope.RUNTIME, true, true );
        if( resources.length > 0 )
        {
            print( pad + "runtime providers: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                print( p + resources[i] );
            }
        }
        resources = resource.getProviders( Scope.TEST, true, true );
        if( resources.length > 0 )
        {
            print( pad + "test providers: (" + resources.length + ")" );
            for( int i=0; i<resources.length; i++ )
            {
                print( p + resources[i] );
            }
        }
        //resources = resource.getConsumers( true, true );
        //if( resources.length > 0 )
        //{
        //    print( pad + "consumers: (" + resources.length + ")" );
        //    for( int i=0; i<resources.length; i++ )
        //    {
        //        print( p + resources[i] );
        //    }
        //}
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
    
    private Builder createStandardBuilder() throws Exception
    {
        return createBuilder( ANT_BUILDER_URI );
    }
    
    private Builder createBuilder( Resource resource ) throws Exception
    {
        return m_builder;
        //URI uri = getBuilderURI( resource );
        //return createBuilder( uri );
    }
    
    private Builder createBuilder( URI uri ) throws Exception
    {
        Object[] params = new Object[]{m_logger, m_library, new Boolean( m_verbose )};
        ClassLoader classloader = Builder.class.getClassLoader();
        Class builderClass = Transit.getInstance().getRepository().getPluginClass( classloader, uri );
        return (Builder) Transit.getInstance().getRepository().instantiate( builderClass, params );
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
    
    static void print( String message )
    {
        System.out.println( message );
    }
    
    private static String getLabel( int n )
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "  [" + n );
        buffer.append( "]" );
        buffer.append( "        " );
        String tag = buffer.toString();
        return tag.substring( 0, 7 ) + " ";
    }

}


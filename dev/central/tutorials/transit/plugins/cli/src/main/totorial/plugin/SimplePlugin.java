/*
 * Copyright 2004 Stephen J. McConnell.
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

package tutorial.plugin;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;


/**
 * A simple plugin enhanced to include cli handling.
 *
 * @author <a href="mailto:dev@dpmlnet">Stephen J. McConnell</a>
 */
public class SimplePlugin
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------

    private static final String URI_SPEC = "@URI-SPEC@";
 
    private static Options buildOptions()
    {
        Options options = new Options();
        OptionBuilder.hasArg();
        OptionBuilder.withArgName( "message-test" );
        OptionBuilder.withDescription( "prints a message" );
        Option option = OptionBuilder.create( "message" );
        options.addOption( option );
        return options;
    }

    // ------------------------------------------------------------------------
    // constructors
    // ------------------------------------------------------------------------

   /**
    * Creation of a new SimplePlugin instance.
    *
    * @param args the commandline arguments
    */
    public SimplePlugin( String[] args ) throws ParseException
    {
        Options options = buildOptions();
        CommandLineParser parser = new GnuParser();
        CommandLine line = parser.parse( options, args );

        if( line.hasOption( "message" ) )
        {
            String message = line.getOptionValue( "message" );
            System.out.println( message );
        }
        else
        {
            handleHelpMessage();
        }
    }

    private void handleHelpMessage()
    {
        System.out.println("");
          HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "transit -load " + URI_SPEC, " ",
          buildOptions(), "", true );
        System.out.println("");
    }
}


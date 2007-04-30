/**
 * Copyright 2003-2004 The Apache Software Foundation
 * Copyright 2005-2007 Stephen McConnell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dpml.cli.option;

import dpml.cli.AbstractCLITestCase;
import dpml.cli.CommandLine;
import dpml.cli.Group;
import dpml.cli.OptionException;
import dpml.cli.builder.ArgumentBuilder;
import dpml.cli.builder.DefaultOptionBuilder;
import dpml.cli.builder.GroupBuilder;
import dpml.cli.commandline.Parser;
import dpml.cli.util.HelpFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Test to exercise nested groups developed to demonstrate bug 32533
 */
public class NestedGroupTest extends AbstractCLITestCase
{
    static final DefaultOptionBuilder OPTION_BUILDER = new DefaultOptionBuilder(  );
    static final ArgumentBuilder ARG_BUILDER = new ArgumentBuilder(  );
    static final GroupBuilder GROUP_BUILDER = new GroupBuilder(  );

    static Group buildActionGroup(  )
    {
        return GROUP_BUILDER.withName( "Action" )
          .withDescription( "Action" )
          .withMinimum( 1 ).withMaximum( 1 )
          .withOption( 
            OPTION_BUILDER.withId( 5 )
              .withShortName( "e" )
              .withLongName( "encrypt" )
              .withDescription( "Encrypt input" )
              .create(  ) )
          .withOption( 
            OPTION_BUILDER.withId( 6 )
              .withShortName( "d" )
              .withLongName( "decrypt" )
              .withDescription( "Decrypt input" )
              .create(  ) )
          .create(  );
    }

    static Group buildAlgorithmGroup(  )
    {
        return GROUP_BUILDER.withName( "Algorithm" )
                       .withDescription( "Encryption Algorithm" ).withMaximum( 1 )
                       .withOption( OPTION_BUILDER.withId( 0 ).withShortName( "b" )
                                            .withLongName( "blowfish" )
                                            .withDescription( "Blowfish" )
                                            .create(  ) )
                       .withOption( OPTION_BUILDER.withId( 1 ).withShortName( "3" )
                                            .withLongName( "3DES" )
                                            .withDescription( "Triple DES" )
                                            .create(  ) ).create(  );
    }

    static Group buildInputGroup(  )
    {
        return GROUP_BUILDER.withName( "Input" ).withDescription( "Input" )
                       .withMinimum( 1 ).withMaximum( 1 )
                       .withOption( OPTION_BUILDER.withId( 2 ).withShortName( "f" )
                                            .withLongName( "file" )
                                            .withDescription( "Input file" )
                                            .withArgument( ARG_BUILDER.withName( 
                    "file" ).withMinimum( 1 ).withMaximum( 1 ).create(  ) )
                                            .create(  ) )
                       .withOption( OPTION_BUILDER.withId( 3 ).withShortName( "s" )
                                            .withLongName( "string" )
                                            .withDescription( "Input string" )
                                            .withArgument( ARG_BUILDER.withName( 
                    "string" ).withMinimum( 1 ).withMaximum( 1 ).create(  ) )
                                            .create(  ) ).create(  );
    }

    static Group buildEncryptionServiceGroup( Group[] nestedGroups )
    {
        GROUP_BUILDER.withName( "encryptionService" )
                .withOption( OPTION_BUILDER.withId( 4 ).withShortName( "h" )
                                     .withLongName( "help" )
                                     .withDescription( "Print this message" )
                                     .create(  ) )
                .withOption( OPTION_BUILDER.withShortName( "k" ).withLongName( "key" )
                                     .withDescription( "Encryption key" )
                                     .create(  ) );

        for( int i = 0; i < nestedGroups.length; i++ )
        {
            GROUP_BUILDER.withOption( nestedGroups[i] );
        }

        return GROUP_BUILDER.create(  );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testNestedGroup(  ) throws OptionException
    {
        final String[] args = {"-eb", "--file", "/tmp/filename.txt"};

        Group[] nestedGroups = 
            {
                buildActionGroup(  ), buildAlgorithmGroup(  ),
                buildInputGroup(  )
            };

        Parser parser = new Parser(  );
        parser.setGroup( buildEncryptionServiceGroup( nestedGroups ) );

        CommandLine commandLine = parser.parse( args );

        assertTrue( "/tmp/filename.txt".equals( commandLine.getValue( "-f" ) ) );
        assertTrue( commandLine.hasOption( "-e" ) );
        assertTrue( commandLine.hasOption( "-b" ) );
        assertFalse( commandLine.hasOption( "-d" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNestedGroupHelp(  )
    {
        Group[] nestedGroups = 
            {
                buildActionGroup(  ), buildAlgorithmGroup(  ),
                buildInputGroup(  )
            };

        HelpFormatter helpFormatter = new HelpFormatter(  );
        helpFormatter.setGroup( buildEncryptionServiceGroup( nestedGroups ) );

        final StringWriter out = new StringWriter(  );
        helpFormatter.setPrintWriter( new PrintWriter( out ) );

        try
        {
            helpFormatter.print(  );

            final BufferedReader bufferedReader = new BufferedReader( new StringReader( 
                        out.toString(  ) ) );
            final String[] expected = new String[]
                {
                    "Usage:                                                                          ",
                    " [-h -k -e|-d -b|-3 -f <file>|-s <string>]                                      ",
                    "encryptionService                                                               ",
                    "  -h (--help)               Print this message                                  ",
                    "  -k (--key)                Encryption key                                      ",
                    "  Action                    Action                                              ",
                    "    -e (--encrypt)          Encrypt input                                       ",
                    "    -d (--decrypt)          Decrypt input                                       ",
                    "  Algorithm                 Encryption Algorithm                                ",
                    "    -b (--blowfish)         Blowfish                                            ",
                    "    -3 (--3DES)             Triple DES                                          ",
                    "  Input                     Input                                               ",
                    "    -f (--file) file        Input file                                          ",
                    "    -s (--string) string    Input string                                        "
                };

            List actual = new ArrayList( expected.length );
            String input;

            while( ( input = bufferedReader.readLine(  ) ) != null )
            {
                actual.add( input );
            }

            // Show they are the same number of lines
            assertEquals( "Help text lines should be " + expected.length,
                actual.size(  ), expected.length );

            for( int i = 0; i < expected.length; i++ )
            {
                if( !expected[i].equals( actual.get( i ) ) )
                {
                    for( int x = 0; x < expected.length; i++ )
                    {
                        System.out.println( "   " + expected[i] );
                        if( expected[i].equals( actual.get( i ) ) )
                        {
                            System.out.println( "== " + actual.get( i ) );
                        }
                        else
                        {
                            System.out.println( "!= " + actual.get( i ) );
                        }
                    }
                }
                assertEquals( expected[i], actual.get( i ) );
            }
        }
        catch( IOException e )
        {
            fail( e.getLocalizedMessage(  ) );
        }
    }
}

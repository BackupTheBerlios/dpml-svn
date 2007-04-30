/**
 * Copyright 2005 The Apache Software Foundation
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
package dpml.cli.bug;

import junit.framework.TestCase;

import dpml.cli.Argument;
import dpml.cli.Group;
import dpml.cli.OptionException;
import dpml.cli.builder.ArgumentBuilder;
import dpml.cli.builder.DefaultOptionBuilder;
import dpml.cli.builder.GroupBuilder;
import dpml.cli.commandline.Parser;
import dpml.cli.option.SourceDestArgument;

/**
 * The first is a loop in Parser.parse() if I set a non-declared option. This
 * code goes into a loop in Parser.java method parse this “while” loop runs
 * endless
 *
 * @author Steve Alberty
 */
public class BugLoopingOptionLookAlikeTest extends TestCase
{
    /**
     * DOCUMENT ME!
     */
    public void testLoopingOptionLookAlike(  )
    {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder(  );
        final ArgumentBuilder abuilder = new ArgumentBuilder(  );
        final GroupBuilder gbuilder = new GroupBuilder(  );
        final Group options = gbuilder.withName( "ant" )
                                      .withOption( obuilder.withShortName( 
                    "help" ).withDescription( "print this message" ).create(  ) )
                                      .withOption( obuilder.withShortName( 
                    "projecthelp" )
                                                           .withDescription( "print project help information" )
                                                           .create(  ) )
                                      .withOption( abuilder.withName( "target" )
                                                           .create(  ) ).create(  );

        final Parser parser = new Parser(  );
        parser.setGroup( options );

        try
        {
            parser.parse( new String[]{"-abcdef", "testfile.txt ", } );
            fail( "OptionException" );
        }
        catch( OptionException e )
        {
            assertEquals( "Unexpected -abcdef while processing ant",
                e.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testLoopingOptionLookAlike2(  )
    {
        final ArgumentBuilder abuilder = new ArgumentBuilder(  );
        final GroupBuilder gbuilder = new GroupBuilder(  );
        final Argument inputfileOpt = abuilder.withName( "input" )
                                               .withMinimum( 1 ).withMaximum( 1 )
                                               .create(  );
        final Argument outputfileOpt = abuilder.withName( "output" )
                                                .withMinimum( 1 ).withMaximum( 1 )
                                                .create(  );
        final Argument targets = new SourceDestArgument( inputfileOpt,
                outputfileOpt );
        final Group options = gbuilder.withOption( targets ).create(  );
        final Parser parser = new Parser(  );
        parser.setGroup( options );

        try
        {
            parser.parse( new String[]
                {
                    "testfile.txt", "testfile.txt", "testfile.txt",
                    "testfile.txt"
                } );
            fail( "OptionException" );
        }
        catch( OptionException e )
        {
            assertEquals( "Unexpected testfile.txt while processing ",
                e.getMessage(  ) );
        }
    }
}

/**
 * Copyright 2004 The Apache Software Foundation
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
package net.dpml.cli.bug;

import junit.framework.TestCase;

import net.dpml.cli.Argument;
import net.dpml.cli.Group;
import net.dpml.cli.Option;
import net.dpml.cli.OptionException;
import net.dpml.cli.builder.ArgumentBuilder;
import net.dpml.cli.builder.CommandBuilder;
import net.dpml.cli.builder.DefaultOptionBuilder;
import net.dpml.cli.builder.GroupBuilder;
import net.dpml.cli.commandline.Parser;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
  */
public class Bug28005Test extends TestCase
{
    /**
     * DOCUMENT ME!
     */
    public void testInfiniteLoop(  )
    {
        final DefaultOptionBuilder optionBuilder = new DefaultOptionBuilder(  );
        final ArgumentBuilder argumentBuilder = new ArgumentBuilder(  );
        final GroupBuilder groupBuilder = new GroupBuilder(  );
        final CommandBuilder commandBuilder = new CommandBuilder(  );

        final Option inputFormatOption = optionBuilder.withLongName( 
                "input-format" ).create(  );

        final Argument argument = argumentBuilder.withName( "file" ).create(  );

        final Group children = groupBuilder.withName( "options" )
                                           .withOption( inputFormatOption )
                                           .create(  );

        final Option command = commandBuilder.withName( "convert" )
                                             .withChildren( children )
                                             .withArgument( argument ).create(  );

        final Group root = groupBuilder.withName( "commands" )
                                       .withOption( command ).create(  );

        final Parser parser = new Parser(  );
        parser.setGroup( root );

        final String[] args = new String[]
            {
                "convert", "test.txt", "--input-format", "a"
            };

        try
        {
            parser.parse( args );
            fail( "a isn't valid!!" );
        }
        catch( OptionException e )
        {
            assertEquals( "Unexpected a while processing commands",
                e.getMessage(  ) );
        }
    }
}

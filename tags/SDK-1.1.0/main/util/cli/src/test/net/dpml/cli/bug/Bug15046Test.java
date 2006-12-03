/**
 * Copyright 2003-2004 The Apache Software Foundation
 * Copyright 2005-2006 Stephen McConnell, The Digital Product Meta Library
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

import net.dpml.cli.CommandLine;
import net.dpml.cli.Group;
import net.dpml.cli.Option;
import net.dpml.cli.builder.ArgumentBuilder;
import net.dpml.cli.builder.DefaultOptionBuilder;
import net.dpml.cli.builder.GroupBuilder;
import net.dpml.cli.commandline.Parser;

/**
 * @author John Keyes
 */
public class Bug15046Test extends TestCase
{
    private static final String[] CLI_ARGS = new String[]{"-z", "c"};

    /**
     * Creates a new Bug15046Test object.
     *
     * @param name DOCUMENT ME!
     */
    public Bug15046Test( String name )
    {
        super( name );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testParamNamedAsOption(  ) throws Exception
    {
        DefaultOptionBuilder obuilder = new DefaultOptionBuilder(  );
        ArgumentBuilder abuilder = new ArgumentBuilder(  );

        Option option = obuilder.withShortName( "z" ).withLongName( "timezone" )
                                .withDescription( "affected option" )
                                .withArgument( abuilder.withName( "timezone" )
                                                       .create(  ) ).create(  );

        GroupBuilder gbuilder = new GroupBuilder(  );
        Group options = gbuilder.withName( "bug15046" ).withOption( option )
                                .create(  );

        Parser parser = new Parser(  );
        parser.setGroup( options );

        CommandLine line = parser.parse( CLI_ARGS );

        assertEquals( "c", line.getValue( "-z" ) );

        Option c = obuilder.withShortName( "c" ).withLongName( "conflict" )
                           .withDescription( "conflicting option" )
                           .withArgument( abuilder.withName( "conflict" )
                                                  .create(  ) ).create(  );

        options = gbuilder.withName( "bug15046" ).withOption( option )
                          .withOption( c ).create(  );

        parser.setGroup( options );
        line = parser.parse( CLI_ARGS );

        assertEquals( "c", line.getValue( "-z" ) );
    }
}

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
public class Bug15648Test extends TestCase
{
    /**
     * Creates a new Bug15648Test object.
     *
     * @param name DOCUMENT ME!
     */
    public Bug15648Test( final String name )
    {
        super( name );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testQuotedArgumentValue(  ) throws Exception
    {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder(  );
        final ArgumentBuilder abuilder = new ArgumentBuilder(  );
        final GroupBuilder gbuilder = new GroupBuilder(  );

        final Option testOption = obuilder.withShortName( "a" )
                                          .withArgument( abuilder.withName( 
                    "quoted string" ).create(  ) ).create(  );

        final Group options = gbuilder.withOption( testOption ).create(  );

        final Parser parser = new Parser(  );
        parser.setGroup( options );

        final CommandLine cmdLine = parser.parse( new String[]
                {
                    "-a", "\"two tokens\""
                } );

        assertTrue( cmdLine.hasOption( "-a" ) );
        assertEquals( "two tokens", cmdLine.getValue( "-a" ) );
    }
}

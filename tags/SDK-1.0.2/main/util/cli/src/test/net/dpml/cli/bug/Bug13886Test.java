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

import net.dpml.cli.Group;
import net.dpml.cli.Option;
import net.dpml.cli.OptionException;
import net.dpml.cli.builder.DefaultOptionBuilder;
import net.dpml.cli.builder.GroupBuilder;
import net.dpml.cli.commandline.Parser;

/**
 * @author John Keyes
 */
public class Bug13886Test extends TestCase
{
    /**
     * Creates a new Bug13886Test object.
     *
     * @param name DOCUMENT ME!
     */
    public Bug13886Test( final String name )
    {
        super( name );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testMandatoryGroup(  ) throws Exception
    {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder(  );
        final GroupBuilder gbuilder = new GroupBuilder(  );

        final Option a = obuilder.withShortName( "a" ).create(  );

        final Option b = obuilder.withShortName( "b" ).create(  );

        final Group options = gbuilder.withOption( a ).withOption( b )
                                      .withMaximum( 1 ).withMinimum( 1 ).create(  );

        final Parser parser = new Parser(  );
        parser.setGroup( options );

        try
        {
            parser.parse( new String[]{} );
            fail( "Expected MissingOptionException not caught" );
        }
        catch( final OptionException exp )
        {
            assertEquals( "Missing option -a|-b", exp.getMessage(  ) );
        }

        try
        {
            parser.parse( new String[]{"-a"} );
        }
        catch( final OptionException exp )
        {
            fail( "Unexpected MissingOptionException caught" );
        }

        try
        {
            parser.parse( new String[]{"-b"} );
        }
        catch( final OptionException exp )
        {
            fail( "Unexpected MissingOptionException caught" );
        }

        try
        {
            parser.parse( new String[]{"-a", "-b"} );
            fail( "Expected UnexpectedOptionException not caught" );
        }
        catch( final OptionException exp )
        {
            assertEquals( "Unexpected -b while processing -a|-b",
                exp.getMessage(  ) );
        }
    }
}

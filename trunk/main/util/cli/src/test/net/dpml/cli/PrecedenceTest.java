/**
 * Copyright 2003-2004 The Apache Software Foundation
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
package net.dpml.cli;

import junit.framework.TestCase;

import net.dpml.cli.builder.ArgumentBuilder;
import net.dpml.cli.builder.DefaultOptionBuilder;
import net.dpml.cli.builder.GroupBuilder;
import net.dpml.cli.commandline.Parser;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author Rob Oxspring
 *
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class PrecedenceTest extends TestCase
{
    private final String[] args = new String[]{"-file"};

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testSimple(  ) throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );

        final Group options = new GroupBuilder(  ).withOption( oBuilder.withShortName( 
                    "file" ).create(  ) ).create(  );

        final CommandLine cl = buildCommandLine( options, args );
        assertEquals( new String[]{"-file"}, cl );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testArgument(  ) throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );
        final ArgumentBuilder aBuilder = new ArgumentBuilder(  );

        final Group options = new GroupBuilder(  ).withOption( oBuilder.withShortName( 
                    "f" ).withArgument( aBuilder.create(  ) ).create(  ) )
                                                  .create(  );

        final CommandLine cl = buildCommandLine( options, args );
        assertEquals( new String[]{"-f"}, cl );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testBurst(  ) throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );
        final GroupBuilder gBuilder = new GroupBuilder(  );
        final Group options = gBuilder.withOption( oBuilder.withShortName( "f" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "i" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "l" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "e" )
                                                           .create(  ) ).create(  );

        final CommandLine cl = buildCommandLine( options, args );
        assertEquals( new String[]{"-f", "-i", "-l", "-e"}, cl );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testChildren(  ) throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );
        final GroupBuilder gBuilder = new GroupBuilder(  );

        final Group children = gBuilder.withOption( oBuilder.withShortName( "i" )
                                                            .create(  ) )
                                       .withOption( oBuilder.withShortName( "l" )
                                                            .create(  ) )
                                       .withOption( oBuilder.withShortName( "e" )
                                                            .create(  ) )
                                       .create(  );
        final Group options = gBuilder.withOption( oBuilder.withShortName( "f" )
                                                           .withChildren( children )
                                                           .create(  ) ).create(  );

        final CommandLine cl = buildCommandLine( options, args );
        assertEquals( new String[]{"-f", "-i", "-l", "-e"}, cl );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void XtestSimpleVsArgument(  ) throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );
        final GroupBuilder gBuilder = new GroupBuilder(  );
        final ArgumentBuilder aBuilder = new ArgumentBuilder(  );

        final Group options = gBuilder.withOption( oBuilder.withShortName( 
                    "file" ).create(  ) )
                                      .withOption( oBuilder.withShortName( "f" )
                                                           .withArgument( aBuilder.create(  ) )
                                                           .create(  ) ).create(  );

        final CommandLine cl = buildCommandLine( options, args );
        assertEquals( new String[]{"-f"}, cl );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void XtestSimpleVsBurst(  ) throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );
        final GroupBuilder gBuilder = new GroupBuilder(  );
        final Group options = gBuilder.withOption( oBuilder.withShortName( 
                    "file" ).create(  ) )
                                      .withOption( oBuilder.withShortName( "f" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "i" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "l" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "e" )
                                                           .create(  ) ).create(  );

        final CommandLine cl = buildCommandLine( options, args );
        assertEquals( new String[]{"-f", "-i", "-l", "-e"}, cl );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void XtestSimpleVsChildren(  ) throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );
        final GroupBuilder gBuilder = new GroupBuilder(  );

        final Group children = gBuilder.withOption( oBuilder.withShortName( "i" )
                                                            .withLongName( "ci" )
                                                            .create(  ) )
                                       .withOption( oBuilder.withShortName( "l" )
                                                            .withLongName( "cl" )
                                                            .create(  ) )
                                       .withOption( oBuilder.withShortName( "e" )
                                                            .withLongName( "ce" )
                                                            .create(  ) )
                                       .create(  );

        final Group options = gBuilder.withOption( oBuilder.withShortName( 
                    "file" ).create(  ) )
                                      .withOption( oBuilder.withShortName( "f" )
                                                           .withChildren( children )
                                                           .create(  ) ).create(  );

        final CommandLine cl = buildCommandLine( options, args );
        assertEquals( new String[]
            {
                "-f", "-i", "--ci", "-l", "--cl", "-e", "--ce"
            }, cl );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testArgumentVsBurst(  ) throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );
        final GroupBuilder gBuilder = new GroupBuilder(  );
        final ArgumentBuilder aBuilder = new ArgumentBuilder(  );

        final Group options = gBuilder.withOption( oBuilder.withShortName( "f" )
                                                           .withArgument( aBuilder.create(  ) )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "i" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "l" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "e" )
                                                           .create(  ) ).create(  );

        final CommandLine cl = buildCommandLine( options, args );
        assertEquals( new String[]{"-f"}, cl );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testArgumentVsChildren(  ) throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );
        final GroupBuilder gBuilder = new GroupBuilder(  );
        final ArgumentBuilder aBuilder = new ArgumentBuilder(  );

        final Group children = gBuilder.withOption( oBuilder.withShortName( "i" )
                                                            .create(  ) )
                                       .withOption( oBuilder.withShortName( "l" )
                                                            .create(  ) )
                                       .withOption( oBuilder.withShortName( "e" )
                                                            .create(  ) )
                                       .create(  );
        final Group options = gBuilder.withOption( oBuilder.withShortName( "f" )
                                                           .withChildren( children )
                                                           .withArgument( aBuilder.create(  ) )
                                                           .create(  ) ).create(  );

        final CommandLine cl = buildCommandLine( options, args );
        assertEquals( new String[]{"-f"}, cl );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testBurstVsChildren(  ) throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );
        final GroupBuilder gBuilder = new GroupBuilder(  );

        final Group children = gBuilder.withOption( oBuilder.withShortName( "i" )
                                                            .withLongName( "ci" )
                                                            .create(  ) )
                                       .withOption( oBuilder.withShortName( "l" )
                                                            .withLongName( "cl" )
                                                            .create(  ) )
                                       .withOption( oBuilder.withShortName( "e" )
                                                            .withLongName( "ce" )
                                                            .create(  ) )
                                       .create(  );

        final Group options = gBuilder.withOption( oBuilder.withShortName( "f" )
                                                           .withChildren( children )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "i" )
                                                           .withLongName( "bi" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "l" )
                                                           .withLongName( "bl" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "e" )
                                                           .withLongName( "be" )
                                                           .create(  ) ).create(  );

        final CommandLine cl = buildCommandLine( options, args );
        assertEquals( new String[]
            {
                "-f", "-i", "--ci", "-l", "--cl", "-e", "--ce"
            }, cl );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void XtestSimpleVsArgumentVsBurst(  ) throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );
        final GroupBuilder gBuilder = new GroupBuilder(  );
        final ArgumentBuilder aBuilder = new ArgumentBuilder(  );

        final Group options = gBuilder.withOption( oBuilder.withShortName( 
                    "file" ).create(  ) )
                                      .withOption( oBuilder.withShortName( "f" )
                                                           .withArgument( aBuilder.create(  ) )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "i" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "l" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "e" )
                                                           .create(  ) ).create(  );

        final CommandLine cl = buildCommandLine( options, args );
        assertEquals( new String[]{"-f"}, cl );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void XtestSimpleVsArgumentVsChildren(  ) throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );
        final GroupBuilder gBuilder = new GroupBuilder(  );
        final ArgumentBuilder aBuilder = new ArgumentBuilder(  );

        final Group children = gBuilder.withOption( oBuilder.withShortName( "i" )
                                                            .withLongName( "ci" )
                                                            .create(  ) )
                                       .withOption( oBuilder.withShortName( "l" )
                                                            .withLongName( "cl" )
                                                            .create(  ) )
                                       .withOption( oBuilder.withShortName( "e" )
                                                            .withLongName( "ce" )
                                                            .create(  ) )
                                       .create(  );

        final Group options = gBuilder.withOption( oBuilder.withShortName( 
                    "file" ).create(  ) )
                                      .withOption( oBuilder.withShortName( "f" )
                                                           .withChildren( children )
                                                           .withArgument( aBuilder.create(  ) )
                                                           .create(  ) ).create(  );

        final CommandLine cl = buildCommandLine( options, args );
        assertEquals( new String[]{"-f"}, cl );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void XtestSimpleVsBurstVsChildren(  ) throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );
        final GroupBuilder gBuilder = new GroupBuilder(  );

        final Group children = gBuilder.withOption( oBuilder.withShortName( "i" )
                                                            .withLongName( "ci" )
                                                            .create(  ) )
                                       .withOption( oBuilder.withShortName( "l" )
                                                            .withLongName( "cl" )
                                                            .create(  ) )
                                       .withOption( oBuilder.withShortName( "e" )
                                                            .withLongName( "ce" )
                                                            .create(  ) )
                                       .create(  );

        final Group options = gBuilder.withOption( oBuilder.withShortName( 
                    "file" ).create(  ) )
                                      .withOption( oBuilder.withShortName( "f" )
                                                           .withChildren( children )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "i" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "l" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "e" )
                                                           .create(  ) ).create(  );

        final CommandLine cl = buildCommandLine( options, args );
        assertEquals( new String[]{"-f", "-i", "-l", "-e"}, cl );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testArgumentVsBurstVsChildren(  ) throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );
        final GroupBuilder gBuilder = new GroupBuilder(  );
        final ArgumentBuilder aBuilder = new ArgumentBuilder(  );

        final Group children = gBuilder.withOption( oBuilder.withShortName( "i" )
                                                            .withLongName( "ci" )
                                                            .create(  ) )
                                       .withOption( oBuilder.withShortName( "l" )
                                                            .withLongName( "cl" )
                                                            .create(  ) )
                                       .withOption( oBuilder.withShortName( "e" )
                                                            .withLongName( "ce" )
                                                            .create(  ) )
                                       .create(  );

        final Group options = gBuilder.withOption( oBuilder.withShortName( "f" )
                                                           .withChildren( children )
                                                           .withArgument( aBuilder.create(  ) )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "i" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "l" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "e" )
                                                           .create(  ) ).create(  );

        final CommandLine cl = buildCommandLine( options, args );
        assertEquals( new String[]{"-f"}, cl );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void XtestSimpleVsArgumentVsBurstVsChildren(  )
        throws OptionException
    {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );
        final GroupBuilder gBuilder = new GroupBuilder(  );
        final ArgumentBuilder aBuilder = new ArgumentBuilder(  );

        final Group children = gBuilder.withOption( oBuilder.withShortName( "i" )
                                                            .withLongName( "ci" )
                                                            .create(  ) )
                                       .withOption( oBuilder.withShortName( "l" )
                                                            .withLongName( "cl" )
                                                            .create(  ) )
                                       .withOption( oBuilder.withShortName( "e" )
                                                            .withLongName( "ce" )
                                                            .create(  ) )
                                       .create(  );

        final Group options = gBuilder.withOption( oBuilder.withShortName( 
                    "file" ).create(  ) )
                                      .withOption( oBuilder.withShortName( "f" )
                                                           .withChildren( children )
                                                           .withArgument( aBuilder.create(  ) )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "i" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "l" )
                                                           .create(  ) )
                                      .withOption( oBuilder.withShortName( "e" )
                                                           .create(  ) ).create(  );

        final CommandLine cl = buildCommandLine( options, args );
        assertEquals( new String[]{"-f"}, cl );
    }

    /**
     * DOCUMENT ME!
     *
     * @param group DOCUMENT ME!
     * @param arguments DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public CommandLine buildCommandLine( final Group group,
        final String[] arguments ) throws OptionException
    {
        Parser p = new Parser(  );
        p.setGroup( group );

        return p.parse( arguments );
    }

    /**
     * DOCUMENT ME!
     *
     * @param options DOCUMENT ME!
     * @param line DOCUMENT ME!
     */
    public void assertEquals( final String[] options, final CommandLine line )
    {
        final List expected = Arrays.asList( options );
        final Set actual = line.getOptionTriggers(  );
        assertTrue( expected.containsAll( actual ) );
        assertTrue( actual.containsAll( expected ) );
    }
}

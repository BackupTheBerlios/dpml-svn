/*
 * Copyright 2004-2005 The Apache Software Foundation
 * Copyright 2005-2006 Stephen McConnell, The Digital Product Management Laboratory
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
 
package dpml.cli.commandline;

import junit.framework.TestCase;

import dpml.cli.CommandLine;
import dpml.cli.Group;
import dpml.cli.OptionException;
import dpml.cli.builder.DefaultOptionBuilder;
import dpml.cli.builder.GroupBuilder;
import dpml.cli.option.DefaultOption;
import dpml.cli.util.HelpFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
  */
public class ParserTest extends TestCase
{
    private Parser m_parser;
    private DefaultOption m_verboseOption;
    private DefaultOption m_helpOption;
    private Group m_options;
    private HelpFormatter m_helpFormatter;
    private StringWriter m_out;
    private BufferedReader m_in;

    /**
     * Test case setup.
     */
    public void setUp(  )
    {
        m_parser = new Parser(  );

        final GroupBuilder gBuilder = new GroupBuilder(  );
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );

        m_helpOption = oBuilder.withLongName( "help" ).withShortName( "h" )
                             .create(  );
        m_verboseOption = oBuilder.withLongName( "verbose" ).withShortName( "v" )
                                .create(  );
        m_options = gBuilder.withOption( m_helpOption ).withOption( m_verboseOption )
                          .create(  );
        m_parser.setGroup( m_options );

        m_helpFormatter = new HelpFormatter(  );
        m_out = new StringWriter(  );
        m_helpFormatter.setPrintWriter( new PrintWriter( m_out ) );
        m_parser.setHelpFormatter( m_helpFormatter );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testParseSuccessful(  ) throws OptionException
    {
        final CommandLine cl = m_parser.parse( new String[]{"-hv"} );

        assertTrue( cl.hasOption( m_helpOption ) );
        assertTrue( cl.hasOption( m_verboseOption ) );

        assertEquals( "--help --verbose", cl.toString(  ) );

        final WriteableCommandLineImpl wcli = (WriteableCommandLineImpl) cl;
        assertEquals( "[--help, --verbose]", wcli.getNormalised(  ).toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testParseWithUnexpectedOption(  )
    {
        try
        {
            m_parser.parse( new String[]{"--unexpected"} );
            fail( "OptionException" );
        }
        catch( OptionException e )
        {
            assertEquals( m_options, e.getOption(  ) );
            //assertEquals("Unexpected --unexpected while processing --help|--verbose",e.getMessage());
            assertEquals( "Unexpected --unexpected while processing --help --verbose",
                e.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testParseAndHelpSuccessful(  ) throws IOException
    {
        final CommandLine cl = m_parser.parseAndHelp( new String[]{"-v"} );

        assertTrue( cl.hasOption( m_verboseOption ) );
        assertEquals( "", m_out.getBuffer(  ).toString(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testParseAndHelpByHelpOption(  ) throws IOException
    {
        m_parser.setHelpOption( m_helpOption );

        assertNull( m_parser.parseAndHelp( new String[]{"-hv"} ) );

        inReader(  );
        assertInReaderUsage(  );
        assertInReaderEOF(  );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testParseAndHelpByHelpTrigger(  ) throws IOException
    {
        m_parser.setHelpTrigger( "--help" );

        assertNull( m_parser.parseAndHelp( new String[]{"-hv"} ) );

        inReader(  );
        assertInReaderUsage(  );
        assertInReaderEOF(  );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testParseAndHelpWithUnexpectedOption(  )
        throws IOException
    {
        assertNull( m_parser.parseAndHelp( new String[]{"--unexpected"} ) );

        inReader(  );
        //assertInReaderLine("Unexpected --unexpected while processing --help|--verbose");
        assertInReaderLine( 
            "Unexpected --unexpected while processing --help --verbose" );
        assertInReaderUsage(  );
        assertInReaderEOF(  );
    }

    private void assertInReaderUsage(  ) throws IOException
    {
        assertInReaderLine( "Usage:" );
        assertInReaderLine( "[--help --verbose]" );
        //assertInReaderLine("--help|--verbose");
        assertInReaderLine( "--help --verbose" );
        assertInReaderLine( "--help (-h)" );
        assertInReaderLine( "--verbose (-v)" );
    }

    private void assertInReaderLine( final String string )
        throws IOException
    {
        assertEquals( string, m_in.readLine(  ).trim(  ) );
    }

    private void assertInReaderEOF(  ) throws IOException
    {
        assertNull( m_in.readLine(  ) );
    }

    private void inReader(  )
    {
        m_in = new BufferedReader( new StringReader( m_out.getBuffer(  ).toString(  ) ) );
    }
}

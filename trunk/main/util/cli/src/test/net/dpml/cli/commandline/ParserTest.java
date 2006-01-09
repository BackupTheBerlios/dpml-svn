package net.dpml.cli.commandline;

import junit.framework.TestCase;

import net.dpml.cli.CommandLine;
import net.dpml.cli.Group;
import net.dpml.cli.OptionException;
import net.dpml.cli.builder.DefaultOptionBuilder;
import net.dpml.cli.builder.GroupBuilder;
import net.dpml.cli.option.DefaultOption;
import net.dpml.cli.util.HelpFormatter;

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
    private Parser parser;
    private DefaultOption verboseOption;
    private DefaultOption helpOption;
    private Group options;
    private HelpFormatter helpFormatter;
    private StringWriter out;
    private BufferedReader in;

    /**
     * DOCUMENT ME!
     */
    public void setUp(  )
    {
        parser = new Parser(  );

        final GroupBuilder gBuilder = new GroupBuilder(  );
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder(  );

        helpOption = oBuilder.withLongName( "help" ).withShortName( "h" )
                             .create(  );
        verboseOption = oBuilder.withLongName( "verbose" ).withShortName( "v" )
                                .create(  );
        options = gBuilder.withOption( helpOption ).withOption( verboseOption )
                          .create(  );
        parser.setGroup( options );

        helpFormatter = new HelpFormatter(  );
        out = new StringWriter(  );
        helpFormatter.setPrintWriter( new PrintWriter( out ) );
        parser.setHelpFormatter( helpFormatter );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testParse_Successful(  ) throws OptionException
    {
        final CommandLine cl = parser.parse( new String[]{"-hv"} );

        assertTrue( cl.hasOption( helpOption ) );
        assertTrue( cl.hasOption( verboseOption ) );

        assertEquals( "--help --verbose", cl.toString(  ) );

        final WriteableCommandLineImpl wcli = (WriteableCommandLineImpl) cl;
        assertEquals( "[--help, --verbose]", wcli.getNormalised(  ).toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testParse_WithUnexpectedOption(  )
    {
        try
        {
            parser.parse( new String[]{"--unexpected"} );
            fail( "OptionException" );
        }
        catch( OptionException e )
        {
            assertEquals( options, e.getOption(  ) );
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
    public void testParseAndHelp_Successful(  ) throws IOException
    {
        final CommandLine cl = parser.parseAndHelp( new String[]{"-v"} );

        assertTrue( cl.hasOption( verboseOption ) );
        assertEquals( "", out.getBuffer(  ).toString(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testParseAndHelp_ByHelpOption(  ) throws IOException
    {
        parser.setHelpOption( helpOption );

        assertNull( parser.parseAndHelp( new String[]{"-hv"} ) );

        inReader(  );
        assertInReaderUsage(  );
        assertInReaderEOF(  );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testParseAndHelp_ByHelpTrigger(  ) throws IOException
    {
        parser.setHelpTrigger( "--help" );

        assertNull( parser.parseAndHelp( new String[]{"-hv"} ) );

        inReader(  );
        assertInReaderUsage(  );
        assertInReaderEOF(  );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testParseAndHelp_WithUnexpectedOption(  )
        throws IOException
    {
        assertNull( parser.parseAndHelp( new String[]{"--unexpected"} ) );

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
        assertEquals( string, in.readLine(  ).trim(  ) );
    }

    private void assertInReaderEOF(  ) throws IOException
    {
        assertNull( in.readLine(  ) );
    }

    private void inReader(  )
    {
        in = new BufferedReader( new StringReader( out.getBuffer(  ).toString(  ) ) );
    }
}

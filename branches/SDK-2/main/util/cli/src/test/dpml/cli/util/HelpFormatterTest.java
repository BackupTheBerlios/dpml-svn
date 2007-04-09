/*
 * Copyright 2003-2005 The Apache Software Foundation
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
package dpml.cli.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import dpml.cli.DisplaySetting;
import dpml.cli.Group;
import dpml.cli.Option;
import dpml.cli.OptionException;
import dpml.cli.builder.DefaultOptionBuilder;
import dpml.cli.builder.GroupBuilder;
import dpml.cli.option.ArgumentTest;
import dpml.cli.option.DefaultOptionTest;
import dpml.cli.resource.ResourceConstants;
import dpml.cli.resource.ResourceHelper;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
  */
public class HelpFormatterTest extends TestCase
{
    private ResourceHelper m_resources = ResourceHelper.getResourceHelper(  );
    private HelpFormatter m_helpFormatter;
    private Option m_verbose;
    private Group m_options;

    /**
     * Test case setup.
     */
    public void setUp()
    {
        m_helpFormatter = new HelpFormatter( "|*", "*-*", "*|", 80 );
        m_helpFormatter.setDivider( 
            "+------------------------------------------------------------------------------+" );
        m_helpFormatter.setHeader( "Jakarta Commons CLI" );
        m_helpFormatter.setFooter( "Copyright 2003\nApache Software Foundation" );
        m_helpFormatter.setShellCommand( "ant" );

        m_verbose = 
          new DefaultOptionBuilder(  )
            .withLongName( "verbose" )
            .withDescription( "print the version information and exit" )
            .create(  );

        m_options = 
          new GroupBuilder(  )
            .withName( "options" )
            .withOption( DefaultOptionTest.buildHelpOption(  ) )
            .withOption( ArgumentTest.buildTargetsArgument(  ) )
            .withOption( 
              new DefaultOptionBuilder(  )
                .withLongName( "diagnostics" )
                .withDescription( "print information that might be helpful to diagnose or report problems." )
                .create(  ) )
            .withOption( new DefaultOptionBuilder(  )
              .withLongName( "projecthelp" )
              .withDescription( "print project help information" )
              .create(  ) )
            .withOption( m_verbose ).create(  );
        m_helpFormatter.setGroup( m_options );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testPrint(  ) throws IOException
    {
        final StringWriter writer = new StringWriter(  );
        final PrintWriter pw = new PrintWriter( writer );
        m_helpFormatter.setPrintWriter( pw );
        m_helpFormatter.print(  );

        // test shell
        assertEquals( "incorrect shell command", "ant",
            m_helpFormatter.getShellCommand(  ) );

        // test group
        assertEquals( "incorrect group", this.m_options,
            m_helpFormatter.getGroup(  ) );

        // test pagewidth
        assertEquals( "incorrect page width", 76, m_helpFormatter.getPageWidth(  ) );

        // test pw
        assertEquals( "incorrect print writer", pw,
            m_helpFormatter.getPrintWriter(  ) );

        // test divider
        assertEquals( "incorrect divider",
            "+------------------------------------------------------------------------------+",
            m_helpFormatter.getDivider(  ) );

        // test header
        assertEquals( "incorrect header", "Jakarta Commons CLI",
            m_helpFormatter.getHeader(  ) );

        // test footer
        assertEquals( "incorrect footer",
            "Copyright 2003\nApache Software Foundation",
            m_helpFormatter.getFooter(  ) );

        // test gutters
        assertEquals( "incorrect left gutter", "|*",
            m_helpFormatter.getGutterLeft(  ) );
        assertEquals( "incorrect right gutter", "*|",
            m_helpFormatter.getGutterRight(  ) );
        assertEquals( "incorrect center gutter", "*-*",
            m_helpFormatter.getGutterCenter(  ) );

        final BufferedReader reader = new BufferedReader( new StringReader( 
                    writer.toString(  ) ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertEquals( "|*Jakarta Commons CLI                                                         *|",
            reader.readLine(  ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertEquals( "|*Usage:                                                                      *|",
            reader.readLine(  ) );
        assertEquals( "|*ant [--help --diagnostics --projecthelp --verbose] [<target1> [<target2>    *|",
            reader.readLine(  ) );
        assertEquals( "|*...]]                                                                       *|",
            reader.readLine(  ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertEquals( "|*options              *-*                                                    *|",
            reader.readLine(  ) );
        assertEquals( "|*  --help (-?,-h)     *-*Displays the help                                   *|",
            reader.readLine(  ) );
        assertEquals( "|*  --diagnostics      *-*print information that might be helpful to diagnose *|",
            reader.readLine(  ) );
        assertEquals( "|*                     *-*or report problems.                                 *|",
            reader.readLine(  ) );
        assertEquals( "|*  --projecthelp      *-*print project help information                      *|",
            reader.readLine(  ) );
        assertEquals( "|*  --verbose          *-*print the version information and exit              *|",
            reader.readLine(  ) );
        assertEquals( "|*  target [target ...]*-*The targets ant should build                        *|",
            reader.readLine(  ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertEquals( "|*Copyright 2003                                                              *|",
            reader.readLine(  ) );
        assertEquals( "|*Apache Software Foundation                                                  *|",
            reader.readLine(  ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertNull( reader.readLine(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testComparator(  ) throws IOException
    {
        final StringWriter writer = new StringWriter(  );
        final PrintWriter pw = new PrintWriter( writer );
        m_helpFormatter.setPrintWriter( pw );

        final Comparator comparator = new OptionComparator(  );
        m_helpFormatter.setComparator( comparator );
        m_helpFormatter.print(  );

        // test comparator
        assertEquals( "invalid comparator", comparator,
            m_helpFormatter.getComparator(  ) );

        final BufferedReader reader = new BufferedReader( new StringReader( 
                    writer.toString(  ) ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertEquals( "|*Jakarta Commons CLI                                                         *|",
            reader.readLine(  ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertEquals( "|*Usage:                                                                      *|",
            reader.readLine(  ) );
        assertEquals( "|*ant [--verbose --projecthelp --help --diagnostics] [<target1> [<target2>    *|",
            reader.readLine(  ) );
        assertEquals( "|*...]]                                                                       *|",
            reader.readLine(  ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertEquals( "|*options              *-*                                                    *|",
            reader.readLine(  ) );
        assertEquals( "|*  --verbose          *-*print the version information and exit              *|",
            reader.readLine(  ) );
        assertEquals( "|*  --projecthelp      *-*print project help information                      *|",
            reader.readLine(  ) );
        assertEquals( "|*  --help (-?,-h)     *-*Displays the help                                   *|",
            reader.readLine(  ) );
        assertEquals( "|*  --diagnostics      *-*print information that might be helpful to diagnose *|",
            reader.readLine(  ) );
        assertEquals( "|*                     *-*or report problems.                                 *|",
            reader.readLine(  ) );
        assertEquals( "|*  target [target ...]*-*The targets ant should build                        *|",
            reader.readLine(  ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertEquals( "|*Copyright 2003                                                              *|",
            reader.readLine(  ) );
        assertEquals( "|*Apache Software Foundation                                                  *|",
            reader.readLine(  ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertNull( reader.readLine(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testPrintHelp(  ) throws IOException
    {
        final StringWriter writer = new StringWriter(  );
        m_helpFormatter.setPrintWriter( new PrintWriter( writer ) );
        m_helpFormatter.printHelp(  );

        final BufferedReader reader = new BufferedReader( new StringReader( 
                    writer.toString(  ) ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertEquals( "|*options              *-*                                                    *|",
            reader.readLine(  ) );
        assertEquals( "|*  --help (-?,-h)     *-*Displays the help                                   *|",
            reader.readLine(  ) );
        assertEquals( "|*  --diagnostics      *-*print information that might be helpful to diagnose *|",
            reader.readLine(  ) );
        assertEquals( "|*                     *-*or report problems.                                 *|",
            reader.readLine(  ) );
        assertEquals( "|*  --projecthelp      *-*print project help information                      *|",
            reader.readLine(  ) );
        assertEquals( "|*  --verbose          *-*print the version information and exit              *|",
            reader.readLine(  ) );
        assertEquals( "|*  target [target ...]*-*The targets ant should build                        *|",
            reader.readLine(  ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertNull( reader.readLine(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testPrintHelpWithException(  ) throws IOException
    {
        final StringWriter writer = new StringWriter(  );
        m_helpFormatter.setPrintWriter( new PrintWriter( writer ) );
        m_helpFormatter.setException( new OptionException( m_verbose ) );
        m_helpFormatter.printHelp(  );

        //System.out.println(writer);
        final BufferedReader reader = new BufferedReader( new StringReader( 
                    writer.toString(  ) ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertEquals( "|*--verbose*-*print the version information and exit                          *|",
            reader.readLine(  ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertNull( reader.readLine(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testPrintHelpTooNarrow(  ) throws IOException
    {
        final StringWriter writer = new StringWriter(  );
        m_helpFormatter = new HelpFormatter( "<", "=", ">", 4 );
        m_helpFormatter.setGroup( m_options );
        m_helpFormatter.setPrintWriter( new PrintWriter( writer ) );
        m_helpFormatter.printHelp(  );

        final BufferedReader reader = new BufferedReader( new StringReader( 
                    writer.toString(  ) ) );
        assertEquals( "<options              = >", reader.readLine(  ) );
        assertEquals( "<  --help (-?,-h)     =D>", reader.readLine(  ) );
        assertEquals( "<                     =i>", reader.readLine(  ) );

        // lots more lines unchecked
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testPrintException(  ) throws IOException
    {
        final StringWriter writer = new StringWriter(  );
        m_helpFormatter.setPrintWriter( new PrintWriter( writer ) );
        m_helpFormatter.setException( new OptionException( m_verbose,
                ResourceConstants.MISSING_OPTION ) );
        m_helpFormatter.printException(  );

        //System.out.println(writer);
        final BufferedReader reader = new BufferedReader( new StringReader( 
                    writer.toString(  ) ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertEquals( "|*Missing option --verbose                                                    *|",
            reader.readLine(  ) );
        assertNull( reader.readLine(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testPrintUsage(  ) throws IOException
    {
        final StringWriter writer = new StringWriter(  );
        m_helpFormatter.setPrintWriter( new PrintWriter( writer ) );
        m_helpFormatter.printUsage(  );

        final BufferedReader reader = new BufferedReader( new StringReader( 
                    writer.toString(  ) ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertEquals( "|*Usage:                                                                      *|",
            reader.readLine(  ) );
        assertEquals( "|*ant [--help --diagnostics --projecthelp --verbose] [<target1> [<target2>    *|",
            reader.readLine(  ) );
        assertEquals( "|*...]]                                                                       *|",
            reader.readLine(  ) );
        assertNull( reader.readLine(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testPrintHeader(  ) throws IOException
    {
        final StringWriter writer = new StringWriter(  );
        m_helpFormatter.setPrintWriter( new PrintWriter( writer ) );
        m_helpFormatter.printHeader(  );

        final BufferedReader reader = new BufferedReader( new StringReader( 
                    writer.toString(  ) ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertEquals( "|*Jakarta Commons CLI                                                         *|",
            reader.readLine(  ) );
        assertNull( reader.readLine(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testPrintFooter(  ) throws IOException
    {
        final StringWriter writer = new StringWriter(  );
        m_helpFormatter.setPrintWriter( new PrintWriter( writer ) );
        m_helpFormatter.printFooter(  );

        final BufferedReader reader = new BufferedReader( new StringReader( 
                    writer.toString(  ) ) );
        assertEquals( "|*Copyright 2003                                                              *|",
            reader.readLine(  ) );
        assertEquals( "|*Apache Software Foundation                                                  *|",
            reader.readLine(  ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertNull( reader.readLine(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testPrintDivider(  ) throws IOException
    {
        final StringWriter writer = new StringWriter(  );
        m_helpFormatter.setPrintWriter( new PrintWriter( writer ) );
        m_helpFormatter.printDivider(  );

        final BufferedReader reader = new BufferedReader( new StringReader( 
                    writer.toString(  ) ) );
        assertEquals( "+------------------------------------------------------------------------------+",
            reader.readLine(  ) );
        assertNull( reader.readLine(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testWrap(  )
    {
        final Iterator i = HelpFormatter.wrap( "Apache Software Foundation", 30 )
                                        .iterator(  );
        assertEquals( "Apache Software Foundation", i.next(  ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testWrapWrapNeeded(  )
    {
        final Iterator i = HelpFormatter.wrap( "Apache Software Foundation", 20 )
                                        .iterator(  );
        assertEquals( "Apache Software", i.next(  ) );
        assertEquals( "Foundation", i.next(  ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testWrapBeforeSpace(  )
    {
        final Iterator i = HelpFormatter.wrap( "Apache Software Foundation", 16 )
                                        .iterator(  );
        assertEquals( "Apache Software", i.next(  ) );
        assertEquals( "Foundation", i.next(  ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testWrapAfterSpace(  )
    {
        final Iterator i = HelpFormatter.wrap( "Apache Software Foundation", 17 )
                                        .iterator(  );
        assertEquals( "Apache Software", i.next(  ) );
        assertEquals( "Foundation", i.next(  ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testWrapInWord(  )
    {
        final Iterator i = HelpFormatter.wrap( "Apache Software Foundation", 8 )
                                        .iterator(  );
        assertEquals( "Apache", i.next(  ) );
        assertEquals( "Software", i.next(  ) );
        assertEquals( "Foundati", i.next(  ) );
        assertEquals( "on", i.next(  ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testWrapNewLine(  )
    {
        final Iterator i = HelpFormatter.wrap( "\nApache Software Foundation\n",
                30 ).iterator(  );
        assertEquals( "", i.next(  ) );
        assertEquals( "Apache Software Foundation", i.next(  ) );
        assertEquals( "", i.next(  ) );
        assertFalse( i.hasNext(  ) );
    }
    
    /**
     * DOCUMENT ME!
     */
    public void testWrapNewLine2(  )
    {
        List wrapped = HelpFormatter.wrap( 
          "A really quite long general description of the option with specific alternatives documented:\n" 
            + "  Indented special case\n" 
            + "  Alternative scenario", 30 );

        final Iterator i = wrapped.iterator(  );

        assertEquals( "A really quite long general", i.next(  ) );
        assertEquals( "description of the option", i.next(  ) );
        assertEquals( "with specific alternatives", i.next(  ) );
        assertEquals( "documented:", i.next(  ) );
        assertEquals( "  Indented special case", i.next(  ) );
        assertEquals( "  Alternative scenario", i.next(  ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testWrapBelow1Length(  )
    {
        try
        {
            HelpFormatter.wrap( "Apache Software Foundation", -1 );
            fail( "IllegalArgumentException" );
        }
        catch( IllegalArgumentException e )
        {
            assertEquals( m_resources.getMessage( 
                    ResourceConstants.HELPFORMATTER_WIDTH_TOO_NARROW,
                    new Object[]{new Integer( -1 )} ), e.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testPad(  ) throws IOException
    {
        final StringWriter writer = new StringWriter(  );
        HelpFormatter.pad( "hello", 10, writer );
        assertEquals( "hello     ", writer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testPadNull(  ) throws IOException
    {
        final StringWriter writer = new StringWriter(  );
        HelpFormatter.pad( null, 10, writer );
        assertEquals( "          ", writer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testPadTooLong(  ) throws IOException
    {
        final StringWriter writer = new StringWriter(  );
        HelpFormatter.pad( "hello world", 10, writer );
        assertEquals( "hello world", writer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testPadTooShort(  ) throws IOException
    {
        final StringWriter writer = new StringWriter(  );
        HelpFormatter.pad( "hello world", -5, writer );
        assertEquals( "hello world", writer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testGutters(  ) throws IOException
    {
        m_helpFormatter = new HelpFormatter( null, null, null, 80 );
        m_helpFormatter.setShellCommand( "ant" );

        final Set lusage = new HashSet(  );
        lusage.add( DisplaySetting.DISPLAY_ALIASES );
        lusage.add( DisplaySetting.DISPLAY_GROUP_NAME );
        m_helpFormatter.setLineUsageSettings( lusage );

        // test line usage
        assertEquals( "incorrect line usage", lusage,
            m_helpFormatter.getLineUsageSettings(  ) );

        final Set fusage = new HashSet(  );
        fusage.add( DisplaySetting.DISPLAY_PARENT_CHILDREN );
        fusage.add( DisplaySetting.DISPLAY_GROUP_ARGUMENT );
        fusage.add( DisplaySetting.DISPLAY_GROUP_OUTER );
        fusage.add( DisplaySetting.DISPLAY_GROUP_EXPANDED );
        fusage.add( DisplaySetting.DISPLAY_ARGUMENT_BRACKETED );
        fusage.add( DisplaySetting.DISPLAY_ARGUMENT_NUMBERED );
        fusage.add( DisplaySetting.DISPLAY_SWITCH_ENABLED );
        fusage.add( DisplaySetting.DISPLAY_SWITCH_DISABLED );
        fusage.add( DisplaySetting.DISPLAY_PROPERTY_OPTION );
        fusage.add( DisplaySetting.DISPLAY_PARENT_CHILDREN );
        fusage.add( DisplaySetting.DISPLAY_PARENT_ARGUMENT );
        fusage.add( DisplaySetting.DISPLAY_OPTIONAL );
        m_helpFormatter.setFullUsageSettings( fusage );

        // test line usage
        assertEquals( "incorrect full usage", fusage,
            m_helpFormatter.getFullUsageSettings(  ) );

        final Set dsettings = new HashSet(  );
        dsettings.add( DisplaySetting.DISPLAY_GROUP_NAME );
        dsettings.add( DisplaySetting.DISPLAY_GROUP_EXPANDED );
        dsettings.add( DisplaySetting.DISPLAY_GROUP_ARGUMENT );

        m_helpFormatter.setDisplaySettings( dsettings );

        m_verbose = 
          new DefaultOptionBuilder(  )
            .withLongName( "verbose" )
            .withDescription( "print the version information and exit" )
            .create(  );

        m_options = 
          new GroupBuilder(  )
            .withName( "options" )
            .withOption( DefaultOptionTest.buildHelpOption(  ) )
            .withOption( ArgumentTest.buildTargetsArgument(  ) )
            .withOption( 
              new DefaultOptionBuilder(  )
                .withLongName( "diagnostics" )
                .withDescription( "print information that might be helpful to diagnose or report problems." )
                .create(  ) )
            .withOption( 
              new DefaultOptionBuilder(  )
                .withLongName( "projecthelp" )
                .withDescription( "print project help information" )
                .create(  ) )
            .withOption( m_verbose )
              .create();

        m_helpFormatter.setGroup( m_options );

        // test default gutters
        assertEquals( "incorrect left gutter",
            HelpFormatter.DEFAULT_GUTTER_LEFT, m_helpFormatter.getGutterLeft(  ) );
        assertEquals( "incorrect right gutter",
            HelpFormatter.DEFAULT_GUTTER_RIGHT, m_helpFormatter.getGutterRight(  ) );
        assertEquals( "incorrect center gutter",
            HelpFormatter.DEFAULT_GUTTER_CENTER,
            m_helpFormatter.getGutterCenter(  ) );

        final StringWriter writer = new StringWriter(  );
        m_helpFormatter.setPrintWriter( new PrintWriter( writer ) );
        m_helpFormatter.print(  );

        final BufferedReader reader = new BufferedReader( new StringReader( 
                    writer.toString(  ) ) );
        assertEquals( "Usage:                                                                          ",
            reader.readLine(  ) );
        assertEquals( "ant [--help --diagnostics --projecthelp --verbose] [<target1> [<target2> ...]]  ",
            reader.readLine(  ) );
        assertEquals( "options                                                                         ",
            reader.readLine(  ) );
        assertEquals( "  --help (-?,-h)         Displays the help                                      ",
            reader.readLine(  ) );
        assertEquals( "  --diagnostics          print information that might be helpful to diagnose or ",
            reader.readLine(  ) );
        assertEquals( "                         report problems.                                       ",
            reader.readLine(  ) );
        assertEquals( "  --projecthelp          print project help information                         ",
            reader.readLine(  ) );
        assertEquals( "  --verbose              print the version information and exit                 ",
            reader.readLine(  ) );
        assertEquals( "  target [target ...]    The targets ant should build                           ",
            reader.readLine(  ) );
        assertNull( reader.readLine(  ) );
    }
}

/**
 * OptionComparator.
 */
class OptionComparator implements Comparator
{
    /**
     * DOCUMENT ME!
     *
     * @param o1 DOCUMENT ME!
     * @param o2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int compare( Object o1, Object o2 )
    {
        Option opt1 = (Option) o1;
        Option opt2 = (Option) o2;

        return -opt1.getPreferredName(  ).compareTo( opt2.getPreferredName(  ) );
    }
}

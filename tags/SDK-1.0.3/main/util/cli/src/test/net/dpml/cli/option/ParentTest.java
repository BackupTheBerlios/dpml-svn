/**
 * Copyright 2003-2005 The Apache Software Foundation
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
package net.dpml.cli.option;

import net.dpml.cli.Argument;
import net.dpml.cli.DisplaySetting;
import net.dpml.cli.Group;
import net.dpml.cli.HelpLine;
import net.dpml.cli.Option;
import net.dpml.cli.OptionException;
import net.dpml.cli.Parent;
import net.dpml.cli.WriteableCommandLine;
import net.dpml.cli.builder.ArgumentBuilder;
import net.dpml.cli.builder.CommandBuilder;
import net.dpml.cli.builder.DefaultOptionBuilder;
import net.dpml.cli.builder.GroupBuilder;
import net.dpml.cli.commandline.WriteableCommandLineImpl;
import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * @author Rob Oxspring
 */
public class ParentTest extends AbstractParentTestCase
{
    /**
     * DOCUMENT ME!
     */
    public static final Argument COMPLEX_ARGUMENT = new ArgumentBuilder(  ).withName( 
            "username" ).withMinimum( 1 ).withMaximum( 1 ).create(  );

    /**
     * DOCUMENT ME!
     */
    public static final Option COMPLEX_CHILD_SSL = new DefaultOptionBuilder(  ).withLongName( 
            "ssl" ).withShortName( "s" ).create(  );

    /**
     * DOCUMENT ME!
     */
    public static final Option COMPLEX_CHILD_BASIC = new DefaultOptionBuilder(  ).withLongName( 
            "basic" ).withShortName( "b" ).create(  );

    /**
     * DOCUMENT ME!
     */
    public static final Option COMPLEX_CHILD_DIGEST = new DefaultOptionBuilder(  ).withLongName( 
            "digest" ).withShortName( "d" ).create(  );

    /**
     * DOCUMENT ME!
     */
    public static final Group COMPLEX_CHILDREN = new GroupBuilder(  ).withName( 
            "login-opts" ).withOption( COMPLEX_CHILD_BASIC )
                                                                     .withOption( COMPLEX_CHILD_DIGEST )
                                                                     .withOption( COMPLEX_CHILD_SSL )
                                                                     .create(  );

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Parent buildLibParent(  )
    {
        final Argument argument = ArgumentTest.buildPathArgument(  );

        return new DefaultOption( "-", "--", false, "--lib",
            "Specifies indexsearch path", null, null, false, argument, null,
            'l' );
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Parent buildKParent(  )
    {
        final Group children = GroupTest.buildApacheCommandGroup(  );

        return new DefaultOption( "-", "--", false, "-k", "desc", null, null,
            false, null, children, 'k' );
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Parent buildComplexParent(  )
    {
        return new CommandBuilder(  ).withName( "login" ).withName( "lo" )
                                     .withName( "l" )
                                     .withArgument( COMPLEX_ARGUMENT )
                                     .withChildren( COMPLEX_CHILDREN ).create(  );
    }

    /* (non-Javadoc)
     * @see net.dpml.cli.ParentTestCase#testProcessParent()
     */
    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessParent(  ) throws OptionException
    {
        final Parent option = buildKParent(  );
        final List args = list( "-k", "start" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.processParent( commandLine, iterator );

        assertEquals( "start", iterator.next(  ) );
        assertFalse( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "-k" ) );
        assertTrue( commandLine.getValues( option ).isEmpty(  ) );
    }

    /* (non-Javadoc)
     * @see net.dpml.cli.OptionTestCase#testCanProcess()
     */
    /**
     * DOCUMENT ME!
     */
    public void testCanProcess(  )
    {
        final Parent option = buildKParent(  );
        assertTrue( option.canProcess( 
                new WriteableCommandLineImpl( option, null ), "-k" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testCanProcessBadMatch(  )
    {
        final Parent option = buildKParent(  );
        assertFalse( option.canProcess( 
                new WriteableCommandLineImpl( option, null ), "-K" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testCanProcessContractedArgument(  )
    {
        final Parent option = buildLibParent(  );
        assertTrue( option.canProcess( 
                new WriteableCommandLineImpl( option, null ), "--lib=/usr/lib" ) );
    }

    /* (non-Javadoc)
     * @see net.dpml.cli.OptionTestCase#testPrefixes()
     */
    /**
     * DOCUMENT ME!
     */
    public void testPrefixes(  )
    {
        final Parent option = buildKParent(  );
        assertContentsEqual( list( "-", "--" ), option.getPrefixes(  ) );
    }

    /* (non-Javadoc)
     * @see net.dpml.cli.OptionTestCase#testProcess()
     */
    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcess(  ) throws OptionException
    {
        final Parent option = CommandTest.buildStartCommand(  );
        final List args = list( "start" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.process( commandLine, iterator );

        assertFalse( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "start" ) );
        assertFalse( commandLine.hasOption( "stop" ) );
        assertTrue( commandLine.getValues( option ).isEmpty(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessNoMatch(  ) throws OptionException
    {
        final Parent option = CommandTest.buildStartCommand(  );
        final List args = list( "whatever" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );

        try
        {
            option.process( commandLine, iterator );
            fail( "unexpected token not thrown" );
        }
        catch( OptionException exp )
        {
            OptionException e = new OptionException( option,
                    ResourceConstants.UNEXPECTED_TOKEN, "whatever" );
            assertEquals( "wrong exception message", e.getMessage(  ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessChildren(  ) throws OptionException
    {
        final Parent option = buildKParent(  );
        final List args = list( "-k", "start" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.process( commandLine, iterator );

        assertNull( option.findOption( "whatever" ) );
        assertNotNull( option.findOption( "start" ) );

        assertFalse( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "-k" ) );
        assertTrue( commandLine.hasOption( "start" ) );
        assertFalse( commandLine.hasOption( "stop" ) );
        assertTrue( commandLine.getValues( option ).isEmpty(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessArgument(  ) throws OptionException
    {
        final Parent option = buildLibParent(  );
        final List args = list( "--lib=C:\\WINDOWS;C:\\WINNT;C:\\" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.process( commandLine, iterator );

        assertFalse( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "--lib" ) );
        assertContentsEqual( list( "C:\\WINDOWS", "C:\\WINNT", "C:\\" ),
            commandLine.getValues( option ) );
    }

    /* (non-Javadoc)
     * @see net.dpml.cli.OptionTestCase#testTriggers()
     */
    /**
     * DOCUMENT ME!
     */
    public void testTriggers(  )
    {
        final Parent option = buildKParent(  );
        assertContentsEqual( list( "-k" ), option.getTriggers(  ) );
    }

    /* (non-Javadoc)
     * @see net.dpml.cli.OptionTestCase#testValidate()
     */
    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testValidate(  ) throws OptionException
    {
        final Parent option = CommandTest.buildStartCommand(  );
        final WriteableCommandLine commandLine = commandLine( option, list(  ) );

        option.validate( commandLine );

        commandLine.addOption( option );

        option.validate( commandLine );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testValidateChildren(  ) throws OptionException
    {
        final Parent option = buildKParent(  );
        final WriteableCommandLine commandLine = commandLine( option, list(  ) );

        option.validate( commandLine );
        commandLine.addOption( option );

        try
        {
            option.validate( commandLine );
            fail( "Missing a command" );
        }
        catch( OptionException moe )
        {
            assertNotNull( moe.getOption(  ) );
            assertNotSame( option, moe.getOption(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testValidateArgument(  ) throws OptionException
    {
        final Command option = CommandTest.buildLoginCommand(  );
        final WriteableCommandLine commandLine = commandLine( option, list(  ) );

        option.validate( commandLine );

        commandLine.addOption( option );

        try
        {
            option.validate( commandLine );
            fail( "Missing a value" );
        }
        catch( OptionException moe )
        {
            assertSame( option, moe.getOption(  ) );
        }
    }

    /* (non-Javadoc)
     * @see net.dpml.cli.OptionTestCase#testAppendUsage()
     */
    /**
     * DOCUMENT ME!
     */
    public void testAppendUsage(  )
    {
        final Option option = buildComplexParent(  );
        final StringBuffer buffer = new StringBuffer(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_GROUP_OUTER );
        option.appendUsage( buffer, settings, null );

        //assertEquals("[login (l,lo) <username> [login-opts (--basic (-b)|--digest (-d)|--ssl (-s))]]",
        //             buffer.toString());
        assertEquals( "[login (l,lo) <username> [login-opts (--basic (-b) --digest (-d) --ssl (-s))]]",
            buffer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testAppendUsageNoArguments(  )
    {
        final Option option = buildComplexParent(  );
        final StringBuffer buffer = new StringBuffer(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_PARENT_ARGUMENT );
        settings.remove( DisplaySetting.DISPLAY_GROUP_OUTER );
        option.appendUsage( buffer, settings, null );

        //assertEquals("[login (l,lo) [login-opts (--basic (-b)|--digest (-d)|--ssl (-s))]]",
        //             buffer.toString());
        assertEquals( "[login (l,lo) [login-opts (--basic (-b) --digest (-d) --ssl (-s))]]",
            buffer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testAppendUsageNoChildren(  )
    {
        final Option option = buildComplexParent(  );
        final StringBuffer buffer = new StringBuffer(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_PARENT_CHILDREN );
        option.appendUsage( buffer, settings, null );

        assertEquals( "[login (l,lo) <username>]", buffer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testAppendUsageNoArgumentsOrChildren(  )
    {
        final Option option = buildComplexParent(  );
        final StringBuffer buffer = new StringBuffer(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_PARENT_CHILDREN );
        settings.remove( DisplaySetting.DISPLAY_PARENT_ARGUMENT );
        option.appendUsage( buffer, settings, null );

        assertEquals( "[login (l,lo)]", buffer.toString(  ) );
    }

    /* (non-Javadoc)
     * @see net.dpml.cli.OptionTestCase#testGetPreferredName()
     */
    /**
     * DOCUMENT ME!
     */
    public void testGetPreferredName(  )
    {
        final Option option = buildLibParent(  );
        assertEquals( "--lib", option.getPreferredName(  ) );
    }

    /* (non-Javadoc)
     * @see net.dpml.cli.OptionTestCase#testGetDescription()
     */
    /**
     * DOCUMENT ME!
     */
    public void testGetDescription(  )
    {
        final Option option = buildLibParent(  );
        assertEquals( "Specifies indexsearch path", option.getDescription(  ) );
    }

    /* (non-Javadoc)
     * @see net.dpml.cli.OptionTestCase#testHelpLines()
     */
    /**
     * DOCUMENT ME!
     */
    public void testHelpLines(  )
    {
        final Option option = buildComplexParent(  );
        final List lines = option.helpLines( 0, DisplaySetting.ALL, null );
        final Iterator i = lines.iterator(  );

        final HelpLine line1 = (HelpLine) i.next(  );
        assertEquals( 0, line1.getIndent(  ) );
        assertEquals( option, line1.getOption(  ) );

        final HelpLine line2 = (HelpLine) i.next(  );
        assertEquals( 1, line2.getIndent(  ) );
        assertEquals( COMPLEX_ARGUMENT, line2.getOption(  ) );

        final HelpLine line3 = (HelpLine) i.next(  );
        assertEquals( 1, line3.getIndent(  ) );
        assertEquals( COMPLEX_CHILDREN, line3.getOption(  ) );

        final HelpLine line4 = (HelpLine) i.next(  );
        assertEquals( 2, line4.getIndent(  ) );
        assertEquals( COMPLEX_CHILD_BASIC, line4.getOption(  ) );

        final HelpLine line5 = (HelpLine) i.next(  );
        assertEquals( 2, line5.getIndent(  ) );
        assertEquals( COMPLEX_CHILD_DIGEST, line5.getOption(  ) );

        final HelpLine line6 = (HelpLine) i.next(  );
        assertEquals( 2, line6.getIndent(  ) );
        assertEquals( COMPLEX_CHILD_SSL, line6.getOption(  ) );

        assertFalse( i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testHelpLinesNoArgument(  )
    {
        final Option option = buildComplexParent(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_PARENT_ARGUMENT );

        final List lines = option.helpLines( 0, settings, null );
        final Iterator i = lines.iterator(  );

        final HelpLine line1 = (HelpLine) i.next(  );
        assertEquals( 0, line1.getIndent(  ) );
        assertEquals( option, line1.getOption(  ) );

        final HelpLine line3 = (HelpLine) i.next(  );
        assertEquals( 1, line3.getIndent(  ) );
        assertEquals( COMPLEX_CHILDREN, line3.getOption(  ) );

        final HelpLine line4 = (HelpLine) i.next(  );
        assertEquals( 2, line4.getIndent(  ) );
        assertEquals( COMPLEX_CHILD_BASIC, line4.getOption(  ) );

        final HelpLine line5 = (HelpLine) i.next(  );
        assertEquals( 2, line5.getIndent(  ) );
        assertEquals( COMPLEX_CHILD_DIGEST, line5.getOption(  ) );

        final HelpLine line6 = (HelpLine) i.next(  );
        assertEquals( 2, line6.getIndent(  ) );
        assertEquals( COMPLEX_CHILD_SSL, line6.getOption(  ) );

        assertFalse( i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testHelpLinesNoChildren(  )
    {
        final Option option = buildComplexParent(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_PARENT_CHILDREN );

        final List lines = option.helpLines( 0, settings, null );
        final Iterator i = lines.iterator(  );

        final HelpLine line1 = (HelpLine) i.next(  );
        assertEquals( 0, line1.getIndent(  ) );
        assertEquals( option, line1.getOption(  ) );

        final HelpLine line2 = (HelpLine) i.next(  );
        assertEquals( 1, line2.getIndent(  ) );
        assertEquals( COMPLEX_ARGUMENT, line2.getOption(  ) );

        assertFalse( i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNullPreferredName(  )
    {
        try
        {
            new CommandBuilder(  ).create(  );
        }
        catch( IllegalStateException exp )
        {
            assertEquals( ResourceHelper.getResourceHelper(  )
                                        .getMessage( ResourceConstants.OPTION_NO_NAME ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testRequired(  )
    {
        Command cmd = new CommandBuilder(  ).withRequired( true )
                                            .withName( "blah" ).create(  );
        assertTrue( "cmd is not required", cmd.isRequired(  ) );
        assertEquals( "id is incorrect", 0, cmd.getId(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testID(  )
    {
        Command cmd = new CommandBuilder(  ).withId( 'c' ).withName( "blah" )
                                            .create(  );
        assertEquals( "id is incorrect", 'c', cmd.getId(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testGetId(  )
    {
        assertEquals( 'h', DefaultOptionTest.buildHelpOption(  ).getId(  ) );
        assertEquals( 'X', DefaultOptionTest.buildXOption(  ).getId(  ) );
        assertEquals( 0, CommandTest.buildStartCommand(  ).getId(  ) );
    }
}

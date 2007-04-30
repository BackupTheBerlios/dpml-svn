/*
 * Copyright 2003-2005 The Apache Software Foundation
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
package dpml.cli.option;

import dpml.cli.Argument;
import dpml.cli.DisplaySetting;
import dpml.cli.HelpLine;
import dpml.cli.Option;
import dpml.cli.OptionException;
import dpml.cli.WriteableCommandLine;
import dpml.cli.builder.ArgumentBuilder;
import dpml.cli.builder.GroupBuilder;
import dpml.cli.commandline.WriteableCommandLineImpl;
import dpml.cli.resource.ResourceConstants;
import dpml.cli.resource.ResourceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

//import dpml.cli.validation.DateValidator;
//import dpml.cli.validation.DateValidatorTest;

/**
 * @author Rob Oxspring
 */
public class ArgumentTest extends AbstractArgumentTestCase
{
    private static final ResourceHelper RESOURCES = ResourceHelper.getResourceHelper(  );

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Argument buildUsernameArgument(  )
    {
        return new ArgumentImpl( "username", "The user to connect as", 1, 1,
            '\0', '\0', null, ArgumentImpl.DEFAULT_CONSUME_REMAINING, null, 0 );
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Argument buildHostArgument(  )
    {
        return new ArgumentImpl( "host", "The host name", 2, 3, '\0', ',',
            null, null, null, 0 );
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Argument buildPathArgument(  )
    {
        return new ArgumentImpl( "path", "The place to look for files", 1,
            Integer.MAX_VALUE, '=', ';', null,
            ArgumentImpl.DEFAULT_CONSUME_REMAINING, null, 0 );
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Argument buildTargetsArgument(  )
    {
        return new ArgumentImpl( "target", "The targets ant should build", 0,
            Integer.MAX_VALUE, '\0', ',', null, null, null, 0 );
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Argument buildSizeArgument(  )
    {
        List defaults = new ArrayList(  );
        defaults.add( "10" );

        return new ArgumentImpl( "size", "The number of units", 1, 1, '\0',
            '\0', null, ArgumentImpl.DEFAULT_CONSUME_REMAINING, defaults, 0 );
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Argument buildBoundsArgument(  )
    {
        List defaults = new ArrayList(  );
        defaults.add( "5" );
        defaults.add( "10" );

        return new ArgumentImpl( "size", "The number of units", 2, 2, '\0',
            '\0', null, ArgumentImpl.DEFAULT_CONSUME_REMAINING, defaults, 0 );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNew(  )
    {
        //try {
        //    new ArgumentImpl("limit", "the last acceptable date", 10, 5, '=', '\0',
        //                     new DateValidator(DateValidatorTest.YYYY_MM_YY), null, null, 0);
        //} catch (IllegalArgumentException e) {
        //    assertEquals(RESOURCES.getMessage("Argument.minimum.exceeds.maximum"), e.getMessage());
        //}
    }

    /*
     * (non-Javadoc)
     *
     * @see dpml.cli.ArgumentTestCase#testProcessValues()
     */
    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessValues(  ) throws OptionException
    {
        final Argument option = buildUsernameArgument(  );
        final List args = list( "rob" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.processValues( commandLine, iterator, option );

        assertFalse( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "username" ) );
        assertEquals( "rob", commandLine.getValue( option ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessValuesBoundaryQuotes(  ) throws OptionException
    {
        final Argument option = buildUsernameArgument(  );
        final List args = list( "\"rob\"" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.processValues( commandLine, iterator, option );

        assertFalse( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "username" ) );
        assertEquals( "rob", commandLine.getValue( option ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessValuesSpareValues(  ) throws OptionException
    {
        final Argument option = buildUsernameArgument(  );
        final List args = list( "rob", "secret" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.processValues( commandLine, iterator, option );

        assertTrue( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "username" ) );
        assertEquals( "rob", commandLine.getValue( option ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testProcessValuesOptional(  )
    {
        final Argument option = buildTargetsArgument(  );
        final List args = list(  );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );

        try
        {
            option.processValues( commandLine, iterator, option );
        }
        catch( final OptionException mve )
        {
            assertEquals( option, mve.getOption(  ) );
            assertEquals( "Missing value(s) target [target ...]",
                mve.getMessage(  ) );
        }

        assertFalse( iterator.hasNext(  ) );
        assertFalse( commandLine.hasOption( option ) );
        assertFalse( commandLine.hasOption( "username" ) );
        assertTrue( commandLine.getValues( option ).isEmpty(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessValuesMultiple(  ) throws OptionException
    {
        final Argument option = buildTargetsArgument(  );
        final List args = list( "compile", "test", "docs" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.processValues( commandLine, iterator, option );

        assertFalse( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "target" ) );
        assertFalse( commandLine.getValues( option ).isEmpty(  ) );
        assertListContentsEqual( args, commandLine.getValues( option ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessValuesContracted(  ) throws OptionException
    {
        final Argument option = buildTargetsArgument(  );
        final List args = list( "compile,test,javadoc", "checkstyle,jdepend" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.processValues( commandLine, iterator, option );

        assertFalse( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "target" ) );
        assertListContentsEqual( list( "compile", "test", "javadoc",
                "checkstyle", "jdepend" ), commandLine.getValues( option ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testProcessValuesContractedTooFew(  )
    {
        final Argument option = buildHostArgument(  );
        final List args = list( "box1" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );

        try
        {
            option.processValues( commandLine, iterator, option );
            option.validate( commandLine );
            fail( "Expected MissingValueException" );
        }
        catch( OptionException mve )
        {
            assertSame( option, mve.getOption(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testProcessValuesContractedTooMany(  )
    {
        final Argument option = buildHostArgument(  );
        final List args = list( "box1,box2,box3,box4" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );

        try
        {
            option.processValues( commandLine, iterator, option );
            option.validate( commandLine );
            fail( "Expected MissingValueException" );
        }
        catch( OptionException mve )
        {
            assertSame( option, mve.getOption(  ) );
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see dpml.cli.OptionTestCase#testCanProcess()
     */
    /**
     * DOCUMENT ME!
     */
    public void testCanProcess(  )
    {
        final Argument option = buildTargetsArgument(  );
        assertTrue( option.canProcess( 
                new WriteableCommandLineImpl( option, null ), "any value" ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see dpml.cli.OptionTestCase#testPrefixes()
     */
    /**
     * DOCUMENT ME!
     */
    public void testPrefixes(  )
    {
        final Argument option = buildTargetsArgument(  );
        assertTrue( option.getPrefixes(  ).isEmpty(  ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see dpml.cli.OptionTestCase#testProcess()
     */
    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcess(  ) throws OptionException
    {
        final Argument option = buildPathArgument(  );
        final List args = list( "-path=/lib;/usr/lib;/usr/local/lib" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.process( commandLine, iterator );

        assertFalse( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "path" ) );
        assertListContentsEqual( list( "-path=/lib", "/usr/lib",
                "/usr/local/lib" ), commandLine.getValues( option ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see dpml.cli.OptionTestCase#testTriggers()
     */
    /**
     * DOCUMENT ME!
     */
    public void testTriggers(  )
    {
        final Argument option = buildTargetsArgument(  );
        assertTrue( option.getTriggers(  ).isEmpty(  ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see dpml.cli.OptionTestCase#testValidate()
     */
    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testValidate(  ) throws OptionException
    {
        final Argument option = buildUsernameArgument(  );
        final WriteableCommandLine commandLine = commandLine( option, list(  ) );

        commandLine.addValue( option, "rob" );

        option.validate( commandLine );
    }

    /**
     * DOCUMENT ME!
     */
    public void testValidateMinimum(  )
    {
        final Argument option = buildUsernameArgument(  );
        final WriteableCommandLine commandLine = commandLine( option, list(  ) );

        try
        {
            option.validate( commandLine );
            fail( "UnexpectedValue" );
        }
        catch( OptionException mve )
        {
            assertEquals( option, mve.getOption(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testRequired(  )
    {
        if( true )
        {
            final Argument arg = buildBoundsArgument(  );

            assertTrue( "not required", arg.isRequired(  ) );
        }

        if( true )
        {
            final Argument arg = buildTargetsArgument(  );

            assertFalse( "should not be required", arg.isRequired(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testValidateMaximum(  )
    {
        final Argument option = buildUsernameArgument(  );
        final WriteableCommandLine commandLine = commandLine( option, list(  ) );

        commandLine.addValue( option, "rob" );
        commandLine.addValue( option, "oxspring" );

        try
        {
            option.validate( commandLine );
            fail( "UnexpectedValue" );
        }
        catch( OptionException uve )
        {
            assertEquals( option, uve.getOption(  ) );
        }
    }

    /*
     public void testValidate_Validator()
         throws OptionException, ParseException {
         final Argument option = buildDateLimitArgument();
         final WriteableCommandLine commandLine = commandLine(option, list());
    
         commandLine.addValue(option, "2004-01-01");
    
         option.validate(commandLine, option);
         assertContentsEqual(Arrays.asList(new Object[] {
                                               DateValidatorTest.YYYY_MM_YY.parse("2004-01-01")
                                           }), commandLine.getValues(option));
     }
     */
    /**
     * DOCUMENT ME!
     */
    public void testAppendUsage(  )
    {
        final Option option = buildUsernameArgument(  );
        final StringBuffer buffer = new StringBuffer(  );
        option.appendUsage( buffer, DisplaySetting.ALL, null );

        assertEquals( "<username>", buffer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testAppendUsageInfinite(  )
    {
        final Option option = buildTargetsArgument(  );
        final StringBuffer buffer = new StringBuffer(  );
        option.appendUsage( buffer, DisplaySetting.ALL, null );

        assertEquals( "[<target1> [<target2> ...]]", buffer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testAppendUsageInfiniteNoOptional(  )
    {
        final Option option = buildTargetsArgument(  );
        final StringBuffer buffer = new StringBuffer(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_OPTIONAL );
        option.appendUsage( buffer, settings, null );

        assertEquals( "<target1> [<target2> ...]", buffer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testAppendUsageInfiniteNoNumbering(  )
    {
        final Option option = buildTargetsArgument(  );
        final StringBuffer buffer = new StringBuffer(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_ARGUMENT_NUMBERED );
        option.appendUsage( buffer, settings, null );

        assertEquals( "[<target> [<target> ...]]", buffer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testAppendUsageMinimum(  )
    {
        final Option option = buildHostArgument(  );
        final StringBuffer buffer = new StringBuffer(  );
        option.appendUsage( buffer, DisplaySetting.ALL, null );

        assertEquals( "<host1> <host2> [<host3>]", buffer.toString(  ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see dpml.cli.OptionTestCase#testGetPreferredName()
     */
    /**
     * DOCUMENT ME!
     */
    public void testGetPreferredName(  )
    {
        final Option option = buildPathArgument(  );
        assertEquals( "path", option.getPreferredName(  ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see dpml.cli.OptionTestCase#testGetDescription()
     */
    /**
     * DOCUMENT ME!
     */
    public void testGetDescription(  )
    {
        final Option option = buildHostArgument(  );
        assertEquals( "The host name", option.getDescription(  ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see dpml.cli.OptionTestCase#testHelpLines()
     */
    /**
     * DOCUMENT ME!
     */
    public void testHelpLines(  )
    {
        final Option option = buildHostArgument(  );
        final List lines = option.helpLines( 0, DisplaySetting.ALL, null );
        final Iterator i = lines.iterator(  );

        final HelpLine line1 = (HelpLine) i.next(  );
        assertEquals( 0, line1.getIndent(  ) );
        assertEquals( option, line1.getOption(  ) );

        assertFalse( i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testCanProcessConsumeRemaining(  )
    {
        final Option option = buildUsernameArgument(  );

        assertTrue( option.canProcess( 
                new WriteableCommandLineImpl( option, null ), "--" ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessConsumeRemaining(  ) throws OptionException
    {
        final Option option = buildPathArgument(  );
        final List args = list( "options", "--", "--ignored", "-Dprop=val" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );

        option.process( commandLine, iterator );

        final List values = commandLine.getValues( option );
        assertTrue( values.contains( "options" ) );
        assertTrue( values.contains( "--ignored" ) );
        assertTrue( values.contains( "-Dprop=val" ) );
        assertEquals( 3, values.size(  ) );
        assertFalse( iterator.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testProcessConsumeNothing(  )
    {
        final Option option = buildPathArgument(  );
        final List args = list( "--" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );

        try
        {
            option.process( commandLine, iterator );
            option.validate( commandLine );
            fail( "Missing Value!" );
        }
        catch( OptionException mve )
        {
            assertEquals( option, mve.getOption(  ) );
            assertEquals( "Missing value(s) path [path ...]", mve.getMessage(  ) );
        }

        assertTrue( commandLine.getValues( option ).isEmpty(  ) );
        assertFalse( iterator.hasNext(  ) );
    }

    //    public void testProcess_DefinedDefaultValue() throws OptionException {
    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessInterrogatedDefaultValue(  )
        throws OptionException
    {
        final Option size = buildSizeArgument(  );
        final List args = list(  );
        final WriteableCommandLine commandLine = commandLine( size, args );
        final ListIterator iterator = args.listIterator(  );

        size.process( commandLine, iterator );

        assertEquals( new Integer( 20 ),
            commandLine.getValue( size, new Integer( 20 ) ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testTooFewDefaults(  )
    {
        List defaults = new ArrayList(  );
        defaults.add( "5" );

        try
        {
            new ArgumentImpl( "size", "The number of units", 2, 2, '\0', '\0',
                null, ArgumentImpl.DEFAULT_CONSUME_REMAINING, defaults, 0 );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                ResourceHelper.getResourceHelper(  )
                              .getMessage( ResourceConstants.ARGUMENT_TOO_FEW_DEFAULTS ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testTooManyDefaults(  )
    {
        List defaults = new ArrayList(  );
        defaults.add( "5" );
        defaults.add( "10" );
        defaults.add( "15" );

        try
        {
            new ArgumentImpl( "size", "The number of units", 2, 2, '\0', '\0',
                null, ArgumentImpl.DEFAULT_CONSUME_REMAINING, defaults, 0 );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                ResourceHelper.getResourceHelper(  )
                              .getMessage( ResourceConstants.ARGUMENT_TOO_MANY_DEFAULTS ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessInterrogatedDefaultValues(  )
        throws OptionException
    {
        final Option bounds = buildBoundsArgument(  );
        final List args = list(  );
        final WriteableCommandLine commandLine = commandLine( bounds, args );
        final ListIterator iterator = args.listIterator(  );

        bounds.process( commandLine, iterator );

        // test with values
        List values = new ArrayList(  );
        values.add( "50" );
        values.add( "100" );
        assertEquals( values, commandLine.getValues( bounds, values ) );

        // test without values
        assertEquals( Collections.EMPTY_LIST,
            commandLine.getValues( bounds, null ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessStripBoundaryQuotes(  ) throws OptionException
    {
        final Option bounds = buildBoundsArgument(  );
        final List args = list(  );
        final WriteableCommandLine commandLine = commandLine( bounds, args );
        final ListIterator iterator = args.listIterator(  );

        bounds.process( commandLine, iterator );

        List values = new ArrayList(  );
        values.add( "50\"" );
        values.add( "\"100" );
        assertEquals( values, commandLine.getValues( bounds, values ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testSourceDestArgument(  )
    {
        final ArgumentBuilder abuilder = new ArgumentBuilder(  );
        final GroupBuilder gbuilder = new GroupBuilder(  );
        final Argument inputfiles = abuilder.withName( "input" ).withMinimum( 0 )
                                            .withMaximum( 0 ).create(  );
        final Argument badOutputFile = abuilder.withName( "output" )
                                                .withMinimum( 1 ).withMaximum( 2 )
                                                .create(  );

        try
        {
            final Argument targets = new SourceDestArgument( inputfiles,
                    badOutputFile );
        }
        catch( final IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                ResourceHelper.getResourceHelper(  )
                              .getMessage( ResourceConstants.SOURCE_DEST_MUST_ENFORCE_VALUES ),
                exp.getMessage(  ) );
        }

        final Argument outputfile = abuilder.withName( "output" ).withMinimum( 1 )
                                            .withMaximum( 1 ).create(  );

        final Argument targets = new SourceDestArgument( inputfiles, outputfile );
        final StringBuffer buffer = new StringBuffer( "test content" );
        targets.appendUsage( buffer, Collections.EMPTY_SET, null );

        assertTrue( "buffer not added",
            buffer.toString(  ).startsWith( "test content" ) );
        assertFalse( "space added", buffer.charAt( 12 ) == ' ' );
    }
}

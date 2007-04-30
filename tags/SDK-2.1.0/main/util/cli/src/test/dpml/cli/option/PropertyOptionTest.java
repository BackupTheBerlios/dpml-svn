/**
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

import dpml.cli.DisplaySetting;
import dpml.cli.HelpLine;
import dpml.cli.Option;
import dpml.cli.OptionException;
import dpml.cli.WriteableCommandLine;
import dpml.cli.commandline.WriteableCommandLineImpl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * @author Rob Oxspring
 */
public class PropertyOptionTest extends AbstractOptionTestCase
{
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
        final Option option = new PropertyOption(  );
        assertTrue( option.canProcess( 
                new WriteableCommandLineImpl( option, null ), "-Dmyprop=myval" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testCanProcessNull(  )
    {
        final Option option = new PropertyOption(  );
        assertFalse( option.canProcess( 
                new WriteableCommandLineImpl( option, null ), (String) null ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testCanProcessTooShort(  )
    {
        final Option option = new PropertyOption(  );
        assertFalse( option.canProcess( 
                new WriteableCommandLineImpl( option, null ), "-D" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testCanProcessBadMatch(  )
    {
        final Option option = new PropertyOption(  );
        assertFalse( option.canProcess( 
                new WriteableCommandLineImpl( option, null ), "-dump" ) );
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
        final Option option = new PropertyOption(  );
        assertContentsEqual( list( "-D" ), option.getPrefixes(  ) );
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
        final Option option = new PropertyOption(  );
        final List args = list( "-Dmyprop=myvalue" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );

        option.process( commandLine, iterator );
        assertEquals( "myvalue", commandLine.getProperty( "myprop" ) );
        assertFalse( iterator.hasNext(  ) );
        assertEquals( 1, commandLine.getProperties(  ).size(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testProcessUnexpectedOptionException(  )
    {
        final Option option = new PropertyOption(  );
        final List args = list( "--help" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );

        try
        {
            option.process( commandLine, iterator );
            fail( "UnexpectedOption" );
        }
        catch( final OptionException uoe )
        {
            assertEquals( option, uoe.getOption(  ) );
            assertEquals( "Unexpected --help while processing -Dproperty=value",
                uoe.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessBadPropertyException(  ) throws OptionException
    {
        final Option option = new PropertyOption(  );
        final List args = list( "-Dmyprop" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );

        option.process( commandLine, iterator );

        assertEquals( "true", commandLine.getProperty( "myprop" ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessSetToEmpty(  ) throws OptionException
    {
        final Option option = new PropertyOption(  );
        final List args = list( "-Dmyprop=" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );

        option.process( commandLine, iterator );
        assertEquals( "", commandLine.getProperty( "myprop" ) );
        assertFalse( iterator.hasNext(  ) );
        assertEquals( 1, commandLine.getProperties(  ).size(  ) );
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
        final Option option = new PropertyOption(  );

        assertContentsEqual( list( "-D" ), option.getTriggers(  ) );
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
        final Option option = new PropertyOption(  );
        final List args = list( "-Dproperty=value" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );

        option.process( commandLine, iterator );

        option.validate( commandLine );
    }

    /*
     * (non-Javadoc)
     *
     * @see dpml.cli.OptionTestCase#testAppendUsage()
     */
    /**
     * DOCUMENT ME!
     */
    public void testAppendUsage(  )
    {
        final Option option = new PropertyOption(  );
        final StringBuffer buffer = new StringBuffer(  );
        option.appendUsage( buffer, DisplaySetting.ALL, null );

        assertEquals( "-D<property>=<value>", buffer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testAppendUsageHidden(  )
    {
        final Option option = new PropertyOption(  );
        final StringBuffer buffer = new StringBuffer(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_PROPERTY_OPTION );
        option.appendUsage( buffer, settings, null );

        assertEquals( "", buffer.toString(  ) );
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
        final Option option = new PropertyOption(  );
        assertEquals( "-D", option.getPreferredName(  ) );
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
        final Option option = new PropertyOption(  );
        assertEquals( PropertyOption.DEFAULT_DESCRIPTION,
            option.getDescription(  ) );
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
        final Option option = new PropertyOption(  );
        final List lines = option.helpLines( 0, DisplaySetting.ALL, null );
        final Iterator i = lines.iterator(  );

        final HelpLine line1 = (HelpLine) i.next(  );
        assertEquals( 0, line1.getIndent(  ) );
        assertEquals( option, line1.getOption(  ) );

        assertFalse( i.hasNext(  ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see dpml.cli.OptionTestCase#testHelpLines()
     */
    /**
     * DOCUMENT ME!
     */
    public void testHelpLinesNoDisplay(  )
    {
        final Option option = new PropertyOption(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_PROPERTY_OPTION );

        final List lines = option.helpLines( 0, settings, null );
        final Iterator i = lines.iterator(  );

        assertFalse( i.hasNext(  ) );
    }
}

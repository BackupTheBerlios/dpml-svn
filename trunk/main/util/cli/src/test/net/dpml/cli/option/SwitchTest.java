/**
 * Copyright 2003-2005 The Apache Software Foundation
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

import net.dpml.cli.DisplaySetting;
import net.dpml.cli.Option;
import net.dpml.cli.OptionException;
import net.dpml.cli.Parent;
import net.dpml.cli.WriteableCommandLine;
import net.dpml.cli.commandline.WriteableCommandLineImpl;
import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * @author Rob Oxspring
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SwitchTest extends AbstractParentTestCase
{
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Switch buildDisplaySwitch(  )
    {
        final Set aliases = new HashSet(  );
        aliases.add( "d" );
        aliases.add( "disp" );

        return new Switch( "+", "-", "display", aliases,
            "Sets whether to display to screen", true, null, null, 'd', null );
    }

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.ParentTestCase#testProcessParent()
     */
    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessParent(  ) throws OptionException
    {
        final Switch option = buildDisplaySwitch(  );
        final List args = list( "+d" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.processParent( commandLine, iterator );

        assertFalse( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "+d" ) );
        assertTrue( commandLine.hasOption( "-display" ) );
        assertEquals( Boolean.TRUE, commandLine.getSwitch( "-d" ) );
        assertTrue( commandLine.getValues( option ).isEmpty(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessParentDisabled(  ) throws OptionException
    {
        final Switch option = buildDisplaySwitch(  );
        final List args = list( "-disp" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.process( commandLine, iterator );

        assertFalse( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "+d" ) );
        assertTrue( commandLine.hasOption( "-display" ) );
        assertEquals( Boolean.FALSE, commandLine.getSwitch( "-d" ) );
        assertTrue( commandLine.getValues( option ).isEmpty(  ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.OptionTestCase#testCanProcess()
     */
    /**
     * DOCUMENT ME!
     */
    public void testCanProcess(  )
    {
        final Switch option = buildDisplaySwitch(  );
        assertTrue( option.canProcess( 
                new WriteableCommandLineImpl( option, null ), "+d" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testCanProcessBadMatch(  )
    {
        final Switch option = buildDisplaySwitch(  );
        assertFalse( option.canProcess( 
                new WriteableCommandLineImpl( option, null ), "-dont" ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.OptionTestCase#testPrefixes()
     */
    /**
     * DOCUMENT ME!
     */
    public void testPrefixes(  )
    {
        final Switch option = buildDisplaySwitch(  );
        assertContentsEqual( list( "-", "+" ), option.getPrefixes(  ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.OptionTestCase#testProcess()
     */
    /**
     * DOCUMENT ME!
     */
    public void testProcess(  )
    {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.OptionTestCase#testTriggers()
     */
    /**
     * DOCUMENT ME!
     */
    public void testTriggers(  )
    {
        final Switch option = buildDisplaySwitch(  );
        assertContentsEqual( list( "-d", "+d", "-disp", "+disp", "+display",
                "-display" ), option.getTriggers(  ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.OptionTestCase#testValidate()
     */
    /**
     * DOCUMENT ME!
     */
    public void testValidate(  )
    {
        final Parent option = buildDisplaySwitch(  );
        final WriteableCommandLine commandLine = commandLine( option, list(  ) );

        try
        {
            option.validate( commandLine );
            fail( "Missing an option" );
        }
        catch( OptionException moe )
        {
            assertSame( option, moe.getOption(  ) );
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.OptionTestCase#testAppendUsage()
     */
    /**
     * DOCUMENT ME!
     */
    public void testAppendUsage(  )
    {
        final Option option = buildDisplaySwitch(  );
        final StringBuffer buffer = new StringBuffer(  );
        option.appendUsage( buffer, DisplaySetting.ALL, null );

        assertEquals( "+display|-display (+d|-d,+disp|-disp)",
            buffer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testAppendUsageNoAlias(  )
    {
        final Option option = buildDisplaySwitch(  );
        final StringBuffer buffer = new StringBuffer(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_ALIASES );
        option.appendUsage( buffer, settings, null );

        assertEquals( "+display|-display", buffer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testAppendUsageNoDisabled(  )
    {
        final Option option = buildDisplaySwitch(  );
        final StringBuffer buffer = new StringBuffer(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_SWITCH_DISABLED );
        option.appendUsage( buffer, settings, null );

        assertEquals( "+display (+d,+disp)", buffer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testAppendUsageNoEnabled(  )
    {
        final Option option = buildDisplaySwitch(  );
        final StringBuffer buffer = new StringBuffer(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_SWITCH_ENABLED );
        option.appendUsage( buffer, settings, null );

        assertEquals( "-display (-d,-disp)", buffer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testAppendUsageNoDisabledOrEnabled(  )
    {
        final Option option = buildDisplaySwitch(  );
        final StringBuffer buffer = new StringBuffer(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_SWITCH_DISABLED );
        settings.remove( DisplaySetting.DISPLAY_SWITCH_ENABLED );
        option.appendUsage( buffer, settings, null );

        assertEquals( "+display (+d,+disp)", buffer.toString(  ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.OptionTestCase#testGetPreferredName()
     */
    /**
     * DOCUMENT ME!
     */
    public void testGetPreferredName(  )
    {
        final Option option = buildDisplaySwitch(  );
        assertEquals( "+display", option.getPreferredName(  ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.OptionTestCase#testGetDescription()
     */
    /**
     * DOCUMENT ME!
     */
    public void testGetDescription(  )
    {
        final Option option = buildDisplaySwitch(  );
        assertEquals( "Sets whether to display to screen",
            option.getDescription(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNullPreferredName(  )
    {
        try
        {
            new Switch( "+", "-", null, null,
                "Sets whether to display to screen", true, null, null, 'd', null );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                ResourceHelper.getResourceHelper(  )
                              .getMessage( ResourceConstants.SWITCH_PREFERRED_NAME_TOO_SHORT ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testEmptyPreferredName(  )
    {
        try
        {
            new Switch( "+", "-", "", null,
                "Sets whether to display to screen", true, null, null, 'd', null );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                ResourceHelper.getResourceHelper(  )
                              .getMessage( ResourceConstants.SWITCH_PREFERRED_NAME_TOO_SHORT ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testNullAliases(  )
    {
        try
        {
            new Switch( "+", "-", "display", null,
                "Sets whether to display to screen", true, null, null, 'd', null );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                ResourceHelper.getResourceHelper(  )
                              .getMessage( ResourceConstants.SWITCH_PREFERRED_NAME_TOO_SHORT ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testNullEnablePrefix(  )
    {
        try
        {
            new Switch( null, "-", "display", null,
                "Sets whether to display to screen", true, null, null, 'd', null );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                ResourceHelper.getResourceHelper(  )
                              .getMessage( ResourceConstants.SWITCH_NO_ENABLED_PREFIX ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testNullDisablePrefix(  )
    {
        try
        {
            new Switch( "+", null, "display", null,
                "Sets whether to display to screen", true, null, null, 'd', null );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                ResourceHelper.getResourceHelper(  )
                              .getMessage( ResourceConstants.SWITCH_NO_DISABLED_PREFIX ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testEnabledPrefixStartsWithDisabledPrefix(  )
    {
        try
        {
            new Switch( "-", "-", "display", null,
                "Sets whether to display to screen", true, null, null, 'd', null );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                ResourceHelper.getResourceHelper(  )
                              .getMessage( ResourceConstants.SWITCH_ENABLED_STARTS_WITH_DISABLED ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testDisabledPrefixStartsWithEnabledPrefix(  )
    {
        try
        {
            new Switch( "o", "on", "display", null,
                "Sets whether to display to screen", true, null, null, 'd', null );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                ResourceHelper.getResourceHelper(  )
                              .getMessage( ResourceConstants.SWITCH_DISABLED_STARTWS_WITH_ENABLED ),
                exp.getMessage(  ) );
        }
    }

   /**
    * Test help lines.
    * @see net.dpml.cli.OptionTestCase#testHelpLines()
    */
    public void testHelpLines()
    {
        // TODO Auto-generated method stub
    }
}

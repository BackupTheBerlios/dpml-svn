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

import net.dpml.cli.DisplaySetting;
import net.dpml.cli.Option;
import net.dpml.cli.OptionException;
import net.dpml.cli.Parent;
import net.dpml.cli.WriteableCommandLine;
import net.dpml.cli.commandline.WriteableCommandLineImpl;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * @author roberto
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DefaultOptionTest extends AbstractParentTestCase
{
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static DefaultOption buildHelpOption(  )
    {
        final Set aliases = new HashSet( list( "-h", "-?" ) );

        return new DefaultOption( "-", "--", true, "--help",
            "Displays the help", aliases, aliases, false, null, null, 'h' );
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static DefaultOption buildXOption(  )
    {
        return new DefaultOption( "-", "--", true, "-X", "This is needed",
            null, null, true, null, null, 'X' );
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
        final DefaultOption option = buildHelpOption(  );
        final List args = list( "--help" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.processParent( commandLine, iterator );

        assertFalse( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "--help" ) );
        assertTrue( commandLine.hasOption( "-?" ) );
        assertTrue( commandLine.getValues( option ).isEmpty(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessParentBurst(  ) throws OptionException
    {
        final DefaultOption option = buildHelpOption(  );
        final List args = list( "-help" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.processParent( commandLine, iterator );

        assertEquals( "-elp", iterator.next(  ) );
        assertFalse( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "--help" ) );
        assertTrue( commandLine.hasOption( "-?" ) );
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
        final DefaultOption option = buildHelpOption(  );
        assertTrue( option.canProcess( 
                new WriteableCommandLineImpl( option, null ), "-?" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testCanProcessBadMatch(  )
    {
        final DefaultOption option = buildHelpOption(  );
        assertFalse( option.canProcess( 
                new WriteableCommandLineImpl( option, null ), "-H" ) );
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
        final DefaultOption option = buildHelpOption(  );
        assertContentsEqual( list( "-", "--" ), option.getPrefixes(  ) );
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
        final DefaultOption option = buildHelpOption(  );
        assertContentsEqual( list( "-?", "-h", "--help" ),
            option.getTriggers(  ) );
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
        final Parent option = buildXOption(  );
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
        final Option option = buildHelpOption(  );
        final StringBuffer buffer = new StringBuffer(  );
        option.appendUsage( buffer, DisplaySetting.ALL, null );

        assertEquals( "[--help (-?,-h)]", buffer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testAppendUsageNoOptional(  )
    {
        final Option option = buildHelpOption(  );
        final StringBuffer buffer = new StringBuffer(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_OPTIONAL );
        option.appendUsage( buffer, settings, null );

        assertEquals( "--help (-?,-h)", buffer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testAppendUsageNoAlias(  )
    {
        final Option option = buildHelpOption(  );
        final StringBuffer buffer = new StringBuffer(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_ALIASES );
        option.appendUsage( buffer, settings, null );

        assertEquals( "[--help]", buffer.toString(  ) );
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
        final Option option = buildHelpOption(  );
        assertEquals( "--help", option.getPreferredName(  ) );
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
        final Option option = buildHelpOption(  );
        assertEquals( "Displays the help", option.getDescription(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testHelpLines(  )
    {
        // TODO Auto-generated method stub
    }
}

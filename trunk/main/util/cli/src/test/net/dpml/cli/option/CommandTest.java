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
import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

import java.util.Collections;
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
public class CommandTest extends AbstractParentTestCase
{
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Command buildStartCommand(  )
    {
        return new Command( "start", "Begins the process",
            Collections.singleton( "go" ), false, null, null, 0 );
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Command buildCommitCommand(  )
    {
        return new Command( "commit", "Commit the changes to the database",
            null, true, null, null, 0 );
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Command buildLoginCommand(  )
    {
        return new Command( "login", "Initiates a session for the user", null,
            false, ArgumentTest.buildUsernameArgument(  ), null, 0 );
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
        final Command option = buildStartCommand(  );
        final List args = list( "go" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.processParent( commandLine, iterator );

        assertFalse( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "start" ) );
        assertTrue( commandLine.hasOption( "go" ) );
        assertTrue( commandLine.getValues( option ).isEmpty(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcessParentSpare(  ) throws OptionException
    {
        final Command option = buildLoginCommand(  );
        final List args = list( "login", "rob" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.processParent( commandLine, iterator );

        assertEquals( "rob", iterator.next(  ) );
        assertFalse( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "login" ) );
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
        final Command option = buildStartCommand(  );
        assertTrue( option.canProcess( 
                new WriteableCommandLineImpl( option, null ), "start" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testCanProcessBadMatch(  )
    {
        final Command option = buildStartCommand(  );
        assertFalse( option.canProcess( 
                new WriteableCommandLineImpl( option, null ), "stop" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testCanProcessAlias(  )
    {
        final Command option = buildStartCommand(  );
        assertTrue( option.canProcess( 
                new WriteableCommandLineImpl( option, null ), "go" ) );
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
        final Command option = buildStartCommand(  );
        assertTrue( option.getPrefixes(  ).isEmpty(  ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.OptionTestCase#testProcess()
     */
    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testProcess(  ) throws OptionException
    {
        final Command option = buildLoginCommand(  );
        final List args = list( "login", "rob" );
        final WriteableCommandLine commandLine = commandLine( option, args );
        final ListIterator iterator = args.listIterator(  );
        option.process( commandLine, iterator );

        assertFalse( iterator.hasNext(  ) );
        assertTrue( commandLine.hasOption( option ) );
        assertTrue( commandLine.hasOption( "login" ) );
        assertEquals( "rob", commandLine.getValue( option ) );
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
        final Command option = buildStartCommand(  );
        final Set triggers = option.getTriggers(  );
        assertContentsEqual( list( "start", "go" ), triggers );
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
        final Parent option = buildCommitCommand(  );
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
        final Option option = buildStartCommand(  );
        final StringBuffer buffer = new StringBuffer(  );
        option.appendUsage( buffer, DisplaySetting.ALL, null );

        assertEquals( "[start (go)]", buffer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNullPreferredName(  )
    {
        try
        {
            new Command( null, "", Collections.singleton( "go" ), false, null,
                null, 0 );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception name",
                ResourceHelper.getResourceHelper(  )
                              .getMessage( ResourceConstants.COMMAND_PREFERRED_NAME_TOO_SHORT ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testEmotyPreferredName(  )
    {
        try
        {
            new Command( "", "", Collections.singleton( "go" ), false, null,
                null, 0 );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception name",
                ResourceHelper.getResourceHelper(  )
                              .getMessage( ResourceConstants.COMMAND_PREFERRED_NAME_TOO_SHORT ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testAppendUsageNoOptional(  )
    {
        final Option option = buildStartCommand(  );
        final StringBuffer buffer = new StringBuffer(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_OPTIONAL );
        option.appendUsage( buffer, settings, null );

        assertEquals( "start (go)", buffer.toString(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testAppendUsageNoAlias(  )
    {
        final Option option = buildStartCommand(  );
        final StringBuffer buffer = new StringBuffer(  );
        final Set settings = new HashSet( DisplaySetting.ALL );
        settings.remove( DisplaySetting.DISPLAY_ALIASES );
        option.appendUsage( buffer, settings, null );

        assertEquals( "[start]", buffer.toString(  ) );
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
        final Option option = buildStartCommand(  );
        assertEquals( "start", option.getPreferredName(  ) );
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
        final Option option = buildLoginCommand(  );
        assertEquals( "Initiates a session for the user",
            option.getDescription(  ) );
    }

    /*
     * (non-Javadoc)
     *
     * @see net.dpml.cli.OptionTestCase#testHelpLines()
     */
    /**
     * DOCUMENT ME!
     */
    public void testHelpLines(  )
    {
        // TODO Auto-generated method stub
    }
}

/*
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
package net.dpml.cli;

import net.dpml.cli.builder.ArgumentBuilder;
import net.dpml.cli.builder.DefaultOptionBuilder;
import net.dpml.cli.builder.GroupBuilder;
import net.dpml.cli.commandline.Parser;
import net.dpml.cli.option.AbstractOptionTestCase;
import net.dpml.cli.option.ArgumentTest;
import net.dpml.cli.option.CommandTest;
import net.dpml.cli.option.DefaultOptionTest;
import net.dpml.cli.option.PropertyOption;
import net.dpml.cli.option.SwitchTest;
import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
  */
public abstract class AbstractCommandLineTestCase extends AbstractCLITestCase
{
    private static final ResourceHelper RESOURCES = ResourceHelper.getResourceHelper(  );

    /**
     * DOCUMENT ME!
     */
    public final Option m_present = new DefaultOptionBuilder(  ).withLongName( 
            "present" ).withLongName( "alsopresent" ).create(  );

    /**
     * DOCUMENT ME!
     */
    public final Option m_missing = new DefaultOptionBuilder(  ).withLongName( 
            "missing" ).create(  );

    /**
     * DOCUMENT ME!
     */
    public final Option m_multiple = new DefaultOptionBuilder(  ).withLongName( 
            "multiple" ).create(  );

    /**
     * DOCUMENT ME!
     */
    public final Option m_bool = new DefaultOptionBuilder(  ).withLongName( 
            "bool" ).create(  );

    /**
     * DOCUMENT ME!
     */
    public final Option m_root = new GroupBuilder(  ).withOption( m_present )
                                                     .withOption( m_missing )
                                                     .withOption( m_multiple )
                                                     .withOption( m_bool )
                                                     .create(  );
    private CommandLine m_commandLine;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected abstract CommandLine createCommandLine(  );

    /*
     * @see TestCase#setUp()
     */
    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void setUp(  ) throws Exception
    {
        super.setUp(  );
        m_commandLine = createCommandLine(  );
    }

    /*
     * Class to test for boolean hasOption(String)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testHasOptionString(  )
    {
        assertTrue( m_commandLine.hasOption( "--present" ) );
        assertTrue( m_commandLine.hasOption( "--alsopresent" ) );
        assertFalse( m_commandLine.hasOption( "--missing" ) );
    }

    /*
     * Class to test for boolean hasOption(Option)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testHasOptionOption(  )
    {
        assertTrue( m_commandLine.hasOption( m_present ) );
        assertFalse( m_commandLine.hasOption( m_missing ) );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testGetOption(  )
    {
        assertSame( m_present, m_commandLine.getOption( "--present" ) );
        assertSame( m_present, m_commandLine.getOption( "--alsopresent" ) );

        //TODO decide whether the following assertion is valid
        //assertSame( missing,m_commandLine.getOption( "--missing" ) );
    }

    /*
     * Class to test for List getValues(String)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testGetValuesString(  )
    {
        assertListContentsEqual( list( "present value" ),
            m_commandLine.getValues( "--present" ) );
        assertListContentsEqual( list( "value 1", "value 2", "value 3" ),
            m_commandLine.getValues( "--multiple" ) );
        assertTrue( m_commandLine.getValues( "--missing" ).isEmpty(  ) );
    }

    /*
     * Class to test for List getValues(String, List)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testGetValuesStringList(  )
    {
        assertListContentsEqual( list( "present value" ),
            m_commandLine.getValues( "--present", null ) );
        assertListContentsEqual( list( "present value" ),
            m_commandLine.getValues( "--alsopresent", null ) );
        assertSame( m_commandLine.getValues( "--missing", Collections.EMPTY_LIST ),
            Collections.EMPTY_LIST );

        final List def = Collections.singletonList( "default value" );
        assertSame( def, m_commandLine.getValues( "--missing", def ) );
    }

    /*
     * Class to test for List getValues(Option)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testGetValuesOption(  )
    {
        assertListContentsEqual( list( "present value" ),
            m_commandLine.getValues( m_present ) );
        assertTrue( m_commandLine.getValues( m_missing ).isEmpty(  ) );
    }

    /*
     * Class to test for List getValues(Option, List)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testGetValuesOptionList(  )
    {
        assertListContentsEqual( list( "present value" ),
            m_commandLine.getValues( m_present ) );
        assertSame( m_commandLine.getValues( m_missing, Collections.EMPTY_LIST ),
            Collections.EMPTY_LIST );

        final List defs = Collections.singletonList( "custom default" );
        assertSame( defs, m_commandLine.getValues( m_missing, defs ) );
    }

    /*
     * Class to test for Object getValue(String)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testGetValueString(  )
    {
        assertEquals( "present value", m_commandLine.getValue( "--present" ) );
        assertEquals( "present value", m_commandLine.getValue( "--alsopresent" ) );
        assertNull( m_commandLine.getValue( "--missing" ) );

        try
        {
            m_commandLine.getValue( "--multiple" );
            fail( "expected IllegalStateException" );
        }
        catch( IllegalStateException e )
        {
            assertEquals( RESOURCES.getMessage( 
                    ResourceConstants.ARGUMENT_TOO_MANY_VALUES ),
                e.getMessage(  ) );
        }
    }

    /*
     * Class to test for Object getValue(String, Object)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testGetValueStringObject(  )
    {
        assertEquals( "present value",
            m_commandLine.getValue( "--present", "default value" ) );
        assertEquals( "present value",
            m_commandLine.getValue( "--alsopresent", "default value" ) );
        assertEquals( "default value",
            m_commandLine.getValue( "--missing", "default value" ) );

        try
        {
            m_commandLine.getValue( "--multiple" );
            fail( "expected IllegalStateException" );
        }
        catch( IllegalStateException e )
        {
            assertEquals( RESOURCES.getMessage( 
                    ResourceConstants.ARGUMENT_TOO_MANY_VALUES ),
                e.getMessage(  ) );
        }
    }

    /*
     * Class to test for Object getValue(Option)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testGetValueOption(  )
    {
        assertEquals( "present value", m_commandLine.getValue( m_present ) );
        assertNull( m_commandLine.getValue( m_missing ) );

        try
        {
            m_commandLine.getValue( m_multiple );
            fail( "expected IllegalStateException" );
        }
        catch( IllegalStateException e )
        {
            assertEquals( RESOURCES.getMessage( 
                    ResourceConstants.ARGUMENT_TOO_MANY_VALUES ),
                e.getMessage(  ) );
        }
    }

    /*
     * Class to test for Object getValue(Option, Object)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testGetValueOptionObject(  )
    {
        assertEquals( "present value",
            m_commandLine.getValue( m_present, "default value" ) );
        assertEquals( "default value",
            m_commandLine.getValue( m_missing, "default value" ) );

        try
        {
            m_commandLine.getValue( m_multiple );
            fail( "expected IllegalStateException" );
        }
        catch( IllegalStateException e )
        {
            assertEquals( RESOURCES.getMessage( 
                    ResourceConstants.ARGUMENT_TOO_MANY_VALUES ),
                e.getMessage(  ) );
        }
    }

    /*
     * Class to test for Boolean getSwitch(String)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testGetSwitchString(  )
    {
        assertEquals( Boolean.TRUE, m_commandLine.getSwitch( "--bool" ) );
        assertNull( m_commandLine.getSwitch( "--missing" ) );
    }

    /*
     * Class to test for Boolean getSwitch(String, Boolean)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testGetSwitchStringBoolean(  )
    {
        assertEquals( Boolean.TRUE,
            m_commandLine.getSwitch( "--bool", Boolean.FALSE ) );
        assertEquals( Boolean.FALSE,
            m_commandLine.getSwitch( "--missing", Boolean.FALSE ) );
    }

    /*
     * Class to test for Boolean getSwitch(Option)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testGetSwitchOption(  )
    {
        assertEquals( Boolean.TRUE, m_commandLine.getSwitch( m_bool ) );
        assertNull( m_commandLine.getSwitch( m_missing ) );
    }

    /*
     * Class to test for Boolean getSwitch(Option, Boolean)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testGetSwitchOptionBoolean(  )
    {
        assertEquals( Boolean.TRUE,
            m_commandLine.getSwitch( m_bool, Boolean.FALSE ) );
        assertEquals( Boolean.FALSE,
            m_commandLine.getSwitch( m_missing, Boolean.FALSE ) );
    }

    /*
     * Class to test for String getProperty(String)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testGetPropertyString(  )
    {
        assertEquals( "present property", m_commandLine.getProperty( "present" ) );
        assertNull( m_commandLine.getProperty( "missing" ) );
    }

    /*
     * Class to test for String getProperty(String, String)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testGetPropertyStringString(  )
    {
        assertEquals( "present property",
            m_commandLine.getProperty( "present", "default property" ) );
        assertEquals( "default property",
            m_commandLine.getProperty( "missing", "default property" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testGetProperties(  )
    {
        assertTrue( m_commandLine.getProperties(  )
                                 .containsAll( list( "present" ) ) );
    }

    /*
     * Class to test for int getOptionCount(String)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testGetOptionCountString(  )
    {
        // one option, one switch
        assertTrue( 1 <= m_commandLine.getOptionCount( "--present" ) );
        assertTrue( 1 <= m_commandLine.getOptionCount( "--bool" ) );
        assertEquals( 0, m_commandLine.getOptionCount( "--missing" ) );
    }

    /*
     * Class to test for int getOptionCount(Option)
     */
    /**
     * DOCUMENT ME!
     */
    public final void testGetOptionCountOption(  )
    {
        // one option, one switch
        assertTrue( 1 <= m_commandLine.getOptionCount( m_present ) );
        assertTrue( 1 <= m_commandLine.getOptionCount( m_bool ) );
        assertEquals( 0, m_commandLine.getOptionCount( m_missing ) );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testGetOptions(  )
    {
        //TODO Implement getOptions().
    }

    /**
     * DOCUMENT ME!
     */
    public final void testGetOptionTriggers(  )
    {
        //TODO Implement getOptionTriggers().
    }

    // OLD TESTS FOLLOW
    /**
     * DOCUMENT ME!
     */
    public final void testProperties(  )
    {
        final Option option = new PropertyOption(  );
        final List args = AbstractCLITestCase.list(  );
        final WriteableCommandLine writeable = AbstractOptionTestCase.commandLine( option,
                args );

        assertTrue( writeable.getProperties(  ).isEmpty(  ) );

        writeable.addProperty( "myprop", "myval" );
        assertEquals( 1, writeable.getProperties(  ).size(  ) );
        assertEquals( "myval", writeable.getProperty( "myprop" ) );

        writeable.addProperty( "myprop", "myval2" );
        assertEquals( 1, writeable.getProperties(  ).size(  ) );
        assertEquals( "myval2", writeable.getProperty( "myprop" ) );

        writeable.addProperty( "myprop2", "myval3" );
        assertEquals( 2, writeable.getProperties(  ).size(  ) );
        assertEquals( "myval3", writeable.getProperty( "myprop2" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testOptions(  )
    {
        final Option option = new PropertyOption(  );
        final List args = AbstractCLITestCase.list(  );
        final WriteableCommandLine writeable = AbstractOptionTestCase.commandLine( option,
                args );

        final Option start = CommandTest.buildStartCommand(  );

        assertFalse( writeable.hasOption( start ) );
        assertFalse( writeable.hasOption( "start" ) );
        assertFalse( writeable.hasOption( "go" ) );

        writeable.addOption( start );

        assertTrue( writeable.hasOption( start ) );
        assertTrue( writeable.hasOption( "start" ) );
        assertTrue( writeable.hasOption( "go" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testValues(  )
    {
        final Option option = new PropertyOption(  );
        final List args = AbstractCLITestCase.list(  );
        final WriteableCommandLine writeable = AbstractOptionTestCase.commandLine( option,
                args );

        final Option start = CommandTest.buildStartCommand(  );

        assertNull( writeable.getValue( start ) );
        assertTrue( writeable.getValues( start ).isEmpty(  ) );

        writeable.addOption( start );

        assertTrue( writeable.getValues( start ).isEmpty(  ) );

        writeable.addValue( start, "file1" );

        assertEquals( "file1", writeable.getValue( start ) );
        assertEquals( "file1", writeable.getValue( "start" ) );
        assertEquals( "file1", writeable.getValue( "go" ) );
        assertEquals( 1, writeable.getValues( start ).size(  ) );
        assertEquals( 1, writeable.getValues( "start" ).size(  ) );
        assertEquals( 1, writeable.getValues( "go" ).size(  ) );
        assertTrue( writeable.getValues( start ).contains( "file1" ) );
        assertTrue( writeable.getValues( "start" ).contains( "file1" ) );
        assertTrue( writeable.getValues( "go" ).contains( "file1" ) );

        writeable.addValue( start, "file2" );

        try
        {
            writeable.getValue( start );
            fail( "Cannot get single value if multiple are present" );
        }
        catch( IllegalStateException ise )
        {
            assertEquals( RESOURCES.getMessage( 
                    ResourceConstants.ARGUMENT_TOO_MANY_VALUES ),
                ise.getMessage(  ) );
        }

        try
        {
            writeable.getValue( "start" );
            fail( "Cannot get single value if multiple are present" );
        }
        catch( IllegalStateException ise )
        {
            assertEquals( RESOURCES.getMessage( 
                    ResourceConstants.ARGUMENT_TOO_MANY_VALUES ),
                ise.getMessage(  ) );
        }

        writeable.getValues( start ).add( "file3" );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testSwitches(  )
    {
        final Option option = new PropertyOption(  );
        final List args = AbstractCLITestCase.list(  );
        final WriteableCommandLine writeable = AbstractOptionTestCase.commandLine( option,
                args );

        final Option start = CommandTest.buildStartCommand(  );

        assertNull( writeable.getSwitch( start ) );
        assertNull( writeable.getSwitch( "start" ) );
        assertNull( writeable.getSwitch( "go" ) );

        writeable.addSwitch( start, true );

        try
        {
            writeable.addSwitch( start, false );
            fail( "Switch cannot be changed" );
        }
        catch( IllegalStateException ise )
        {
            assertEquals( RESOURCES.getMessage( 
                    ResourceConstants.SWITCH_ALREADY_SET ), ise.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public final void testSwitchesTrue(  )
    {
        final Option option = new PropertyOption(  );
        final List args = AbstractCLITestCase.list(  );
        final WriteableCommandLine writeable = AbstractOptionTestCase.commandLine( option,
                args );

        final Option start = CommandTest.buildStartCommand(  );

        writeable.addSwitch( start, true );
        assertSame( Boolean.TRUE, writeable.getSwitch( start ) );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testSwitchesFalse(  )
    {
        final Option option = new PropertyOption(  );
        final List args = AbstractCLITestCase.list(  );
        final WriteableCommandLine writeable = AbstractOptionTestCase.commandLine( option,
                args );

        final Option start = CommandTest.buildStartCommand(  );

        writeable.addSwitch( start, false );
        assertSame( Boolean.FALSE, writeable.getSwitch( start ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public final void testGetOptionsOrder(  ) throws OptionException
    {
        final Option help = DefaultOptionTest.buildHelpOption(  );
        final Option login = CommandTest.buildLoginCommand(  );
        final Option targets = ArgumentTest.buildTargetsArgument(  );

        final Group group = new GroupBuilder(  ).withOption( help )
                                                .withOption( login )
                                                .withOption( targets ).create(  );

        final Parser parser = new Parser(  );
        parser.setGroup( group );

        final CommandLine cl = parser.parse( new String[]
                {
                    "login", "rob", "--help", "target1", "target2"
                } );

        final Iterator i = cl.getOptions(  ).iterator(  );

        assertSame( login, i.next(  ) );
        assertSame( help, i.next(  ) );
        assertSame( targets, i.next(  ) );
        assertSame( targets, i.next(  ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public final void testGetOptionCount(  ) throws OptionException
    {
        final Option help = DefaultOptionTest.buildHelpOption(  );
        final Option login = CommandTest.buildLoginCommand(  );
        final Option targets = ArgumentTest.buildTargetsArgument(  );
        final Option display = SwitchTest.buildDisplaySwitch(  );

        final Group group = new GroupBuilder(  ).withOption( help )
                                                .withOption( login )
                                                .withOption( targets )
                                                .withOption( display ).create(  );

        final Parser parser = new Parser(  );
        parser.setGroup( group );

        final CommandLine cl = parser.parse( new String[]
                {
                    "--help", "login", "rob", "+display", "--help", "--help",
                    "target1", "target2"
                } );

        assertEquals( 1, cl.getOptionCount( login ) );
        assertEquals( 3, cl.getOptionCount( help ) );
        assertEquals( 2, cl.getOptionCount( targets ) );
        assertEquals( 1, cl.getOptionCount( display ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public final void testGetOptionCountStrings(  ) throws OptionException
    {
        final Option help = DefaultOptionTest.buildHelpOption(  );
        final Option login = CommandTest.buildLoginCommand(  );
        final Option targets = ArgumentTest.buildTargetsArgument(  );
        final Option display = SwitchTest.buildDisplaySwitch(  );

        final Group group = new GroupBuilder(  ).withOption( help )
                                                .withOption( login )
                                                .withOption( targets )
                                                .withOption( display ).create(  );

        final Parser parser = new Parser(  );
        parser.setGroup( group );

        final CommandLine cl = parser.parse( new String[]
                {
                    "--help", "login", "rob", "+display", "--help", "--help",
                    "target1", "target2"
                } );

        assertEquals( 1, cl.getOptionCount( "login" ) );
        assertEquals( 3, cl.getOptionCount( "-?" ) );
        assertEquals( 1, cl.getOptionCount( "+display" ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public final void testOptionAsArgument(  ) throws OptionException
    {
        final Option p = new DefaultOptionBuilder(  ).withShortName( "p" )
                                                     .create(  );
        final Argument argument = new ArgumentBuilder(  ).create(  );
        final Option withArgument = new DefaultOptionBuilder(  ).withShortName( 
                "attr" ).withArgument( argument ).create(  );

        final Group group = new GroupBuilder(  ).withOption( p )
                                                .withOption( withArgument )
                                                .create(  );

        final Parser parser = new Parser(  );
        parser.setGroup( group );

        final CommandLine cl = parser.parse( new String[]{"-p", "-attr", "p"} );

        assertEquals( 1, cl.getOptionCount( "-p" ) );
        assertTrue( cl.hasOption( "-p" ) );
        assertTrue( cl.hasOption( "-attr" ) );
        assertTrue( cl.getValue( "-attr" ).equals( "p" ) );
    }
}

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

import dpml.cli.AbstractCommandLineTestCase;
import dpml.cli.CommandLine;

import java.util.Iterator;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * @author Rob Oxspring
 */
public class PreferencesCommandLineTest extends AbstractCommandLineTestCase
{
    /* (non-Javadoc)
     * @see dpml.cli.CommandLineTest#createCommandLine()
     */
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected CommandLine createCommandLine(  )
    {
        // TODO Auto-generated method stub
        final Preferences props = Preferences.userNodeForPackage( PreferencesCommandLineTest.class );
        props.put( "--present", "present value" );
        props.put( "--alsopresent", "" );
        props.put( "--multiple", "value 1|value 2|value 3" );
        props.put( "--bool", "true" );

        props.put( "present", "present property" );

        return new PreferencesCommandLine( ROOT, props, '|' );
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected CommandLine createCommandLineNoSep(  )
    {
        // TODO Auto-generated method stub
        final Preferences props = Preferences.userNodeForPackage( PreferencesCommandLineTest.class );
        props.put( "--present", "present value" );
        props.put( "--alsopresent", "" );
        props.put( "--multiple", "value 1|value 2|value 3" );
        props.put( "--bool", "false" );

        props.put( "present", "present property" );

        return new PreferencesCommandLine( ROOT, props );
    }

    /**
     * DOCUMENT ME!
     */
    public void testPropertyValues(  )
    {
        // nothing to test
        CommandLine cmdline = createCommandLine(  );

        assertEquals( "wrong value", "present value",
            cmdline.getValue( "--present" ) );
        assertEquals( "wrong value", "present value",
            cmdline.getValue( "--alsopresent" ) );
        assertEquals( "wrong # of values", 3,
            cmdline.getValues( "--multiple" ).size(  ) );
        assertEquals( "wrong value 1", "value 1",
            cmdline.getValues( "--multiple" ).get( 0 ) );
        assertEquals( "wrong value 2", "value 2",
            cmdline.getValues( "--multiple" ).get( 1 ) );
        assertEquals( "wrong value 3", "value 3",
            cmdline.getValues( "--multiple" ).get( 2 ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNoSeparator(  )
    {
        // nothing to test
        CommandLine cmdline = createCommandLineNoSep(  );

        assertEquals( "wrong value", "present value",
            cmdline.getValue( "--present" ) );
        assertEquals( "wrong value", "present value",
            cmdline.getValue( "--alsopresent" ) );
        assertEquals( "wrong # of values", 1,
            cmdline.getValues( "--multiple" ).size(  ) );
        assertEquals( "wrong value", "value 1|value 2|value 3",
            cmdline.getValue( "--multiple" ) );
        assertFalse( "expected a false",
            cmdline.getSwitch( "--bool" ).booleanValue(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNullOption(  )
    {
        // nothing to test
        CommandLine cmdline = createCommandLine(  );

        assertFalse( "should not find null option",
            cmdline.hasOption( (String) null ) );
        assertTrue( "expected a true",
            cmdline.getSwitch( "--bool" ).booleanValue(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testPreferenceTriggers(  )
    {
        // nothing to test
        CommandLine cmdline = createCommandLine(  );

        Set triggers = cmdline.getOptionTriggers(  );
        Iterator iter = triggers.iterator(  );
        assertEquals( "wrong # of triggers", 4, triggers.size(  ) );
        assertTrue( "cannot find trigger", triggers.contains( "--bool" ) );
        assertTrue( "cannot find trigger", triggers.contains( "--present" ) );
        assertTrue( "cannot find trigger", triggers.contains( "--multiple" ) );
        assertTrue( "cannot find trigger", triggers.contains( "--alsopresent" ) );

        assertFalse( "should not find null option",
            cmdline.hasOption( (String) null ) );
        assertTrue( "expected a true",
            cmdline.getSwitch( "--bool" ).booleanValue(  ) );
    }
}

/**
 * Copyright 2004 The Apache Software Foundation
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
package dpml.cli;

import junit.framework.TestCase;

import dpml.cli.builder.ArgumentBuilder;
import dpml.cli.builder.SwitchBuilder;
import dpml.cli.commandline.WriteableCommandLineImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests the interaction of command line values and defaults supplied in different ways.
 *
 * Tests marked xxxParsed involve values parsed from a command line.
 *
 * Tests marked xxxMethod involve defaults supplied in the query method.
 *
 * Tests marked xxxOption involve defaults specified in the model.
 *
 * @author Rob Oxspring
 */
public class CommandLineDefaultsTest extends TestCase
{
    /*
     * utils to grab the default from the method
     */
    private Object methodSwitch( WriteableCommandLine cl, Option o, Boolean bool )
    {
        return cl.getSwitch( o, bool );
    }

    private Object methodSwitchNull( WriteableCommandLine cl, Option o )
    {
        return methodSwitch( cl, o, null );
    }

    private Object methodSwitchOff( WriteableCommandLine cl, Option o )
    {
        return methodSwitch( cl, o, Boolean.FALSE );
    }

    private Object methodSwitchOn( WriteableCommandLine cl, Option o )
    {
        return methodSwitch( cl, o, Boolean.TRUE );
    }

    private Object methodValueMissing( WriteableCommandLine cl, Option o )
    {
        return cl.getValue( o );
    }

    private Object methodValuePresent( WriteableCommandLine cl, Option o )
    {
        return cl.getValue( o, "method" );
    }

    /*
     * utils to grab the default from the option model
     */
    private Option optionSwitch( Boolean bool )
    {
        return new SwitchBuilder(  ).withName( "switch" ).withSwitchDefault( bool )
                                    .create(  );
    }

    private Option optionSwitchNull(  )
    {
        return optionSwitch( null );
    }

    private Option optionSwitchOff(  )
    {
        return optionSwitch( Boolean.FALSE );
    }

    private Option optionSwitchOn(  )
    {
        return optionSwitch( Boolean.TRUE );
    }

    private Option optionValueMissing(  )
    {
        return new ArgumentBuilder(  ).create(  );
    }

    private Option optionValuePresent(  )
    {
        return new ArgumentBuilder(  ).withDefaults( Arrays.asList( 
                new String[]{"option"} ) ).create(  );
    }

    /*
     * utils to grab the input from the command line
     */
    private WriteableCommandLine parsedSwitch( Option o, Boolean bool )
    {
        final List args;

        if( bool == null )
        {
            args = Collections.EMPTY_LIST;
        }
        else
        {
            args = Collections.singletonList( String.valueOf( bool )
                                                    .toLowerCase(  ) );
        }

        WriteableCommandLine cl = new WriteableCommandLineImpl( o, args );
        o.defaults( cl );

        if( bool != null )
        {
            cl.addSwitch( o, bool.booleanValue(  ) );
        }

        return cl;
    }

    private WriteableCommandLine parsedSwitchNull( Option o )
    {
        return parsedSwitch( o, null );
    }

    private WriteableCommandLine parsedSwitchOn( Option o )
    {
        return parsedSwitch( o, Boolean.TRUE );
    }

    private WriteableCommandLine parsedValueMissing( Option o )
    {
        WriteableCommandLine cl = new WriteableCommandLineImpl( o,
                Collections.EMPTY_LIST );
        o.defaults( cl );

        return cl;
    }

    private WriteableCommandLine parsedValuePresent( Option o )
    {
        WriteableCommandLine cl = new WriteableCommandLineImpl( o,
                Arrays.asList( new String[]{"parsed"} ) );
        o.defaults( cl );
        cl.addValue( o, "parsed" );

        return cl;
    }

    /*
     * tests
     */
    /**
     * DOCUMENT ME!
     */
    public void testSwitchMethod(  )
    {
        final Option o = optionSwitchNull(  );
        final WriteableCommandLine cl = parsedSwitchNull( o );
        final Object v = methodSwitchOn( cl, o );
        assertEquals( Boolean.TRUE, v );
    }

    /**
     * DOCUMENT ME!
     */
    public void testSwitchMethodOption(  )
    {
        final Option o = optionSwitchOff(  );
        final WriteableCommandLine cl = parsedSwitchNull( o );
        final Object v = methodSwitchOn( cl, o );
        assertEquals( Boolean.TRUE, v );
    }

    /**
     * DOCUMENT ME!
     */
    public void testSwitchOption(  )
    {
        final Option o = optionSwitchOn(  );
        final WriteableCommandLine cl = parsedSwitchNull( o );
        final Object v = methodSwitchNull( cl, o );
        assertEquals( Boolean.TRUE, v );
    }

    /**
     * DOCUMENT ME!
     */
    public void testSwitchParsed(  )
    {
        final Option o = optionSwitchNull(  );
        final WriteableCommandLine cl = parsedSwitchOn( o );
        final Object v = methodSwitchNull( cl, o );
        assertEquals( Boolean.TRUE, v );
    }

    /**
     * DOCUMENT ME!
     */
    public void testSwitchParsedMethod(  )
    {
        final Option o = optionSwitchOff(  );
        final WriteableCommandLine cl = parsedSwitchOn( o );
        final Object v = methodSwitchNull( cl, o );
        assertEquals( Boolean.TRUE, v );
    }

    /**
     * DOCUMENT ME!
     */
    public void testSwitchParsedMethodOption(  )
    {
        final Option o = optionSwitchOff(  );
        final WriteableCommandLine cl = parsedSwitchOn( o );
        final Object v = methodSwitchOff( cl, o );
        assertEquals( Boolean.TRUE, v );
    }

    /**
     * DOCUMENT ME!
     */
    public void testSwitchParsedOption(  )
    {
        final Option o = optionSwitchOff(  );
        final WriteableCommandLine cl = parsedSwitchOn( o );
        final Object v = methodSwitchNull( cl, o );
        assertEquals( Boolean.TRUE, v );
    }

    /**
     * DOCUMENT ME!
     */
    public void testValues(  )
    {
        final Option o = optionValueMissing(  );
        final WriteableCommandLine cl = parsedValueMissing( o );
        final Object v = methodValueMissing( cl, o );
        assertNull( v );
    }

    /**
     * DOCUMENT ME!
     */
    public void testValuesMethod(  )
    {
        final Option o = optionValueMissing(  );
        final WriteableCommandLine cl = parsedValueMissing( o );
        final Object v = methodValuePresent( cl, o );
        assertEquals( "method", v );
    }

    /**
     * DOCUMENT ME!
     */
    public void testValuesMethodOption(  )
    {
        final Option o = optionValuePresent(  );
        final WriteableCommandLine cl = parsedValueMissing( o );
        final Object v = methodValuePresent( cl, o );
        assertEquals( "method", v );
    }

    /**
     * DOCUMENT ME!
     */
    public void testValuesOption(  )
    {
        final Option o = optionValuePresent(  );
        final WriteableCommandLine cl = parsedValueMissing( o );
        final Object v = methodValueMissing( cl, o );
        assertEquals( "option", v );
    }

    /**
     * DOCUMENT ME!
     */
    public void testValuesParsed(  )
    {
        final Option o = optionValueMissing(  );
        final WriteableCommandLine cl = parsedValuePresent( o );
        final Object v = methodValueMissing( cl, o );
        assertEquals( "parsed", v );
    }

    /**
     * DOCUMENT ME!
     */
    public void testValuesParsedMethod(  )
    {
        final Option o = optionValueMissing(  );
        final WriteableCommandLine cl = parsedValuePresent( o );
        final Object v = methodValuePresent( cl, o );
        assertEquals( "parsed", v );
    }

    /**
     * DOCUMENT ME!
     */
    public void testValuesParsedMethodOption(  )
    {
        final Option o = optionValuePresent(  );
        final WriteableCommandLine cl = parsedValuePresent( o );
        final Object v = methodValuePresent( cl, o );
        assertEquals( "parsed", v );
    }

    /**
     * DOCUMENT ME!
     */
    public void testValuesParsedOption(  )
    {
        final Option o = optionValuePresent(  );
        final WriteableCommandLine cl = parsedValuePresent( o );
        final Object v = methodValueMissing( cl, o );
        assertEquals( "parsed", v );
    }
}

/*
 * Copyright 2004-2005 The Apache Software Foundation
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
package net.dpml.cli.commandline;

import net.dpml.cli.AbstractCommandLineTestCase;
import net.dpml.cli.CommandLine;
import net.dpml.cli.Option;
import net.dpml.cli.WriteableCommandLine;
import net.dpml.cli.builder.DefaultOptionBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Rob Oxspring
 */
public class DefaultingCommandLineTest extends AbstractCommandLineTestCase
{
    private CommandLine first;
    private CommandLine second;
    private Option inFirst = new DefaultOptionBuilder(  ).withLongName( 
            "infirst" ).create(  );
    private Option inBoth = new DefaultOptionBuilder(  ).withLongName( "inboth" )
                                                        .create(  );
    private Option inSecond = new DefaultOptionBuilder(  ).withLongName( 
            "insecond" ).create(  );

    /* (non-Javadoc)
     * @see net.dpml.cli.CommandLineTest#createCommandLine()
     */
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected final CommandLine createCommandLine(  )
    {
        final WriteableCommandLine writeable = new WriteableCommandLineImpl( m_root,
                new ArrayList(  ) );
        writeable.addOption( m_present );
        writeable.addProperty( "present", "present property" );
        writeable.addSwitch( m_bool, true );
        writeable.addValue( m_present, "present value" );
        writeable.addOption( m_multiple );
        writeable.addValue( m_multiple, "value 1" );
        writeable.addValue( m_multiple, "value 2" );
        writeable.addValue( m_multiple, "value 3" );

        final DefaultingCommandLine defaults = new DefaultingCommandLine(  );
        defaults.appendCommandLine( writeable );

        return defaults;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void setUp(  ) throws Exception
    {
        super.setUp(  );

        WriteableCommandLine writeable;

        writeable = new WriteableCommandLineImpl( m_root, new ArrayList(  ) );
        writeable.addOption( inFirst );
        writeable.addOption( inBoth );
        writeable.addProperty( "infirst", "infirst first value" );
        writeable.addProperty( "inboth", "inboth first value" );
        writeable.addSwitch( inFirst, true );
        writeable.addSwitch( inBoth, true );
        writeable.addValue( inFirst, "infirst first value 1" );
        writeable.addValue( inFirst, "infirst first value 2" );
        writeable.addValue( inBoth, "inboth first value 1" );
        writeable.addValue( inBoth, "inboth first value 2" );
        first = writeable;

        writeable = new WriteableCommandLineImpl( m_root, new ArrayList(  ) );
        writeable.addOption( inSecond );
        writeable.addOption( inBoth );
        writeable.addProperty( "insecond", "insecond second value" );
        writeable.addProperty( "inboth", "inboth second value" );
        writeable.addSwitch( inSecond, true );
        writeable.addSwitch( inBoth, true );
        writeable.addValue( inSecond, "insecond second value 1" );
        writeable.addValue( inSecond, "insecond second value 2" );
        writeable.addValue( inBoth, "inboth second value 1" );
        writeable.addValue( inBoth, "inboth second value 2" );
        second = writeable;
    }

    /**
     * DOCUMENT ME!
     */
    public final void testAppendCommandLine(  )
    {
        final DefaultingCommandLine defaults = new DefaultingCommandLine(  );
        Iterator i;

        i = defaults.commandLines(  );
        assertFalse( i.hasNext(  ) );

        defaults.appendCommandLine( first );
        i = defaults.commandLines(  );
        assertSame( first, i.next(  ) );
        assertFalse( i.hasNext(  ) );

        defaults.appendCommandLine( second );
        i = defaults.commandLines(  );
        assertSame( first, i.next(  ) );
        assertSame( second, i.next(  ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testInsertCommandLine(  )
    {
        final DefaultingCommandLine defaults = new DefaultingCommandLine(  );
        Iterator i;

        i = defaults.commandLines(  );
        assertFalse( i.hasNext(  ) );

        defaults.insertCommandLine( 0, first );
        i = defaults.commandLines(  );
        assertSame( first, i.next(  ) );
        assertFalse( i.hasNext(  ) );

        defaults.insertCommandLine( 0, second );
        i = defaults.commandLines(  );
        assertSame( second, i.next(  ) );
        assertSame( first, i.next(  ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testTriggers(  )
    {
        final DefaultingCommandLine defaults = new DefaultingCommandLine(  );
        defaults.appendCommandLine( first );
        defaults.appendCommandLine( second );

        Set set = defaults.getOptionTriggers(  );
        Iterator iter = set.iterator(  );
        assertEquals( "wrong # of triggers", 3, set.size(  ) );
        assertTrue( "cannot find trigger", set.contains( "--insecond" ) );
        assertTrue( "cannot find trigger", set.contains( "--inboth" ) );
        assertTrue( "cannot find trigger", set.contains( "--infirst" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testDefaults(  )
    {
        final DefaultingCommandLine defaults = new DefaultingCommandLine(  );

        assertEquals( "wrong # of defaults", 0,
            defaults.getValues( "--insecond" ).size(  ) );
        assertEquals( "wrong Set of defaults", Collections.EMPTY_LIST,
            defaults.getValues( "--insecond", null ) );
    }
}

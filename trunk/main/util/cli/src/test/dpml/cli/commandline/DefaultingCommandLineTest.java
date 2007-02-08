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
import dpml.cli.Option;
import dpml.cli.WriteableCommandLine;
import dpml.cli.builder.DefaultOptionBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Rob Oxspring
 */
public class DefaultingCommandLineTest extends AbstractCommandLineTestCase
{
    private CommandLine m_first;
    private CommandLine m_second;
    private Option m_inFirst = new DefaultOptionBuilder().withLongName( 
            "infirst" ).create(  );
    private Option m_inBoth = new DefaultOptionBuilder().withLongName( "inboth" )
                                                        .create(  );
    private Option m_inSecond = new DefaultOptionBuilder().withLongName( 
            "insecond" ).create(  );

    /* (non-Javadoc)
     * @see dpml.cli.CommandLineTest#createCommandLine()
     */
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected final CommandLine createCommandLine(  )
    {
        final WriteableCommandLine writeable = 
          new WriteableCommandLineImpl( ROOT, new ArrayList(  ) );
        writeable.addOption( PRESENT );
        writeable.addProperty( "present", "present property" );
        writeable.addSwitch( BOOLEAN, true );
        writeable.addValue( PRESENT, "present value" );
        writeable.addOption( MULTIPLE );
        writeable.addValue( MULTIPLE, "value 1" );
        writeable.addValue( MULTIPLE, "value 2" );
        writeable.addValue( MULTIPLE, "value 3" );

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

        writeable = new WriteableCommandLineImpl( ROOT, new ArrayList(  ) );
        writeable.addOption( m_inFirst );
        writeable.addOption( m_inBoth );
        writeable.addProperty( "infirst", "infirst m_first value" );
        writeable.addProperty( "inboth", "inboth m_first value" );
        writeable.addSwitch( m_inFirst, true );
        writeable.addSwitch( m_inBoth, true );
        writeable.addValue( m_inFirst, "infirst m_first value 1" );
        writeable.addValue( m_inFirst, "infirst m_first value 2" );
        writeable.addValue( m_inBoth, "inboth m_first value 1" );
        writeable.addValue( m_inBoth, "inboth m_first value 2" );
        m_first = writeable;

        writeable = new WriteableCommandLineImpl( ROOT, new ArrayList(  ) );
        writeable.addOption( m_inSecond );
        writeable.addOption( m_inBoth );
        writeable.addProperty( "insecond", "insecond m_second value" );
        writeable.addProperty( "inboth", "inboth m_second value" );
        writeable.addSwitch( m_inSecond, true );
        writeable.addSwitch( m_inBoth, true );
        writeable.addValue( m_inSecond, "insecond m_second value 1" );
        writeable.addValue( m_inSecond, "insecond m_second value 2" );
        writeable.addValue( m_inBoth, "inboth m_second value 1" );
        writeable.addValue( m_inBoth, "inboth m_second value 2" );
        m_second = writeable;
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

        defaults.appendCommandLine( m_first );
        i = defaults.commandLines(  );
        assertSame( m_first, i.next(  ) );
        assertFalse( i.hasNext(  ) );

        defaults.appendCommandLine( m_second );
        i = defaults.commandLines(  );
        assertSame( m_first, i.next(  ) );
        assertSame( m_second, i.next(  ) );
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

        defaults.insertCommandLine( 0, m_first );
        i = defaults.commandLines(  );
        assertSame( m_first, i.next(  ) );
        assertFalse( i.hasNext(  ) );

        defaults.insertCommandLine( 0, m_second );
        i = defaults.commandLines(  );
        assertSame( m_second, i.next(  ) );
        assertSame( m_first, i.next(  ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testTriggers(  )
    {
        final DefaultingCommandLine defaults = new DefaultingCommandLine(  );
        defaults.appendCommandLine( m_first );
        defaults.appendCommandLine( m_second );

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

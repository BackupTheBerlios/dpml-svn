/**
 * Copyright 2004 The Apache Software Foundation
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

import net.dpml.cli.option.ArgumentTest;

/**
 * @author Rob Oxspring
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class AbstractWriteableCommandLineTestCase
    extends AbstractCommandLineTestCase
{
    private WriteableCommandLine m_writeable;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected abstract WriteableCommandLine createWriteableCommandLine(  );

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
        final WriteableCommandLine cl = createWriteableCommandLine(  );
        cl.addOption( m_present );
        cl.addProperty( "present", "present property" );
        cl.addSwitch( m_bool, true );
        cl.addValue( m_present, "present value" );
        cl.addOption( m_multiple );
        cl.addValue( m_multiple, "value 1" );
        cl.addValue( m_multiple, "value 2" );
        cl.addValue( m_multiple, "value 3" );

        return cl;
    }

    /*
     * @see CommandLineTest#setUp()
     */
    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public void setUp(  ) throws Exception
    {
        super.setUp(  );
        m_writeable = createWriteableCommandLine(  );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testAddOption(  )
    {
        assertFalse( m_writeable.hasOption( m_present ) );
        m_writeable.addOption( m_present );
        assertTrue( m_writeable.hasOption( m_present ) );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testAddValue(  )
    {
        assertFalse( m_writeable.hasOption( m_present ) );
        assertTrue( m_writeable.getValues( m_present ).isEmpty(  ) );
        m_writeable.addValue( m_present, "value" );
        assertContentsEqual( list( "value" ), m_writeable.getValues( m_present ) );

        // most options shouldn't appear due to adding values
        assertFalse( m_writeable.hasOption( m_present ) );

        final Argument arg = ArgumentTest.buildHostArgument(  );

        assertFalse( m_writeable.hasOption( arg ) );
        assertTrue( m_writeable.getValues( arg ).isEmpty(  ) );
        m_writeable.addValue( arg, "value" );
        assertContentsEqual( list( "value" ), m_writeable.getValues( arg ) );

        // Arguments should force the option present
        assertTrue( m_writeable.hasOption( arg ) );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testAddSwitch(  )
    {
        assertFalse( m_writeable.hasOption( m_present ) );
        assertNull( m_writeable.getSwitch( m_present ) );
        m_writeable.addSwitch( m_present, true );
        assertEquals( Boolean.TRUE, m_writeable.getSwitch( m_present ) );
        assertTrue( m_writeable.hasOption( m_present ) );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testAddProperty(  )
    {
        assertNull( m_writeable.getProperty( "present" ) );
        m_writeable.addProperty( "present", "present value" );
        assertEquals( "present value", m_writeable.getProperty( "present" ) );
    }
}

/**
 * Copyright 2004 The Apache Software Foundation
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
        cl.addOption( PRESENT );
        cl.addProperty( "present", "present property" );
        cl.addSwitch( BOOLEAN, true );
        cl.addValue( PRESENT, "present value" );
        cl.addOption( MULTIPLE );
        cl.addValue( MULTIPLE, "value 1" );
        cl.addValue( MULTIPLE, "value 2" );
        cl.addValue( MULTIPLE, "value 3" );

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
        assertFalse( m_writeable.hasOption( PRESENT ) );
        m_writeable.addOption( PRESENT );
        assertTrue( m_writeable.hasOption( PRESENT ) );
    }

    /**
     * DOCUMENT ME!
     */
    public final void testAddValue(  )
    {
        assertFalse( m_writeable.hasOption( PRESENT ) );
        assertTrue( m_writeable.getValues( PRESENT ).isEmpty(  ) );
        m_writeable.addValue( PRESENT, "value" );
        assertContentsEqual( list( "value" ), m_writeable.getValues( PRESENT ) );

        // most options shouldn't appear due to adding values
        assertFalse( m_writeable.hasOption( PRESENT ) );

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
        assertFalse( m_writeable.hasOption( PRESENT ) );
        assertNull( m_writeable.getSwitch( PRESENT ) );
        m_writeable.addSwitch( PRESENT, true );
        assertEquals( Boolean.TRUE, m_writeable.getSwitch( PRESENT ) );
        assertTrue( m_writeable.hasOption( PRESENT ) );
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

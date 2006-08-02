/*
 * Copyright 2004-2005 The Apache Software Foundation
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
package net.dpml.cli.builder;

import junit.framework.TestCase;

import net.dpml.cli.Argument;
import net.dpml.cli.Group;
import net.dpml.cli.option.DefaultOption;
import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
  */
public class DefaultOptionBuilderTest extends TestCase
{
    private static final ResourceHelper RESOURCES = ResourceHelper.getResourceHelper(  );
    private DefaultOptionBuilder m_defaultOptionBuilder;

    /*
     * @see TestCase#setUp()
     */
    /**
     * DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp(  ) throws Exception
    {
        m_defaultOptionBuilder = new DefaultOptionBuilder(  );
    }

    /*
     * Class to test for void DefaultOptionBuilder(String, String, boolean)
     */
    /**
     * DOCUMENT ME!
     */
    public void testNewNullShortPrefix(  )
    {
        try
        {
            new DefaultOptionBuilder( null, null, false );
            fail( "null short prefix is not permitted" );
        }
        catch( IllegalArgumentException e )
        {
            assertEquals( RESOURCES.getMessage( 
                    ResourceConstants.OPTION_ILLEGAL_SHORT_PREFIX ),
                e.getMessage(  ) );
        }
    }

    /*
     * Class to test for void DefaultOptionBuilder(String, String, boolean)
     */
    /**
     * DOCUMENT ME!
     */
    public void testNewEmptyShortPrefix(  )
    {
        try
        {
            new DefaultOptionBuilder( "", null, false );
            fail( "empty short prefix is not permitted" );
        }
        catch( IllegalArgumentException e )
        {
            assertEquals( RESOURCES.getMessage( 
                    ResourceConstants.OPTION_ILLEGAL_SHORT_PREFIX ),
                e.getMessage(  ) );
        }
    }

    /*
     * Class to test for void DefaultOptionBuilder(String, String, boolean)
     */
    /**
     * DOCUMENT ME!
     */
    public void testNewNullLongPrefix(  )
    {
        try
        {
            new DefaultOptionBuilder( "-", null, false );
            fail( "null long prefix is not permitted" );
        }
        catch( IllegalArgumentException e )
        {
            assertEquals( RESOURCES.getMessage( 
                    ResourceConstants.OPTION_ILLEGAL_LONG_PREFIX ),
                e.getMessage(  ) );
        }
    }

    /*
     * Class to test for void DefaultOptionBuilder(String, String, boolean)
     */
    /**
     * DOCUMENT ME!
     */
    public void testNewEmptyLongPrefix(  )
    {
        try
        {
            new DefaultOptionBuilder( "-", "", false );
            fail( "empty long prefix is not permitted" );
        }
        catch( IllegalArgumentException e )
        {
            assertEquals( RESOURCES.getMessage( 
                    ResourceConstants.OPTION_ILLEGAL_LONG_PREFIX ),
                e.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testCreate(  )
    {
        try
        {
            m_defaultOptionBuilder.create(  );
            fail( "options must have a name" );
        }
        catch( IllegalStateException e )
        {
            assertEquals( RESOURCES.getMessage( 
                    ResourceConstants.OPTION_NO_NAME ), e.getMessage(  ) );
        }

        m_defaultOptionBuilder.withShortName( "j" );
        m_defaultOptionBuilder.create(  );
        m_defaultOptionBuilder.withLongName( "jkeyes" );
        m_defaultOptionBuilder.create(  );

        DefaultOptionBuilder builder = new DefaultOptionBuilder( "-", "--",
                    true );
        builder.withShortName( "mx" );
    }

    /**
     * DOCUMENT ME!
     */
    public void testName(  )
    {
        // withLongName && this.preferred != null
        //{
            m_defaultOptionBuilder.withShortName( "a" );
            m_defaultOptionBuilder.withLongName( "apples" );
        //}
        // withShortName && this.preferred != null
        //{
            m_defaultOptionBuilder.withLongName( "apples" );
            m_defaultOptionBuilder.withShortName( "a" );
        //}
        // withShortName && this.preferred != null
        //{
            m_defaultOptionBuilder.withLongName( "apples" );
            m_defaultOptionBuilder.withShortName( "a" );
        //}
    }

    /**
     * DOCUMENT ME!
     */
    public void testWithDescription(  )
    {
        String description = "desc";
        m_defaultOptionBuilder.withShortName( "a" );
        m_defaultOptionBuilder.withDescription( description );

        DefaultOption opt = m_defaultOptionBuilder.create(  );
        assertEquals( "wrong description found", description,
            opt.getDescription(  ) );
    }

    /**
     * DOCUMENT ME!
     */
     
    public void testWithRequired2(  )
    {
        doTestWithRequired1();
        doTestWithRequired2();
    }
    
    private void doTestWithRequired1(  )
    {
        boolean required = false;
        m_defaultOptionBuilder.withShortName( "a" );
        m_defaultOptionBuilder.withRequired( required );

        DefaultOption opt = m_defaultOptionBuilder.create(  );
        assertEquals( "wrong required found", required, opt.isRequired(  ) );
    }

    private void doTestWithRequired2(  )
    {
        boolean required = true;
        m_defaultOptionBuilder.withShortName( "a" );
        m_defaultOptionBuilder.withRequired( required );

        DefaultOption opt = m_defaultOptionBuilder.create(  );
        assertEquals( "wrong required found", required, opt.isRequired(  ) );
    }
    
    /**
     * DOCUMENT ME!
     */
    public void testWithChildren(  )
    {
        GroupBuilder gbuilder = new GroupBuilder(  );

        m_defaultOptionBuilder.withShortName( "a" );
        m_defaultOptionBuilder.withRequired( true );

        DefaultOption opt = m_defaultOptionBuilder.create(  );

        Group group = gbuilder.withName( "withchildren" ).withOption( opt )
                              .create(  );

        m_defaultOptionBuilder.withShortName( "b" );
        m_defaultOptionBuilder.withChildren( group );

        DefaultOption option = m_defaultOptionBuilder.create(  );
        assertEquals( "wrong children found", group, option.getChildren(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testWithArgument(  )
    {
        ArgumentBuilder abuilder = new ArgumentBuilder(  );
        abuilder.withName( "myarg" );

        Argument arg = abuilder.create(  );

        m_defaultOptionBuilder.withShortName( "a" );
        m_defaultOptionBuilder.withRequired( true );
        m_defaultOptionBuilder.withArgument( arg );

        DefaultOption opt = m_defaultOptionBuilder.create(  );

        assertEquals( "wrong argument found", arg, opt.getArgument(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testWithId(  )
    {
        m_defaultOptionBuilder.withShortName( "a" );
        m_defaultOptionBuilder.withId( 0 );

        DefaultOption opt = m_defaultOptionBuilder.create(  );

        assertEquals( "wrong id found", 0, opt.getId(  ) );
    }
}

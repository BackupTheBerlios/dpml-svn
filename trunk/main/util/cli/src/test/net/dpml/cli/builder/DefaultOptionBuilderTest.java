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
    private static final ResourceHelper resources = ResourceHelper.getResourceHelper(  );
    private DefaultOptionBuilder defaultOptionBuilder;

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
        this.defaultOptionBuilder = new DefaultOptionBuilder(  );
    }

    /*
     * Class to test for void DefaultOptionBuilder(String, String, boolean)
     */
    /**
     * DOCUMENT ME!
     */
    public void testNew_NullShortPrefix(  )
    {
        try
        {
            new DefaultOptionBuilder( null, null, false );
            fail( "null short prefix is not permitted" );
        }
        catch( IllegalArgumentException e )
        {
            assertEquals( resources.getMessage( 
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
    public void testNew_EmptyShortPrefix(  )
    {
        try
        {
            new DefaultOptionBuilder( "", null, false );
            fail( "empty short prefix is not permitted" );
        }
        catch( IllegalArgumentException e )
        {
            assertEquals( resources.getMessage( 
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
    public void testNew_NullLongPrefix(  )
    {
        try
        {
            new DefaultOptionBuilder( "-", null, false );
            fail( "null long prefix is not permitted" );
        }
        catch( IllegalArgumentException e )
        {
            assertEquals( resources.getMessage( 
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
    public void testNew_EmptyLongPrefix(  )
    {
        try
        {
            new DefaultOptionBuilder( "-", "", false );
            fail( "empty long prefix is not permitted" );
        }
        catch( IllegalArgumentException e )
        {
            assertEquals( resources.getMessage( 
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
            this.defaultOptionBuilder.create(  );
            fail( "options must have a name" );
        }
        catch( IllegalStateException e )
        {
            assertEquals( resources.getMessage( 
                    ResourceConstants.OPTION_NO_NAME ), e.getMessage(  ) );
        }

        this.defaultOptionBuilder.withShortName( "j" );
        this.defaultOptionBuilder.create(  );
        this.defaultOptionBuilder.withLongName( "jkeyes" );
        this.defaultOptionBuilder.create(  );

        {
            DefaultOptionBuilder builder = new DefaultOptionBuilder( "-", "--",
                    true );
            builder.withShortName( "mx" );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testName(  )
    {
        // withLongName && this.preferred != null
        {
            this.defaultOptionBuilder.withShortName( "a" );
            this.defaultOptionBuilder.withLongName( "apples" );
        }
        // withShortName && this.preferred != null
        {
            this.defaultOptionBuilder.withLongName( "apples" );
            this.defaultOptionBuilder.withShortName( "a" );
        }
        // withShortName && this.preferred != null
        {
            this.defaultOptionBuilder.withLongName( "apples" );
            this.defaultOptionBuilder.withShortName( "a" );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testWithDescription(  )
    {
        String description = "desc";
        this.defaultOptionBuilder.withShortName( "a" );
        this.defaultOptionBuilder.withDescription( description );

        DefaultOption opt = this.defaultOptionBuilder.create(  );
        assertEquals( "wrong description found", description,
            opt.getDescription(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testWithRequired(  )
    {
        {
            boolean required = false;
            this.defaultOptionBuilder.withShortName( "a" );
            this.defaultOptionBuilder.withRequired( required );

            DefaultOption opt = this.defaultOptionBuilder.create(  );
            assertEquals( "wrong required found", required, opt.isRequired(  ) );
        }

        {
            boolean required = true;
            this.defaultOptionBuilder.withShortName( "a" );
            this.defaultOptionBuilder.withRequired( required );

            DefaultOption opt = this.defaultOptionBuilder.create(  );
            assertEquals( "wrong required found", required, opt.isRequired(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testWithChildren(  )
    {
        GroupBuilder gbuilder = new GroupBuilder(  );

        this.defaultOptionBuilder.withShortName( "a" );
        this.defaultOptionBuilder.withRequired( true );

        DefaultOption opt = this.defaultOptionBuilder.create(  );

        Group group = gbuilder.withName( "withchildren" ).withOption( opt )
                              .create(  );

        {
            this.defaultOptionBuilder.withShortName( "b" );
            this.defaultOptionBuilder.withChildren( group );

            DefaultOption option = this.defaultOptionBuilder.create(  );
            assertEquals( "wrong children found", group, option.getChildren(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testWithArgument(  )
    {
        ArgumentBuilder abuilder = new ArgumentBuilder(  );
        abuilder.withName( "myarg" );

        Argument arg = abuilder.create(  );

        this.defaultOptionBuilder.withShortName( "a" );
        this.defaultOptionBuilder.withRequired( true );
        this.defaultOptionBuilder.withArgument( arg );

        DefaultOption opt = this.defaultOptionBuilder.create(  );

        assertEquals( "wrong argument found", arg, opt.getArgument(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testWithId(  )
    {
        this.defaultOptionBuilder.withShortName( "a" );
        this.defaultOptionBuilder.withId( 0 );

        DefaultOption opt = this.defaultOptionBuilder.create(  );

        assertEquals( "wrong id found", 0, opt.getId(  ) );
    }
}

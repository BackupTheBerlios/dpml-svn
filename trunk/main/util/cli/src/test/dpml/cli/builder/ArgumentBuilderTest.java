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
package dpml.cli.builder;

import junit.framework.TestCase;

import dpml.cli.option.ArgumentImpl;
import dpml.cli.resource.ResourceConstants;
import dpml.cli.resource.ResourceHelper;

//import dpml.cli.validation.DateValidator;
//import dpml.cli.validation.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
  */
public class ArgumentBuilderTest extends TestCase
{
    private static final ResourceHelper RESOURCES = ResourceHelper.getResourceHelper(  );
    private ArgumentBuilder m_argumentBuilder;

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
        this.m_argumentBuilder = new ArgumentBuilder(  );
    }

    /**
     * DOCUMENT ME!
     */
    public void testConsumeRemaining(  )
    {
        this.m_argumentBuilder.withConsumeRemaining( "--" );
        this.m_argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.m_argumentBuilder.create(  );

        assertEquals( "incorrect consume remaining token", "--",
            arg.getConsumeRemaining(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNullConsumeRemaining(  )
    {
        try
        {
            this.m_argumentBuilder.withConsumeRemaining( null );
            fail( "cannot use null consume remaining token" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                RESOURCES.getMessage( 
                    ResourceConstants.ARGUMENT_BUILDER_NULL_CONSUME_REMAINING ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testEmptyConsumeRemaining(  )
    {
        try
        {
            this.m_argumentBuilder.withConsumeRemaining( "" );
            fail( "cannot use empty string consume remaining token" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                RESOURCES.getMessage( 
                    ResourceConstants.ARGUMENT_BUILDER_EMPTY_CONSUME_REMAINING ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testDefault(  )
    {
        this.m_argumentBuilder.withDefault( "defaultString" );
        this.m_argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.m_argumentBuilder.create(  );

        assertEquals( "incorrect number of default values", 1,
            arg.getDefaultValues(  ).size(  ) );
        assertEquals( "incorrect default value", "defaultString",
            arg.getDefaultValues(  ).get( 0 ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testDefaultX2(  )
    {
        this.m_argumentBuilder.withDefault( "defaultString1" );
        this.m_argumentBuilder.withDefault( "defaultString2" );
        this.m_argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.m_argumentBuilder.create(  );

        assertEquals( "incorrect number of default values", 2,
            arg.getDefaultValues(  ).size(  ) );
        assertEquals( "incorrect default value-1", "defaultString1",
            arg.getDefaultValues(  ).get( 0 ) );
        assertEquals( "incorrect default value-2", "defaultString2",
            arg.getDefaultValues(  ).get( 1 ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNullDefault(  )
    {
        try
        {
            this.m_argumentBuilder.withDefault( null );
            fail( "cannot use null default" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                RESOURCES.getMessage( 
                    ResourceConstants.ARGUMENT_BUILDER_NULL_DEFAULT ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testDefaults(  )
    {
        final List defaults = new ArrayList(  );
        defaults.add( "one" );
        defaults.add( "two" );

        this.m_argumentBuilder.withDefaults( defaults );
        this.m_argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.m_argumentBuilder.create(  );

        assertEquals( "incorrect number of default values", 2,
            arg.getDefaultValues(  ).size(  ) );
        assertEquals( "incorrect default value-1", "one",
            arg.getDefaultValues(  ).get( 0 ) );
        assertEquals( "incorrect default value-2", "two",
            arg.getDefaultValues(  ).get( 1 ) );
        assertEquals( "incorrect default values list", defaults,
            arg.getDefaultValues(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNullDefaults(  )
    {
        try
        {
            this.m_argumentBuilder.withDefaults( null );
            fail( "cannot use null defaults" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                RESOURCES.getMessage( 
                    ResourceConstants.ARGUMENT_BUILDER_NULL_DEFAULTS ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testId(  )
    {
        this.m_argumentBuilder.withId( 1 );
        this.m_argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.m_argumentBuilder.create(  );

        assertEquals( "incorrect id", 1, arg.getId(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testInitialSeparator(  )
    {
        this.m_argumentBuilder.withInitialSeparator( ',' );
        this.m_argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.m_argumentBuilder.create(  );

        assertEquals( "incorrect initial separator", ',',
            arg.getInitialSeparator(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testMaximum(  )
    {
        this.m_argumentBuilder.withMaximum( 1 );
        this.m_argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.m_argumentBuilder.create(  );

        assertEquals( "incorrect maximum", 1, arg.getMaximum(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNegativeMaximum(  )
    {
        try
        {
            this.m_argumentBuilder.withMaximum( -1 );
            fail( "cannot use negative maximum" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                RESOURCES.getMessage( 
                    ResourceConstants.ARGUMENT_BUILDER_NEGATIVE_MAXIMUM ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testMinimum(  )
    {
        this.m_argumentBuilder.withMinimum( 1 );
        this.m_argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.m_argumentBuilder.create(  );

        assertEquals( "incorrect maximum", 1, arg.getMinimum(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNegativeMinimum(  )
    {
        try
        {
            this.m_argumentBuilder.withMinimum( -1 );
            fail( "cannot use negative minimum" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                RESOURCES.getMessage( 
                    ResourceConstants.ARGUMENT_BUILDER_NEGATIVE_MINIMUM ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testName(  )
    {
        this.m_argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.m_argumentBuilder.create(  );

        assertEquals( "incorrect preferred name", "arg",
            arg.getPreferredName(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNullName(  )
    {
        try
        {
            this.m_argumentBuilder.withName( null );
            fail( "cannot use null name" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                RESOURCES.getMessage( 
                    ResourceConstants.ARGUMENT_BUILDER_NULL_NAME ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testEmptyName(  )
    {
        try
        {
            this.m_argumentBuilder.withName( "" );
            fail( "cannot use empty name" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                RESOURCES.getMessage( 
                    ResourceConstants.ARGUMENT_BUILDER_EMPTY_NAME ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testSubsequentSeparator(  )
    {
        this.m_argumentBuilder.withSubsequentSeparator( ':' );
        this.m_argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.m_argumentBuilder.create(  );

        assertEquals( "incorrect subsequent separator", ':',
            arg.getSubsequentSeparator(  ) );
    }

    //public void testValidator() {
    /**
     * DOCUMENT ME!
     */
    public void testNullValidator(  )
    {
        try
        {
            this.m_argumentBuilder.withValidator( null );
            fail( "cannot use null validator" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                RESOURCES.getMessage( 
                    ResourceConstants.ARGUMENT_BUILDER_NULL_VALIDATOR ),
                exp.getMessage(  ) );
        }
    }
}

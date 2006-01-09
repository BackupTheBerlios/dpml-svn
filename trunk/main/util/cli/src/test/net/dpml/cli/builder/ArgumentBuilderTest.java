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

import net.dpml.cli.option.ArgumentImpl;
import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

//import net.dpml.cli.validation.DateValidator;
import net.dpml.cli.validation.Validator;

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
    private static final ResourceHelper resources = ResourceHelper.getResourceHelper(  );
    private ArgumentBuilder argumentBuilder;

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
        this.argumentBuilder = new ArgumentBuilder(  );
    }

    /**
     * DOCUMENT ME!
     */
    public void testConsumeRemaining(  )
    {
        this.argumentBuilder.withConsumeRemaining( "--" );
        this.argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.argumentBuilder.create(  );

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
            this.argumentBuilder.withConsumeRemaining( null );
            fail( "cannot use null consume remaining token" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                resources.getMessage( 
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
            this.argumentBuilder.withConsumeRemaining( "" );
            fail( "cannot use empty string consume remaining token" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                resources.getMessage( 
                    ResourceConstants.ARGUMENT_BUILDER_EMPTY_CONSUME_REMAINING ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testDefault(  )
    {
        this.argumentBuilder.withDefault( "defaultString" );
        this.argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.argumentBuilder.create(  );

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
        this.argumentBuilder.withDefault( "defaultString1" );
        this.argumentBuilder.withDefault( "defaultString2" );
        this.argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.argumentBuilder.create(  );

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
            this.argumentBuilder.withDefault( null );
            fail( "cannot use null default" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                resources.getMessage( 
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

        this.argumentBuilder.withDefaults( defaults );
        this.argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.argumentBuilder.create(  );

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
            this.argumentBuilder.withDefaults( null );
            fail( "cannot use null defaults" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                resources.getMessage( 
                    ResourceConstants.ARGUMENT_BUILDER_NULL_DEFAULTS ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testId(  )
    {
        this.argumentBuilder.withId( 1 );
        this.argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.argumentBuilder.create(  );

        assertEquals( "incorrect id", 1, arg.getId(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testInitialSeparator(  )
    {
        this.argumentBuilder.withInitialSeparator( ',' );
        this.argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.argumentBuilder.create(  );

        assertEquals( "incorrect initial separator", ',',
            arg.getInitialSeparator(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testMaximum(  )
    {
        this.argumentBuilder.withMaximum( 1 );
        this.argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.argumentBuilder.create(  );

        assertEquals( "incorrect maximum", 1, arg.getMaximum(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNegativeMaximum(  )
    {
        try
        {
            this.argumentBuilder.withMaximum( -1 );
            fail( "cannot use negative maximum" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                resources.getMessage( 
                    ResourceConstants.ARGUMENT_BUILDER_NEGATIVE_MAXIMUM ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testMinimum(  )
    {
        this.argumentBuilder.withMinimum( 1 );
        this.argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.argumentBuilder.create(  );

        assertEquals( "incorrect maximum", 1, arg.getMinimum(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNegativeMinimum(  )
    {
        try
        {
            this.argumentBuilder.withMinimum( -1 );
            fail( "cannot use negative minimum" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                resources.getMessage( 
                    ResourceConstants.ARGUMENT_BUILDER_NEGATIVE_MINIMUM ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testName(  )
    {
        this.argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.argumentBuilder.create(  );

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
            this.argumentBuilder.withName( null );
            fail( "cannot use null name" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                resources.getMessage( 
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
            this.argumentBuilder.withName( "" );
            fail( "cannot use empty name" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                resources.getMessage( 
                    ResourceConstants.ARGUMENT_BUILDER_EMPTY_NAME ),
                exp.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testSubsequentSeparator(  )
    {
        this.argumentBuilder.withSubsequentSeparator( ':' );
        this.argumentBuilder.withName( "arg" );

        ArgumentImpl arg = (ArgumentImpl) this.argumentBuilder.create(  );

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
            this.argumentBuilder.withValidator( null );
            fail( "cannot use null validator" );
        }
        catch( IllegalArgumentException exp )
        {
            assertEquals( "wrong exception message",
                resources.getMessage( 
                    ResourceConstants.ARGUMENT_BUILDER_NULL_VALIDATOR ),
                exp.getMessage(  ) );
        }
    }
}

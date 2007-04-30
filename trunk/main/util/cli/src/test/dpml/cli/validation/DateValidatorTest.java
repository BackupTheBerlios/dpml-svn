/*
 * Copyright 2003-2005 The Apache Software Foundation
 * Copyright 2005-2007 Stephen McConnell
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
package dpml.cli.validation;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import dpml.cli.resource.ResourceConstants;
import dpml.cli.resource.ResourceHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * JUnit test case for DateValidator.
 *
 * @author Rob Oxspring
 * @author John Keyes
 */
public class DateValidatorTest extends TestCase
{
    private static final ResourceHelper RESOURCES = ResourceHelper.getResourceHelper(  );

    /**
     * D M YY.
     */
    public static final DateFormat D_M_YY = new SimpleDateFormat( "d/M/yy" );

    /**
     * YYYY_MM_YY
     */
    public static final DateFormat YYYY_MM_YY = new SimpleDateFormat( "yyyy-MM-dd" );
    
    private List m_formats = Arrays.asList( new Object[]{D_M_YY, YYYY_MM_YY} );

    /**
     * Test single format validation.
     *
     * @throws InvalidArgumentException if an error occurs
     */
    public void testSingleFormatValidate(  ) throws InvalidArgumentException
    {
        final Object[] array = new Object[]{"23/12/03"};
        final List list = Arrays.asList( array );
        final Validator validator = new DateValidator( D_M_YY );

        validator.validate( list );

        final Iterator i = list.iterator(  );
        assertEquals( "2003-12-23", YYYY_MM_YY.format( (Date) i.next(  ) ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * Test default format validation.
     *
     * @throws InvalidArgumentException if an error occurs
     */
    public void testDefaultDateFormatValidate(  )
        throws InvalidArgumentException
    {
        final Date now = new Date(  );
        final String date = DateFormat.getDateInstance(  ).format( now );
        final DateFormat df = new SimpleDateFormat( "yyyy/M/dd" );
        final String formatted = df.format( now );
        final Object[] array = new Object[]{date};
        final List list = Arrays.asList( array );
        final Validator validator = DateValidator.getDateInstance(  );

        validator.validate( list );

        final Iterator i = list.iterator(  );
        assertEquals( formatted, df.format( (Date) i.next(  ) ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * DTest default time format validation.
     *
     * @throws InvalidArgumentException if an error occurs
     */
    public void testDefaultTimeFormatValidate(  )
        throws InvalidArgumentException
    {
        final Date now = new Date(  );
        final String time = DateFormat.getTimeInstance(  ).format( now );
        final DateFormat df = new SimpleDateFormat( "HH:mm:ss" );
        final String formatted = df.format( now );
        final Object[] array = new Object[]{time};
        final List list = Arrays.asList( array );
        final Validator validator = DateValidator.getTimeInstance(  );

        validator.validate( list );

        final Iterator i = list.iterator(  );
        assertEquals( formatted, df.format( (Date) i.next(  ) ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * Test default date and time format validation.
     *
     * @throws InvalidArgumentException if an error occurs
     */
    public void testDefaultDateTimeFormatValidate(  )
        throws InvalidArgumentException
    {
        final Date now = new Date(  );
        final String date = DateFormat.getDateTimeInstance(  ).format( now );
        final DateFormat df = new SimpleDateFormat( "yyyy/M/dd HH:mm:ss" );
        final String formatted = df.format( now );
        final Object[] array = new Object[]{date};
        final List list = Arrays.asList( array );
        final Validator validator = DateValidator.getDateTimeInstance(  );

        validator.validate( list );

        final Iterator i = list.iterator(  );
        assertEquals( formatted, df.format( (Date) i.next(  ) ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * Test default validator.
     *
     * @throws InvalidArgumentException if an error occurs
     */
    public void testDefaultValidator(  ) throws InvalidArgumentException
    {
        final Date now = new Date(  );
        final String date = DateFormat.getInstance(  ).format( now );
        final DateFormat df = new SimpleDateFormat( "yyyy/M/dd HH:mm" );
        final String formatted = df.format( now );
        final Object[] array = new Object[]{date};
        final List list = Arrays.asList( array );
        final Validator validator = new DateValidator(  );

        validator.validate( list );

        final Iterator i = list.iterator(  );
        assertEquals( formatted, df.format( (Date) i.next(  ) ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * General validation test.
     *
     * @throws InvalidArgumentException if an error occurs
     */
    public void testValidate(  ) throws InvalidArgumentException
    {
        final Object[] array = new Object[]{"23/12/03", "2002-10-12"};
        final List list = Arrays.asList( array );
        final Validator validator = new DateValidator( m_formats );

        validator.validate( list );

        final Iterator i = list.iterator(  );
        assertEquals( "2003-12-23", YYYY_MM_YY.format( (Date) i.next(  ) ) );
        assertEquals( "2002-10-12", YYYY_MM_YY.format( (Date) i.next(  ) ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * Test minimum bounds.
     *
     * @throws InvalidArgumentException if an error occurs
     */
    public void testMinimumBounds(  ) throws InvalidArgumentException
    {
        final DateValidator validator = new DateValidator( m_formats );
        final Calendar cal = Calendar.getInstance(  );

        if( true )
        {
            final Object[] array = new Object[]{"23/12/03", "2002-10-12"};
            final List list = Arrays.asList( array );
            cal.set( 2002, 1, 12 );

            final Date min = cal.getTime(  );
            validator.setMinimum( min );
            assertTrue( "maximum bound is set", validator.getMaximum(  ) == null );
            assertEquals( "minimum bound is incorrect", min,
                validator.getMinimum(  ) );
            validator.validate( list );
        }

        if( true )
        {
            final Object[] array = new Object[]{"23/12/03", "2002-10-12"};
            final List list = Arrays.asList( array );
            cal.set( 2003, 1, 12 );

            final Date min = cal.getTime(  );
            validator.setMinimum( min );

            try
            {
                validator.validate( list );
                fail( "minimum out of bounds exception not caught" );
            }
            catch( final InvalidArgumentException exp )
            {
                assertEquals( RESOURCES.getMessage( 
                        ResourceConstants.DATEVALIDATOR_DATE_OUTOFRANGE,
                        new Object[]{"2002-10-12"} ), exp.getMessage(  ) );
            }
        }
    }

    /**
     * Test formats.
     *
     * @throws InvalidArgumentException if an error occurs
     */
    public void testFormats(  ) throws InvalidArgumentException
    {
        final DateValidator validator = new DateValidator( m_formats );
        assertEquals( "date format is incorrect",
            ( (SimpleDateFormat) m_formats.get( 0 ) ).toPattern(  ),
            ( (SimpleDateFormat) validator.getFormats(  )[0] ).toPattern(  ) );
        assertEquals( "date format is incorrect",
            ( (SimpleDateFormat) m_formats.get( 1 ) ).toPattern(  ),
            ( (SimpleDateFormat) validator.getFormats(  )[1] ).toPattern(  ) );
    }

    /**
     * Test maximum bounds.
     *
     * @throws InvalidArgumentException if an error occurs
     */
    public void testMaximumBounds(  ) throws InvalidArgumentException
    {
        final DateValidator validator = new DateValidator( m_formats );
        final Calendar cal = Calendar.getInstance(  );

        if( true )
        {
            final Object[] array = new Object[]{"23/12/03", "2002-10-12"};
            final List list = Arrays.asList( array );
            cal.set( 2004, 1, 12 );

            final Date max = cal.getTime(  );
            validator.setMaximum( max );
            assertTrue( "minimum bound is set", validator.getMinimum(  ) == null );
            assertEquals( "maximum bound is incorrect", max,
                validator.getMaximum(  ) );
            validator.validate( list );
        }

        if( true )
        {
            final Object[] array = new Object[]{"23/12/03", "2004-10-12"};
            final List list = Arrays.asList( array );
            cal.set( 2004, 1, 12 );

            final Date max = cal.getTime(  );
            validator.setMaximum( max );

            try
            {
                validator.validate( list );
                fail( "maximum out of bounds exception not caught" );
            }
            catch( final InvalidArgumentException exp )
            {
                assertEquals( RESOURCES.getMessage( 
                        ResourceConstants.DATEVALIDATOR_DATE_OUTOFRANGE,
                        new Object[]{"2004-10-12"} ), exp.getMessage(  ) );
            }
        }
    }

    /**
     * Return the test source.
     * @return the test
     */
    public static Test suite(  )
    {
        Test result = new TestSuite( DateValidatorTest.class ); // default behavior
        result = new TimeZoneTestSuite( "EST", result ); // ensure it runs in EST timezone
        return result;
    }
}

/*
 * Copyright 2003-2005 The Apache Software Foundation
 * Copyright 2005 Stephen McConnell
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
package net.dpml.cli.validation;

import java.text.DateFormat;
import java.text.ParsePosition;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

/**
 * The <code>DateValidator</code> validates the argument values
 * are date or time value(s).
 *
 * The following example shows how to validate that
 * an argument value(s) is a Date of the following
 * type: d/M/yy (see {@link java.text.DateFormat}).
 *
 * <pre>
 * DateFormat date = new SimpleDateFormat("d/M/yy");
 * ...
 * ArgumentBuilder builder = new ArgumentBuilder();
 * Argument dateFormat =
 *     builder.withName("date");
 *            .withValidator(new DateValidator(dateFormat));
 * </pre>
 *
 * The following example shows how to validate that
 * an argument value(s) is a time of the following
 * type: HH:mm:ss (see {@link java.text.DateFormat}).
 *
 * <pre>
 * DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
 * ...
 * ArgumentBuilder builder = new ArgumentBuilder();
 * Argument time =
 *     builder.withName("time");
 *            .withValidator(new DateValidator(timeFormat));
 * </pre>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 * @see java.text.DateFormat
 */
public class DateValidator implements Validator 
{
    /** i18n */
    private static final ResourceHelper RESOURCES = 
      ResourceHelper.getResourceHelper();

    /** an array of permitted DateFormats */
    private DateFormat[] m_formats;

    /** minimum Date allowed i.e: a valid date occurs later than this date */
    private Date m_minimum;

    /** maximum Date allowed i.e: a valid date occurs earlier than this date */
    private Date m_maximum;

    /** leniant parsing */
    private boolean m_isLenient;

    /**
     * Creates a Validator for the default date/time format
     */
    public DateValidator() 
    {
        this( DateFormat.getInstance() );
    }

    /**
     * Creates a Validator for the specified DateFormat.
     *
     * @param format
     *            a DateFormat which dates must conform to
     */
    public DateValidator( final DateFormat format )
    {
        setFormat( format );
    }

    /**
     * Creates a Validator for the List of specified DateFormats.
     *
     * @param formats a List of DateFormats which dates must conform to
     */
    public DateValidator( final List formats )
    {
        for( Iterator iter = formats.iterator(); iter.hasNext();)
        {
            DateFormat format = (DateFormat) iter.next();
        }
        setFormats( formats );
    }

    /**
     * Creates a Validator for dates.
     *
     * @return DateValidator a Validator for dates
     */
    public static DateValidator getDateInstance()
    {
        return new DateValidator( DateFormat.getDateInstance() );
    }

    /**
     * Creates a Validator for times.
     *
     * @return DateValidator a Validator for times
     */
    public static DateValidator getTimeInstance()
    {
        return new DateValidator( DateFormat.getTimeInstance() );
    }

    /**
     * Creates a Validator for date/times
     *
     * @return DateValidator a Validator for date/times
     */
    public static DateValidator getDateTimeInstance()
    {
        return new DateValidator( DateFormat.getDateTimeInstance() );
    }

   /**
    * Validate each String value in the specified List against this instances
    * permitted DateFormats.
    *
    * If a value is valid then it's <code>String</code> value in the list is
    * replaced with it's <code>Date</code> value.
    *
    * @param values the list of values to validate 
    * @exception InvalidArgumentException if a value is invalid
    * @see net.dpml.cli.validation.Validator#validate(java.util.List)
    */
    public void validate( final List values ) throws InvalidArgumentException
    {
        // for each value
        for( final ListIterator i = values.listIterator(); i.hasNext();) 
        {
            final Object next = i.next();
            if( next instanceof Date )
            {
                return;
            }
        
            final String value = (String) next;

            Date date = null;

            // create a resuable ParsePosition instance
            final ParsePosition pp = new ParsePosition( 0 );

            // for each permitted DateFormat
            for( int f=0; ( f<m_formats.length ) && ( date == null ); ++f )
            {
                // reset the parse position
                pp.setIndex( 0 );
                date = m_formats[f].parse( value, pp );

                // if the wrong number of characters have been parsed
                if( pp.getIndex() < value.length() )
                {
                    date = null;
                }
            }

            // if date has not been set throw an InvalidArgumentException
            if( date == null )
            {
                throw new InvalidArgumentException( value );
            }

            // if the date is outside the bounds
            if( isDateEarlier( date ) || isDateLater( date ) )
            {
                throw new InvalidArgumentException(
                  RESOURCES.getMessage(
                    ResourceConstants.DATEVALIDATOR_DATE_OUTOFRANGE,
                    value ) );
            }

            // replace the value in the list with the actual Date
            i.set( date );
        }
    }

   /**
    * Set the leaniant flag.
    * @param lenient true if leniant
    */
    public void setLeniant( final boolean lenient )
    {
        for( int i=0; i<m_formats.length; i++ )
        {
            m_formats[i].setLenient( lenient );
        }
        m_isLenient = lenient;
    }

   /**
    * Return the leaniant flag.
    * @return true if leniant
    */
    public boolean isLeniant() 
    {
        return m_isLenient;
    }

    /**
     * Returns the maximum date permitted.
     *
     * @return Date the maximum date permitted. If no maximum date has been
     *         specified then return <code>null</code>.
     */
    public Date getMaximum()
    {
        return m_maximum;
    }

    /**
     * Sets the maximum Date to the specified value.
     *
     * @param maximum
     *            the maximum Date permitted
     */
    public void setMaximum( final Date maximum )
    {
        m_maximum = maximum;
    }

    /**
     * Returns the minimum date permitted.
     *
     * @return Date the minimum date permitted. If no minimum date has been
     *         specified then return <code>null</code>.
     */
    public Date getMinimum()
    {
        return m_minimum;
    }

    /**
     * Sets the minimum Date to the specified value.
     *
     * @param minimum
     *            the minimum Date permitted
     */
    public void setMinimum( Date minimum )
    {
        m_minimum = minimum;
    }

    /**
     * Returns whether the specified Date is later than the maximum date.
     *
     * @param date
     *            the Date to evaluate
     *
     * @return boolean whether <code>date</code> is earlier than the maximum
     *         date
     */
    private boolean isDateLater( Date date )
    {
        return ( m_maximum != null ) && ( date.getTime() > m_maximum.getTime() );
    }

    /**
     * Returns whether the specified Date is earlier than the minimum date.
     *
     * @param date the Date to evaluate
     * @return boolean whether <code>date</code> is earlier than the minimum
     *         date
     */
    private boolean isDateEarlier( Date date )
    {
        return ( m_minimum != null ) && ( date.getTime() < m_minimum.getTime() );
    }

    /**
     * Sets the date format permitted.
     *
     * @param format
     *              the format to use
     */
    public void setFormat( final DateFormat format )
    {
        setFormats( new DateFormat[]{format} );
    }

    /**
     * Sets the date formats permitted.
     *
     * @param formats
     *               the List of DateFormats to use
     */
    public void setFormats( final List formats )
    {
        setFormats( (DateFormat[]) formats.toArray( new DateFormat[formats.size()] ) );
    }

    /**
     * Sets the date formats permitted.
     *
     * @param formats the array of DateFormats to use
     */
    public void setFormats( final DateFormat[] formats )
    {
        m_formats = formats;
        setLeniant( m_isLenient );
    }

    /**
     * Gets the date formats permitted.
     *
     * @return the permitted formats
     */
    public DateFormat[] getFormats()
    {
        return m_formats;
    }
}

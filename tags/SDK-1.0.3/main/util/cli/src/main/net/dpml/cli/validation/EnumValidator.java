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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

/**
 * The <code>EnumValidator</code> validates the string argument
 * values are valid.
 *
 * The following example shows how to limit the valid values
 * for the color argument to 'red', 'green', or 'blue'.
 *
 * <pre>
 * Set values = new HashSet();
 * values.add("red");
 * values.add("green");
 * values.add("blue");
 * ...
 * ArgumentBuilder builder = new ArgumentBuilder();
 * Argument color =
 *     builder.withName("color");
 *            .withValidator(new EnumValidator(values));
 * </pre>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class EnumValidator implements Validator
{
    /** List of permitted values */
    private Set m_validValues;

    /**
     * Creates a new EnumValidator for the specified values.
     *
     * @param values The list of permitted values
     */
    public EnumValidator( final Set values )
    {
        setValidValues( values );
    }

   /**
    * Validate the list of values against the list of permitted values.
    *
    * @param values the list of values to validate 
    * @exception InvalidArgumentException if a value is invalid
    * @see net.dpml.cli.validation.Validator#validate(java.util.List)
    */
    public void validate( final List values ) throws InvalidArgumentException
    {
        for( final Iterator iter = values.iterator(); iter.hasNext();) 
        {
            final String value = (String) iter.next();
            if( !m_validValues.contains( value ) )
            {
                throw new InvalidArgumentException(
                  ResourceHelper.getResourceHelper().getMessage(
                    ResourceConstants.ENUM_ILLEGAL_VALUE,
                    new Object[]{value, getValuesAsString()} ) );
            }
        }
    }

    /**
     * Returns the permitted values in a comma separated String
     *
     * @return String formatted list of values
     */
    String getValuesAsString()
    {
        final StringBuffer buff = new StringBuffer();
        buff.append( "[" );
        for( final Iterator iter = m_validValues.iterator(); iter.hasNext();)
        {
            buff.append( "'" ).append( iter.next() ).append( "'" );
            if( iter.hasNext() )
            {
                buff.append( ", " );
            }
        }
        buff.append( "]" );
        return buff.toString();
    }

    /**
     * Returns the Set of valid argument values.
     *
     * @return Returns the Set of valid argument values.
     */
    public Set getValidValues()
    {
        return m_validValues;
    }

    /**
     * Specifies the Set of valid argument values.
     *
     * @param validValues The Set of valid argument values.
     */
    protected void setValidValues( Set validValues )
    {
        m_validValues = validValues;
    }
}

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
package net.dpml.cli.commandline;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.dpml.cli.CommandLine;
import net.dpml.cli.Option;
import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

/**
 * Instances of CommandLine represent a command line that has been processed
 * according to the definition supplied to the parser.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class CommandLineImpl implements CommandLine
{
    /**
     * Detects the presence of an option with the specified trigger in this 
     * CommandLine.
     * 
     * @param trigger the trigger to search for
     * @return true iff an option with this trigger is present
     */
    public final boolean hasOption( final String trigger )
    {
        return hasOption( getOption( trigger ) );
    }

    /**
     * Retrieves the Argument values associated with the specified Option
     * 
     * @param trigger a trigger used to lookup the Option
     * @return a list of values or an empty List if none are found
     */
    public final List getValues( final String trigger )
    {
        return getValues( getOption( trigger ), Collections.EMPTY_LIST );
    }

    /**
     * Retrieves the Argument values associated with the specified Option
     * 
     * @param trigger a trigger used to lookup the Option
     * @param defaultValues the result to return if no values are found
     * @return a list of values or defaultValues if none are found
     */
    public final List getValues(
      final String trigger, final List defaultValues )
    {
        return getValues( getOption( trigger ), defaultValues );
    }

    /**
     * Retrieves the Argument values associated with the specified Option
     * 
     * @param option the Option associated with the values
     * @return a list of values or an empty List if none are found
     */
    public final List getValues( final Option option )
    {
        return getValues( option, Collections.EMPTY_LIST );
    }

    /**
     * Retrieves the single Argument value associated with the specified Option
     * 
     * @param trigger a trigger used to lookup the Option
     * @return the matching value or null if none exists
     * @throws IllegalStateException if more than one values are found
     */
    public final Object getValue( final String trigger ) throws IllegalStateException
    {
        return getValue( getOption( trigger ), null );
    }

    /**
     * Retrieves the single Argument value associated with the specified Option
     * 
     * @param trigger a trigger used to lookup the Option
     * @param defaultValue the result to use if no values are found
     * @return the matching value or defaultValue if none exists
     * @throws IllegalStateException if more than one values are found
     */
    public final Object getValue(
      final String trigger, final Object defaultValue ) throws IllegalStateException
    {
        return getValue( getOption( trigger ), defaultValue );
    }

    /**
     * Retrieves the single Argument value associated with the specified Option
     * 
     * @param option the Option associated with the value
     * @return the matching value or null if none exists
     * @throws IllegalStateException if more than one values are found
     */
    public final Object getValue( final Option option ) throws IllegalStateException
    {
        return getValue( option, null );
    }

    /**
     * Retrieves the single Argument value associated with the specified Option
     * 
     * @param option the Option associated with the value
     * @param defaultValue the result to use if no values are found
     * @return the matching value or defaultValue if none exists
     * @throws IllegalStateException if more than one value is found
     */
    public final Object getValue( final Option option, final Object defaultValue )
      throws IllegalStateException
    {
        final List values;
        if( defaultValue == null )
        {
            values = getValues( option );
        }
        else
        {
            values = getValues( option, Collections.singletonList( defaultValue ) );
        }
        if( values.size() > 1 )
        {
            throw new IllegalStateException(
              ResourceHelper.getResourceHelper().getMessage(
                ResourceConstants.ARGUMENT_TOO_MANY_VALUES ) );
        }
        if( values.isEmpty() )
        {
            return defaultValue;
        }
        return values.get( 0 );
    }

    /**
     * Retrieves the Boolean value associated with the specified Switch
     * 
     * @param trigger a trigger used to lookup the Option
     * @return the Boolean associated with trigger or null if none exists
     */
    public final Boolean getSwitch( final String trigger )
    {
        return getSwitch( getOption( trigger ), null );
    }

    /**
     * Retrieves the Boolean value associated with the specified Switch
     * 
     * @param trigger a trigger used to lookup the Option
     * @param defaultValue the Boolean to use if none match
     * @return the Boolean associated with trigger or defaultValue if none exists
     */
    public final Boolean getSwitch(
      final String trigger, final Boolean defaultValue )
    {
        return getSwitch( getOption( trigger ), defaultValue );
    }

    /**
     * Retrieves the Boolean value associated with the specified Switch
     * 
     * @param option the Option associated with the value
     * @return the Boolean associated with option or null if none exists
     */
    public final Boolean getSwitch( final Option option )
    {
        return getSwitch( option, null );
    }

    /**
     * Retrieves the value associated with the specified property 
     * 
     * @param property the property name to lookup
     * @return the value of the property or null
     */
    public final String getProperty( final String property )
    {
        return getProperty( property, null );
    }

    /**
     * Retrieves the number of times the specified Option appeared in this 
     * CommandLine
     * 
     * @param trigger a trigger used to lookup the Option
     * @return the number of occurrences of the option
     */
    public final int getOptionCount( final String trigger )
    {
        return getOptionCount( getOption( trigger ) );
    }

    /**
     * Retrieves the number of times the specified Option appeared in this 
     * CommandLine
     * 
     * @param option the Option associated to check
     * @return the number of occurrences of the option
     */
    public final int getOptionCount( final Option option )
    {
        if( option == null )
        {
            return 0;
        }
        int count = 0;
        for( Iterator i = getOptions().iterator(); i.hasNext();) 
        {
            if( option.equals( i.next() ) )
            {
                ++count;
            }
        }
        return count;
    }
}

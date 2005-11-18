/**
 * Copyright 2004 The Apache Software Foundation
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.dpml.cli.CommandLine;
import net.dpml.cli.Option;

/**
 * Manages a queue of default CommandLines. This CommandLine implementation is
 * backed by a queue of CommandLine instances which are queried in turn until a
 * suitable result is found.
 * 
 * CommandLine instances can either be added to the back of the queue or can be
 * pushed in at a specific position.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 * @see #appendCommandLine(CommandLine)
 * @see #insertCommandLine(int, CommandLine)
 */
public class DefaultingCommandLine extends CommandLineImpl
{
    /**
     * The list of default CommandLine instances
     */
    private final List m_commandLines = new ArrayList();

    /**
     * Adds a CommandLine instance to the back of the queue. The supplied
     * CommandLine will be used as defaults when all other CommandLines produce
     * no result
     * 
     * @param commandLine the default values to use if all CommandLines
     */
    public void appendCommandLine( final CommandLine commandLine )
    {
        m_commandLines.add( commandLine );
    }
    
    /**
     * Adds a CommandLine instance to a specified position in the queue.
     * 
     * @param index ths position at which to insert
     * @param commandLine the CommandLine to insert
     */
    public void insertCommandLine(
        final int index,
        final CommandLine commandLine )
    {
        m_commandLines.add( index, commandLine );
    }
    
    /**
     * Builds an iterator over the build in CommandLines.
     * 
     * @return an unmodifiable iterator
     */
    public Iterator commandLines()
    {
        return Collections.unmodifiableList( m_commandLines ).iterator();
    }

    /**
     * Finds the Option with the specified trigger
     * 
     * @param trigger the name of the option to retrieve
     * @return the Option matching the trigger or null if none exists
     */
    public Option getOption( String trigger )
    {
        for( final Iterator i = m_commandLines.iterator(); i.hasNext();)
        {
            final CommandLine commandLine = (CommandLine) i.next();
            final Option actual = commandLine.getOption( trigger );
            if( actual != null )
            {
                return actual;
            }
        }
        return null;
    }

    /**
     * Retrieves a list of all Options found in this CommandLine
     * 
     * @return a none null list of Options
     */
    public List getOptions()
    {
        final List options = new ArrayList();
        final List temp = new ArrayList();
        for( final Iterator i = m_commandLines.iterator(); i.hasNext();)
        {
            final CommandLine commandLine = (CommandLine) i.next();
            temp.clear();
            temp.addAll( commandLine.getOptions() );
            temp.removeAll( options );
            options.addAll( temp );
        }
        return Collections.unmodifiableList( options );
    }

    /**
     * Retrieves a list of all Option triggers found in this CommandLine
     * 
     * @return a none null list of Option triggers
     */
    public Set getOptionTriggers()
    {
        final Set all = new HashSet();
        for( final Iterator i = m_commandLines.iterator(); i.hasNext();)
        {
            final CommandLine commandLine = (CommandLine) i.next();
            all.addAll( commandLine.getOptionTriggers() );
        }
        return Collections.unmodifiableSet( all );
    }

    /**
     * Detects the presence of an option in this CommandLine.
     * 
     * @param option the Option to search for
     * @return true iff the option is present
     */
    public boolean hasOption( Option option )
    {
        for( final Iterator i = m_commandLines.iterator(); i.hasNext();)
        {
            final CommandLine commandLine = (CommandLine) i.next();
            if( commandLine.hasOption( option ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the Argument values associated with the specified Option
     * 
     * @param option the Option associated with the values
     * @param defaultValues the result to return if no values are found
     * @return a list of values or defaultValues if none are found
     */
    public List getValues( Option option, List defaultValues )
    {
        for( final Iterator i = m_commandLines.iterator(); i.hasNext();)
        {
            final CommandLine commandLine = (CommandLine) i.next();
            final List actual = commandLine.getValues( option );
            if( actual != null && !actual.isEmpty() )
            {
                return actual;
            }
        }
        if( defaultValues == null )
        {
            return Collections.EMPTY_LIST;
        }
        else
        {
            return defaultValues;
        }
    }

    /**
     * Retrieves the Boolean value associated with the specified Switch
     * 
     * @param option the Option associated with the value
     * @param defaultValue the Boolean to use if none match
     * @return the Boolean associated with option or defaultValue if none exists
     */
    public Boolean getSwitch( Option option, Boolean defaultValue )
    {
        for( final Iterator i = m_commandLines.iterator(); i.hasNext();)
        {
            final CommandLine commandLine = (CommandLine) i.next();
            final Boolean actual = commandLine.getSwitch( option );
            if( actual != null )
            {
                return actual;
            }
        }
        return defaultValue;
    }

    /**
     * Retrieves the value associated with the specified property 
     * 
     * @param property the property name to lookup
     * @param defaultValue the value to use if no other is found
     * @return the value of the property or defaultValue
     */
    public String getProperty( String property, String defaultValue )
    {
        for( final Iterator i = m_commandLines.iterator(); i.hasNext();)
        {
            final CommandLine commandLine = (CommandLine) i.next();
            final String actual = commandLine.getProperty( property );
            if( actual != null )
            {
                return actual;
            }
        }
        return defaultValue;
    }

    /**
     * Retrieves the set of all property names associated with this CommandLine
     * 
     * @return a none null set of property names 
     */
    public Set getProperties() 
    {
        final Set all = new HashSet();
        for( final Iterator i = m_commandLines.iterator(); i.hasNext();)
        {
            final CommandLine commandLine = (CommandLine) i.next();
            all.addAll( commandLine.getProperties() );
        }
        return Collections.unmodifiableSet( all );
    }
}

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
package net.dpml.cli.option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import net.dpml.cli.Argument;
import net.dpml.cli.DisplaySetting;
import net.dpml.cli.Group;
import net.dpml.cli.OptionException;
import net.dpml.cli.WriteableCommandLine;
import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

/**
 * Represents a cvs "update" style command line option.
 *
 * Like all Parents, Commands can have child options and can be part of
 * Arguments.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Command extends ParentImpl
{
    /** The display name for the command */
    private final String m_preferredName;

    /** The aliases for this command */
    private final Set m_aliases;

    /** All the names for this command */
    private final Set m_triggers;

    /**
     * Creates a new Command instance.
     *
     * @param preferredName the name normally used to refer to the Command
     * @param description a description of the Command
     * @param aliases alternative names for the Command
     * @param required true if the Command is required
     * @param argument an Argument that the command takes
     * @param children the Group of child options for this Command
     * @param id a unique id for the Command
     * @see ParentImpl#ParentImpl(Argument, Group, String, int, boolean)
     */
    public Command(
      final String preferredName, final String description, final Set aliases, 
      final boolean required, final Argument argument, final Group children, final int id )
    {
        super( argument, children, description, id, required );

        // check the preferred name is valid
        if( ( preferredName == null ) || ( preferredName.length() < 1 ) )
        {
            throw new IllegalArgumentException(
              ResourceHelper.getResourceHelper().getMessage(
                ResourceConstants.COMMAND_PREFERRED_NAME_TOO_SHORT ) );
        }

        m_preferredName = preferredName;

        // gracefully and defensively handle aliases
        
        if( null == aliases )
        {
            m_aliases = Collections.EMPTY_SET;
        }
        else
        {
            m_aliases = Collections.unmodifiableSet( new HashSet( aliases ) );
        }
        
        // populate the triggers Set
        final Set newTriggers = new HashSet();
        newTriggers.add( preferredName );
        newTriggers.addAll( m_aliases );
        m_triggers = Collections.unmodifiableSet( newTriggers );
    }

   /**
    * Process the parent.
    * @param commandLine the commandline
    * @param arguments an iterator of arguments
    * @exception OptionException if an error occurs
    */
    public void processParent(
      final WriteableCommandLine commandLine, final ListIterator arguments )
      throws OptionException
    {
        // grab the argument to process
        final String arg = (String) arguments.next();

        // if we can process it
        if( canProcess( commandLine, arg ) )
        {
            // then note the option
            commandLine.addOption( this );

            // normalise the argument list
            arguments.set( m_preferredName );
        }
        else
        {
            throw new OptionException(
              this,
              ResourceConstants.UNEXPECTED_TOKEN, 
              arg );
        }
    }

    /**
     * Identifies the argument prefixes that should trigger this option. This
     * is used to decide which of many Options should be tried when processing
     * a given argument string.
     * 
     * The returned Set must not be null.
     * 
     * @return The set of triggers for this Option
     */
    public Set getTriggers()
    {
        return m_triggers;
    }

    /**
     * Checks that the supplied CommandLine is valid with respect to this
     * option.
     * 
     * @param commandLine the CommandLine to check.
     * @throws OptionException if the CommandLine is not valid.
     */
    public void validate( WriteableCommandLine commandLine ) throws OptionException
    {
        if( isRequired() && !commandLine.hasOption( this ) )
        {
            throw new OptionException(
              this,
              ResourceConstants.OPTION_MISSING_REQUIRED,
              getPreferredName() );
        }
        super.validate( commandLine );
    }

    /**
     * Appends usage information to the specified StringBuffer
     * 
     * @param buffer the buffer to append to
     * @param helpSettings a set of display settings @see DisplaySetting
     * @param comp a comparator used to sort the Options
     */
    public void appendUsage(
      final StringBuffer buffer, final Set helpSettings, final Comparator comp )
      {
        // do we display optionality
        final boolean optional =
          !isRequired() && helpSettings.contains( DisplaySetting.DISPLAY_OPTIONAL );
        final boolean displayAliases = 
          helpSettings.contains( DisplaySetting.DISPLAY_ALIASES );

        if( optional )
        {
            buffer.append( '[' );
        }

        buffer.append( m_preferredName );

        if( displayAliases && !m_aliases.isEmpty() )
        {
            buffer.append( " (" );
            final List list = new ArrayList( m_aliases );
            Collections.sort( list );
            for( final Iterator i = list.iterator(); i.hasNext();)
            {
                final String alias = (String) i.next();
                buffer.append( alias );
                if( i.hasNext() )
                {
                    buffer.append( ',' );
                }
            }
            buffer.append( ')' );
        }

        super.appendUsage( buffer, helpSettings, comp );
        if( optional )
        {
            buffer.append( ']' );
        }
    }

    /**
     * The preferred name of an option is used for generating help and usage
     * information.
     * 
     * @return The preferred name of the option
     */
    public String getPreferredName()
    {
        return m_preferredName;
    }
}

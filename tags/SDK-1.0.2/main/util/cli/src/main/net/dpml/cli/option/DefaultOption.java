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

/**
 * A Parent implementation representing normal options.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultOption extends ParentImpl
{
    /**
     * The default token used to prefix a short option
     */
    public static final String DEFAULT_SHORT_PREFIX = "-";

    /**
     * The default token used to prefix a long option
     */
    public static final String DEFAULT_LONG_PREFIX = "--";

    /**
     * The default value for the burstEnabled constructor parameter
     */
    public static final boolean DEFAULT_BURST_ENABLED = true;
    
    private final String m_preferredName;
    private final Set m_aliases;
    private final Set m_burstAliases;
    private final Set m_triggers;
    private final Set m_prefixes;
    private final String m_shortPrefix;
    private final boolean m_burstEnabled;
    private final int m_burstLength;

    /**
     * Creates a new DefaultOption
     *
     * @param shortPrefix the prefix used for short options
     * @param longPrefix the prefix used for long options
     * @param burstEnabled should option bursting be enabled
     * @param preferredName the preferred name for this Option, this should 
     *   begin with either shortPrefix or longPrefix
     * @param description a description of this Option
     * @param aliases the alternative names for this Option
     * @param burstAliases the aliases that can be burst
     * @param required whether the Option is strictly required
     * @param argument the Argument belonging to this Parent, or null
     * @param children the Group children belonging to this Parent, ot null
     * @param id the unique identifier for this Option
     * @throws IllegalArgumentException if the preferredName or an alias isn't
     *     prefixed with shortPrefix or longPrefix
     */
    public DefaultOption(
      final String shortPrefix, final String longPrefix, final boolean burstEnabled,
      final String preferredName, final String description, final Set aliases,
      final Set burstAliases, final boolean required, final Argument argument,
      final Group children, final int id ) 
      throws IllegalArgumentException
    {
        super( argument, children, description, id, required );

        m_shortPrefix = shortPrefix;
        m_burstEnabled = burstEnabled;
        m_burstLength = shortPrefix.length() + 1;
        m_preferredName = preferredName;
        
        if( aliases == null )
        {
            m_aliases = Collections.EMPTY_SET;
        }
        else
        {
            m_aliases = Collections.unmodifiableSet( new HashSet( aliases ) );
        }
        
        if( burstAliases == null )
        {
            m_burstAliases = Collections.EMPTY_SET;
        }
        else
        {
            m_burstAliases = Collections.unmodifiableSet( new HashSet( burstAliases ) );
        }
        
        final Set newTriggers = new HashSet();
        newTriggers.add( m_preferredName );
        newTriggers.addAll( m_aliases );
        newTriggers.addAll( m_burstAliases );
        m_triggers = Collections.unmodifiableSet( newTriggers );

        final Set newPrefixes = new HashSet( super.getPrefixes() );
        newPrefixes.add( m_shortPrefix );
        newPrefixes.add( longPrefix );
        m_prefixes = Collections.unmodifiableSet( newPrefixes );

        checkPrefixes( newPrefixes );
    }

    /**
     * Indicates whether this Option will be able to process the particular
     * argument.
     * 
     * @param commandLine the CommandLine object to store defaults in
     * @param argument the argument to be tested
     * @return true if the argument can be processed by this Option
     */
    public boolean canProcess(
      final WriteableCommandLine commandLine, final String argument )
    {
        return 
          ( argument != null ) 
          && ( 
            super.canProcess( commandLine, argument ) 
            || ( 
              ( argument.length() >= m_burstLength ) 
              && m_burstAliases.contains( 
                argument.substring( 0, m_burstLength ) ) 
            ) 
          );
    }

    /**
     * Process the parent.
     * @param commandLine the CommandLine object to store defaults in
     * @param arguments the ListIterator over String arguments
     * @exception OptionException if an error occurs
     */
    public void processParent( WriteableCommandLine commandLine, ListIterator arguments )
      throws OptionException 
    {
        final String argument = (String) arguments.next();

        if( m_triggers.contains( argument ) )
        {
            commandLine.addOption( this );
            arguments.set( m_preferredName );
        } 
        else if( m_burstEnabled && ( argument.length() >= m_burstLength ) )
        {
            final String burst = argument.substring( 0, m_burstLength );
            if( m_burstAliases.contains( burst ) )
            {
                commandLine.addOption( this );
                //HMM test bursting all vs bursting one by one.
                arguments.set( m_preferredName );

                if( getArgument() == null )
                {
                    arguments.add( m_shortPrefix + argument.substring( m_burstLength ) );
                }
                else
                {
                    arguments.add( argument.substring( m_burstLength ) );
                }
                arguments.previous();
            } 
            else
            {
                throw new OptionException(
                  this, ResourceConstants.CANNOT_BURST, argument );
            }
        }
        else
        {
            throw new OptionException(
              this, 
              ResourceConstants.UNEXPECTED_TOKEN,
              argument );
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
     * Identifies the argument prefixes that should be considered options. This
     * is used to identify whether a given string looks like an option or an
     * argument value. Typically an option would return the set [--,-] while
     * switches might offer [-,+].
     * 
     * The returned Set must not be null.
     * 
     * @return The set of prefixes for this Option
     */
    public Set getPrefixes() 
    {
        return m_prefixes;
    }

    /**
     * Checks that the supplied CommandLine is valid with respect to this
     * option.
     * 
     * @param commandLine the CommandLine to check.
     * @throws OptionException if the CommandLine is not valid.
     */
    public void validate( WriteableCommandLine commandLine )
      throws OptionException
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
          !isRequired() 
          && helpSettings.contains( DisplaySetting.DISPLAY_OPTIONAL );
          
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

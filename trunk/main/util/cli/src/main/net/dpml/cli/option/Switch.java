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
 * A Parent implementation representing normal switch options.
 * For example: <code>+d|-d</code> or <code>--enable-x|--disable-x</code>.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Switch extends ParentImpl
{
    /** i18n */
    public static final ResourceHelper resources = 
      ResourceHelper.getResourceHelper();

    /**
     * The default prefix for enabled switches
     */
    public static final String DEFAULT_ENABLED_PREFIX = "+";

    /**
     * The default prefix for disabled switches
     */
    public static final String DEFAULT_DISABLED_PREFIX = "-";
    
    private final String m_enabledPrefix;
    private final String m_disabledPrefix;
    private final Set m_triggers;
    private final String m_preferredName;
    private final Set m_aliases;
    private final Set m_prefixes;
    private final Boolean m_defaultSwitch;

    /**
     * Creates a new Switch with the specified parameters
     * @param enabledPrefix the prefix used for enabled switches
     * @param disabledPrefix the prefix used for disabled switches
     * @param preferredName the preferred name of the switch
     * @param aliases the aliases by which the Switch is known
     * @param description a description of the Switch
     * @param required whether the Option is strictly required
     * @param argument the Argument belonging to this Parent, or null
     * @param children the Group children belonging to this Parent, ot null
     * @param id the unique identifier for this Option
     * @throws IllegalArgumentException if the preferredName or an alias isn't
     *     prefixed with enabledPrefix or disabledPrefix
     */
    public Switch(
      final String enabledPrefix, final String disabledPrefix, final String preferredName,
      final Set aliases, final String description, final boolean required,
      final Argument argument, final Group children, final int id, 
      final Boolean switchDefault )
    {
        super( argument, children, description, id, required );

        if( enabledPrefix == null )
        {
            throw new IllegalArgumentException(
              resources.getMessage( 
                ResourceConstants.SWITCH_NO_ENABLED_PREFIX ) );
        }

        if( disabledPrefix == null )
        {
            throw new IllegalArgumentException(
              resources.getMessage( 
                ResourceConstants.SWITCH_NO_DISABLED_PREFIX ) );
        }

        if( enabledPrefix.startsWith(disabledPrefix ) )
        {
            throw new IllegalArgumentException(
              resources.getMessage( 
                ResourceConstants.SWITCH_ENABLED_STARTS_WITH_DISABLED ) );
        }

        if( disabledPrefix.startsWith( enabledPrefix ) )
        {
            throw new IllegalArgumentException(
              resources.getMessage( 
                ResourceConstants.SWITCH_DISABLED_STARTWS_WITH_ENABLED ) );
        }

        m_enabledPrefix = enabledPrefix;
        m_disabledPrefix = disabledPrefix;
        m_preferredName = preferredName;

        if( ( preferredName == null) || ( preferredName.length() < 1 ) )
        {
            throw new IllegalArgumentException(
              resources.getMessage(
                ResourceConstants.SWITCH_PREFERRED_NAME_TOO_SHORT ) );
        }

        final Set newTriggers = new HashSet();
        newTriggers.add( enabledPrefix + preferredName );
        newTriggers.add( disabledPrefix + preferredName );
        m_triggers = Collections.unmodifiableSet( newTriggers );

        if( aliases == null )
        {
            m_aliases = Collections.EMPTY_SET;
        } 
        else
        {
            m_aliases = Collections.unmodifiableSet( new HashSet( aliases ) );

            for( final Iterator i = aliases.iterator(); i.hasNext(); )
            {
                final String alias = (String) i.next();
                newTriggers.add( enabledPrefix + alias );
                newTriggers.add( disabledPrefix + alias );
            }
        }

        final Set newPrefixes = new HashSet( super.getPrefixes() );
        newPrefixes.add( enabledPrefix );
        newPrefixes.add( disabledPrefix );
        m_prefixes = Collections.unmodifiableSet( newPrefixes );
        m_defaultSwitch = switchDefault;
        checkPrefixes( newPrefixes );
    }

    public void processParent(
      final WriteableCommandLine commandLine, final ListIterator arguments )
      throws OptionException
    {
        final String arg = (String) arguments.next();

        if( canProcess( commandLine, arg ) )
        {
            if( arg.startsWith( m_enabledPrefix ) )
            {
                commandLine.addSwitch( this, true );
                arguments.set( m_enabledPrefix + m_preferredName );
            }
            if( arg.startsWith( m_disabledPrefix ) )
            {
                commandLine.addSwitch( this, false );
                arguments.set( m_disabledPrefix + m_preferredName );
            }
        } 
        else
        {
            throw new OptionException(
              this, 
              ResourceConstants.UNEXPECTED_TOKEN, 
              arg );
        }
    }

    public Set getTriggers() 
    {
        return m_triggers;
    }

    public Set getPrefixes() 
    {
        return m_prefixes;
    }

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

    public void appendUsage(
      final StringBuffer buffer, final Set helpSettings, final Comparator comp )
    {
        // do we display optionality
        final boolean optional =
          !isRequired() 
          && helpSettings.contains( DisplaySetting.DISPLAY_OPTIONAL );
          
        final boolean displayAliases = 
          helpSettings.contains( DisplaySetting.DISPLAY_ALIASES );
        final boolean disabled = 
          helpSettings.contains( DisplaySetting.DISPLAY_SWITCH_DISABLED );
        final boolean enabled =
            !disabled || helpSettings.contains( DisplaySetting.DISPLAY_SWITCH_ENABLED );
        final boolean both = disabled && enabled;

        if( optional )
        {
            buffer.append( '[' );
        }

        if( enabled )
        {
            buffer.append( m_enabledPrefix ).append( m_preferredName );
        }

        if( both )
        {
            buffer.append( '|' );
        }

        if( disabled )
        {
            buffer.append( m_disabledPrefix ).append( m_preferredName );
        }

        if( displayAliases && !m_aliases.isEmpty() )
        {
            buffer.append(" (");

            final List list = new ArrayList( m_aliases );
            Collections.sort( list );
            for( final Iterator i = list.iterator(); i.hasNext(); )
            {
                final String alias = (String) i.next();

                if( enabled )
                {
                    buffer.append( m_enabledPrefix ).append( alias );
                }
                
                if( both )
                {
                    buffer.append( '|' );
                }

                if( disabled )
                {
                    buffer.append( m_disabledPrefix ).append( alias );
                }

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

    public String getPreferredName()
    {
        return m_enabledPrefix + m_preferredName;
    }

    public void defaults( final WriteableCommandLine commandLine )
    {
        commandLine.setDefaultSwitch( this, m_defaultSwitch );
    }
}

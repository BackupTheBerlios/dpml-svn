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
package net.dpml.cli.builder;

import java.util.HashSet;
import java.util.Set;

import net.dpml.cli.Argument;
import net.dpml.cli.Group;
import net.dpml.cli.option.DefaultOption;
import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

/**
 * Builds DefaultOption instances.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultOptionBuilder
{
    private final String m_shortPrefix;
    private final String m_longPrefix;
    private final boolean m_burstEnabled;
    private String m_preferredName;
    private Set m_aliases;
    private Set m_burstAliases;
    private boolean m_required;
    private String m_description;
    private Argument m_argument;
    private Group m_children;
    private int m_id;

    /**
     * Creates a new DefaultOptionBuilder using defaults
     * @see DefaultOption#DEFAULT_SHORT_PREFIX
     * @see DefaultOption#DEFAULT_LONG_PREFIX
     * @see DefaultOption#DEFAULT_BURST_ENABLED
     */
    public DefaultOptionBuilder()
    {
        this( 
          DefaultOption.DEFAULT_SHORT_PREFIX, 
          DefaultOption.DEFAULT_LONG_PREFIX,
          DefaultOption.DEFAULT_BURST_ENABLED );
    }

    /**
     * Creates a new DefaultOptionBuilder
     * @param shortPrefix the prefix to use for short options
     * @param longPrefix the prefix to use for long options
     * @param burstEnabled whether to allow gnu style bursting
     * @throws IllegalArgumentException if either prefix is less than on
     *                                  character long
     */
    public DefaultOptionBuilder( 
      final String shortPrefix, final String longPrefix, final boolean burstEnabled )
      throws IllegalArgumentException
    {
        if( ( shortPrefix == null ) || ( shortPrefix.length() == 0 ) )
        {
            throw new IllegalArgumentException(
              ResourceHelper.getResourceHelper().getMessage(
                ResourceConstants.OPTION_ILLEGAL_SHORT_PREFIX ) );
        }

        if( ( longPrefix == null ) || ( longPrefix.length() == 0 ) )
        {
            throw new IllegalArgumentException(
              ResourceHelper.getResourceHelper().getMessage(
                ResourceConstants.OPTION_ILLEGAL_LONG_PREFIX ) );
        }

        m_shortPrefix = shortPrefix;
        m_longPrefix = longPrefix;
        m_burstEnabled = burstEnabled;
        reset();
    }

    /**
     * Creates a DefaultOption instance
     * @return the new instance
     * @throws IllegalStateException if no names have been supplied
     */
    public DefaultOption create() throws IllegalStateException
    {
        if( m_preferredName == null )
        {
            throw new IllegalStateException(
              ResourceHelper.getResourceHelper().getMessage(
                ResourceConstants.OPTION_NO_NAME ) );
        }
        final DefaultOption option =
            new DefaultOption(
              m_shortPrefix, 
              m_longPrefix, 
              m_burstEnabled, 
              m_preferredName, 
              m_description,
              m_aliases, 
              m_burstAliases, 
              m_required, 
              m_argument, 
              m_children, 
              m_id );
        reset();
        return option;
    }

    /**
     * Resets the builder.
     * @return this <code>DefaultOptionBuilder</code>.
     */
    public DefaultOptionBuilder reset()
    {
        m_preferredName = null;
        m_description = null;
        m_aliases = new HashSet();
        m_burstAliases = new HashSet();
        m_required = false;
        m_argument = null;
        m_children = null;
        m_id = 0;
        return this;
    }

    /**
     * Use this short option name. The first name is used as the preferred
     * display name for the Command and then later names are used as aliases.
     *
     * @param shortName the name to use
     * @return this builder
     */
    public DefaultOptionBuilder withShortName( final String shortName )
    {
        final String name = m_shortPrefix + shortName;
        if( m_preferredName == null )
        {
            m_preferredName = name;
        }
        else
        {
            m_aliases.add( name );
        }
        if( m_burstEnabled && ( name.length() == ( m_shortPrefix.length() + 1 ) ) )
        {
            m_burstAliases.add( name );
        }
        return this;
    }

    /**
     * Use this long option name.  The first name is used as the preferred
     * display name for the Command and then later names are used as aliases.
     *
     * @param longName the name to use
     * @return this builder
     */
    public DefaultOptionBuilder withLongName( final String longName )
    {
        final String name = m_longPrefix + longName;
        if( m_preferredName == null )
        {
            m_preferredName = name;
        }
        else
        {
            m_aliases.add( name );
        }
        return this;
    }

    /**
     * Use this option description
     * @param newDescription the description to use
     * @return this builder
     */
    public DefaultOptionBuilder withDescription( final String newDescription )
    {
        m_description = newDescription;
        return this;
    }

    /**
     * Use this optionality
     * @param newRequired true iff the Option is required
     * @return this builder
     */
    public DefaultOptionBuilder withRequired( final boolean newRequired )
    {
        m_required = newRequired;
        return this;
    }

    /**
     * Use this child Group
     * @param newChildren the child Group to use
     * @return this builder
     */
    public DefaultOptionBuilder withChildren( final Group newChildren )
    {
        m_children = newChildren;
        return this;
    }

    /**
     * Use this Argument
     * @param newArgument the argument to use
     * @return this builder
     */
    public DefaultOptionBuilder withArgument( final Argument newArgument )
    {
        m_argument = newArgument;
        return this;
    }

    /**
     * Sets the id
     *
     * @param newId
     *            the id of the DefaultOption
     * @return this DefaultOptionBuilder
     */
    public final DefaultOptionBuilder withId( final int newId )
    {
        m_id = newId;
        return this;
    }
}

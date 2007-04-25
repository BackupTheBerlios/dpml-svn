/*
 * Copyright 2003-2005 The Apache Software Foundation
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
package dpml.cli.builder;

import java.util.HashSet;
import java.util.Set;

import dpml.cli.Argument;
import dpml.cli.Group;
import dpml.cli.option.Switch;
import dpml.cli.resource.ResourceConstants;
import dpml.cli.resource.ResourceHelper;

/**
 * Builds Switch instance.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class SwitchBuilder
{
    private final String m_enabledPrefix;
    private final String m_disabledPrefix;
    private String m_description;
    private String m_preferredName;
    private Set m_aliases;
    private boolean m_required;
    private Argument m_argument;
    private Group m_children;
    private int m_id;
    private Boolean m_switchDefault;

    /**
     * Creates a new SwitchBuilder using defaults.
     * @see Switch#DEFAULT_ENABLED_PREFIX
     * @see Switch#DEFAULT_DISABLED_PREFIX
     */
    public SwitchBuilder()
    {
        this( Switch.DEFAULT_ENABLED_PREFIX, Switch.DEFAULT_DISABLED_PREFIX );
    }

    /**
     * Creates a new SwitchBuilder
     * @param enabledPrefix the prefix to use for enabling the option
     * @param disabledPrefix the prefix to use for disabling the option
     * @throws IllegalArgumentException if either prefix is less than 1
     *                                  character long or the prefixes match
     */
    public SwitchBuilder( final String enabledPrefix, final String disabledPrefix )
      throws IllegalArgumentException
    {
        if( ( enabledPrefix == null ) || ( enabledPrefix.length() < 1 ) ) 
        {
            throw new IllegalArgumentException(
              ResourceHelper.getResourceHelper().getMessage(
                ResourceConstants.SWITCH_ILLEGAL_ENABLED_PREFIX ) );
        }

        if( ( disabledPrefix == null ) || ( disabledPrefix.length() < 1 ) )
        {
            throw new IllegalArgumentException(
              ResourceHelper.getResourceHelper().getMessage(
                ResourceConstants.SWITCH_ILLEGAL_DISABLED_PREFIX ) );
        }

        if( enabledPrefix.equals( disabledPrefix ) )
        {
            throw new IllegalArgumentException(
              ResourceHelper.getResourceHelper().getMessage(
                ResourceConstants.SWITCH_IDENTICAL_PREFIXES ) );
        }

        m_enabledPrefix = enabledPrefix;
        m_disabledPrefix = disabledPrefix;
        reset();
    }

    /**
     * Creates a new Switch instance
     * @return a new Switch instance
     */
    public Switch create()
    {
        final Switch option =
            new Switch(
              m_enabledPrefix, 
              m_disabledPrefix, 
              m_preferredName, 
              m_aliases, 
              m_description,
              m_required, 
              m_argument, 
              m_children, 
              m_id, 
              m_switchDefault );
        reset();
        return option;
    }

    /**
     * Resets the builder.
     * @return the builder
     */
    public SwitchBuilder reset() 
    {
        m_description = null;
        m_preferredName = null;
        m_required = false;
        m_aliases = new HashSet();
        m_argument = null;
        m_children = null;
        m_id = 0;
        m_switchDefault = null;
        return this;
    }

    /**
     * Use this option description
     * @param newDescription the description to use
     * @return this builder
     */
    public SwitchBuilder withDescription( final String newDescription ) 
    {
        m_description = newDescription;
        return this;
    }

    /**
     * Use this option name. The first name is used as the preferred
     * display name for the Command and then later names are used as aliases.
     *
     * @param name the name to use
     * @return this builder
     */
    public SwitchBuilder withName( final String name )
    {
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
     * Use this optionality
     * @param newRequired true iff the Option is required
     * @return this builder
     */
    public SwitchBuilder withRequired( final boolean newRequired )
    {
        m_required = newRequired;
        return this;
    }

    /**
     * Use this Argument
     * @param newArgument the argument to use
     * @return this builder
     */
    public SwitchBuilder withArgument( final Argument newArgument )
    {
        m_argument = newArgument;
        return this;
    }

    /**
     * Use this child Group
     * @param newChildren the child Group to use
     * @return this builder
     */
    public SwitchBuilder withChildren( final Group newChildren )
    {
        m_children = newChildren;
        return this;
    }

    /**
     * Sets the id
     *
     * @param newId the id of the Switch
     * @return this SwitchBuilder
     */
    public final SwitchBuilder withId( final int newId )
    {
        m_id = newId;
        return this;
    }

    /**
     * Sets the default state for this switch
     *
     * @param newSwitchDefault the default state
     * @return this SwitchBuilder
     */
    public final SwitchBuilder withSwitchDefault( final Boolean newSwitchDefault )
    {
        m_switchDefault = newSwitchDefault;
        return this;
    }
}

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
import net.dpml.cli.option.Command;
import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

/**
 * Builds Command instances
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CommandBuilder
{
    /** the preferred name of the command */
    private String m_preferredName;

    /** the description of the command */
    private String m_description;

    /** the aliases of the command */
    private Set m_aliases;

    /** whether the command is required or not */
    private boolean m_required;

    /** the argument of the command */
    private Argument m_argument;

    /** the children of the command */
    private Group m_children;

    /** the id of the command */
    private int m_id;

    /**
     * Creates a new <code>CommandBuilder</code> instance.
     */
    public CommandBuilder()
    {
        reset();
    }

    /**
     * Creates a new <code>Command</code> instance using the properties of the
     * <code>CommandBuilder</code>.
     *
     * @return the new Command instance
     */
    public Command create()
    {
        // check we have a valid name
        if( m_preferredName == null )
        {
            throw new IllegalStateException(
              ResourceHelper.getResourceHelper().getMessage(
                ResourceConstants.OPTION_NO_NAME ) );
        }

        // build the command
        final Command option =
          new Command( 
            m_preferredName, m_description, m_aliases, m_required, m_argument, m_children, m_id );

        // reset the builder
        reset();
        return option;
    }

    /**
     * Resets the CommandBuilder to the defaults for a new Command.
     *
     * This method is called automatically at the end of the
     * {@link #create() create} method.
     * @return this <code>CommandBuilder</code>.
     */
    public CommandBuilder reset()
    {
        m_preferredName = null;
        m_description = null;
        m_aliases = new HashSet();
        m_required = false;
        m_argument = null;
        m_children = null;
        m_id = 0;
        return this;
    }

    /**
     * Specifies the name for the next <code>Command</code>
     * that is created.  The first name is used as the preferred
     * display name for the <code>Command</code> and then
     * later names are used as aliases.
     *
     * @param name the name for the next <code>Command</code>
     * that is created.
     * @return this <code>CommandBuilder</code>.
     */
    public CommandBuilder withName( final String name )
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
     * Specifies the description for the next <code>Command</code>
     * that is created.  This description is used to produce
     * help documentation for the <code>Command</code>.
     *
     * @param newDescription the description for the next
     * <code>Command</code> that is created.
     * @return this <code>CommandBuilder</code>.
     */
    public CommandBuilder withDescription( final String newDescription )
    {
        m_description = newDescription;
        return this;
    }

    /**
     * Specifies whether the next <code>Command</code> created is
     * required or not.
     * @param newRequired whether the next <code>Command</code> created is
     * required or not.
     * @return this <code>CommandBuilder</code>.
     */
    public CommandBuilder withRequired( final boolean newRequired )
    {
        m_required = newRequired;
        return this;
    }

    /**
     * Specifies the children for the next <code>Command</code>
     * that is created.
     *
     * @param newChildren the child options for the next <code>Command</code>
     * that is created.
     * @return this <code>CommandBuilder</code>.
     */
    public CommandBuilder withChildren( final Group newChildren )
    {
        m_children = newChildren;
        return this;
    }

    /**
     * Specifies the argument for the next <code>Command</code>
     * that is created.
     *
     * @param newArgument the argument for the next <code>Command</code>
     * that is created.
     * @return this <code>CommandBuilder</code>.
     */
    public CommandBuilder withArgument( final Argument newArgument )
    {
        m_argument = newArgument;
        return this;
    }

    /**
     * Specifies the id for the next <code>Command</code> that is created.
     *
     * @param newId the id for the next <code>Command</code> that is created.
     * @return this <code>CommandBuilder</code>.
     */
    public final CommandBuilder withId( final int newId )
    {
        m_id = newId;
        return this;
    }
}

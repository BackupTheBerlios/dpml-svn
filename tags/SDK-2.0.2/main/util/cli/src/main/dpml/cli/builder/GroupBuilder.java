/**
 * Copyright 2003-2004 The Apache Software Foundation
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
package dpml.cli.builder;

import java.util.ArrayList;
import java.util.List;

import dpml.cli.Group;
import dpml.cli.Option;
import dpml.cli.option.GroupImpl;

/**
 * Builds Group instances.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class GroupBuilder
{
    private String m_name;
    private String m_description;
    private List m_options;
    private int m_minimum;
    private int m_maximum;

    /**
     * Creates a new GroupBuilder
     */
    public GroupBuilder()
    {
        reset();
    }

    /**
     * Creates a new Group instance
     * @return the new Group instance
     */
    public Group create()
    {
        final GroupImpl group =
          new GroupImpl(
            m_options, 
            m_name, 
            m_description, 
            m_minimum, 
            m_maximum );
        reset();
        return group;
    }

    /**
     * Resets the builder
     * @return this builder
     */
    public GroupBuilder reset()
    {
        m_name = null;
        m_description = null;
        m_options = new ArrayList();
        m_minimum = 0;
        m_maximum = Integer.MAX_VALUE;
        return this;
    }

    /**
     * Use this option description
     * @param newDescription the description to use
     * @return this builder
     */
    public GroupBuilder withDescription( final String newDescription )
    {
        m_description = newDescription;
        return this;
    }

    /**
     * Use this option name
     * @param newName the name to use
     * @return this builder
     */
    public GroupBuilder withName( final String newName )
    {
        m_name = newName;
        return this;
    }

    /**
     * A valid group requires at least this many options present
     * @param newMinimum the minimum Options required
     * @return this builder
     */
    public GroupBuilder withMinimum( final int newMinimum )
    {
        m_minimum = newMinimum;
        return this;
    }

    /**
     * A valid group requires at most this many options present
     * @param newMaximum the maximum Options allowed
     * @return this builder
     */
    public GroupBuilder withMaximum( final int newMaximum )
    {
        m_maximum = newMaximum;
        return this;
    }

    /**
     * Add this option to the group
     * @param option the Option to add
     * @return this builder
     */
    public GroupBuilder withOption( final Option option )
    {
        m_options.add( option );
        return this;
    }
}

/*
 * Copyright 2004 Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avalon.jmx.util;

import java.util.HashMap;
import java.util.Set;

import javax.management.DynamicMBean;

/**
 * It reprensents a managed object in the managegement space.  It is a container for
 * zero or more management topics and zero or more management lists.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.1 $
 */

public class Target
{
    private final String m_name;
    private final HashMap m_topics;
    private final Object m_managedResource;

    /**
     * Creates new Target
     *
     * @param name the name for the target
     * @param managedResource the object that this managedResource represents in the management hierarchy
     */
    public Target( final String name, final Object managedResource )
    {
        m_name = name;
        m_managedResource = managedResource;
        m_topics = new HashMap();
    }

    /**
     * Returns the name of the Target
     * @return  the name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Returns the object managed by the target
     *
     * @return  the managed object
     */
    public Object getManagedResource()
    {
        return m_managedResource;
    }

    /**
     * Topics are a set of attributes and operations relevant to a particular
     * aspect of an object.  A Target must typically have at least one topic in
     * order to be manageable.
     *
     * @param topic the topic
     */
    public void addTopic( final String name, final DynamicMBean topic )
    {
        m_topics.put( name, topic );
    }

    /**
     * Removes a topic for this target
     * @param name  the name of the topic to remove
     */
    public void removeTopic( final String name )
    {
        m_topics.remove( name );
    }

    /**
     * Gets a topic for this Target
     *
     * @param name the name of the topic
     * @return  the topic of that name
     */
    public DynamicMBean getTopic( final String name )
    {
        return ( DynamicMBean ) m_topics.get( name );
    }

    /**
     * Returns the Set of topics for this Target
     *
     * @return the Set of topic names
     */
    public Set getTopicNames()
    {
        return m_topics.keySet();
    }
}

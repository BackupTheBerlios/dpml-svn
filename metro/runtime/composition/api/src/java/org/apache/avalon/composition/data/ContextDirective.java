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

package org.apache.avalon.composition.data;

import java.io.Serializable;

/**
 * A context descriptor declares the context creation criteria for
 * the context instance and context entries.
 *
 * <p><b>XML</b></p>
 * <p>A context directive may contain multiple import statements.  Each import
 * statement corresponds to a request for a context value from the container.</p>
 * <pre>
 *    &lt;context class="<font color="darkred">MyContextClass</font>"&gt;
 *       &lt;entry key="<font color="darkred">special</font>"&gt;
 *         &lt;import key="<font color="darkred">urn:avalon:classloader</font>"/&gt;
 *       &lt;/entry&gt;
 *       &lt;entry key="<font color="darkred">xxx</font>"&gt;
 *         &lt;param class="<font color="darkred">MySpecialClass</font>"&gt;
 *           &lt;param&gt<font color="darkred">hello</font>&lt;/param&gt;
 *           &lt;param class="<font color="darkred">java.io.File</font>"&gt;<font color="darkred">../lib</font>&lt;/param&gt;
 *         &lt;/param&gt;
 *       &lt;/entry&gt;
 *    &lt;/context&gt;
 * </pre>
 *
 * @see EntryDirective
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ContextDirective.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ContextDirective implements Serializable
{
    /**
     * The set of entry directives.
     */
    private final EntryDirective[] m_entries;

    /**
     * The constext casting classname.
     */
    private final String m_classname;

    /**
     * Creation of a new file target.
     * @param entries the set of entry descriptors
     */
    public ContextDirective( final EntryDirective[] entries )
    {
        this( null, entries );
    }

    /**
     * Creation of a new file target.
     * @param classname the context implementation class
     * @param entries the set of entry descriptors
     */
    public ContextDirective( final String classname, final EntryDirective[] entries )
    {
        m_classname = classname;
        if( entries != null )
        {
            m_entries = entries;
        }
        else
        {
            m_entries = new EntryDirective[0];
        }
    }

    /**
     * Return the classname of the context implementation to use.
     * @return the classname
     */
    public String getClassname()
    {
        return m_classname;
    }

    /**
     * Return the set of entry directives.
     * @return the entries
     */
    public EntryDirective[] getEntryDirectives()
    {
        return m_entries;
    }

    /**
     * Return a named entry.
     * @param key the context entry key
     * @return the entry corresponding to the supplied key or null if the
     *   key is unknown
     */
    public EntryDirective getEntryDirective( String key )
    {
        for( int i = 0; i < m_entries.length; i++ )
        {
            EntryDirective entry = m_entries[ i ];
            if( entry.getKey().equals( key ) )
            {
                return entry;
            }
        }
        return null;
    }
}

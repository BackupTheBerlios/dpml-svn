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
 * A entry descriptor declares the context entry import or creation criteria for
 * a single context entry instance.
 *
 * <p><b>XML</b></p>
 * <p>A entry may contain either (a) a single nested import directive, or (b) a single param constructor directives.</p>
 * <pre>
 *  <font color="gray">&lt;context&gt;</font>
 *
 *    &lt!-- option (a) nested import -->
 *    &lt;entry key="<font color="darkred">my-home-dir</font>"&gt;
 *       &lt;include key="<font color="darkred">urn:avalon:home</font>"/&gt;
 *    &lt;/entry&gt;
 *
 *    &lt!-- option (b) param constructors -->
 *    &lt;entry key="<font color="darkred">title</font>"&gt;
 *       &lt;param&gt;<font color="darkred">Lord of the Rings</font>&lt;/&gt;
 *    &lt;/entry&gt;
 *    &lt;entry key="<font color="darkred">home</font>"&gt;
 *      &lt;param class="<font color="darkred">java.io.File</font>"&gt;<font color="darkred">../home</font>&lt;/param&gt;
 *    &lt;/entry&gt;
 *
 *  <font color="gray">&lt;/context&gt;</font>
 * </pre>
 *
 * @see ImportDirective
 * @see Parameter
 * @see ContextDirective
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: EntryDirective.java 30977 2004-07-30 08:57:54Z niclas $
 */
public abstract class EntryDirective implements Serializable
{
    /**
     * The entry key.
     */
    private final String m_key;

    /**
     * Creation of a new entry directive using a import directive.
     * @param key the entry key
     */
    public EntryDirective( final String key )
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        m_key = key;
    }

    /**
     * Return the context key.
     * @return the key
     */
    public String getKey()
    {
        return m_key;
    }
}

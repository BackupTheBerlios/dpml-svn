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
 * <p>An file include directive.</p>
 * <p><b>XML</b></p>
 * <p>An include element is normally contained within a scoping structure such as a
 * fileset or directory set. The include element contains the single attribute name
 * which is used to refer to the file or directory (depending on the containing
 * context.</p>
 * <pre>
 *    <font color="gray">&lt;fileset dir="lib"&gt;</font>
 *       &lt;include name="<font color="darkred">avalon-framework.jar</font>" /&gt;
 *    <font color="gray">&lt;/fileset&gt;</font>
 * </pre>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: IncludeDirective.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class IncludeDirective implements Serializable
{

    /**
     * The base directory
     */
    private final String m_path;

    /**
     * Create a IncludeDirective instance.
     *
     * @param path the path to include
     */
    public IncludeDirective( final String path )
    {
        m_path = path;
    }

    /**
     * Return the included path.
     *
     * @return the path
     */
    public String getPath()
    {
        return m_path;
    }
}

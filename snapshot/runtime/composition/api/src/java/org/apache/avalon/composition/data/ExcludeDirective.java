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
 * <p>A file exclude directive.</p>
 * <p><b>XML</b></p>
 * <p>An exclude element is normally contained within a scoping structure such as a
 * fileset or directory set. The exclude element contains the single attribute name
 * which is used to refer to the file or directory (depending on the containing
 * context.</p>
 * <pre>
 *    <font color="gray">&lt;fileset dir="lib"&gt;</font>
 *       &lt;exclude name="<font color="darkred">avalon-framework.jar</font>" /&gt;
 *    <font color="gray">&lt;/fileset&gt;</font>
 * </pre>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ExcludeDirective.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ExcludeDirective implements Serializable
{

    /**
     * The base directory
     */
    private final String m_path;

    /**
     * Create an ExcludeDirective instance.
     *
     * @param path the path to include
     */
    public ExcludeDirective( final String path )
    {
        m_path = path;
    }

    /**
     * Return the excluded path.
     *
     * @return the path
     */
    public String getPath()
    {
        return m_path;
    }
}

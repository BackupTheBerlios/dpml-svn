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

/**
 * An inport directive used within a context directive to request a container scoped values.
 *
 * <p><b>XML</b></p>
 * <p>An import statement declares that a context value must be supplied by the container,
 * using the container scoped value of the <code>name</code> attribute, and that the value should be
 * supplied as a context entry keyed under the value of the <code>key</code> attribute.</p>
 * <pre>
 *
 *    <font color="gray">
 *    &lt;--
 *    Declare the import of the value of "urn:avalon:home" as a keyed context
 *    entry using the key "home".
 *    --&gt;</font>
 *
 *  <font color="gray">&lt;context&gt;</font>
 *    <font color="gray">&lt;entry key="home">&gt;</font>
 *      &lt;import key="<font color="darkred">urn:avalon:home</font>"/&gt;
 *    <font color="gray">&lt;/entry&gt;</font>
 *  <font color="gray">&lt;/context&gt;</font>
 * </pre>
 *
 * @see ContextDirective
 * @see EntryDirective
 * @see Parameter
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ImportDirective.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ImportDirective extends EntryDirective
{
    /**
     * The container scoped key.
     */
    private final String m_import;

    /**
     * Creation of a new entry directive.
     * @param key the context entry key
     * @param containerKey the container scoped key value to import
     */
    public ImportDirective( final String key, final String containerKey )
    {
        super( key );
        if( null == containerKey )
        {
            throw new NullPointerException( "containerKey" );
        }
        m_import = containerKey;
    }

    /**
     * Return the container scoped key that defines the object to be imported.
     *
     * @return the contain scoped key
     */
    public String getImportKey()
    {
        return m_import;
    }
}

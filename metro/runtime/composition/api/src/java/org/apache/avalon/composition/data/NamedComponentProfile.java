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
 * A named deployment profile.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: NamedComponentProfile.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class NamedComponentProfile extends DeploymentProfile
{

    /**
     * The component classname.
     */
    private String m_classname;

    /**
     * The profile key.
     */
    private String m_key;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

    public NamedComponentProfile( 
           final String name, 
           final String classname,
           final String key,
           final int activation )
    {
        super( name, activation, Mode.EXPLICIT, null );
        m_classname = classname;
        m_key = key;
    }

    //--------------------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------------------

    /**
     * Return the component type classname.
     *
     * @return classname of the component type
     */
    public String getClassname()
    {
        return m_classname;
    }

    /**
     * Return the component profile key.
     *
     * @return the name of a profile pacikaged with the component type
     */
    public String getKey()
    {
        return m_key;
    }

    /**
     * Returns a string representation of the profile.
     * @return a string representation
     */
    public String toString()
    {
        return "[" + getName() + "-" + getKey() + "]";
    }
}

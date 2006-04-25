/*
 * Copyright 2005 Stephen J. McConnell
 *
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

package net.dpml.library.info;

import net.dpml.lang.AbstractDirective;

/**
 * Base class for a data directives.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class DataDirective extends AbstractDirective
{
    private final String m_key;
    
   /**
    * Creation of a new data directive.
    * @param key the unique datatype key
    */
    public DataDirective( String key )
    {
        m_key = key;
    }
    
   /**
    * Return the datatype key.
    * @return the key
    */
    public String getKey()
    {
        return m_key;
    }
}

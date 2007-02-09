/*
 * Copyright 2006 Stephen McConnell.
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

package dpml.lang;

import net.dpml.lang.PartManager;

/**
 * Abstract base class for artifact content handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultPartManager implements PartManager
{
    private final Part m_part;
    
    DefaultPartManager( Part part )
    {
        m_part = part;
    }
    
   /**
    * Returns the part URI as a string.
    * @return the codebase uri
    */
    public String getCodebaseURI()
    {
        return m_part.getCodebaseURI();
    }
    
   /**
    * Returns the part title.
    * @return the title
    */
    public String getTitle()
    {
        return m_part.getTitle();
    }
    
   /**
    * Returns the part description.
    * @return the description
    */
    public String getDescription()
    {
        return m_part.getDescription();
    }
}

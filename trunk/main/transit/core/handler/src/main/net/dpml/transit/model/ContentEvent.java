/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.transit.model;

import java.util.EventObject;

/**
 * A event pertaining to content model changes.
 */
public class ContentEvent extends EventObject
{
    private final ContentModel m_content;

   /**
    * Creation of a new ContentEvent signalling modification of 
    * content director configuration.
    * 
    * @param content the content director that was added or removed
    */
    public ContentEvent( ContentModel content )
    {
        super( content );
        m_content = content;
    }
    
   /**
    * Return the content director that was modified.
    * @return the content director
    */
    public ContentModel getContentModel()
    {
        return m_content;
    }
}

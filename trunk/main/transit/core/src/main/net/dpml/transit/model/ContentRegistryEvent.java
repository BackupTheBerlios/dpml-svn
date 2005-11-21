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
 * An event issued by a registry signalling changes tye addition or removal
 * of a content model.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class ContentRegistryEvent extends EventObject
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final ContentModel m_content;
    private final ContentRegistryModel m_registry;

   /**
    * Creation of a new RegistryEvent signalling the addition
    * or removal of a content model to or from a registry.
    * 
    * @param registry the content registry model
    * @param content the content model that was added or removed
    */   
    public ContentRegistryEvent( ContentRegistryModel registry, ContentModel content )
    {
        super( registry );
        m_content = content;
        m_registry = registry;
    }
    
   /**
    * Return the content registry model initiating the event.
    * @return the content registry model
    */
    public ContentRegistryModel getRegistryModel()
    {
        return m_registry;
    }

   /**
    * Return the content model that was added or removed.
    * @return the content model
    */
    public ContentModel getContentModel()
    {
        return m_content;
    }
}

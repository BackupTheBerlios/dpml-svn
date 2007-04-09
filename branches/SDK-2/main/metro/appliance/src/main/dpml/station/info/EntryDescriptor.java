/*
 * Copyright 2007 Stephen J. McConnell.
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
 
package dpml.station.info;

import dpml.util.ElementHelper;

import java.net.URI;

import net.dpml.lang.DecodingException;
import net.dpml.util.Resolver;

import org.w3c.dom.Element;

/**
 * Immutable datastructure used to describe an deployment scenario.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class EntryDescriptor
{
    private final String m_key;
    private final Element m_element;
    
    public EntryDescriptor( Element element, Resolver resolver )
    {
        m_element = element;
        m_key = ElementHelper.getAttribute( element, "key", null, resolver );
    }
    
   /**
    * Return the element defining the entry descriptor.
    * @return the defining element
    */
    public Element getElement()
    {
        return m_element;
    }
    
   /**
    * Return the key for thiks entry.
    * @return the key
    */
    public String getKey()
    {
        return m_key;
    }
}

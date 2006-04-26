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

import org.w3c.dom.Element;

/**
 * Generic data directives.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class GenericDataDirective extends DataDirective
{
    private final Element m_element;
    
   /**
    * Creation of a new generic data directive.
    * @param element the definining element
    */
    public GenericDataDirective( Element element )
    {
        super( getElementID( element ) );
        
        m_element = element;
    }
    
   /**
    * Return the element defining this datatype.
    * @return the DOM element
    */
    public Element getElement()
    {
        return m_element;
    }
    
    private static String getElementID( Element element )
    {
        if( null == element )
        {
            throw new NullPointerException( "element" );
        }
        else
        {
            return element.getTagName();
        }
    }
}

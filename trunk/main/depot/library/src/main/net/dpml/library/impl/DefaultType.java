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

package net.dpml.library.impl;

import net.dpml.library.info.TypeDirective;
import net.dpml.library.Type;

import net.dpml.util.Logger;

import org.w3c.dom.Element;

/**
 * Internal exception throw to indicate a bad name reference.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultType extends DefaultDictionary implements Type
{
    private final DefaultResource m_resource;
    private final TypeDirective m_directive;
    private final Logger m_logger;
    
   /**
    * Creation of a new DefaultType.
    * @param resource the enclosing resource
    * @param directive the type production directive
    */
    DefaultType( Logger logger, DefaultResource resource, TypeDirective directive )
    {
        super( resource, directive );
        
        m_resource = resource;
        m_directive = directive;
        m_logger = logger;
    }

   /**
    * Get the type identifier.
    * @return the type id
    */
    public String getID()
    {
        return m_directive.getID();
    }
    
   /**
    * Get the type alias flag.
    * @return the type alias flag
    */
    public boolean getAlias()
    {
        return m_directive.getAlias();
    }
    
   /**
    * Get the DOM element defining the produced datatype.
    * @return the DOM element
    */
    public Element getElement()
    {
        return m_directive.getElement();
    }
    
    Logger getLogger()
    {
        return m_logger;
    }
}

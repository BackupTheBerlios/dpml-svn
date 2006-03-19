/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.metro.builder;

import net.dpml.library.Type;

/**
 * Generic implementation of a type.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultType implements Type
{
    private final String m_type;
    private final boolean m_alias;
    private final Object m_data;
    
   /**
    * Creation of a new generic type production handler.
    * @param type the type id
    * @param alias the alias flag
    * @param data associated type datastructure
    */
    public DefaultType( String type, boolean alias, Object data )
    {
        m_type = type;
        m_alias = alias;
        m_data = data;
    }
    
   /**
    * Return the type production id.
    * @return the type id
    */
    public String getID()
    {
        return m_type;
    }

   /**
    * Return the type production alias flag value.
    * @return the alias flag
    */
    public boolean getAlias()
    {
        return m_alias;
    }

   /**
    * Return the type production datatype.
    * @return the datatytpe
    */
    public Object getData()
    {
        return m_data;
    }
}

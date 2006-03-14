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

package net.dpml.lang;

import java.io.Writer;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Map;

import org.w3c.dom.TypeInfo;
import org.w3c.dom.Element;

/**
 * Utility used to build a plugin strategy from a DOM element.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
    
public class DefaultType implements Type
{
    private final String m_type;
    private final boolean m_alias;
    private final Object m_data;
    
    public DefaultType( String type, boolean alias, Object data )
    {
        m_type = type;
        m_alias = alias;
        m_data = data;
    }
    
    public String getID()
    {
        return m_type;
    }

    public boolean getAlias()
    {
        return m_alias;
    }

    public Object getData()
    {
        return m_data;
    }
}

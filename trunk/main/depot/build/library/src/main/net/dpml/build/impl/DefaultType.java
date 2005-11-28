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

package net.dpml.build.impl;

import net.dpml.build.model.Type;
import net.dpml.build.info.TypeDirective;


/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class DefaultType extends DefaultDictionary implements Type
{
    private final TypeDirective m_directive;
    
    DefaultType( DefaultDictionary parent, TypeDirective directive )
    {
        super( parent, directive );
        m_directive = directive;
    }
    
    //----------------------------------------------------------------------------
    // Type
    //----------------------------------------------------------------------------
    
   /**
    * Return the name of the type.
    * @return the name of the type
    */
    public String getName()
    {
        return m_directive.getName();
    }
    
   /**
    * Return the alias association policy.
    * @return true if alias production is required
    */
    public boolean getAlias()
    {
        return m_directive.getAlias();
    }
}

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

package net.dpml.tools.info;

import java.beans.Expression;
import java.beans.BeanDescriptor;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.beans.Encoder;
import java.util.Properties;

import net.dpml.transit.Category;
import net.dpml.transit.util.Enum;

/**
 * The ResourceIncludeDirective class describes a the inclusion of a typed value.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class TaggedIncludeDirective extends ResourceIncludeDirective
{
    public static final Mode KEY = Mode.KEY;
    public static final Mode REF = Mode.REF;
    
    private final Category m_tag;
    
    public TaggedIncludeDirective( Mode mode, Category tag, String value, Properties properties )
    {
        super( mode, value, properties );
        m_tag = tag;
    }
    
    public Category getCategory()
    {
        return m_tag;
    }
    
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof TaggedIncludeDirective ) )
        {
            TaggedIncludeDirective include = (TaggedIncludeDirective) other;
            if( !equals( m_tag, include.m_tag ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }
    
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= super.hashValue( m_tag );
        return hash;
    }
}

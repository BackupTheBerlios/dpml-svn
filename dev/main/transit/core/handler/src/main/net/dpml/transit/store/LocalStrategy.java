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

package net.dpml.transit.store;

/**
 * Interface implemented by removable storage unit.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class LocalStrategy extends Strategy
{
    private String m_classname;
    private boolean m_bootstrap;

    public LocalStrategy( String classname, boolean bootstrap )
    {
        m_classname = classname;
        m_bootstrap = bootstrap;
    }

    public String getClassname()
    {
        return m_classname;
    }

    public boolean isBootstrap()
    {
        return m_bootstrap;
    }

    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof LocalStrategy )
        {
            LocalStrategy local = (LocalStrategy) other;
            if( false == m_classname.equals( local.getClassname() ) )
            {
                return false;
            }
            else if( isBootstrap() != local.isBootstrap() )
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
        int hash = new Boolean( m_bootstrap ).hashCode();
        hash ^= m_classname.hashCode();
        return hash;
    }
}

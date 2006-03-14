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

package net.dpml.part;

import java.io.Serializable;
import java.util.Arrays;

import net.dpml.transit.Value;

public class Plugin implements Serializable
{
    private final String m_classname;
    private final Value[] m_params;
    
    public Plugin( String classname, Value[] params )
    {
        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }
        if( null == params )
        {
            throw new NullPointerException( "params" );
        }
        m_classname = classname;
        m_params = params;
    }
    
    public String getClassname()
    {
        return m_classname;
    }
    
    public Value[] getValues()
    {
        return m_params;
    }
    
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof Plugin )
        {
            Plugin plugin = (Plugin) other;
            if( !m_classname.equals( plugin.m_classname ) )
            {
                return false;
            }
            else
            {
            
                return Arrays.equals( m_params, plugin.m_params );
            }
        }
        else
        {
            return false;
        }
    }
    
    public int hashCode()
    {
        int hash = m_classname.hashCode();
        for( int i=0; i<m_params.length; i++ )
        {
            hash ^= m_params[i].hashCode();
        }
        return hash;
    }
}

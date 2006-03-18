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

import net.dpml.lang.Value;

/**
 * Plugin part strategy implementation datatype.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Plugin implements Serializable
{
    private final String m_classname;
    private final Value[] m_params;
    
   /**
    * Creation of an new plugin datatype.
    * @param classname the target class
    * @param params an array of default value arguments
    */ 
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
    
   /**
    * Get the target classname.
    * @return the cloassname
    */ 
    public String getClassname()
    {
        return m_classname;
    }
    
   /**
    * Get the array of default constructor values.
    * @return the value array
    */ 
    public Value[] getValues()
    {
        return m_params;
    }
    
   /**
    * Test if this instance is equal to the supplied instance.
    * @param other the other instance
    * @return the equality status
    */
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
    
   /**
    * Get the hashcode for this instance.
    * @return the hash value
    */
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

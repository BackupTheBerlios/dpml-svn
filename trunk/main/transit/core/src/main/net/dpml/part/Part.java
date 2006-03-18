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

import net.dpml.lang.Classpath;

/**
 * Part datastructure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Part implements Serializable
{
    private final Info m_info;
    private final Strategy m_strategy;
    private final Classpath m_classpath;
    
   /**
    * Creation of a new part datastructure.
    * @param info the info descriptor
    * @param strategy the part deployment strategy
    * @param classpath the part classpath definition
    */
    public Part( Info info, Strategy strategy, Classpath classpath )
    {
        if( null == info )
        {
            throw new NullPointerException( "info" );
        }
        if( null == strategy )
        {
            throw new NullPointerException( "strategy" );
        }
        if( null == classpath )
        {
            throw new NullPointerException( "classpath" );
        }
        m_info = info;
        m_strategy = strategy;
        m_classpath = classpath;
    }
    
   /**
    * Get the part info descriptor.
    *
    * @return the part info datastructure
    */
    public Info getInfo()
    {
        return m_info;
    }
    
   /**
    * Get the part deployment strategy.
    *
    * @return the strategy definition
    */
    public Strategy getStrategy()
    {
        return m_strategy;
    }
    
   /**
    * Get the part classpath definition.
    *
    * @return the classpath definition
    */
    public Classpath getClasspath()
    {
        return m_classpath;
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
        else if( other instanceof Part )
        {
            Part part = (Part) other;
            if( !m_info.equals( part.m_info ) )
            {
                return false;
            }
            else if( !m_strategy.equals( part.m_strategy ) )
            {
                return false;
            }
            else
            {
                return m_classpath.equals( part.m_classpath );
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
        int hash = m_info.hashCode();
        hash ^= m_strategy.hashCode();
        hash ^= m_classpath.hashCode();
        return hash;
    }
}

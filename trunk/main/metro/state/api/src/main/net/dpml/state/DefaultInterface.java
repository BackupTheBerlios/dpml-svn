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

package net.dpml.state.impl;

import java.io.Serializable;

import net.dpml.state.Interface;

/**
 * Default implementation of an operation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultInterface implements Interface, Serializable
{
    private final String m_classname;
    
   /**
    * Creation of a new interface declaration.
    * @param classname the overriding method name
    * @exception NullPointerException if the operation name is null
    */
    public DefaultInterface( String classname ) throws NullPointerException
    {
        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }
        m_classname = classname;
    }
    
   /**
    * Overriden method returning the interface classname.
    * @return the name (interface classname)
    * @see #getClassname()
    */
    public String getName()
    {
        return getClassname();
    }
    
   /**
    * Return the interface classname.
    * @return the classname
    */
    public String getClassname()
    {
        return m_classname;
    }
    
   /**
    * Return a string representation of the instance.
    * @return the string value
    */
    public String toString()
    {
        return "interface:" + m_classname;
    }
    
   /**
    * Compare this object to another for equality.
    * @param other the other object
    * @return true if the object is equal to this object
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof DefaultInterface )
        {
            DefaultInterface description = (DefaultInterface) other;
            return m_classname.equals( description.m_classname );
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hashcode for this instance.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = getClass().hashCode();
        hash ^= m_classname.hashCode();
        return hash;
    }
}

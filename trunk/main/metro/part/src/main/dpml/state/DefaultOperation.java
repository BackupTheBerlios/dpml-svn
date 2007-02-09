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

package dpml.state;

import java.io.Serializable;

import net.dpml.state.Operation;

/**
 * Default implementation of an operation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultOperation implements Operation, Serializable
{
    private final String m_name;
    private final String m_method;
    
   /**
    * Creation of a new operation.
    * @param name the operation name
    * @exception NullPointerException if the operation name is null
    */
    public DefaultOperation( final String name ) throws NullPointerException
    {
        this( name, null );
    }
    
   /**
    * Creation of a new operation.
    * @param name the operation name
    * @param method the overriding method name
    * @exception NullPointerException if the operation name is null
    */
    public DefaultOperation( final String name, String method ) throws NullPointerException
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        m_name = name;
        m_method = method;
    }
    
   /**
    * Return the action name.
    * @return the name
    */
    public String getName()
    {
        return m_name;
    }
    
   /**
    * Return the optional overriding method name.  If the 
    * value returned is null the method shall be assumed to be the 
    * equivalent of "get[Name]().
    * @return the operation method name
    */
    public String getMethodName()
    {
        return m_method;
    }
    
   /**
    * Return a string representation of the instance.
    * @return the string value
    */
    public String toString()
    {
        return "operation:" + m_name;
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
        else if( other instanceof DefaultOperation )
        {
            DefaultOperation operation = (DefaultOperation) other;
            
            if( !m_name.equals( operation.getName() ) )
            {
                return false;
            }
            else if( null == m_method )
            {
                return null == operation.m_method;
            }
            else
            {
                return m_method.equals( operation.m_method );
            }
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
        hash ^= m_name.hashCode();
        if( null != m_method )
        {
            hash ^= m_method.hashCode();
        }
        return hash;
    }
}

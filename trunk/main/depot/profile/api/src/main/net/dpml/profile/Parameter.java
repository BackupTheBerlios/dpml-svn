/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 2004-2005 Niclas Hedhman.
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

package net.dpml.profile;

import java.io.Serializable;

/**
 * Exception to indicate that there was a profile related error.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: TransitException.java 2786 2005-06-08 03:02:29Z mcconnell@dpml.net $
 */
public class Parameter implements Serializable
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final String m_name;
    private final Serializable m_value;

    /**
     * Construct a new <code>Parameter</code> instance.
     *
     * @param name the parameter name
     * @param value the serializable parameter value (may be null)
     * @exception NullPointerException if the supplied parameter name is null
     */
    public Parameter( final String name, Serializable value ) throws NullPointerException
    {
        m_name = name;
        m_value = value;
    }

   /**
    * Return the name of the parameter.
    * @return the parameter name
    */
    public String getName()
    {
        return m_name;
    }

   /**
    * Return the parameter value.
    * @return the value of the parameter (possibly null)
    */
    public Object getValue()
    {
        return m_value;
    }

    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof Parameter )
        {
            Parameter param = (Parameter) other;
            if( !m_name.equals( param.getName() ) )
            {
                return false;
            }
            else if( null == m_value )
            {
                return null == param.getValue();
            }
            else
            {
                return m_value == param.getValue();
            }
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        int hash = m_name.hashCode();
        if( null == m_value )
        {
            return hash;
        }
        else
        {
            return hash ^= m_value.hashCode();
        }
    }
}



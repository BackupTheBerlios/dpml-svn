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

package net.dpml.transit.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A parameter represents a codebase constructor argument.  A parameter 
 * combines a nbame and a resolvable value.  The name is primarily for management 
 * usage. The associated value is an a resolvable object.  Resolved parameter values take 
 * precendence during the selection of constructor parameter arguments during 
 * instantiation of a codebase plugin.
 * 
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class Parameter implements Serializable
{
   /**
    * Utility operation that consiolidates an array of parameters and supplimentary
    * arguments to an array of objects.
    * 
    * @param params the parameter array
    * @param args supplimentary arguments
    * @return the consolidated argument array
    */
    public static Object[] getArgs( Parameter[] params, Object[] args )
    {
        ArrayList list = new ArrayList();
        for( int i=0; i < params.length; i++ )
        {
            Parameter param = params[i];
            Value value = param.getValue();
            Object object = value.resolve();
            list.add( object );
        }
        for( int i=0; i < args.length; i++ )
        {
            Object value = args[i];
            list.add( value );
        }
        return list.toArray();
    }

   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final String m_key;
    private final Value m_value;

    /**
     * Construct a new <code>Parameter</code> instance.
     *
     * @param key the parameter key
     * @param value the serializable parameter value (may be null)
     * @exception NullPointerException if the supplied parameter key is null
     */
    public Parameter( final String key, Value value ) throws NullPointerException
    {
        m_key = key;
        m_value = value;
    }

   /**
    * Return the name of the parameter.
    * @return the parameter name
    */
    public String getKey()
    {
        return m_key;
    }

   /**
    * Return the parameter value.
    * @return the value of the parameter (possibly null)
    */
    public Value getValue()
    {
        return m_value;
    }

   /**
    * Test if the supplied object is equal to this object.
    * @param other the other object
    * @return TRUE if the objects are equivalent
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof Parameter )
        {
            Parameter param = (Parameter) other;
            if( !m_key.equals( param.getKey() ) )
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

   /**
    * Return the hashcode for the instance.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = m_key.hashCode();
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



/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.util.criteria;

import java.lang.reflect.Constructor;
import java.io.Serializable;

/**
 * A parameter is an immutable class that contains a description 
 * of an allowable parameter within a crieria instance.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Parameter.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class Parameter implements Serializable
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

   /**
    * Return the set of keys corresponding to the supplied set of 
    * parameters.
    * @param params the parameter sequence
    * @return the corresponding keys
    */
    public static String[] getKeys( Parameter[] params )
    {
        String[] keys = new String[ params.length ];
        for( int i=0; i<params.length; i++ )
        {
            keys[i] = params[i].getKey();
        }
        return keys;
    }

    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private final String m_key;
    private final Class m_type;
    private final boolean m_required;
    private final Object m_default;

    //--------------------------------------------------------------
    // constructors
    //--------------------------------------------------------------

    /**
     * Creation of a new required parameter constraint.
     * @param key the parameter key
     * @param type the name of a class constraining assigned values
     */
    public Parameter( 
      final String key, final Class type ) 
    {
        m_key = key;
        m_type = type;
        m_required = true;
        m_default = null;
    }

    /**
     * Creation of a new optional parameter constraint.
     * @param key the parameter key
     * @param type the name of a class constraining assigned values
     * @param value the default value
     */
    public Parameter( 
      final String key, final Class type, Object value ) 
    {
        m_key = key;
        m_type = type;
        m_required = false;
        m_default = value;
    }

    //--------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------

   /**
    * Return the key for the parameter.
    * @return the key
    */
    public String getKey()
    {
        return m_key;
    }

   /**
    * Return the classname for the parameter.
    * @return the classname
    */
    public Class getParameterClass()
    {
        return m_type;
    }

   /**
    * Return TRUE is the parameter is required.
    * @return the required status of the parameter
    */
    public boolean isRequired()
    {
        return m_required;
    }

   /**
    * Return TRUE is the parameter is optional.
    * @return the optional status of the parameter
    */
    public boolean isOptional()
    {
        return !isRequired();
    }

   /**
    * Return the default value for this parameter.
    * @return the default value
    */
    public Object getDefault()
    {
        return m_default;
    }

   /**
    * Resolve a supplied argument to a value.
    * @param value the supplied argument
    * @return the resolved object
    * @exception Exception if an error occurs
    */
    public Object resolve( Object value ) throws CriteriaException
    {
        return resolve( m_type, value );
    }

   /**
    * Resolve a supplied argument to a value.
    * @param type the base class
    * @param value the supplied argument
    * @return the resolved object
    * @exception Exception if an error occurs
    */
    protected Object resolve( Class type, Object value ) throws CriteriaException
    {
        if( value == null ) 
            return null;
        if( type == null ) 
            throw new NullPointerException( "type" );
        if( type.isInstance( value ) )
        {
            return value;
        }
        else
        { 
            Constructor constructor = null;
            try
            {
                constructor = 
                  type.getConstructor( 
                    new Class[]{ value.getClass() } );
            }
            catch( NoSuchMethodException nsme )
            {
                final String error =
                  "Value of class: [" 
                  + value.getClass().getName() 
                  + "] supplied for key [" 
                  + getKey() 
                  + "] is not an instance of type: [" 
                  + type.getName()
                  + "].";
                throw new CriteriaException( error );
            }

            try
            {
                return constructor.newInstance( 
                  new Object[]{ value } );
            }
            catch( Throwable e )
            {
                final String error =
                  "Value of class: [" 
                  + value.getClass().getName() 
                  + "] supplied for key [" 
                  + getKey() 
                  + "] is not an instance of or was not resolvable to the type: [" 
                  + type.getName()
                  + "].";
                throw new CriteriaException( error, e );
            }
        }
    }
}

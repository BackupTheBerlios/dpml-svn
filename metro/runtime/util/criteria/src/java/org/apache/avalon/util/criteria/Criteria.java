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

import java.util.HashMap;

/**
 * A abstract utility class that can be used to simplify the 
 * creation of domain specific criteria.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Criteria.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class Criteria extends HashMap
{
    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

     private final Parameter[] m_params;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------


   /**
    * Creation of a new criteria instance.
    * @param params the set of parameters managed by the criteria
    */
    public Criteria( final Parameter[] params )
    {
        if( null == params ) 
          throw new NullPointerException( "params" );
        m_params = params;
    }

    //--------------------------------------------------------------
    // Criteria
    //--------------------------------------------------------------

   /**
    * Set a named parameter of the criteria to a value.
    * @param key the parameter key
    * @param value the value to assign to the key
    * @return the original value
    * @exception CriteriaRuntimeException if the supplied value fails
    *    the validation test for its associated parameter
    */
    public Object put( final Object key, final Object value ) 
    {
        if( !(key instanceof String ))
        {
            final String error = 
              "Invalid key: " + key;
            throw new IllegalArgumentException( error );
        }

        Object current = super.get( key );

        if( null == value )
        {
            super.put( key, null );
            return current;
        }

        final Parameter p = getParameter( (String) key );

        try
        {
            final Object v = p.resolve( value );
            if( p.getParameterClass().isInstance( v ) )
            {
                super.put( key, v );
                return current;
            }
            else
            {
                final String error = 
                  "Resolved value: " + v 
                  + " does not implement the parameter type: "
                  + p.getParameterClass();
                throw new IllegalArgumentException( error );
            }
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to assign a value to the key: " + key;
            throw new CriteriaRuntimeException( error, e );
        }
    }

   /**
    * Return the currently assigned value for a key.
    * @return the assigned value
    */
    public Object get( final Object key )
    {
        Parameter param = getParameterFromObject( key );
        Object value = super.get( param.getKey() );
        if( null != value )
        {
            return value;
        }
        else
        {
            return param.getDefault();
        }
    }

    //--------------------------------------------------------------
    // private and protected
    //--------------------------------------------------------------

   /**
    * Return the currently assigned value for a key.
    * @return the assigned value
    */
    protected Object getValue( final Parameter param )
    {
        return get( param.getKey() );
    }

   /**
    * Return the parameter keys associated with the criteria.
    * @return the keys
    */
    protected String[] getKeys()
    {
        return Parameter.getKeys( getParameters() );
    }

   /**
    * Return the parameter associated to the criteria.
    * @return the parameters
    */
    protected Parameter[] getParameters()
    {
        return m_params;
    }

    protected Parameter getParameter( final String key )
    {
        Parameter[] params = getParameters();
        for( int i=0; i<params.length; i++ )
        {
            Parameter parameter = params[i];
            if( parameter.getKey().equals( key ) ) return parameter;
        }

        final String error = 
          "Unknown key: [" + key + "].";
        throw new IllegalArgumentException( error );
    }

    private Parameter getParameterFromObject( Object key )
    {
        if( key instanceof Parameter )
        {
            return (Parameter) key;
        }
        else
        {
            return getParameter( key.toString() );
        }
    }


}

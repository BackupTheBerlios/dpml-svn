/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.metro.tools;

import java.util.ArrayList;
import java.util.List;

import net.dpml.transit.Value;
import net.dpml.transit.Construct;
import net.dpml.transit.Construct.Array;

/**
 * Defintion of a context entry parameter directive.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ArrayDataType implements ValueBuilder
{
    private String m_classname;
    private List m_params = new ArrayList();

   /**
    * Set the context entry classname.
    * @param classname the context entry classname
    */
    public void setClass( final String classname )
    {
        System.out.println( "SET CLASSNAME: " + classname );
        m_classname = classname;
    }

   /**
    * Return the context entry parameter classname.
    * @return the classname
    */
    public String getClassname()
    {
        return m_classname;
    }

   /**
    * Create, assign and return a new nested entry constructor parameter.
    * @return the new context entry param
    */
    public ValueDataType createValue()
    {
        final ValueDataType param = new ValueDataType();
        m_params.add( param );
        return param;
    }

   /**
    * Create, assign and return a new nested array.
    * @return the new array parameter
    */
    public ArrayDataType createArray()
    {
        System.out.println( "CREATE ARRAY" );
        final ArrayDataType param = new ArrayDataType();
        m_params.add( param );
        return param;
    }
    
   /**
    * Return the set of nested param directives.
    * @return the params
    */
    private ValueBuilder[] getValueBuilders()
    {
        return (ValueBuilder[]) m_params.toArray( new ValueBuilder[0] );
    }
    
   /**
    * Build a value datastructure.
    * @param classname the default classname
    * @return the serializable value descriptor
    */
    public Value buildValue( String classname )
    {
        String name = getClassname( classname );
        ValueBuilder[] params = getValueBuilders();
        Value[] values = new Value[ params.length ];
        for( int i=0; i<values.length; i++ )
        {
            ValueBuilder p = params[i];
            values[i] = p.buildValue( name );
        }
        return new Array( name, values );
    }
    
    String getClassname( String fallback )
    {
        if( null == m_classname )
        {
            return fallback;
        }
        else
        {
            return m_classname;
        }
    }
}

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

import net.dpml.metro.data.ValueDirective;

import net.dpml.transit.Construct;
import net.dpml.transit.Value;

/**
 * Defintion of a context entry parameter directive.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ValueDataType implements ValueBuilder
{
    private String m_classname;
    private String m_method;
    private String m_value;
    private List m_params = new ArrayList();

   /**
    * Set the context entry classname.
    * @param classname the context entry classname
    */
    public void setClass( final String classname )
    {
        m_classname = classname;
    }

   /**
    * Set the method name.
    * @param method the name of a static method
    */
    public void setMethod( final String method )
    {
        m_method = method;
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
    * Return the context entry parameter classname.
    * @return the classname
    */
    String getMethodName()
    {
        return m_method;
    }

   /**
    * Set the value of the context entry parameter.
    * @param value the param value
    */
    public void setValue( final String value )
    {
        m_value = value;
    }

   /**
    * Return the value of the context entry param.
    * @return the value
    */
    public String getValue()
    {
        return m_value;
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
    * Create, assign and return a new nested array datatype.
    * @return the new array datatype
    */
    public ArrayDataType createArray()
    {
        final ArrayDataType param = new ArrayDataType();
        m_params.add( param );
        return param;
    }

   /**
    * Return the set of nested param directives.
    * @return the params
    */
    public ValueBuilder[] getValueBuilders()
    {
        return (ValueBuilder[]) m_params.toArray( new ValueBuilder[0] );
    }

   /**
    * Build a value datastructure.
    * @return the serializable value descriptor
    */
    public Value buildValue()
    {
        String classname = getClassname();
        String method = getMethodName();
        String value = getValue();
        if( null != value )
        {
            return new ValueDirective( classname, method, value );
        }
        else
        {
            ValueBuilder[] params = getValueBuilders();
            Value[] values = new Value[ params.length ];
            for( int i=0; i<values.length; i++ )
            {
                ValueBuilder p = params[i];
                values[i] = p.buildValue();
            }
            return new ValueDirective( classname, method, values );
        }
    }
}

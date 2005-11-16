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

package net.dpml.metro.runtime.tools.datatypes;

import java.util.ArrayList;
import java.util.List;

import net.dpml.parameters.Parameters;
import net.dpml.parameters.impl.DefaultParameters;

import org.apache.tools.ant.Project;


/**
 * A datatype supporting the creation of a parameters instance.
 */
public class ParametersDataType
{
    private final List m_parameters = new ArrayList();
    private final Project m_project;

    public ParametersDataType( Project project )
    {
        m_project = project;
    }

   /**
    * Create, allocate and return a new parameter with this set of parameters.
    * @return a new parameter directive
    */
    public ParameterDataType createParameter()
    {
        final ParameterDataType parameter = new ParameterDataType( m_project );
        m_parameters.add( parameter );
        return parameter;
    }

   /**
    * Return the set of parameter directives declarared within this parameters directives.
    * @return the set of parameter directives
    */
    public ParameterDataType[] getParameterDataTypes()
    {
        return (ParameterDataType[]) m_parameters.toArray( new ParameterDataType[0] );
    }

    public Parameters getParameters()
    {
        DefaultParameters parameters = new DefaultParameters();
        ParameterDataType[] params = getParameterDataTypes();
        for( int i=0; i<params.length; i++ )
        {
            ParameterDataType p = params[i];
            String name = p.getName();
            String value = p.getValue();
            parameters.setParameter( name, value );
        }
        return parameters;
    }

   /**
    * A parameter directive.
    */
    public static class ParameterDataType
    {
        private final Project m_project;

        private String m_name;
        private String m_value;

        public ParameterDataType( Project project )
        {
            m_project = project;
        }

       /**
        * Set the parameter name.
        * @param name the parameter name
        */
        public void setName( final String name )
        {
            m_name = name;
        }

       /**
        * Set the value assigned to the named parameter.
        * @param value the parameter value
        */
        public void setValue( final String value )
        {
            m_value = m_project.replaceProperties( value );
        }

       /**
        * Return the parameter name.
        * @return the parameter name
        */
        public String getName()
        {
            return m_name;
        }

       /**
        * Return the value assigned to the parameter.
        * @return the parameter value
        */
        public String getValue()
        {
            return m_value;
        }
    }
}

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

package org.apache.avalon.composition.data;

import java.io.Serializable;

/**
 * A <code>Parameter</code> represents a single constructor typed argument value.  A parameter
 * container a classname (default value of <code>java.lang.String</code>) and possible sub-parameters.
 * A parameter's value is established by creating a new instance using the parameter's classname,
 * together with the values directived from the sub-sidiary parameters as constructor arguments.
 *
 * <p><b>XML</b></p>
 * <p>A param is a nested structure containing a string value or contructor parameter arguments.</p>
 * <pre>
 *    <font color="gray">&lt;-- Simple string param declaration --&gt;</font>
 *
 *    &lt;param&gt;<font color="darkred">London</font>&lt;/param&gt;
 *
 *    <font color="gray">&lt;-- Typed param declaration --&gt;</font>
 *
 *    &lt;param class="<font color="darkred">java.io.File</font>"&gt;<font color="darkred">./home</font>&lt;/param&gt;
 *
 *    <font color="gray">&lt;-- Typed parameter declaration referencing a context value --&gt;</font>
 *
 *    &lt;param class="<font color="darkred">java.lang.ClassLoader</font>"&gt;<font color="darkred">${my-classloader-import-key}</font>&lt;/param&gt;
 *
 *    <font color="gray">&lt;-- Multi-argument parameter declaration --&gt;</font>
 *
 *    &lt;param class="<font color="darkred">MyClass</font>"&gt;
 *       &lt;param class="<font color="darkred">java.io.File</font>"><font color="darkred">./home</font>&lt;/param&gt;
 *       &lt;param&gt;<font color="darkred">London</font>&lt;/param&gt;
 *    &lt;/param&gt;
 * </pre>
 *
 * <p>TODO: Fix usage of basic type (int, float, long, etc.) - how do we return 
 *   basic types - can't use getValue() becuase it returns an Object unless
 *   have some way of packing the basic type into a carrier</p>
 *
 * @see EntryDirective
 * @see ImportDirective
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Parameter.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class Parameter implements Serializable
{

    /**
     * The classname to use as the parameter implementation class (defaults to java.lang.String)
     */
    private final String m_classname;

    /**
     * The supplied argument.
     */
    private String m_argument;

    /**
     * The sub-parameters from which the value for this parameter may be derived.
     */
    private final Parameter[] m_parameters;

    /**
     * The derived value.
     */
    private transient Object m_value;

    /**
     * Creation of a new parameter using the default <code>java.lang.String</code>
     * type and a supplied value.
     *
     * @param value the string value
     */
    public Parameter( final String value )
    {
        m_classname = "java.lang.String";
        m_parameters = new Parameter[ 0 ];
        m_argument = value;
    }

    /**
     * Creation of a new entry directive using a supplied classname and value.
     * @param classname the classname of the parameter
     * @param value the parameter constructor value
     */
    public Parameter( final String classname, final String value )
    {
        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }

        m_classname = classname;
        m_parameters = new Parameter[ 0 ];
        m_argument = value;
    }

    /**
     * Creation of a new entry directive.
     * @param classname the classname of the entry implementation
     * @param parameters implementation class constructor parameter directives
     */
    public Parameter( final String classname, final Parameter[] parameters )
    {
        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }
        if( null == parameters )
        {
            throw new NullPointerException( "parameters" );
        }

        m_classname = classname;
        m_parameters = parameters;
    }

    /**
     * Return the classname of the parameter implementation to use.
     * @return the classname
     */
    public String getClassname()
    {
        return m_classname;
    }

    /**
     * Return the argument (may be null).
     */
    public String getArgument()
    {
        return m_argument;
    }

    /**
     * Return the constructor parameters for this parameter.
     */
    public Parameter[] getParameters()
    {
        return m_parameters;
    }
}

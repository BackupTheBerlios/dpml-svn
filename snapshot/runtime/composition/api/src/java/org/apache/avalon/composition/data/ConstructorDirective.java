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


/**
 * A entry descriptor declares the context entry import or creation criteria for
 * a single context entry instance.
 *
 * <p><b>XML</b></p>
 * <p>A entry may contain either (a) a single nested import directive, or (b) a single param constructor directives.</p>
 * <pre>
 *  <font color="gray">&lt;context&gt;</font>
 *
 *    &lt!-- option (a) nested import -->
 *    &lt;entry key="<font color="darkred">my-home-dir</font>"&gt;
 *       &lt;include key="<font color="darkred">urn:avalon:home</font>"/&gt;
 *    &lt;/entry&gt;
 *
 *    &lt!-- option (b) param constructors -->
 *    &lt;entry key="<font color="darkred">title</font>"&gt;
 *       &lt;param&gt;<font color="darkred">Lord of the Rings</font>&lt;/&gt;
 *    &lt;/entry&gt;
 *    &lt;entry key="<font color="darkred">home</font>"&gt;
 *      &lt;param class="<font color="darkred">java.io.File</font>"&gt;<font color="darkred">../home</font>&lt;/param&gt;
 *    &lt;/entry&gt;
 *
 *  <font color="gray">&lt;/context&gt;</font>
 * </pre>
 *
 * @see ImportDirective
 * @see Parameter
 * @see ContextDirective
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ConstructorDirective.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ConstructorDirective extends EntryDirective
{
    /**
     * The constructor classname.
     */
    private final String m_classname;

    /**
     * The constructor param.
     */
    private final Parameter[] m_params;

    /**
     * The alternative argument.
     */
    private final String m_argument;

    /**
     * Creation of a new entry directive using a constructor
     * classname and single argument value.
     * @param key the entry key
     * @param value the single argument value
     */
    public ConstructorDirective( 
      final String key, final String value )
    {
        this( key, "java.lang.String", value );
    }

    /**
     * Creation of a new entry directive using a constructor
     * classname and single argument value.
     * @param key the entry key
     * @param classname the classname of the entry implementation
     * @param value the single argument value
     */
    public ConstructorDirective( 
      final String key, final String classname, final String value )
    {
        super( key );

        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }

        m_params = new Parameter[0];
        m_classname = classname;
        m_argument = value;
    }

    /**
     * Creation of a new entry directive using a parameter.
     * @param key the entry key
     * @param parameters implementation class constructor parameter directives
     */
    public ConstructorDirective( final String key, final Parameter[] parameters )
    {
        this( key, "java.lang.String", parameters );
    }

    /**
     * Creation of a new entry directive using a parameter.
     * @param key the entry key
     * @param classname the classname of the entry implementation
     * @param params implementation class constructor parameter directives
     */
    public ConstructorDirective( 
      final String key, final String classname, final Parameter[] params )
    {
        super( key );

        if( null == params )
        {
            throw new NullPointerException( "parameters" );
        }
        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }

        m_classname = classname;
        m_params = params;
        m_argument = null;
    }

    /**
     * Return the constructor classname
     * @return the classname
     */
    public String getClassname()
    {
        return m_classname;
    }

    /**
     * Return the parameter directive if the mode is PARAM else null.
     * @return the directive
     */
    public Parameter[] getParameters()
    {
        return m_params;
    }

    /**
     * Return the constructor single argument
     * @return the costructor argument
     */
    public String getArgument()
    {
        return m_argument;
    }
}

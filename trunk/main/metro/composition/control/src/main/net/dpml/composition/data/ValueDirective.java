/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.composition.data;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import net.dpml.part.Part;

/**
 * A <code>Value</code> represents a single constructor typed argument value. The directive
 * container a classname (default value of <code>java.lang.String</code>) and possible sub-directives.
 * The directives value is established by creating a new instance using the classname
 * together with the values directived from the sub-sidiary directives as constructor arguments.
 *
 * <p><b>XML</b></p>
 * <p>A value is a nested structure containing a string value or contructor parameter arguments.</p>
 * <pre>
 *    <font color="gray">&lt;-- Simple string param declaration --&gt;</font>
 *
 *    &lt;value&gt;<font color="darkred">London</font>&lt;/value&gt;
 *
 *    <font color="gray">&lt;-- Typed value declaration --&gt;</font>
 *
 *    &lt;value class="<font color="darkred">java.io.File</font>"&gt;<font color="darkred">./home</font>&lt;/value&gt;
 *
 *    <font color="gray">&lt;-- Multi-argument parameter declaration --&gt;</font>
 *
 *    &lt;value class="<font color="darkred">MyClass</font>"&gt;
 *       &lt;value class="<font color="darkred">java.io.File</font>"><font color="darkred">./home</font>&lt;/value&gt;
 *       &lt;value&gt;<font color="darkred">London</font>&lt;/value&gt;
 *    &lt;/value&gt;
 * </pre>
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: Parameter.java 2119 2005-03-23 02:04:46Z mcconnell@dpml.net $
 */
public class ValueDirective implements Part, Serializable
{
    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final String m_key;
    private final String m_classname;
    private final String m_local;
    private final Value[] m_values;

    //--------------------------------------------------------------------------
    // constructors
    //--------------------------------------------------------------------------

    public ValueDirective( String key, String classname, String value )
    {
        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        m_key = key;
        m_classname = classname;
        m_local = value;

        m_values = new Value[0];
    }

    public ValueDirective( String key, String classname, Value[] values )
    {
        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }
        if( null == values )
        {
            throw new NullPointerException( "values" );
        }
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        m_key = key;
        m_classname = classname;
        m_values = values;
        m_local = null;
    }

    //--------------------------------------------------------------------------
    // Part
    //--------------------------------------------------------------------------

    /**
     * Return the part handler uri.
     * @return the uri of the part handler
     */
     public URI getPartHandlerURI()
     {
         return PART_HANDLER_URI;
     }

    //--------------------------------------------------------------------------
    // ValueDirective
    //--------------------------------------------------------------------------

    public String getKey()
    {
        return m_key;
    }

    public String getClassname()
    {
        return m_classname;
    }

    public String getLocalValue()
    {
        return m_local;
    }

    public Value[] getValues()
    {
        return m_values;
    }

    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( false == ( other instanceof ValueDirective ) )
        {
            return false;
        }
        else
        {
            ValueDirective value = (ValueDirective) other;
            if( false == equals( m_key, value.getKey() ) )
            {
                return false;
            }
            else if( false == equals( m_classname, value.getClassname() ) )
            {
                return false;
            }
            else if( false == equals( m_local, value.getLocalValue() ) )
            {
                return false;
            }
            else if( m_values.length != value.getValues().length )
            {
                return false;
            }
            else
            {
                Value[] mine = getValues();
                Value[] yours = value.getValues();
                for( int i=0; i<mine.length; i++ )
                {
                    Value a = mine[i];
                    Value b = yours[i];
                    if( false == equals( a, b ) )
                    {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    public int hashCode()
    {
        int hash = m_key.hashCode();
        if( null != m_classname )
        {
            hash ^= m_classname.hashCode();
        }
        if( null != m_local )
        {
            hash ^= m_local.hashCode();
        }
        for( int i=0; i<m_values.length; i++ )
        {
            hash ^= m_values[i].hashCode();
        }
        return hash;
    }

    private boolean equals( Object o1, Object o2 )
    {
        if( null == o1 )
        {
            return ( null == o2 );
        }
        else 
        {
            return o1.equals( o2 );
        }
    }

    //--------------------------------------------------------------------------
    // static utils
    //--------------------------------------------------------------------------

    public static class Value implements Serializable
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
         * The sub-directives from which the value for this value directive may be derived.
         */
        private final Value[] m_children;

        /**
         * Creation of a new value directive using the default <code>java.lang.String</code>
         * type and a supplied value.
         *
         * @param value the string value
         */
        public Value( final String value )
        {
            m_classname = "java.lang.String";
            m_children = new Value[ 0 ];
            m_argument = value;
        }

        /**
         * Creation of a new entry directive using a supplied classname and value.
         * @param classname the classname of the value
         * @param value the value constructor value
         */
        public Value( final String classname, final String value )
        {
            if( null == classname )
            {
                m_classname = "java.lang.String";
            }
            else
            {
                m_classname = classname;
            }
            m_children = new Value[ 0 ];
            m_argument = value;
        }

        /**
         * Creation of a new entry directive.
         * @param classname the classname of the entry implementation
         * @param children implementation class constructor directives
         * @exception NullArgumentException if either the classname argument or the
         *           children argument is null, or any of the elements in the
         *           children array is null.
         */
        public Value( final String classname, final Value[] children )
            throws NullPointerException
        {
            if( null == classname )
            {
                throw new NullPointerException( "classname" );
            }
            if( null == children )
            {
                throw new NullPointerException( "children" );
            }
            for( int i = 0; i < children.length; i++ )
            {
                if( children[ i ] == null )
                {
                    throw new NullPointerException( "child [" + i + "]" );
                }
            }
            m_classname = classname;
            m_children = children;
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
         * Return the constructor descriptors for this value descriptor.
         */
        public Value[] getValueDirectives()
        {
            return m_children;
        }
    
       /**
        * Test if the supplied object is equal to this object.
        * @param other the object to compare with this instance
        * @return TRUE if the supplied object is equal to this object
        */
        public boolean equals( Object other )
        {
            if( null == other )
            {
                return false;
            }
            else
            {
                if( other instanceof Value )
                {
                    Value param = (Value) other;
                    if( false == m_classname.equals( param.getClassname() ) )
                    {
                        return false;
                    }
                    if( null == m_argument )
                    {
                        if( null != param.getArgument() )
                        {
                            return false;
                        }
                    }
                    else if( false == m_argument.equals( param.getArgument() ) )
                    {
                        return false;
                    }
                    if( getValueDirectives().length != param.getValueDirectives().length )
                    {
                        return false;
                    }
                    else
                    {
                        Value[] myParams = getValueDirectives();
                        Value[] yourParams = param.getValueDirectives();
                            for( int i=0; i<myParams.length; i++ )
                        {
                            Value p = myParams[i];
                            Value q = yourParams[i];
                            if( false == p.equals( q ) )
                            {
                                return false;
                            }
                        }
                        return true;
                    }
                }
                else
                {
                    return false;
                }
            }
        }
    
       /**
        * Return the hashcode for the instance.
        * @return the instance hashcode
        */
        public int hashCode()
        {
            int hash = m_classname.hashCode();
            if( null != m_argument )
            {
                hash ^= m_argument.hashCode();
            }
            for( int i=0; i<m_children.length; i++ )
            {
                hash ^= m_children[i].hashCode();
            }
            return hash;
        }
    }

    private static URI PART_HANDLER_URI = setupURI( "@PART-HANDLER-URI@" );
    private static URI PART_BUILDER_URI = setupURI( "@PART-BUILDER-URI@" );

    protected static URI setupURI( String spec )
    {
        try
        {
            return new URI( spec );
        }
        catch( URISyntaxException ioe )
        {
            return null;
        }
    }
}

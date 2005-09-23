/*
 * Copyright 2004-2005 Stephen J. McConnell.
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
    // static
    //--------------------------------------------------------------------------

   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final String m_classname;
    private final String m_local;
    private final ValueDirective[] m_values;
    private final URI m_handler;

    //--------------------------------------------------------------------------
    // constructors
    //--------------------------------------------------------------------------

    public ValueDirective( String value )
    {
        this( String.class.getName(), value );
    }

    public ValueDirective( String classname, String value )
    {
        this( PART_HANDLER_URI, classname, value );
    }
    
    public ValueDirective( URI handler, String classname, String value )
    {
        if( null == handler )
        {
            throw new NullPointerException( "handler" );
        }
        if( null == classname )
        {
            m_classname = String.class.getName();
        }
        else
        {
            m_classname = classname;
        }
        m_local = value;
        m_values = new ValueDirective[0];
        m_handler = handler;
    }

    public ValueDirective( String classname, ValueDirective[] values )
    {
        this( PART_HANDLER_URI, classname, values );
    }
    
    public ValueDirective( URI handler, String classname, ValueDirective[] values )
    {
        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }
        if( null == handler )
        {
            throw new NullPointerException( "handler" );
        }
        if( null == values )
        {
            throw new NullPointerException( "values" );
        }
        m_handler = handler;
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
         return m_handler;
     }

    //--------------------------------------------------------------------------
    // ValueDirective
    //--------------------------------------------------------------------------

    public String getClassname()
    {
        return m_classname;
    }

    public String getLocalValue()
    {
        return m_local;
    }

    public ValueDirective[] getValues()
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
            if( false == equals( m_classname, value.getClassname() ) )
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
                ValueDirective[] mine = getValues();
                ValueDirective[] yours = value.getValues();
                for( int i=0; i<mine.length; i++ )
                {
                    ValueDirective a = mine[i];
                    ValueDirective b = yours[i];
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
        int hash = m_classname.hashCode();
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

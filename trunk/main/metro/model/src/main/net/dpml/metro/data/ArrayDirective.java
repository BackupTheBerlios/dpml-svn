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

package net.dpml.metro.data;

import java.net.URI;
import java.net.URISyntaxException;
import java.lang.reflect.Array;
import java.util.Map;
import java.util.Arrays;

import net.dpml.transit.Value;
import net.dpml.transit.Construct;

import net.dpml.part.Directive;

/**
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ArrayDirective implements Directive, Value
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
    private final Value[] m_values;
    
    //--------------------------------------------------------------------------
    // constructors
    //--------------------------------------------------------------------------
   /**
    * Create a new array directive.
    *
    * @param classname the array type
    * @param values the construct values
    */
    public ArrayDirective( String classname, Value[] values )
    {
        if( null == classname )
        {
            m_classname = Object.class.getName();
        }
        else
        {
            m_classname = classname;
        }
        
        m_values = values;
    }
    
    //--------------------------------------------------------------------------
    // ArrayDirective
    //--------------------------------------------------------------------------

    public String getClassname()
    {
        return m_classname;
    }
    
    public Value[] getValues()
    {
        return m_values;
    }
    
    //--------------------------------------------------------------------------
    // Value
    //--------------------------------------------------------------------------
    
   /**
    * Resolve an instance from the value using the context classloader.
    * @return the resolved instance
    * @exception Exception if error occurs during instance resolution
    */
    public Object resolve() throws Exception
    {
        return resolve( null, false );
    }
    
   /**
    * Resolve an instance from the value using a supplied context map. If any 
    * target expressions in immediate or nested values contain a symbolic
    * expression the value will be resolved using the supplied map.
    *
    * @param map the context map
    * @return the resolved instance
    * @exception Exception if error occurs during instance resolution
    */
    public Object resolve( Map map ) throws Exception
    {
        return resolve( map, false );
    }

   /**
    * Resolve an instance from the value using a supplied isolvation policy. 
    *
    * @param isolate the isolation policy
    * @return the resolved instance
    * @exception Exception if error occurs during instance resolution
    */
    public Object resolve( boolean isolate ) throws Exception
    {
        return resolve( null, isolate );
    }

   /**
    * Resolve an instance from the value using a supplied context map. If any 
    * target expressions in immediate or nested values contain a symbolic
    * expression the value will be resolved using the supplied map.
    *
    * @param map the context map
    * @param isolate the isolation policy
    * @return the resolved instance
    * @exception Exception if error occurs during instance resolution
    */
    public Object resolve( Map map, boolean isolate ) throws Exception
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class c = loader.loadClass( m_classname );
        Object[] instances = (Object[]) Array.newInstance( c, m_values.length );
        for( int i=0; i<m_values.length; i++ )
        {
            Value value = m_values[i];
            instances[i] = value.resolve( map, isolate );
        }
        return instances;
    }
    
    //--------------------------------------------------------------------------
    // Object
    //--------------------------------------------------------------------------

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
        if( !( other instanceof ArrayDirective ) )
        {
            return false;
        }
        else
        {
            ArrayDirective directive = (ArrayDirective) other;
            if( !m_classname.equals( directive.m_classname ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( m_values, directive.m_values );
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
        for( int i=0; i<m_values.length; i++ )
        {
            hash ^= m_values[i].hashCode();
        }
        return hash;
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

    private static final URI PART_HANDLER_URI = setupURI( "@PART-HANDLER-URI@" );

   /**
    * Internal utility to create a static uri.
    * @param spec the uri spec
    * @return the uri
    */
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

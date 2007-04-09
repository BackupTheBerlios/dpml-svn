/*
 * Copyright 2005 Stephen J. McConnell
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

package dpml.library.info;

import java.util.Properties;

import dpml.util.Category;

/**
 * The IncludeDirective class describes a dependency on a named resource.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class IncludeDirective extends AbstractDirective
{
   /**
    * Current module scoped key mode constant.
    */
    public static final Mode KEY = Mode.KEY;
    
   /**
    * Absolute resource reference mode constant.
    */
    public static final Mode REF = Mode.REF;
    
   /**
    * URN resource reference mode constant.
    */
    public static final Mode URI = Mode.URI;
    
    private final Mode m_mode;
    private final String m_value;
    private final Category m_category;
    
   /**
    * Creation of a new include directive.
    * @param mode the include mode
    * @param category the runtime category
    * @param value the value (key or reference address depending on mode)
    * @param properties supplimentary properties
    */
    public IncludeDirective( Mode mode, Category category, String value, Properties properties )
    {
        super( properties );
        
        if( null == mode )
        {
            throw new NullPointerException( "mode" );
        }
        if( null == value )
        {
            throw new NullPointerException( "value" );
        }
        if( null == category )
        {
            m_category = Category.PRIVATE; // was PRIVATE the changed to UNDEFINED
        }
        else
        {
            m_category = category;
        }
        m_mode = mode;
        m_value = value;
    }
    
   /**
    * Return the include mode.
    * @return the mode
    */
    public Mode getMode()
    {
        return m_mode;
    }
    
   /**
    * Return the category associated with the include.
    * @return the category
    */
    public Category getCategory()
    {
        return m_category;
    }

   /**
    * Return the include value.
    * @return the value
    */
    public String getValue()
    {
        return m_value;
    }

   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof IncludeDirective ) )
        {
            IncludeDirective include = (IncludeDirective) other;
            if( !equals( m_mode, include.m_mode ) )
            {
                return false;
            }
            if( !equals( m_category, include.m_category ) )
            {
                return false;
            }
            else
            {
                return equals( m_value, include.m_value );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hash value.
    * @return the hascode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= super.hashValue( m_mode );
        hash ^= super.hashValue( m_category );
        hash ^= super.hashValue( m_value );
        return hash;
    }
   
   /**
    * Return a string representation of the include.
    * @return the string value
    */
    public String toString()
    {
        return "include:" + m_mode + ":" + m_category + ":" + m_value;
    }
    
   /**
    * Mode of inclusion.
    */
    public enum Mode
    {
       /**
        * Include by reference to a local key.
        */
        KEY,

       /**
        * Include by reference to an absolute resource address.
        */
        REF,
    
       /**
        * Include by urn definition.
        */
        URI;
    }
}

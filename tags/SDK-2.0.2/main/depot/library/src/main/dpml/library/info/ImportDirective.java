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

/**
 * The ImportDirective class describes a the import of resource via a file or uri reference.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ImportDirective extends AbstractDirective
{
   /**
    * URI strategy constant.
    */
    public static final Mode URI = Mode.URI;
    
   /**
    * File strategy constant.
    */
    public static final Mode FILE = Mode.FILE;
    
    private Mode m_mode;
    private final String m_value;
    
   /**
    * Creation of a new import directive.
    * @param mode the import mode
    * @param value the value (file or uri depending on mode)
    */
    public ImportDirective( Mode mode, String value )
    {
        this( mode, value, null );
    }
    
   /**
    * Creation of a new import directive.
    * @param mode the import mode
    * @param value the value (file or uri depending on mode)
    * @param properties supplimentary properties
    */
    public ImportDirective( Mode mode, String value, Properties properties )
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

        m_mode = mode;
        m_value = value;
    }
    
   /**
    * Return the import mode.
    * @return the mode
    */
    public Mode getMode()
    {
        return m_mode;
    }
    
   /**
    * Return the import value.
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
        if( super.equals( other ) && ( other instanceof ImportDirective ) )
        {
            ImportDirective object = (ImportDirective) other;
            if( !equals( m_mode, object.m_mode ) )
            {
                return false;
            }
            else
            {
                return equals( m_value, object.m_value );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hash value.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= super.hashValue( m_mode );
        hash ^= super.hashValue( m_value );
        return hash;
    }

   /**
    * Mode of inclusion.
    */
    public enum Mode
    {
       /**
        * File include stratgy constant.
        */
        FILE,

       /**
        * URI include stratgy constant.
        */
        URI;
    }
}

/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.transit;

import java.beans.Expression;
import java.beans.BeanDescriptor;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.beans.Encoder;

import net.dpml.transit.util.ValuedEnum;

/**
 * Classoader category enumeration.
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public final class Category extends ValuedEnum
{
    static final long serialVersionUID = 1L;

   /**
    * System category.
    */
    public static final Category SYSTEM = new Category( "system", 0 );

   /**
    * API category.
    */
    public static final Category PUBLIC = new Category( "public", 1 );

   /**
    * SPI category.
    */
    public static final Category PROTECTED = new Category( "protected", 2 );

   /**
    * Implementation category.
    */
    public static final Category PRIVATE = new Category( "private", 3 );

   /**
    * Array of scope enumeration values.
    */
    private static final Category[] ENUM_VALUES = new Category[]{ SYSTEM, PUBLIC, PROTECTED, PRIVATE };

   /**
    * Returns an array of activation enum values.
    * @return the activation policies array
    */
    public static Category[] values()
    {
        return ENUM_VALUES;
    }
    
   /**
    * Internal constructor.
    * @param label the enumeration label.
    * @param index the enumeration index.
    * @param map the set of constructed enumerations.
    */
    private Category( String label, int index )
    {
        super( label, index );
    }
    
    public String toString()
    {
        return getName().toUpperCase();
    }
    
    public static Category parse( String value )
    {
        if( value.equalsIgnoreCase( "system" ) )
        {
            return SYSTEM;
        }
        else if( value.equalsIgnoreCase( "public" ) )
        {
            return PUBLIC;
        }
        else if( value.equalsIgnoreCase( "protected" ))
        {
            return PROTECTED;
        }
        else if( value.equalsIgnoreCase( "private" ))
        {
            return PRIVATE;
        }
        else
        {
            final String error =
              "Unrecognized category argument [" + value + "]";
            throw new IllegalArgumentException( error );
        }
    }
}


/*
 * Copyright 2004-2006 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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

package net.dpml.metro.info;

import net.dpml.lang.Enum;

/**
 * Lifestyle policy enumeration.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class LifestylePolicy extends Enum
{
    static final long serialVersionUID = 1L;

   /**
    * Transient lifestyle policy.
    */
    public static final LifestylePolicy TRANSIENT = new LifestylePolicy( "transient" );

   /**
    * Per-thread lifestyle policy.
    */
    public static final LifestylePolicy THREAD = new LifestylePolicy( "thread" );

   /**
    * Singleton lifestyle policy.
    */
    public static final LifestylePolicy SINGLETON = new LifestylePolicy( "singleton" );

   /**
    * Array of static activation policy enumeration values.
    */
    private static final LifestylePolicy[] ENUM_VALUES = 
      new LifestylePolicy[]{TRANSIENT, THREAD, SINGLETON};

   /**
    * Returns an array of activation enum values.
    * @return the activation policies array
    */
    public static LifestylePolicy[] values()
    {
        return ENUM_VALUES;
    }
    
   /**
    * Internal constructor.
    * @param label the enumeration label.
    */
    private LifestylePolicy( String label )
    {
        super( label );
    }

   /**
    * Return a string representation of the lifestyle constant.
    * @return the string value
    */
    public String toString()
    {
        return getName().toUpperCase();
    }
    
   /**
    * Parse the supplied value into a lifestyle policy.
    * @param value the value to parse
    * @return the lifestyle policy
    * @exception IllegalArgumentException if the value cannot be mapped
    *   to a lifestyle policy enumeration name
    */
    public static LifestylePolicy parse( String value ) throws IllegalArgumentException
    {
        if( value.equalsIgnoreCase( "transient" ) )
        {
            return TRANSIENT;
        }
        else if( value.equalsIgnoreCase( "thread" ) )
        {
            return THREAD;
        }
        else if( value.equalsIgnoreCase( "singleton" ) )
        {
            return SINGLETON;
        }
        else
        {
            final String error =
              "Unrecognized lifestyle policy argument [" + value + "]";
            throw new IllegalArgumentException( error );
        }
    }
}


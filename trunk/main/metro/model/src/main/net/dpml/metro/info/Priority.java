/*
 * Copyright 2004 Stephen J. McConnell.
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

import net.dpml.lang.ValuedEnum;

/**
 * Logging priority enumeration.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Priority extends ValuedEnum
{
    static final long serialVersionUID = 1L;

   /**
    * ERROR logging priority.
    */
    public static final Priority ERROR = new Priority( "error", 40 );

   /**
    * WARN logging priority.
    */
    public static final Priority WARN = new Priority( "warn", 30 );

   /**
    * INFO logging priority.
    */
    public static final Priority INFO = new Priority( "info", 20 );

   /**
    * DEBUG logging priority.
    */
    public static final Priority DEBUG = new Priority( "debug", 10 );

   /**
    * DEBUG logging priority.
    */
    public static final Priority TRACE = new Priority( "trace", 5 );

   /**
    * Array of static priority enumeration values.
    */
    private static final Priority[] ENUM_VALUES = 
      new Priority[]{ERROR, WARN, INFO, DEBUG, TRACE};

   /**
    * Returns an array of priority enum values.
    * @return the priority policies array
    */
    public static Priority[] values()
    {
        return ENUM_VALUES;
    }
    
   /**
    * Internal constructor.
    * @param label the enumeration label.
    * @param index the enumeration index.
    */
    private Priority( String label, int index )
    {
        super( label, index );
    }
    
   /**
    * Parse the supplied value into a logging priority constant.
    * @param value the value to parse
    * @return the logging priority constant
    * @exception IllegalArgumentException if the value cannot be mapped
    *   to a logging priority enumeration name
    */
    public static Priority parse( String value ) throws IllegalArgumentException
    {
        if( value.equalsIgnoreCase( "error" ) )
        {
            return ERROR;
        }
        else if( value.equalsIgnoreCase( "warn" ) )
        {
            return WARN;
        }
        else if( value.equalsIgnoreCase( "info" ) )
        {
            return INFO;
        }
        else if( value.equalsIgnoreCase( "debug" ) )
        {
            return DEBUG;
        }
        else if( value.equalsIgnoreCase( "trace" ) )
        {
            return TRACE;
        }
        else
        {
            final String error =
              "Unrecognized logging priority [" + value + "]";
            throw new IllegalArgumentException( error );
        }
    }
    
   /**
    * Return a string representation of the priority constant.
    * @return the string value
    */
    public String toString()
    {
        return getName().toUpperCase();
    }
}


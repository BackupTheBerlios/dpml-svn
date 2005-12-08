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

package net.dpml.station.info;

import net.dpml.transit.util.Enum;

/**
 * Lifestyle policy enumeration.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class StartupPolicy extends Enum
{
    static final long serialVersionUID = 1L;

   /**
    * Disable policy.
    */
    public static final StartupPolicy DISABLED = new StartupPolicy( "disabled" );

   /**
    * Manual startup policy.
    */
    public static final StartupPolicy MANUAL = new StartupPolicy( "manual" );

   /**
    * Automatic startup policy.
    */
    public static final StartupPolicy AUTOMATIC = new StartupPolicy( "automatic" );

   /**
    * Array of scope enumeration values.
    */
    private static final StartupPolicy[] ENUM_VALUES = new StartupPolicy[]{DISABLED, MANUAL, AUTOMATIC};

   /**
    * Returns an array of activation enum values.
    * @return the activation policies array
    */
    public static StartupPolicy[] values()
    {
        return ENUM_VALUES;
    }
    
   /**
    * Internal constructor.
    * @param label the enumeration label.
    */
    private StartupPolicy( String label )
    {
        super( label );
    }
    
   /**
    * Return a string representation of the scope.
    * @return the string value
    */
    public String toString()
    {
        return getName().toUpperCase();
    }
    
   /**
    * Return a startup policy matching the supplied value.
    * @param value the policy name
    * @return the startup policy
    * @exception NullPointerException if the value if null
    * @exception IllegalArgumentException if the value if not recognized
    */
    public static StartupPolicy parse( String value ) throws NullPointerException, IllegalArgumentException
    {
        if( null == value )
        {
            throw new NullPointerException( "value" );
        }
        if( value.equalsIgnoreCase( "disabled" ) )
        {
            return DISABLED;
        }
        else if( value.equalsIgnoreCase( "manual" ) )
        {
            return MANUAL;
        }
        else if( value.equalsIgnoreCase( "automatic" ) )
        {
            return AUTOMATIC;
        }
        else
        {
            final String error =
              "Unrecognized startup policy argument [" + value + "]";
            throw new IllegalArgumentException( error );
        }
    }
}


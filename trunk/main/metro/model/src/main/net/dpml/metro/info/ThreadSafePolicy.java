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

import net.dpml.lang.Enum;

/**
 * Collection policy enummeration.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ThreadSafePolicy extends Enum
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

   /**
    * Weak collection policy.
    */
    public static final ThreadSafePolicy TRUE = new ThreadSafePolicy( "true" );

   /**
    * Soft collection policy.
    */
    public static final ThreadSafePolicy FALSE = new ThreadSafePolicy( "false" );

   /**
    * Hard collection policy.
    */
    public static final ThreadSafePolicy UNKNOWN = new ThreadSafePolicy( "unknown" );

   /**
    * Array of static thread-safe policy enumeration values.
    */
    private static final ThreadSafePolicy[] ENUM_VALUES = 
      new ThreadSafePolicy[]{TRUE, FALSE, UNKNOWN};

   /**
    * Returns an array of policy enum values.
    * @return the policies array
    */
    public static ThreadSafePolicy[] values()
    {
        return ENUM_VALUES;
    }
        
   /**
    * Internal constructor.
    * @param label the enumeration label.
    * @param index the enumeration index.
    * @param map the set of constructed enumerations.
    */
    private ThreadSafePolicy( String label )
    {
        super( label );
    }
   
   /**
    * Parse the supplied name.
    * @param value the value to parse
    * @return the collection policy
    */
    public static ThreadSafePolicy parse( String value )
    {
        if( value.equalsIgnoreCase( "true" ) )
        {
            return TRUE;
        }
        else if( value.equalsIgnoreCase( "false" ) )
        {
            return FALSE;
        }
        else
        {
            return UNKNOWN;
        }
    }
}

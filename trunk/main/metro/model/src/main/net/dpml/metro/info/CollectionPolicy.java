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
public final class CollectionPolicy extends Enum
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
    public static final CollectionPolicy WEAK = new CollectionPolicy( "weak" );

   /**
    * Soft collection policy.
    */
    public static final CollectionPolicy SOFT = new CollectionPolicy( "soft" );

   /**
    * Hard collection policy.
    */
    public static final CollectionPolicy HARD = new CollectionPolicy( "hard" );

   /**
    * Collection policy to be established at system discretion.
    */
    public static final CollectionPolicy SYSTEM = new CollectionPolicy( "system" );
        
   /**
    * Array of static activation policy enumeration values.
    */
    private static final CollectionPolicy[] ENUM_VALUES = 
      new CollectionPolicy[]{WEAK, SOFT, HARD, SYSTEM};

   /**
    * Returns an array of activation enum values.
    * @return the activation policies array
    */
    public static CollectionPolicy[] values()
    {
        return ENUM_VALUES;
    }
        
   /**
    * Internal constructor.
    * @param label the enumeration label.
    */
    private CollectionPolicy( String label )
    {
        super( label );
    }
   
   /**
    * Parse the supplied name.
    * @param value the value to parse
    * @return the collection policy
    */
    public static CollectionPolicy parse( String value )
    {
        if( value.equalsIgnoreCase( "hard" ) )
        {
            return HARD;
        }
        else if( value.equalsIgnoreCase( "soft" ) )
        {
            return SOFT;
        }
        else if( value.equalsIgnoreCase( "weak" ) )
        {
            return WEAK;
        }
        else if( value.equalsIgnoreCase( "system" ) )
        {
            return SYSTEM;
        }
        else
        {
            final String error =
              "Unrecognized collection policy argument [" + value + "]";
            throw new IllegalArgumentException( error );
        }
    }
}

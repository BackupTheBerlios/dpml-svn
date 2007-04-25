/*
 * Copyright 2006 Stephen J. McConnell.
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

package dpml.util;

/**
 * Classoader category enumeration.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public enum Category
{
   /**
    * System category.
    */
    SYSTEM,

   /**
    * API category.
    */
    PUBLIC,

   /**
    * SPI category.
    */
    PROTECTED,

   /**
    * Implementation category.
    */
    PRIVATE;
    
   /**
    * Return a category given a supplied ordinal value.
    * @param index the ordinal value
    * @return the category
    */
    public static Category valueOf( final int index )
    {
        if( index == SYSTEM.ordinal() )
        {
            return SYSTEM;
        }
        else if( index == PUBLIC.ordinal() )
        {
            return PUBLIC;
        }
        else if( index == PROTECTED.ordinal() )
        {
            return PROTECTED;
        }
        else if( index == PRIVATE.ordinal() )
        {
            return PRIVATE;
        }
        else
        {
            final String error = 
              "Category ordinal [" + index + "] is not a valid value.";
            throw new IllegalArgumentException( error );
        }
    }
}

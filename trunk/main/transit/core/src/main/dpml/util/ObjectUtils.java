/*
 * Copyright 2007 Stephen J. McConnell
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
 * Object equality and hashcode utilities.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ObjectUtils
{
    private ObjectUtils()
    {
    }
    
   /**
    * Utility to hash an array.
    * @param array the array
    * @return the hash value
    */
    public static int hashArray( Object[] array )
    {
        if( null == array )
        {
            return 0;
        }
        int hash = 0;
        for( Object object : array )
        {
            hash ^= hashValue( object );
        }
        return hash;
    }
    
   /**
    * Utility to hash an object.
    * @param value the object
    * @return the hash value
    */
    public static int hashValue( Object value )
    {
        if( null == value )
        {
            return 0;
        }
        else if( value instanceof Object[] )
        {
            return hashArray( (Object[]) value );
        }
        else
        {
            return value.hashCode();
        }
    }
    
   /**
    * Utility to compare two object for equality.
    * @param a the first object
    * @param b the second object
    * @return true if the objects are equal
    */
    public static boolean equals( Object a, Object b )
    {
        if( null == a )
        {
            return ( null == b );
        }
        else
        {
            return a.equals( b );
        }
    }
}

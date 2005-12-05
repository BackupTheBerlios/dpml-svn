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

package net.dpml.metro.data;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import net.dpml.metro.part.Directive;

/**
 * Abstract base class for directives.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class AbstractDirective implements Serializable
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------

   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;
    
    //--------------------------------------------------------------------------
    // utilities
    //--------------------------------------------------------------------------
    
   /**
    * Return the hashcode for the instance.
    * @return the instance hashcode
    */
    public int hashCode()
    {
        return getClass().hashCode();
    }
    
   /**
    * Utility to hash an array.
    * @param array the array
    * @return the hash value
    */
    int hashArray( Object[] array )
    {
        if( null == array )
        {
            return 0;
        }
        int hash = 0;
        for( int i=0; i<array.length; i++ )
        {
            Object object = array[i];
            hash ^= hashValue( object );
        }
        return hash;
    }
    
   /**
    * Utility to hash an object.
    * @param value the object
    * @return the hash value
    */
    int hashValue( Object value )
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
    * Test if the supplied object is equal to this object.
    * @param other the object to compare with this instance
    * @return TRUE if the supplied object is equal to this object
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else
        {
            return ( other instanceof AbstractDirective );
        }
    }
    
   /**
    * Utility to compare two object for equality.
    * @param a the first object
    * @param b the second object
    * @return true if the objects are equal
    */
    boolean equals( Object a, Object b )
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
    
    //--------------------------------------------------------------------------
    // Part
    //--------------------------------------------------------------------------

   /**
    * Return the part handler uri.
    * @return the uri of the part handler
    */
    public URI getPartHandlerURI()
    {
        return PART_HANDLER_URI;
    }

   /**
    * Static utility to create the part handler uri.
    * @param spec the part handler uri string
    * @return the constant part handler uri
    */
    protected static URI setupURI( String spec )
    {
        try
        {
            return new URI( spec );
        }
        catch( URISyntaxException ioe )
        {
            return null;
        }
    }
    
    private static final URI PART_HANDLER_URI = setupURI( "@PART-HANDLER-URI@" );

}

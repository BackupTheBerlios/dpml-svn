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

package net.dpml.component;

import net.dpml.lang.Enum;

/**
 * Provider deployment status enumeration.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Status extends Enum
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

   /**
    * The provider has been instantiated but remains uncommissioned.
    */
    public static final Status INSTANTIATED = new Status( "instantiated" );

   /**
    * The provider is in the process of commissioning its internal structure
    * following which the provider will attempt to establish a instance value
    * and transition to AVAILABLE.
    */
    public static final Status COMMISSIONING = new Status( "commissioning" );
    
   /**
    * The provider has successfully established the target instance.
    */
    public static final Status AVAILABLE = new Status( "available" );

   /**
    * The provider is in the process of decommissioning it's internal parts.
    */
    public static final Status DECOMMISSIONING = new Status( "decommissioning" );

   /**
    * The provider has completed local decommissioning.
    */
    public static final Status DECOMMISSIONED = new Status( "decommissioned" );

   /**
    * The provider is decommissioned not longer available.
    */
    public static final Status DISPOSED = new Status( "disposed" );
    
   /**
    * Array of static status enumeration values.
    */
    private static final Status[] ENUM_VALUES = 
      new Status[]{INSTANTIATED, COMMISSIONING, AVAILABLE, DECOMMISSIONING, DECOMMISSIONED, DISPOSED};

   /**
    * Returns an array of activation enum values.
    * @return the activation policies array
    */
    public static Status[] values()
    {
        return ENUM_VALUES;
    }
        
   /**
    * Internal constructor.
    * @param label the enumeration label.
    */
    private Status( String label )
    {
        super( label );
    }
   
   /**
    * Parse the supplied name.
    * @param value the value to parse
    * @return the collection policy
    */
    public static Status parse( String value )
    {
        if( INSTANTIATED.getName().equalsIgnoreCase( value ) )
        {
            return INSTANTIATED;
        }
        else if( COMMISSIONING.getName().equalsIgnoreCase( value ) )
        {
            return COMMISSIONING;
        }
        else if( AVAILABLE.getName().equalsIgnoreCase( value ) )
        {
            return AVAILABLE;
        }
        else if( DECOMMISSIONING.getName().equalsIgnoreCase( value ) )
        {
            return DECOMMISSIONING;
        }
        else if( DECOMMISSIONED.getName().equalsIgnoreCase( value ) )
        {
            return DECOMMISSIONED;
        }
        else if( DISPOSED.getName().equalsIgnoreCase( value ) )
        {
            return DISPOSED;
        }
        else
        {
            final String error =
              "Unrecognized status argument [" + value + "]";
            throw new IllegalArgumentException( error );
        }
    }
}

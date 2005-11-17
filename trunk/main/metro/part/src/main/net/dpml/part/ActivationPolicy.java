/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.metro.part;

import net.dpml.transit.util.Enum;

/**
 * Policy enummeration for the declaration of activation semantics.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */

public final class ActivationPolicy extends Enum
{
    static final long serialVersionUID = 1L;

   /**
    * System managed activation policy.
    */
    public static final ActivationPolicy SYSTEM = new ActivationPolicy( "system" );

   /**
    * Activation on startup enabled.
    */
    public static final ActivationPolicy STARTUP = new ActivationPolicy( "startup" );

   /**
    * Activation on startup disabled.
    */
    public static final ActivationPolicy DEMAND = new ActivationPolicy( "demand" );

   /**
    * Array of static activation policy enumeration values.
    */
    private static final ActivationPolicy[] ENUM_VALUES = new ActivationPolicy[]{ SYSTEM, STARTUP, DEMAND };

   /**
    * Returns an array of activation enum values.
    * @return the activation policies array
    */
    public static ActivationPolicy[] values()
    {
        return ENUM_VALUES;
    }
        
    public static ActivationPolicy parse( String value )
    {
        if( value.equalsIgnoreCase( "system" ) )
        {
            return SYSTEM;
        }
        else if( value.equalsIgnoreCase( "startup" ))
        {
            return STARTUP;
        }
        else if( value.equalsIgnoreCase( "demand" ))
        {
            return DEMAND;
        }
        else
        {
            final String error =
              "Unrecognized activation policy argument [" + value + "]";
              throw new IllegalArgumentException( error );
        }
    }
        
   /**
    * Internal constructor.
    * @param label the enumeration label.
    * @param index the enumeration index.
    * @param map the set of constructed enumerations.
    */
    private ActivationPolicy( String label )
    {
        super( label );
    }
}

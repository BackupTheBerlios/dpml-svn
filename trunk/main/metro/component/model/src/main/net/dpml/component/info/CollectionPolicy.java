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

package net.dpml.component.info;

import java.beans.Expression;
import java.beans.BeanDescriptor;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.beans.Encoder;

import net.dpml.transit.util.Enum;

/**
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: InfoDescriptor.java 2522 2005-05-12 11:23:50Z mcconnell@dpml.net $
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
    private static final CollectionPolicy[] ENUM_VALUES = new CollectionPolicy[]{ WEAK, SOFT, HARD, SYSTEM };

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
    * @param index the enumeration index.
    * @param map the set of constructed enumerations.
    */
    private CollectionPolicy( String label )
    {
        super( label );
    }
        
    public static CollectionPolicy parse( String value )
    {
        if( value.equalsIgnoreCase( "hard" ) )
        {
            return HARD;
        }
        else if( value.equalsIgnoreCase( "soft" ))
        {
            return SOFT;
        }
        else if( value.equalsIgnoreCase( "weak" ))
        {
            return WEAK;
        }
        else if( value.equalsIgnoreCase( "system" ))
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

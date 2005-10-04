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

package net.dpml.composition.info;

import java.beans.Expression;
import java.beans.BeanDescriptor;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.beans.Encoder;

import net.dpml.transit.util.Enum;

/**
 * Lifestyle policy enumeration.
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: InfoDescriptor.java 2522 2005-05-12 11:23:50Z mcconnell@dpml.net $
 */
public final class LifestylePolicy extends Enum
{
    static final long serialVersionUID = 1L;

   /**
    * Weak collection policy.
    */
    public static final LifestylePolicy TRANSIENT = new LifestylePolicy( "transient" );

   /**
    * Soft collection policy.
    */
    public static final LifestylePolicy THREAD = new LifestylePolicy( "thread" );

   /**
    * Hard collection policy.
    */
    public static final LifestylePolicy SINGLETON = new LifestylePolicy( "singleton" );

   /**
    * Array of static activation policy enumeration values.
    */
    private static final LifestylePolicy[] ENUM_VALUES = new LifestylePolicy[]{ TRANSIENT, THREAD, SINGLETON };

   /**
    * Returns an array of activation enum values.
    * @return the activation policies array
    */
    public static LifestylePolicy[] values()
    {
        return ENUM_VALUES;
    }
    
   /**
    * Internal constructor.
    * @param label the enumeration label.
    * @param index the enumeration index.
    * @param map the set of constructed enumerations.
    */
    private LifestylePolicy( String label )
    {
        super( label );
    }
    
    public static LifestylePolicy parse( String value )
    {
        if( value.equalsIgnoreCase( "transient" ) )
        {
            return TRANSIENT;
        }
        else if( value.equalsIgnoreCase( "thread" ))
        {
            return THREAD;
        }
        else if( value.equalsIgnoreCase( "singleton" ))
        {
            return SINGLETON;
        }
        else
        {
            final String error =
              "Unrecognized lifestyle policy argument [" + value + "]";
            throw new IllegalArgumentException( error );
        }
    }
}


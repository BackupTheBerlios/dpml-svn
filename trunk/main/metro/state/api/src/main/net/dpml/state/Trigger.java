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

package net.dpml.state;

import java.beans.Expression;
import java.beans.BeanDescriptor;
import java.beans.DefaultPersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.beans.Encoder;

/**
 * Interface describing a condition within which an action may be invoked.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Trigger
{
   /**
    * Initialization event emumeration.
    */
    public static final TriggerEvent INITIALIZATION = new TriggerEvent( "initialization" );

   /**
    * Termination event emumeration.
    */
    public static final TriggerEvent TERMINATION = new TriggerEvent( "termination" );

   /**
    * Return the event enumneration that this trigger is associated with.
    * @return the triggering event class
    */
    TriggerEvent getEvent();
    
   /**
    * Return the actions  that this trigger initiates.
    * @return the triggered action
    */
    Action getAction();
    
   /**
    * Trigger policy enumeration.
    */
    public static final class TriggerEvent extends Enum
    {
        static final long serialVersionUID = 1L;

       /**
        * Array of static trigger event enumeration values.
        */
        private static final TriggerEvent[] ENUM_VALUES = new TriggerEvent[]{INITIALIZATION, TERMINATION};

       /**
        * Returns an array of trigger event enum values.
        * @return the trigger event array
        */
        public static TriggerEvent[] values()
        {
            return ENUM_VALUES;
        }
        
       /**
        * Parse the supplied value and return the corresponding 
        * trigger event class.
        * @param value the event name
        * @return the trgger constant
        */ 
        public static TriggerEvent parse( String value )
        {
            if( value.equalsIgnoreCase( "initialization" ) )
            {
                return INITIALIZATION;
            }
            else if( value.equalsIgnoreCase( "termination" ) )
            {
                return TERMINATION;
            }
            else
            {
                final String error =
                  "Unrecognized trigger event argument [" + value + "]";
                throw new IllegalArgumentException( error );
            }
        }
        
       /**
        * Internal constructor.
        * @param label the enumeration label.
        * @param index the enumeration index.
        * @param map the set of constructed enumerations.
        */
        private TriggerEvent( String label )
        {
            super( label );
        }
    }
    
   /**
    * Internal BeanInfo class that exposes an persistence delegate.
    */
    public static final class TriggerEventBeanInfo extends SimpleBeanInfo
    {
        private static final BeanDescriptor BEAN_DESCRIPTOR = setupBeanDescriptor();
    
       /**
        * Return the bean descriptor.
        * @return the descriptor
        */
        public BeanDescriptor getBeanDescriptor()
        {
            return BEAN_DESCRIPTOR;
        }
    
        private static BeanDescriptor setupBeanDescriptor()
        {
            BeanDescriptor descriptor = new BeanDescriptor( TriggerEvent.class );
            descriptor.setValue( 
              "persistenceDelegate", 
              new TriggerEventPersistenceDelegate() );
            return descriptor;
        }
        
       /**
        * Persistence delegate.
        */
        private static class TriggerEventPersistenceDelegate extends DefaultPersistenceDelegate
        {
           /**
            * Create an expression.
            * @paran old the old instance
            * @param encoder the encoder
            * @return the expression
            */
            public Expression instantiate( Object old, Encoder encoder )
            {
                TriggerEvent event = (TriggerEvent) old;
                return new Expression( TriggerEvent.class, "parse", new Object[]{event.getName()} );
            }
        }
    }
}

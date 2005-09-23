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

package net.dpml.part;

import java.beans.Expression;
import java.beans.BeanDescriptor;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.beans.Encoder;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.transit.util.Enum;

/**
 * The Resolvable interface is implemented by components capable of exposing
 * runtime objects.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public interface Control extends Remote
{
   /**
    * Get the activation policy for the control.
    *
    * @return the activation policy
    * @see #ActivationPolicy.SYSTEM
    * @see #ActivationPolicy.STARTUP
    * @see #ActivationPolicy.DEMAND
    */
    ActivationPolicy getActivationPolicy() throws RemoteException;

   /**
    * Return an initialized instance of the service.
    * @return the resolved service instance
    */
    Object resolve() throws Exception;

   /**
    * Return an initialized instance of the component using a supplied isolation policy.
    * If the isolation policy is TRUE an implementation shall make best efforts to isolate
    * implementation concerns under the object that is returned.  Typically isolation 
    * involves the creation of a proxy of a component implementation instance that 
    * exposes a component's service interfaces to a client.  If the isolation policy if
    * FALSE the implementation shall return the component implementation instance.
    * 
    * @param isolation the isolation policy
    * @return the resolved instance
    */
    Object resolve( boolean isolation ) throws Exception;

   /**
    * Release a reference to an object managed by the instance.
    * 
    * @param instance the instance to release
    */
    void release( Object instance ) throws RemoteException;

   /**
    * Activation policy enumeration.
    */
    public static final class ActivationPolicy extends Enum
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
    
    public static final class ActivationPolicyBeanInfo extends SimpleBeanInfo
    {
        private static final BeanDescriptor BEAN_DESCRIPTOR = setupBeanDescriptor();
    
        public BeanDescriptor getBeanDescriptor()
        {
            return BEAN_DESCRIPTOR;
        }
    
        private static BeanDescriptor setupBeanDescriptor()
        {
            BeanDescriptor descriptor = new BeanDescriptor( ActivationPolicy.class );
            descriptor.setValue( 
              "persistenceDelegate", 
              new ActivationPolicyPersistenceDelegate() );
            return descriptor;
        }
        
        private static class ActivationPolicyPersistenceDelegate extends DefaultPersistenceDelegate
        {
            public Expression instantiate( Object old, Encoder encoder )
            {
                ActivationPolicy policy = (ActivationPolicy) old;
                return new Expression( ActivationPolicy.class, "parse", new Object[]{ policy.getName() } );
            }
        }
    }

}

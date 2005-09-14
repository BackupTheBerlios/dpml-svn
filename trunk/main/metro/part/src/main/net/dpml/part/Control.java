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

import java.io.IOException;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

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
    * System managed activation policy.
    */
    ActivationPolicy SYSTEM_MANAGED_ACTIVATION = new ActivationPolicy( 0, "default", "System" );

   /**
    * Activation on startup enabled.
    */
    ActivationPolicy ACTIVATION_ON_STARTUP = new ActivationPolicy( 0, "startup", "Startup" );

   /**
    * Activation on startup disabled.
    */
    ActivationPolicy ACTIVATION_ON_DEMAND = new ActivationPolicy( 0, "demand", "Demand" );

   /**
    * Enumeration of available activation policies.
    */
    public static final ActivationPolicy[] ACTIVATION_POLICIES = 
      new ActivationPolicy[]{
        SYSTEM_MANAGED_ACTIVATION, 
        ACTIVATION_ON_STARTUP, 
        ACTIVATION_ON_DEMAND};

   /**
    * Get the activation policy for the control.
    *
    * @return the activation policy
    * @see #SYSTEM_MANAGED_ACTIVATION
    * @see #ACTIVATION_ON_STARTUP
    * @see #ACTIVATION_ON_DEMAND
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
    public static final class ActivationPolicy implements Serializable
    {
        private final int m_index;
        private final String m_label;
        private final String m_key;

       /**
        * Internal constructor.
        * @param index the enumeration index.
        */
        private ActivationPolicy( int index, final String key, final String label )
        {
            m_index = index;
            m_label = label;
            m_key = key;
        }

       /**
        * Return the key used to identify this policy instance.
        * @return the key
        */
        public String key()
        {
            return m_key;
        }

       /**
        * Test this policy for equality with the supplied instance.
        * @param other the object to test against
        * @return true if the instances are equivalent
        */
        public boolean equals( Object other )
        {
            if( null == other ) 
            {
                return false;
            }
            else if( other.getClass() == ActivationPolicy.class )
            {
                ActivationPolicy policy = (ActivationPolicy) other;
                return policy.m_index == m_index;
            }
            else
            {
                return false;
            }
        }

       /**
        * Return the hascode for this instance.
        * @return the instance hashcode
        */
        public int hashCode()
        {
            return m_index;
        }

       /**
        * Return the string representation of this instance.
        * @return the string
        */
        public String toString()
        {
            return m_label;
        }
    }
}

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

package net.dpml.station.info;

import java.beans.Expression;
import java.beans.BeanDescriptor;
import java.beans.DefaultPersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.beans.Encoder;

/**
 * Bean info for state encoding of the startup policy enumeration.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class StartupPolicyBeanInfo extends SimpleBeanInfo
{
    private static final BeanDescriptor BEAN_DESCRIPTOR = setupBeanDescriptor();

   /**
    * Creation of a bean descriptor.
    * @return the bean descriptor
    */
    public BeanDescriptor getBeanDescriptor()
    {
        return BEAN_DESCRIPTOR;
    }
    
    private static BeanDescriptor setupBeanDescriptor()
    {
        BeanDescriptor descriptor = new BeanDescriptor( StartupPolicy.class );
        descriptor.setValue( 
          "persistenceDelegate", 
          new PolicyPersistenceDelegate() );
        return descriptor;
    }
    
   /**
    * StartupPolicy persitence delegate.
    */
    private static class PolicyPersistenceDelegate extends DefaultPersistenceDelegate
    {
       /**
        * Return an expression.
        * @param old the old value
        * @param encoder the encoder
        * @return an expression
        */
        public Expression instantiate( Object old, Encoder encoder )
        {
            if( null == old )
            {
                throw new IllegalStateException( "old" );
            }
            StartupPolicy policy = (StartupPolicy) old;
            String name = policy.getName();
            return new Expression( StartupPolicy.class, "parse", new Object[]{name} );
        }
    }
}

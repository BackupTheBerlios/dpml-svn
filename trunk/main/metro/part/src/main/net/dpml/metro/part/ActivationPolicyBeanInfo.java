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

import java.beans.Expression;
import java.beans.BeanDescriptor;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.beans.Encoder;

import net.dpml.transit.util.Enum;

/**
 * Utility class containing a persistence encoding delegate for
 * the ActivationPolicy class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ActivationPolicyBeanInfo extends SimpleBeanInfo
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

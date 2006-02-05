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

package net.dpml.metro.info;

import java.beans.Expression;
import java.beans.BeanDescriptor;
import java.beans.DefaultPersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.beans.Encoder;

/**
 * BeanInfo that declares a specialized persistence delegate for the collection policy class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ThreadSafePolicyBeanInfo extends SimpleBeanInfo
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
        BeanDescriptor descriptor = new BeanDescriptor( ThreadSafePolicy.class );
          descriptor.setValue( 
            "persistenceDelegate", 
            new ThreadSafePolicyPersistenceDelegate() );
        return descriptor;
    }
    
   /**
    * Persistence delegate implementation.
    */
    private static class ThreadSafePolicyPersistenceDelegate extends DefaultPersistenceDelegate
    {
       /**
        * Return the expression value.
        * @param old the old instance
        * @param encoder the encoder
        * @return the expression
        */
        public Expression instantiate( Object old, Encoder encoder )
        {
            ThreadSafePolicy policy = (ThreadSafePolicy) old;
            return new Expression( policy, ThreadSafePolicy.class, "parse", new Object[]{policy.getName()} );
        }
    }
}

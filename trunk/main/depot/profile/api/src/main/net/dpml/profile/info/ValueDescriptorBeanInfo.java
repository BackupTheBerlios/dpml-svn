/*
 * Copyright 2005 Stephen J. McConnell.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dpml.profile.info;

import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Expression;
import java.beans.Encoder;

/**
 * BeanInfo for the CategoryDescriptor class that declares a persistence
 * delegate.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ValueDescriptorBeanInfo extends SimpleBeanInfo
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
        BeanDescriptor descriptor = new BeanDescriptor( ValueDescriptor.class );
        descriptor.setValue( 
          "persistenceDelegate", 
          new ValueDescriptorPersistenceDelegate() );
        return descriptor;
    }
    
   /**
    * Persistence delegate.
    */
    private static class ValueDescriptorPersistenceDelegate extends DefaultPersistenceDelegate
    {
       /**
        * Instantiate an expression.
        * @param old the old value
        * @param encoder the encoder
        * @return the expression
        */
        public Expression instantiate( Object old, Encoder encoder )
        {
            ValueDescriptor construct = (ValueDescriptor) old;
            Object[] args = new Object[3];
            args[0] = construct.getTargetExpression();
            args[1] = construct.getMethodName();
            if( construct.isCompound() )
            {
                args[2] = construct.getValueDescriptors();
            }
            else
            {
                args[2] = construct.getBaseValue();
            }
            return new Expression( old, old.getClass(), "new", args );
        }
    }
}
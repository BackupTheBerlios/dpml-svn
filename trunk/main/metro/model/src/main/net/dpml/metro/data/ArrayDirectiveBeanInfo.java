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
package net.dpml.metro.data;

import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;

/**
 * BeanInfo for the ContextDescriptor class that declares a persistence
 * delegate.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ArrayDirectiveBeanInfo extends SimpleBeanInfo
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
        BeanDescriptor descriptor = new BeanDescriptor( ArrayDirective.class );
        descriptor.setValue( 
          "persistenceDelegate", 
          new ArrayDirectivePersistenceDelegate() );
        return descriptor;
    }
    
   /**
    * Persistence delegate implementation.
    */
    private static class ArrayDirectivePersistenceDelegate extends DefaultPersistenceDelegate
    {
       /**
        * Return the expression value.
        * @param old the old instance
        * @param encoder the encoder
        * @return the expression
        */
        public Expression instantiate( Object old, Encoder encoder )
        {
            ArrayDirective construct = (ArrayDirective) old;
            Object[] args = new Object[2];
            args[0] = construct.getClassname();
            args[1] = construct.getValues();
            return new Expression( old, old.getClass(), "new", args );
        }
    }
}

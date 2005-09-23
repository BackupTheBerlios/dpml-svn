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
package net.dpml.composition.data;

import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;

/**
 * BeanInfo for the ContextDescriptor class that declares a persistence
 * delegate.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ValueDirectiveBeanInfo extends SimpleBeanInfo
{
    private static final BeanDescriptor BEAN_DESCRIPTOR = setupBeanDescriptor();
    
    public BeanDescriptor getBeanDescriptor()
    {
        return BEAN_DESCRIPTOR;
    }
    
    private static BeanDescriptor setupBeanDescriptor()
    {
        BeanDescriptor descriptor = new BeanDescriptor( ValueDirective.class );
        descriptor.setValue( 
          "persistenceDelegate", 
          new ValueDirectivePersistenceDelegate() );
        return descriptor;
    }
    
    public static class ValueDirectivePersistenceDelegate extends DefaultPersistenceDelegate
    {
        public Expression instantiate( Object old, Encoder encoder )
        {
            ValueDirective value = (ValueDirective) old;
            Object[] args = new Object[3];
            args[0] = value.getPartHandlerURI();
            args[1] = value.getClassname();
            if( null !=  value.getLocalValue() )
            {
                args[2] = value.getLocalValue();
            }
            else
            {
                args[2] = value.getValues();
            }
            return new Expression( old, old.getClass(), "new", args );
        }
    }
}

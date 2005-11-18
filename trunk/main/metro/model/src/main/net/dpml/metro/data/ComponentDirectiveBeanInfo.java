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
public final class ComponentDirectiveBeanInfo extends SimpleBeanInfo
{
    private static final BeanDescriptor BEAN_DESCRIPTOR = setupBeanDescriptor();
    
    public BeanDescriptor getBeanDescriptor()
    {
        return BEAN_DESCRIPTOR;
    }
    
    private static BeanDescriptor setupBeanDescriptor()
    {
        BeanDescriptor descriptor = new BeanDescriptor( ComponentDirective.class );
        descriptor.setValue( 
          "persistenceDelegate", 
          new ComponentDirectivePersistenceDelegate() );
        return descriptor;
    }
    
    private static class ComponentDirectivePersistenceDelegate extends DefaultPersistenceDelegate
    {
        public Expression instantiate( Object old, Encoder encoder )
        {
            ComponentDirective directive = (ComponentDirective) old;
            Object[] args = new Object[10];
            args[0] = directive.getName();
            args[1] = directive.getActivationPolicy();
            args[2] = directive.getCollectionPolicy();
            args[3] = directive.getLifestylePolicy();
            args[4] = directive.getClassname();
            args[5] = directive.getCategoriesDirective();
            args[6] = directive.getContextDirective();
            args[7] = directive.getParameters();
            args[8] = directive.getConfiguration();
            args[9] = directive.getClassLoaderDirective();
            return new Expression( old, old.getClass(), "new", args );
        }
    }
}

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
package net.dpml.component.info;;

import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.DefaultPersistenceDelegate;

/**
 * BeanInfo for the CategoryDescriptor class that declares a persistence
 * delegate.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class TypeBeanInfo extends SimpleBeanInfo
{
    private static final BeanDescriptor BEAN_DESCRIPTOR = setupBeanDescriptor();
    
    public BeanDescriptor getBeanDescriptor()
    {
        return BEAN_DESCRIPTOR;
    }
    
    private static BeanDescriptor setupBeanDescriptor()
    {
        BeanDescriptor descriptor = new BeanDescriptor( Type.class );
        descriptor.setValue( 
          "persistenceDelegate", 
          new DefaultPersistenceDelegate( 
            new String[]{ 
              "info", "categoryDescriptors", "contextDescriptor",
              "serviceDescriptors", "configuration", "partReferences" } ) );
        return descriptor;
    }
}

/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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
package net.dpml.composition.info;

import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Expression;
import java.beans.Encoder;

/**
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class InfoDescriptorBeanInfo extends SimpleBeanInfo
{
    private static final BeanDescriptor BEAN_DESCRIPTOR = setupBeanDescriptor();
    
    public BeanDescriptor getBeanDescriptor()
    {
        return BEAN_DESCRIPTOR;
    }
    
    public static class InfoDescriptorPersistenceDelegate extends DefaultPersistenceDelegate
    {
        public Expression instantiate( Object old, Encoder encoder )
        {
            InfoDescriptor info = (InfoDescriptor) old;
            
            Object[] args = new Object[ 8 ];
            args[0] = info.getName();
            args[1] = info.getClassname();
            args[2] = info.getVersion();
            args[3] = info.getLifestyle();
            args[4] = info.getCollectionPolicy();
            args[5] = info.getConfigurationSchema();
            args[6] = new Boolean( info.isThreadsafe() );
            args[7] = info.getProperties();
            return new Expression( old, old.getClass(), "new", args );
        }
    }

    private static BeanDescriptor setupBeanDescriptor()
    {
        BeanDescriptor descriptor = new BeanDescriptor( InfoDescriptor.class );
        descriptor.setValue( "persistenceDelegate", new InfoDescriptorPersistenceDelegate() );
        return descriptor;
    }
}

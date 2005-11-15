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
package net.dpml.configuration.impl;

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.DefaultPersistenceDelegate;
import java.beans.PropertyDescriptor;

import net.dpml.configuration.ConfigurationException;

/**
 * BeanInfo that declares a persistence delegate.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class DefaultConfigurationBeanInfo extends SimpleBeanInfo
{
    private static final BeanDescriptor BEAN_DESCRIPTOR = setupBeanDescriptor();
    
   /**
    * Return the bean descriptor.
    * @return the bean descriptor
    */
    public BeanDescriptor getBeanDescriptor()
    {
        return BEAN_DESCRIPTOR;
    }
    
   /**
    * Return the property descriptor array.
    * @return the property descriptors
    */
    public PropertyDescriptor[] getPropertyDescriptors()
    {
        return new PropertyDescriptor[0];
    }
    
    private static BeanDescriptor setupBeanDescriptor()
    {
        BeanDescriptor descriptor = new BeanDescriptor( DefaultConfiguration.class );
        descriptor.setValue( 
          "persistenceDelegate", 
          new DefaultConfigurationPersistenceDelegate() );
        return descriptor;
    }
    
   /**
    * Persistence delegate.
    */
    public static class DefaultConfigurationPersistenceDelegate extends DefaultPersistenceDelegate
    {
       /**
        * Create the expression.
        * @param old the old value
        * @param encoder the encoder
        * @return the expression
        */
        public Expression instantiate( Object old, Encoder encoder )
        {
            if( null == old )
            {
                return null;
            }
            
            DefaultConfiguration config = (DefaultConfiguration) old;
            
            Object[] args = new Object[ 8 ];
            args[0] = config.getName();
            args[1] = config.getLocation();
            try
            {
                args[2] = config.getNamespace();
            }
            catch( ConfigurationException e )
            {
                args[2] = "";
            }
            try
            {
                args[3] = config.getPrefix();
            }
            catch( ConfigurationException e )
            {
                args[3] = "";
            }
            try
            {
                args[4] = config.getValue();
            }
            catch( ConfigurationException e )
            {
                args[4] = null;
            }
            args[5] = config.getAttributes();
            args[6] = config.getChildren();
            args[7] = new Boolean( config.isReadOnly() );
            
            return new Expression( old, DefaultConfiguration.class, "new", args );
        }
    }
}

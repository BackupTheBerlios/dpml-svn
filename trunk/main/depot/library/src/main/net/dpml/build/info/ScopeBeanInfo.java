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

package net.dpml.build.info;

import java.beans.Expression;
import java.beans.BeanDescriptor;
import java.beans.DefaultPersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.beans.Encoder;

/**
 * Bean info for state encoding of the Lifestyle policy enumeration.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ScopeBeanInfo extends SimpleBeanInfo
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
        BeanDescriptor descriptor = new BeanDescriptor( Scope.class );
        descriptor.setValue( 
          "persistenceDelegate", 
          new ScopePersistenceDelegate() );
        return descriptor;
    }
    
   /**
    * Scope persitence delegate.
    */
    private static class ScopePersistenceDelegate extends DefaultPersistenceDelegate
    {
       /**
        * Return an expression.
        * @param old the old value
        * @param encoder the encoder
        * @return an expression
        */
        public Expression instantiate( Object old, Encoder encoder )
        {
            Scope scope = (Scope) old;
            return new Expression( Scope.class, "parse", new Object[]{scope.getName()} );
        }
    }
}

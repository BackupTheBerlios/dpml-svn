/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.metro.facilities.reflector.typehandlers;

import java.util.TreeSet; 

import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.metro.facilities.reflector.ReflectionException;

import org.apache.metro.facilities.reflector.spi.ReflectorProvider;
import org.apache.metro.facilities.reflector.spi.TypeHandler;

/**
 * @avalon.component name="type-handler-object" lifestyle="singleton"
 * @avalon.service type="org.apache.metro.facilities.reflector.spi.TypeHandler"
 */
public class ObjectTypeHandler extends AbstractObjectTypeHandler
    implements Serviceable
{
    /**
     * @avalon.dependency type="org.apache.metro.facilities.reflector.spi.ReflectorProvider"
     *                    key="provider"
     */
    public void service( ServiceManager man )
        throws ServiceException
    {
        ReflectorProvider provider = (ReflectorProvider) man.lookup( "provider" );
        provider.addTypeHandler( this, Object[].class );
    }
    
    public boolean isDefault()
    {
        return true;
    }

    public String[] getNames( Object container )
        throws ReflectionException
    {
        TreeSet names = new TreeSet();
        getNames( names, container );
        return packageNames( names );
    }
} 

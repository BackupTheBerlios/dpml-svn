/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.metro.tools;

import java.util.LinkedList;
import java.util.List;

import net.dpml.metro.info.ServiceDescriptor;

/**
 * A datatype that enables custom part builders.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ServicesDataType
{
    private List m_services = new LinkedList();

   /**
    * Create a new component builder task.
    * @return a new component builder task
    */
    public ServiceDataType createService()
    {
        ServiceDataType service = new ServiceDataType();
        m_services.add( service );
        return service;
    }

    private ServiceDataType[] getServiceDataTypes()
    {
        return (ServiceDataType[]) m_services.toArray( new ServiceDataType[0] );
    }

    ServiceDescriptor[] getServiceDescriptors() 
    {
        ServiceDataType[] builders = getServiceDataTypes();
        ServiceDescriptor[] parts = new ServiceDescriptor[ builders.length ];
        for( int i=0; i<builders.length; i++ )
        {
            ServiceDataType builder = builders[i];
            parts[i] = builder.getServiceDescriptor();
        }
        return parts;
    }
}

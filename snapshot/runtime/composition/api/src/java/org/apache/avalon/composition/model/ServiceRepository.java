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

package org.apache.avalon.composition.model;

import org.apache.avalon.framework.Version;
import org.apache.avalon.meta.info.ReferenceDescriptor;
import org.apache.avalon.meta.info.Service;

/**
 * A service repository provides support for the storage and retrival
 * of service defintions.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ServiceRepository.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface ServiceRepository
{
    /**
     * Locate a {@link Service} instances associated with the
     * supplied classname and version. If a service defintion is not
     * found locally, the implementation redirects the request to
     * the parent service manager.
     *
     * @param classname the service class name
     * @param version the service version
     * @return the service matching the supplied classname and version.
     * @exception UnknownServiceException if a matching service cannot be found
     */
    Service getService( String classname, Version version ) throws ServiceUnknownException;

    /**
     * Locate a {@link Service} instances associated with the
     * supplied referecne descriptor. If a service defintion is not
     * found locally, the implementation redirects the request to
     * the parent service manager.
     *
     * @param reference the reference descriptor
     * @return the service matching the supplied descriptor.
     * @exception UnknownServiceException if a matching service cannot be found
     */
    Service getService( ReferenceDescriptor reference )
            throws ServiceUnknownException;

}

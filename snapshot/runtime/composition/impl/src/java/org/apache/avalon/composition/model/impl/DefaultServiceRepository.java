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

package org.apache.avalon.composition.model.impl;

import java.util.Iterator;
import java.util.List;

import org.apache.avalon.composition.model.ServiceUnknownException;
import org.apache.avalon.composition.model.ServiceRepository;

import org.apache.avalon.framework.Version;
import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.meta.info.ReferenceDescriptor;
import org.apache.avalon.meta.info.Service;

/**
 * A service repository provides support for the retrival
 * of service defintions.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultServiceRepository.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultServiceRepository implements ServiceRepository
{
    //==============================================================
    // immutable state
    //==============================================================

    /**
     * The logging channel.
     */
    private final Logger m_logger;

    /**
     * The parent service manager (may be null)
     */
    private final ServiceRepository m_parent;

    /**
     * List of service entries.
     */
    private final List m_services;

    //==============================================================
    // constructor
    //==============================================================

    /**
     * Creation of a new root service manager.
     * @param logger the logging channel
     * @param services the list of available services
     * @exception NullPointerException if the services list is null
     */
    DefaultServiceRepository( 
      final Logger logger, final List services ) throws NullPointerException
    {
        this( logger, null, services );
    }

    /**
     * Creation of a new service manager.
     * @param logger the logging channel
     * @param parent the parent type manager
     * @param services the list of available services
     * @exception NullPointerException if the services list is null
     */
    DefaultServiceRepository( 
      final Logger logger, final ServiceRepository parent, final List services ) 
      throws NullPointerException
    {
        if( services == null )
        {
            throw new NullPointerException( "services" );
        }
        m_parent = parent;
        m_services = services;
        m_logger = logger;
    }

    //==============================================================
    // implemetation
    //==============================================================

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
    public Service getService( final String classname, final Version version ) 
      throws ServiceUnknownException
    {
        return getService( new ReferenceDescriptor( classname, version ) );
    }

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
    public Service getService( final ReferenceDescriptor reference ) 
      throws ServiceUnknownException
    {
        Service service = getLocalService( reference );
        if( service == null )
        {
            if( m_parent != null )
            {
                return m_parent.getService( reference );
            } 
            else
            {
                final String error = "Unknown service defintion: " + reference;
                throw new ServiceUnknownException( error );
            }
        }
        return service;
    }

    private Service getLocalService( final ReferenceDescriptor reference )
    {
        Iterator iterator = m_services.iterator();
        while( iterator.hasNext() )
        {
            Service service = (Service) iterator.next();
            if( service.equals( reference ) )
            {
                return service;
            }
        }
        return null;
    }

    protected Logger getLogger()
    {
        return m_logger;
    }
}

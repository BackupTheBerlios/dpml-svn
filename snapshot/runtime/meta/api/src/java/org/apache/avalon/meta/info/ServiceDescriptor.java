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

package org.apache.avalon.meta.info;

import java.util.Properties;

/**
 * This descriptor defines the type of service offerend or required
 * by a component. The type corresponds to the class name of the
 * class/interface implemented by component. Associated with each
 * classname is a version object so that different versions of same
 * interface can be represented.
 *
 * <p>Also associated with each service is a set of arbitrary
 * attributes that can be used to store extra information
 * about service. See {@link InfoDescriptor} for example
 * of how to declare the container specific attributes.</p>
 *
 * <p>Possible uses for the attributes are to declare a service
 * as "stateless", "pass-by-value", "remotable" or even to attach
 * attributes such as security or transaction constraints. These
 * attributes are container specific and should not be relied
 * upon to work in all containers.</p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ServiceDescriptor.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ServiceDescriptor
        extends Descriptor
{
    /**
     * The service reference that descriptor is describing.
     */
    private final ReferenceDescriptor m_designator;

    /**
     * Construct a service descriptor.
     *
     * @param descriptor the service descriptor
     * @exception NullPointerException if the descriptor argument is null
     */
    public ServiceDescriptor( final ServiceDescriptor descriptor )
            throws NullPointerException
    {
        super( descriptor.getProperties() );
        m_designator = descriptor.getReference();
    }


    /**
     * Construct a service descriptor for specified ReferenceDescriptor
     *
     * @param designator the service reference
     * @exception NullPointerException if the designator argument is null
     */
    public ServiceDescriptor( final ReferenceDescriptor designator )
            throws NullPointerException
    {
        this( designator, null );
    }

    /**
     * Construct a service with specified name, version and attributes.
     *
     * @param designator the ReferenceDescriptor
     * @param attributes the attributes of service
     * @exception NullPointerException if the designator argument is null
     */
    public ServiceDescriptor( final ReferenceDescriptor designator,
                              final Properties attributes )
            throws NullPointerException
    {
        super( attributes );

        if ( null == designator )
        {
            throw new NullPointerException( "designator" );
        }

        m_designator = designator;
    }

    /**
     * Retrieve the reference that service descriptor refers to.
     *
     * @return the reference that service descriptor refers to.
     */
    public ReferenceDescriptor getReference()
    {
        return m_designator;
    }

    /**
     * Return the cashcode for this instance.
     * @return the instance hashcode
     */
    public int hashCode()
    {
        return m_designator.hashCode();
    }

   /**
    * Test is the supplied object is equal to this object.
    * @return true if the object are equivalent
    */
    public boolean equals(Object other)
    {
        boolean isEqual = super.equals( other ) && other instanceof ServiceDescriptor;
        isEqual = isEqual && m_designator.equals( ( (ServiceDescriptor) other ).m_designator );
        return isEqual;
    }
}

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
package org.apache.avalon.composition.data;

import org.apache.avalon.meta.info.ServiceDescriptor;

/**
 * A ServiceDirective is a class that holds a reference to a published
 * service together with a component implementation path reference, referencing 
 * the component implementing the service.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ServiceDirective.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class ServiceDirective extends ServiceDescriptor
{

   /**
    * The relative path to the component implementing the service.
    */
    private final String m_path;

   /**
    * Creation of a new service directive.
    * 
    * @param descriptor the published service
    * @param path the relative path of the implementing component
    */
    public ServiceDirective( ServiceDescriptor descriptor, String path )
    {
        super( descriptor );

        // TODO: put in place relative and absolute addressing

        if( ( null != path ) && path.startsWith( "/" ) )
        {
            m_path = path.substring( 1, path.length() );
        }
        else
        {
            m_path = path;
        }
    }

   /**
    * Return the virtual service component relative path.
    * @return the relative component path (possibly null)
    */
    public String getPath()
    {
        return m_path;
    }

}

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

package org.apache.avalon.meta.info.builder.tags;

import java.util.ArrayList;

import org.apache.avalon.framework.Version;
import org.apache.avalon.meta.info.ReferenceDescriptor;
import org.apache.avalon.meta.info.ServiceDescriptor;

import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;

/**
 * A doclet tag representing services exported by a type.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ServicesTag.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ServicesTag extends AbstractTag
{
   /**
    * The service export tag key.
    */
    public static final String KEY = "service";

   /**
    * The service tag constructor.
    * @param clazz the javadoc class descriptor
    */
    public ServicesTag( final JavaClass clazz )
    {
        super( clazz );
    }

   /**
    * Return the the set of exported services.
    * @return an array of service descriptors
    */
    public ServiceDescriptor[] getServices()
    {
        final ArrayList services = new ArrayList();
        final DocletTag[] tags = getJavaClass().getTagsByName( getNS() + Tags.DELIMITER + KEY );
        for( int i = 0; i < tags.length; i++ )
        {
            services.add( getService( tags[i] ) );
        }
        return (ServiceDescriptor[])services.toArray( new ServiceDescriptor[ services.size() ] );
    }

    private ServiceDescriptor getService( DocletTag tag )
    {
        final String value = getNamedParameter( tag, TYPE_PARAM );
        final String type = resolveType( value );
        final String versionString = getNamedParameter( tag, VERSION_PARAM, null );
        final Version version = resolveVersion( versionString, value );
        final ReferenceDescriptor ref = new ReferenceDescriptor( type, version );
        return new ServiceDescriptor( ref, null );
    }
}

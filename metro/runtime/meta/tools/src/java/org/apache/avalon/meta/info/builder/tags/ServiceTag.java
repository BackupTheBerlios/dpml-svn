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

import java.util.Properties;

import org.apache.avalon.framework.Version;
import org.apache.avalon.meta.info.ReferenceDescriptor;
import org.apache.avalon.meta.info.Service;

import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;

/**
 * A doclet tag the declares a service definition.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ServiceTag.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ServiceTag extends AbstractTag
{
   /**
    * Javadoc tag key for the name tag.
    */
    public static final String KEY = "service";

   /**
    * The version parameter
    */
    public static final String VERSION_PARAM = "version";

   /**
    * Type tag constructor.
    * @param clazz the javadoc class descriptor
    */
    public ServiceTag( final JavaClass clazz )
    {
        super( clazz );
    }

   /**
    * Return the value of the Avalon 'service' tag.
    * @return the service descriptor or null if no service is declared
    */
    public Service getService()
    {
        final DocletTag tag = getJavaClass().getTagByName( getNS() + Tags.DELIMITER + KEY );
        if( null == tag )
        {
            return null;
        }
        final Version version = Version.getVersion( getNamedParameter( tag, VERSION_PARAM, "" ) );
        final String type = getJavaClass().getFullyQualifiedName();
        final Properties properties = new AttributeTag( getJavaClass() ).getProperties();
        final ReferenceDescriptor ref = new ReferenceDescriptor( type, version );
        return new Service( ref, properties );
    }
}

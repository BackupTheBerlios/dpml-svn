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

import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;

/**
 * A doclet tag representing the set of attributes associated with the class.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: AttributeTag.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class AttributeTag extends AbstractTag
{
   /**
    * The javadoc key for the attribute tag.
    */
    public static final String KEY = "attribute";

   /**
    * The parameter name for the attribute key.
    */
    public static final String KEY_PARAM = "key";

   /**
    * The parameter name for the attribute value.
    */
    public static final String VALUE_PARAM = "value";

   /**
    * Creation of a new attribute tag.
    * @param clazz the javadoc class descriptor
    */
    public AttributeTag( final JavaClass clazz )
    {
        super( clazz );
    }

   /**
    * Return the value of all Avalon 'attribute' tags as a Properties value.
    * @return the attribute set as a property instance
    */
    public Properties getProperties()
    {
        final Properties properties = new Properties();
        final DocletTag[] tags = getJavaClass().getTagsByName( getNS() + Tags.DELIMITER + KEY  );
        for( int i = 0; i < tags.length; i++ )
        {
            final DocletTag tag = tags[ i ];
            final String key = getNamedParameter( tag, KEY_PARAM  );
            final String value = getNamedParameter( tag, VALUE_PARAM );
            properties.setProperty( key, value );
        }
        return properties;
    }
}

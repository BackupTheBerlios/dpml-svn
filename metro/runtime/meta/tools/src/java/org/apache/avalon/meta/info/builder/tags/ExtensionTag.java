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

import org.apache.avalon.meta.info.ExtensionDescriptor;

import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;

/**
 * A doclet tag handler for the 'extension' tag.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ExtensionTag.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ExtensionTag extends AbstractTag
{

   /**
    * The javadoc key for the extension tag.
    */
    public static final String KEY = "extension";

   /**
    * The stage tag id parameter name.
    */
    public static final String LEGACY_URN_PARAM = "urn";

   /**
    * The stage tag id parameter name.
    */
    public static final String LEGACY_KEY_PARAM = "key";

   /**
    * The stage tag id parameter name.
    */
    public static final String ID_PARAM = "id";

   /**
    * The extension tag constructor.
    * @param clazz the javadoc class descriptor.
    */
    public ExtensionTag( final JavaClass clazz )
    {
        super( clazz );
    }

   /**
    * Return an array of StageDescriptor instances based on declared 'stage' tags.
    * @return the stage descriptors
    */
    public ExtensionDescriptor[] getExtensions()
    {
        final ArrayList list = new ArrayList();
        final DocletTag[] tags = 
          getJavaClass().getTagsByName( getNS() + Tags.DELIMITER + KEY );
        for( int i = 0; i < tags.length; i++ )
        {
            list.add( getExtension( tags[i] ) );
        }
        return (ExtensionDescriptor[])list.toArray( 
          new ExtensionDescriptor[ list.size() ] );
    }

    private ExtensionDescriptor getExtension( DocletTag tag )
    {
        String value = getNamedParameter( tag, TYPE_PARAM, null );
        if( value != null )
        {
            final String type = resolveType( value );
            return new ExtensionDescriptor( type );
        }
        else
        {
            value = getNamedParameter( tag, LEGACY_KEY_PARAM, null );
            if( value == null )
            {
                value = getNamedParameter( tag, LEGACY_URN_PARAM, null );
            }
            if( value == null )
            {
                value = getNamedParameter( tag, ID_PARAM );
            }
            return new ExtensionDescriptor( value );
        }
    }
}

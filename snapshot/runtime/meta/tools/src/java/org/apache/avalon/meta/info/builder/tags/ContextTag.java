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
import java.util.HashSet;
import java.util.Set;
import java.util.Properties;

import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import org.apache.avalon.meta.info.ContextDescriptor;
import org.apache.avalon.meta.info.EntryDescriptor;

/**
 * A doclet tag handler for the 'extension' tag.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ContextTag.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ContextTag extends AbstractTag
{
   /**
    * The default context class.
    */
    protected static final String CONTEXT_CLASS =
        "org.apache.avalon.framework.context.Context";

   /**
    * The javadoc key for the context tag.
    */
    public static final String KEY = "context";

   /**
    * The javadoc context context strategy parameter.
    */
    public static final String STRATEGY_PARAM = "strategy";

   /**
    * The javadoc context tag key parameter.
    */
    public static final String KEY_PARAM = "key";

   /**
    * The javadoc context tag volatile flag.
    */
    public static final String VOLATILE_PARAM = "volatile";

   /**
    * The javadoc context tag alias parameter.
    */
    public static final String ALIAS_PARAM = "alias";

   /**
    * The javadoc context tag optional parameter.
    */
    public static final String OPTIONAL_PARAM = "optional";

   /**
    * The javadoc context entry tag name.
    */
    public static final String ENTRY = "entry";

   /**
    * Context tag constructor.
    * @param clazz the javadoc class descriptor
    */
    public ContextTag( final JavaClass clazz )
    {
        super( clazz );
    }

   /**
    * Return a single ContextDescriptor instance.
    * @return the context descriptors
    */
    public ContextDescriptor getContext()
    {
        JavaMethod[] methods = findTaggedMethods( 
          getJavaClass(), getNS() + Tags.DELIMITER + ENTRY );
        if( methods.length == 0 )
        {
            return new ContextDescriptor( new EntryDescriptor[0] );
        }
        else
        {
            
            //
            // collect the @avalon.entry tags from this class and 
            // all supertypes methods marked with @avalon.entry 
            // 

            final ArrayList list = new ArrayList();
            final Set marked = new HashSet( 10 );
            for( int j = 0; j < methods.length; j++ )
            {
                final DocletTag[] tags = 
                  methods[j].getTagsByName( getNS() + Tags.DELIMITER + ENTRY );
                for( int i = 0; i < tags.length; i++ )
                {
                    final String key = getNamedParameter( tags[i], KEY_PARAM );
                    if( !marked.contains( key ) )
                    {
                        list.add( getEntry( tags[i] ) );
                        marked.add( key );
                    }
                }
            }

            final EntryDescriptor[] entries =
              (EntryDescriptor[])list.toArray( new EntryDescriptor[ list.size() ] );

            String type = null;
            String strategy = null;
            for( int j = 0; j < methods.length; j++ )
            {
                JavaMethod method = methods[j];
                final DocletTag tag = method.getTagByName( getNS() + Tags.DELIMITER + KEY );
                if( tag != null )
                {
                    type = 
                      resolveType( getNamedParameter( tag, TYPE_PARAM, CONTEXT_CLASS ) );
                    strategy = getNamedParameter( tag, STRATEGY_PARAM, null );
                    break;
                }
            }

            Properties properties = null;
            if( strategy != null )
            {
                properties = new Properties();
                properties.setProperty( 
                   ContextDescriptor.STRATEGY_KEY, strategy );
            }

            return new ContextDescriptor( type, entries, properties );
        }
    }

    private EntryDescriptor getEntry( DocletTag tag )
    {
        final String key = getNamedParameter( tag, KEY_PARAM );
        final String alias = getNamedParameter( tag, ALIAS_PARAM, null );
        final String entryType = getNamedParameter( tag, TYPE_PARAM, "java.lang.String" );
        final String optional = getNamedParameter( tag, OPTIONAL_PARAM, "false" );
        final boolean isOptional = "true".equals( optional );
        final String volatileValue = getNamedParameter( tag, VOLATILE_PARAM, "false" );
        final boolean isVolatile = "true".equals( volatileValue );
        return new EntryDescriptor( key, entryType, isOptional, isVolatile, alias );
    }
}

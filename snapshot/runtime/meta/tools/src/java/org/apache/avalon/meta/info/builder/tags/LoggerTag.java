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


import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import org.apache.avalon.meta.info.CategoryDescriptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A doclet tag handler supporting 'logger' tags.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: LoggerTag.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class LoggerTag extends AbstractTag
{
    /**
     * The javadoc key for the logger tag.
     */
    protected static final String KEY = "logger";

    /**
     * The javadoc parameter name for the logging channel name
     */
    public static final String NAME_PARAM = "name";

    /**
     * The default logger class.
     */
    protected static final String LOGGER_CLASS =
            "org.apache.avalon.framework.logger.Logger";

    /**
     * The deprecated logger class
     */
    protected static final String DEPRECATED_LOGGER_CLASS =
            "org.apache.log.Logger";

    private JavaMethod[] m_methods;

    /**
     * The logger tag constructor.
     * @param clazz the javadoc class descriptor.
     */
    public LoggerTag( final JavaClass clazz )
    {
        super( clazz );
        setMethods();
    }

    /**
     * Return an array of logger descriptors relative to the 'logger' tags declared under the
     * LogEnabled interface.
     * @return the set of logger descriptos
     */
    public CategoryDescriptor[] getCategories()
    {
        final ArrayList loggers = new ArrayList();
        final Set marked = new HashSet( 10 );

        for ( int j = 0; j < m_methods.length; j++ )
        {
            final DocletTag[] tags =
                    m_methods[j].getTagsByName( getNS()
                    + Tags.DELIMITER + KEY );

            for ( int i = 0; i < tags.length; i++ )
            {
                final String name =
                        getNamedParameter( tags[i], NAME_PARAM, "" );

                if ( !marked.contains( name ) )
                {
                    final CategoryDescriptor logger =
                            new CategoryDescriptor( name, null );
                    loggers.add( logger );
                    marked.add( name );
                }
            }
        }
        return (CategoryDescriptor[]) loggers.toArray( new CategoryDescriptor[loggers.size()] );
    }

    /**
     * Set the value of the composition method.
     */
    private void setMethods()
    {
        m_methods = getLifecycleMethods( "enableLogging", LOGGER_CLASS );
        if ( m_methods.length == 0 )
        {
            m_methods = getLifecycleMethods( "setLogger", DEPRECATED_LOGGER_CLASS );
        }
    }
}

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
import org.apache.avalon.framework.Version;
import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.ReferenceDescriptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A doclet tag representing the lifestyle assigned to the Type.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DependencyTag.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DependencyTag extends AbstractTag
{
    /**
     * The dependency tag name.
     */
    public static final String KEY = "dependency";

    /**
     * The dependency tag key parameter name.
     */
    public static final String KEY_PARAM = "key";

    /**
     * The dependency tag optional parameter name.
     */
    public static final String OPTIONAL_PARAM = "optional";

    /**
     * The default component manager class.
     */
    protected static final String COMPONENT_MANAGER_CLASS =
            "org.apache.avalon.framework.component.ComponentManager";

    /**
     * The default service manager class.
     */
    protected static final String SERVICE_MANAGER_CLASS =
            "org.apache.avalon.framework.service.ServiceManager";

    private JavaMethod[] m_methods;

    /**
     * Class constructor.
     * @param clazz the javadoc class descriptor
     */
    public DependencyTag( final JavaClass clazz )
    {
        super( clazz );
        setMethods();
    }

    /**
     * Return the array of dependency descriptors based on the set of
     * 'dependency' tags associated with the components compose or service method.
     * @return the set of dependencies
     */
    public DependencyDescriptor[] getDependencies()
    {
        int n = -1;
        final ArrayList deps = new ArrayList();
        final Set marked = new HashSet( 10 );

        for ( int j = 0; j < m_methods.length; j++ )
        {
            JavaMethod method = m_methods[j];
            boolean flag = method.isConstructor();
            if( flag ) 
                n++;

            final DocletTag[] tags =
                    method.getTagsByName( getNS()
                    + Tags.DELIMITER + KEY );

            for ( int i = 0; i < tags.length; i++ )
            {
                DocletTag tag = tags[i];
                DependencyDescriptor dep = getDependency( tag, flag, n );
                final String key = dep.getKey();
                if ( !marked.contains( key ) )
                {
                    deps.add( dep );
                    marked.add( key );
                }
            }
        }
        return (DependencyDescriptor[]) deps.toArray(
                new DependencyDescriptor[deps.size()] );
    }

    private DependencyDescriptor getDependency( DocletTag tag, boolean flag, int n )
    {
        final String value = getNamedParameter( tag, TYPE_PARAM );
        final String type = resolveType( value );
        final String versionString = getNamedParameter( tag, VERSION_PARAM, null );
        final Version version = resolveVersion( versionString, value );
        final String key = getNamedParameter( tag, KEY_PARAM, type );
        final String optional = getNamedParameter( tag, OPTIONAL_PARAM, "false" );
        final boolean isOptional = "true".equals( optional.toLowerCase() );
        final ReferenceDescriptor ref = new ReferenceDescriptor( type, version );
        if( flag )
        {
            return new DependencyDescriptor( key, ref, isOptional, null, n );
        }
        else
        { 
            return new DependencyDescriptor( key, ref, isOptional, null );
        }
    }

    /**
     * Set the value of the composition method.
     */
    private void setMethods()
    {
        m_methods = getLifecycleMethods( "compose", COMPONENT_MANAGER_CLASS );
        if ( m_methods.length == 0 )
        {
            m_methods = getLifecycleMethods( "service", SERVICE_MANAGER_CLASS );
            if ( m_methods.length == 0 )
            {
                m_methods = 
                  findConstructorMethods( 
                   getJavaClass(), 
                   getNS() + Tags.DELIMITER + KEY );
            }
        }
    }
}

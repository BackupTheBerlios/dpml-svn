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

package org.apache.avalon.meta.info.builder;

import java.io.InputStream;

import org.apache.avalon.meta.info.Service;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * A ServiceBuilder is responsible for building {@link Service}
 * objects from Configuration objects.
 *
 * <p><b>UML</b></p>
 * <p><image src="doc-files/ServiceBuilder.gif" border="0"/></p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ServiceBuilder.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class ServiceBuilder
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( ServiceBuilder.class );

    private final ServiceCreator m_xmlServiceCreator = createXMLServiceCreator();
    private final ServiceCreator m_serialServiceCreator = new SerializedServiceCreator();

    /**
     * Create a {@link Service} object for specified Class.
     *
     * @param clazz The class of Component
     * @return the created Service
     * @throws Exception if an error occurs
     */
    public Service build( final Class clazz )
        throws Exception
    {
        return build( clazz.getName(), clazz.getClassLoader() );
    }

    /**
     * Create a {@link Service} object for specified
     * classname, in specified ClassLoader.
     *
     * @param classname The classname of Component
     * @param classLoader the ClassLoader to load info from
     * @return the created Service
     * @throws Exception if an error occurs
     */
    public Service build( final String classname,
                                    final ClassLoader classLoader )
        throws Exception
    {
        final Service info = buildFromSerDescriptor( classname, classLoader );
        if( null != info )
        {
            return info;
        }
        else
        {
            return buildFromXMLDescriptor( classname, classLoader );
        }
    }

    /**
     * Build Service from the XML descriptor format.
     *
     * @param classname The classname of Component
     * @param classLoader the ClassLoader to load info from
     * @return the created Service
     * @throws Exception if an error occurs
     */
    private Service buildFromSerDescriptor( final String classname,
                                                      final ClassLoader classLoader )
        throws Exception
    {
        final String xinfo =
            classname.replace( '.', '/' ) + ".sinfo";
        final InputStream inputStream =
            classLoader.getResourceAsStream( xinfo );
        if( null == inputStream )
        {
            return null;
        }

        return m_serialServiceCreator.createService( classname, inputStream );
    }

    /**
     * Build Service from the XML descriptor format.
     *
     * @param classname The classname of Component
     * @param classLoader the ClassLoader to load info from
     * @return the created Service
     * @throws Exception if an error occurs
     */
    private Service buildFromXMLDescriptor( final String classname,
                                                      final ClassLoader classLoader )
        throws Exception
    {
        //
        // get the input stream for the .xservice resource
        //

        final String xservice =
            classname.replace( '.', '/' ) + ".xservice";
        final InputStream inputStream =
            classLoader.getResourceAsStream( xservice );

        if( null == inputStream )
        {
            final String message =
                REZ.getString( "builder.missing-info.error",
                               classname );
            throw new Exception( message );
        }

        //
        // build the type
        //

        final ServiceCreator xmlServiceCreator = getXMLServiceCreator( classname );
        return xmlServiceCreator.createService( classname, inputStream );
    }

    /**
     * Utility to get xml info builder, else throw
     * an exception if missing descriptor.
     *
     * @return the ServiceCreator
     */
    private ServiceCreator getXMLServiceCreator( final String classname )
        throws Exception
    {
        if( null != m_xmlServiceCreator )
        {
            return m_xmlServiceCreator;
        }
        else
        {
            final String message =
                REZ.getString( "builder.missing-xml-creator.error",
                               classname );
            throw new Exception( message );
        }
    }

    /**
     * Utility to get XMLServiceCreator if XML files are on
     * ClassPath.
     *
     * @return the XML {@link ServiceCreator}
     */
    private static ServiceCreator createXMLServiceCreator()
    {
        ServiceCreator xmlServiceCreator = null;
        try
        {
            xmlServiceCreator = new XMLServiceCreator();
        }
        catch( final Exception e )
        {
            //Ignore it if ClassNot found due to no
            //XML Classes on classpath
        }
        return xmlServiceCreator;
    }
}

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
import java.io.IOException;
import java.util.Properties;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.parameters.ParameterException;

import org.apache.avalon.meta.ConfigurationBuilder;
import org.apache.avalon.meta.info.Type;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.xml.sax.InputSource;

/**
 * A TypeBuilder is responsible for building {@link Type}
 * objects from Configuration objects. The format for Configuration object
 * is specified in the <a href="package-summary.html#external">package summary</a>.
 *
 * <p><b>UML</b></p>
 * <p><image src="doc-files/TypeBuilder.gif" border="0"/></p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: TypeBuilder.java 31046 2004-07-31 17:20:26Z niclas $
 */
public final class TypeBuilder
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( TypeBuilder.class );

    private final TypeFactory m_xmlTypeFactory = createxmlTypeFactory();
    private final TypeCreator m_serialTypeCreator = new SerializedTypeCreator();

    /**
     * Create a {@link Type} object for specified Class.
     *
     * @param clazz The class of Component
     * @return the created Type
     * @throws Exception if an error occurs
     */
    public Type buildType( final Class clazz )
        throws Exception
    {
        //
        // build the configuration defaults and the profiles and
        // supply these as arguments to thye type build
        //

        final Type info = buildFromSerDescriptor( clazz );
        if( null != info )
        {
            return info;
        }
        else
        {
            return buildFromXMLDescriptor( clazz );
        }
    }

    /**
     * Build Type from the XML descriptor format.
     *
     * @param clazz The class for Component
     * @return the created Type
     * @throws Exception if an error occurs
     */
    private Type buildFromSerDescriptor( final Class clazz )
        throws Exception
    {
        Type type = buildFromSerDescriptor( clazz, ".ztype" );
        if( type != null )
        {
            return type;
        }
        else
        {
            return buildFromSerDescriptor( clazz, ".zinfo" );
        }
    }

    private Type buildFromSerDescriptor( final Class clazz, String form )
        throws Exception
    {
        final String classname = clazz.getName();
        final ClassLoader classLoader = clazz.getClassLoader();
        final String address = classname.replace( '.', '/' ) + form;
        final InputStream stream =
            classLoader.getResourceAsStream( address );
        if( null == stream )
        {
            return null;
        }
        return m_serialTypeCreator.createType( classname, stream );
    }

    /**
     * Build Type from the XML descriptor format.  The implementation
     * will attempt to locate a &lt;classname&gt;.xtype resource.  If
     * not found, the implementation will attempt to locate
     * a &lt;classname&gt;.info resource.  Once a resource is established
     * the type will be resolved relative to the root element.  Normally
     * the root element is a &lttype&gt;, however the implementation
     * also recognises the legacy &lt;blockinfo&gt; schema.
     *
     * @param clazz The class for Component
     * @return the created Type
     * @throws Exception if an error occurs
     */
    private Type buildFromXMLDescriptor( Class clazz )
        throws Exception
    {
        final String classname = clazz.getName();
        final ClassLoader classLoader = clazz.getClassLoader();

        final TypeFactory xmlTypeFactory = getXMLTypeFactory();

        //
        // get the input stream for the .xtype resource
        //

        String path =
            classname.replace( '.', '/' ) + ".xtype";
        InputStream inputStream =
            classLoader.getResourceAsStream( path );

        if( null == inputStream )
        {
            path =
              classname.replace( '.', '/' ) + ".xinfo";
            inputStream =
              classLoader.getResourceAsStream( path );
        }

        if( null == inputStream )
        {
            final String message =
                REZ.getString( "builder.missing-info.error", classname );
            throw new Exception( message );
        }

        final InputSource inputSource = new InputSource( inputStream );
        final Configuration xinfo = ConfigurationBuilder.build( inputSource );

        final String xdefaults =
            classname.replace( '.', '/' ) + ".xconfig";
        final InputStream defaultsStream =
            classLoader.getResourceAsStream( xdefaults );

        Configuration defaults;
        if( defaultsStream != null )
        {
            final InputSource defaultsSource = new InputSource( defaultsStream );
            defaults = resolveConfiguration(
              classLoader, ConfigurationBuilder.build( defaultsSource ) );
        }
        else
        {
            defaults = new DefaultConfiguration( "configuration", (String) null );
        }

        final String xparams =
            classname.replace( '.', '/' ) + ".xparams";
        final InputStream paramsStream =
            classLoader.getResourceAsStream( xparams );

        Parameters params;
        if( paramsStream != null )
        {
            try
            {
                Properties properties = new Properties();
                properties.load( paramsStream );
                params = Parameters.fromProperties( properties );
            }
            catch( IOException ioe )
            {
                final String error =
                    "Unexpected exception while attempting to resolve parameters from src: "
                        + xparams;
                    throw new ParameterException( error );
            }
        }
        else
        {
            params = null;
        }

        //
        // build the type
        //

        return xmlTypeFactory.createType( classname, xinfo, defaults, params );
    }

    private Configuration resolveConfiguration( ClassLoader classloader, Configuration config )
      throws Exception
    {
        if( config == null )
        {
            throw new NullPointerException("config");
        }
        String src = config.getAttribute( "src", null );
        if( src == null )
        {
            return config;
        }
        else
        {
            if( src.startsWith( "resource://" ) )
            {
                final String url = src.substring( 11 );
                final InputStream stream =
                    classloader.getResourceAsStream( url );
                if( null == stream )
                {
                    final String error =
                        "Requested configuration source does not exist: " + src;
                    throw new ConfigurationException( error );
                }
                final InputSource source = new InputSource( stream );
                return resolveConfiguration(
                  classloader, ConfigurationBuilder.build( source ) );
            }
            else
            {
                try
                {
                    return resolveConfiguration(
                      classloader, ConfigurationBuilder.build( src ) );
                }
                catch( Throwable e )
                {
                    final String error =
                        "Unexpected exception while attempting to resolve configuration from src : "
                        + src;
                    throw new ConfigurationException( error, e );
                }
            }
        }
    }

    /**
     * Utility to get xml info builder, else throw
     * an exception if missing descriptor.
     *
     * @return the TypeCreator
     */
    private TypeFactory getXMLTypeFactory()
        throws Exception
    {
        if( null != m_xmlTypeFactory )
        {
            return m_xmlTypeFactory;
        }
        else
        {
            final String message =
                REZ.getString( "builder.missing-xml-creator.error" );
            throw new Exception( message );
        }
    }

    /**
     * Utility to get xmlTypeFactory if XML files are on
     * ClassPath.
     *
     * @return the XML {@link TypeFactory}
     */
    private static TypeFactory createxmlTypeFactory()
    {
        TypeFactory xmlTypeFactory = null;
        try
        {
            xmlTypeFactory = new XMLTypeCreator();
        }
        catch( final Exception e )
        {
            //Ignore it if ClassNot found due to no
            //XML Classes on classpath
        }
        return xmlTypeFactory;
    }
}

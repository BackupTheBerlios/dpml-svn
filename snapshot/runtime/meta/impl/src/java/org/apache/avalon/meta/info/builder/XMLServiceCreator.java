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
import java.util.Properties;
import java.util.ArrayList;

import org.apache.avalon.framework.Version;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.avalon.meta.ConfigurationBuilder;
import org.apache.avalon.meta.info.Service;
import org.apache.avalon.meta.info.EntryDescriptor;
import org.apache.avalon.meta.info.ReferenceDescriptor;
import org.apache.avalon.meta.info.builder.BuildException;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.avalon.util.configuration.ConfigurationUtil;

import org.xml.sax.InputSource;

/**
 * Utility class the handles the internalization of an XML description
 * of a service into a {@link Service} instance.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: XMLServiceCreator.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class XMLServiceCreator
    implements ServiceCreator
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( XMLServiceCreator.class );

    /**
     * Create a {@link Service} from stream
     *
     * @param key the name of the service type
     * @param input the input stream that the resource is loaded from
     * @return the created {@link Service}
     * @exception Exception if an error occurs
     */
    public Service createService( String key, InputStream input )
        throws Exception
    {
        if( input == null )
        {
            throw new NullPointerException( "input" );
        }

        final InputSource source = new InputSource( input );
        final Configuration xservice = ConfigurationBuilder.build( source );
        return build( key, xservice );
    }

    /**
     * Create a {@link Service} object for a supplied classname from
     * configuration data.
     *
     * @param classname the classname of the service
     * @param info the service defintion
     * @return the created Service
     * @throws BuildException if an error occurs
     */
    private Service build( final String classname, final Configuration info )
        throws BuildException
    {
        final String topLevelName = info.getName();
        if( !topLevelName.equals( "service" ) )
        {
            final String message =
                REZ.getString( "builder.bad-toplevel-service-element.error",
                               classname,
                               topLevelName );
            throw new BuildException( message );
        }

        final Properties attributes =
            buildAttributes( info.getChild( "attributes" ) );
        final EntryDescriptor[] entries =
            buildEntries( info.getChild( "entries" ).getChildren("entry") );
        final String versionString = info.getChild( "version" ).getValue( "1.0" );
        final Version version = buildVersion( versionString );

        return new Service( new ReferenceDescriptor( classname, version ), entries, attributes );
    }

    /**
     * Build up a list of attributes from specific config tree.
     *
     * @param config the attributes config
     * @return the Properties object representing attributes
     * @throws ConfigurationException if an error occurs
     */
    public Properties buildAttributes( final Configuration config )
        throws BuildException
    {
        final Properties attributes = new Properties();
        final Configuration[] children = config.getChildren( "attribute" );
        for( int i = 0; i < children.length; i++ )
        {
            Configuration child = children[ i ];
            String key = null;
            try
            {
                key = child.getAttribute( "key" );
            }
            catch( ConfigurationException ce )
            {
                final String error =
                  "Missing 'key' attribute in 'attribute' element.\n"
                  + ConfigurationUtil.list( child );
                throw new BuildException( error, ce );
            }

            String value = null;
            try
            {
                value = child.getAttribute( "value" );
            }
            catch( Throwable e )
            {
                value = child.getValue( "" );
            }
            attributes.setProperty( key, value );
        }
        return attributes;
    }

    /**
     * A utility method to build an array of {@link EntryDescriptor}
     * objects from specified configuration.
     *
     * @param entrySet the set of entrys to build
     * @return the created {@link EntryDescriptor}s
     * @throws ConfigurationException if an error occurs
     */
    protected EntryDescriptor[] buildEntries( final Configuration[] entrySet )
        throws BuildException
    {
        final ArrayList entrys = new ArrayList();

        for( int i = 0; i < entrySet.length; i++ )
        {
            final EntryDescriptor service = buildEntry( entrySet[ i ] );
            entrys.add( service );
        }

        return (EntryDescriptor[])entrys.toArray( new EntryDescriptor[ entrys.size() ] );
    }

    /**
     * Create a {@link EntryDescriptor} from configuration.
     *
     * @param config the configuration
     * @return the created {@link EntryDescriptor}
     * @throws ConfigurationException if an error occurs
     */
    protected EntryDescriptor buildEntry( final Configuration config )
        throws BuildException
    {
        try
        {
            final String key = config.getAttribute( "key" );
            final String type = config.getAttribute( "type", "java.lang.String" );
            final boolean isVolatile = config.getAttributeAsBoolean( "volatile", false );
            final boolean optional =
              config.getAttributeAsBoolean( "optional", false );
            final String alias = config.getAttribute( "alias", null );
            return new EntryDescriptor( key, type, optional, isVolatile, alias );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to build entry descriptor."
              + ConfigurationUtil.list( config );
            throw new BuildException( error, e );
        }
    }

    /**
     * A utility method to parse a Version object from specified string.
     *
     * @param version the version string
     * @return the created Version object
     */
    protected Version buildVersion( final String version ) 
      throws BuildException
    {
        try
        {
            return Version.getVersion( version );
        }
        catch( Throwable e )
        {
            final String error = 
              REZ.getString( "builder.bad-version", version );
            throw new BuildException( error, e );
        }
    }
}

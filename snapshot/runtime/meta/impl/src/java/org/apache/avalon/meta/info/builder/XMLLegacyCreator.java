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
import java.util.ArrayList;
import java.util.Properties;

import org.apache.avalon.framework.Version;
import org.apache.avalon.framework.configuration.Configuration;

import org.apache.avalon.meta.ConfigurationBuilder;
import org.apache.avalon.meta.info.InfoDescriptor;
import org.apache.avalon.meta.info.ContextDescriptor;
import org.apache.avalon.meta.info.EntryDescriptor;
import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.ExtensionDescriptor;
import org.apache.avalon.meta.info.CategoryDescriptor;
import org.apache.avalon.meta.info.ReferenceDescriptor;
import org.apache.avalon.meta.info.ServiceDescriptor;
import org.apache.avalon.meta.info.StageDescriptor;
import org.apache.avalon.meta.info.Type;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.avalon.util.configuration.ConfigurationUtil;

import org.xml.sax.InputSource;

/**
 * Handles internalization of a legacy Phoenix XML based description of a {@link Type}
 * from a Configuration object.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: XMLLegacyCreator.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class XMLLegacyCreator
    extends XMLTypeCreator
    implements TypeCreator
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( XMLLegacyCreator.class );

    /**
     * Create a {@link Type} object for specified
     * classname, loaded from specified {@link InputStream}.
     *
     * @param implementationKey The classname of Component
     * @param inputStream the InputStream to load Type from
     * @return the created Type
     * @throws Exception if an error occurs
     */
    public Type createType( String implementationKey,
                            InputStream inputStream )
        throws Exception
    {
        if( inputStream == null )
        {
            throw new NullPointerException( "input" );
        }
        final InputSource input = new InputSource( inputStream );
        final String classname = implementationKey;
        final Configuration xinfo = ConfigurationBuilder.build( input );
        return build( classname, xinfo );
    }

    /**
     * Create a {@link Type} object for specified
     * classname and configuration.
     *
     * @param classname The classname of the component
     * @param config the meta info configuration fragment
     * @return the created Type
     * @throws ConfigurationException if an error occurs
     */
    public Type createType( String classname, Configuration config )
        throws BuildException
    {
        return build( classname, config );
    }

    /**
     * Create a {@link Type} object for specified classname from
     * specified configuration data.
     *
     * @param classname The classname of Component
     * @param info the Type configuration
     * @return the created Type
     * @throws BuildException if an error occurs
     */
    private Type build( final String classname, final Configuration info )
        throws BuildException
    {
        final String topLevelName = info.getName();
        if( !topLevelName.equals( "blockinfo" ) )
        {
            final String message =
              REZ.getString( "builder.bad-toplevel-block-element.error",
                   classname,
                   topLevelName );
            throw new BuildException( message );
        }

        Configuration configuration = null;

        configuration = info.getChild( "block" );

        final InfoDescriptor descriptor =
          buildInfoDescriptor( classname, configuration );

        configuration = info.getChild( "loggers" );
        final CategoryDescriptor[] loggers = new CategoryDescriptor[0];
        final ContextDescriptor context = 
          buildPhoenixContext();

        configuration = info.getChild( "services" );
        final ServiceDescriptor[] services = 
          buildBlockServices( configuration );

        configuration = info.getChild( "dependencies" );
        final DependencyDescriptor[] dependencies = 
          buildBlockDependencies( configuration );

        configuration = info.getChild( "stages" );
        final StageDescriptor[] phases = 
          buildStages( configuration );

        configuration = info.getChild( "extensions" );
        final ExtensionDescriptor[] extensions = 
          buildExtensions( configuration );

        return new Type(
            descriptor, loggers, context, services, dependencies, 
            phases, extensions );
    }

    /**
     * A utility method to build a {@link InfoDescriptor}
     * object from specified configuration data and classname.
     *
     * @param classname The classname of Component (used to create descriptor)
     * @param info the component info configuration fragment
     * @return the created InfoDescriptor
     * @throws ConfigurationException if an error occurs
     */
    public InfoDescriptor buildInfoDescriptor(
      final String classname, final Configuration info )
      throws BuildException
    {
        final String name = 
          info.getChild( "name" ).getValue( null );
        final String schema = 
          info.getChild( "schema-type" ).getValue( null );
        final Version version = 
          buildVersion( info.getChild( "version" ).getValue( "1.0" ) );
        final Properties attributes =
            buildAttributes( info.getChild( "attributes" ) );
        final String lifestyle = InfoDescriptor.SINGLETON;
        return new InfoDescriptor( 
          name, classname, version, lifestyle, null, schema, attributes );
    }

    private ContextDescriptor buildPhoenixContext()
    {
        EntryDescriptor name = 
          new EntryDescriptor( 
            ContextDescriptor.NAME_KEY, 
            "java.lang.String", false, false, "block.name" );
        EntryDescriptor partition = 
          new EntryDescriptor( 
            ContextDescriptor.PARTITION_KEY, 
            "java.lang.String", false, false, "app.name" );
        EntryDescriptor home = 
          new EntryDescriptor( 
            ContextDescriptor.HOME_KEY, 
            "java.io.File", false, false, "app.home" );

        return new ContextDescriptor( new EntryDescriptor[]{ name, partition, home } );
    }

    /**
     * A utility method to build an array of {@link ServiceDescriptor}
     * objects from specified configuration.
     *
     * @param depSet the set of dependency configurations
     * @return the created dependency descriptor
     * @throws ConfigurationException if an error occurs
     */
    protected DependencyDescriptor[] buildBlockDependencies( final Configuration depSet )
        throws BuildException
    {
        final Configuration[] deps = depSet.getChildren( "dependency" );
        final ArrayList dependencies = new ArrayList();
        for( int i = 0; i < deps.length; i++ )
        {
            final DependencyDescriptor dependency = 
              buildBlockDependency( deps[ i ] );
            dependencies.add( dependency );
        }
        return (DependencyDescriptor[])dependencies.toArray( 
          new DependencyDescriptor[ 0 ] );
    }


    /**
     * A utility method to build an array of {@link ServiceDescriptor}
     * objects from specified configuraiton.
     *
     * @param servicesSet the services configuration
     * @return the created ServiceDescriptor
     * @throws BuildException if an error occurs
     */
    protected ServiceDescriptor[] buildBlockServices( final Configuration servicesSet )
        throws BuildException
    {
        final Configuration[] elements = servicesSet.getChildren( "service" );
        final ArrayList services = new ArrayList();

        for( int i = 0; i < elements.length; i++ )
        {
            final ServiceDescriptor service = buildBlockService( elements[ i ] );
            services.add( service );
        }

        return (ServiceDescriptor[])services.toArray( new ServiceDescriptor[ 0 ] );
    }

    /**
     * A utility method to build a <code>ServiceDescriptor</code>
     * object from specified configuraiton data.
     *
     * @param service the service Configuration
     * @return the created ServiceDescriptor
     * @throws BuildException if an error occurs
     */
    protected ServiceDescriptor buildBlockService( final Configuration service )
        throws BuildException
    {
        final ReferenceDescriptor designator = buildReferenceDescriptor( service );
        final Properties attributes =
            buildAttributes( service.getChild( "attributes" ) );
        return new ServiceDescriptor( designator, attributes );
    }

    /**
     * A utility method to build a {@link ReferenceDescriptor}
     * object from specified configuraiton data.
     *
     * @param service the service Configuration
     * @return the created ReferenceDescriptor
     * @throws ConfigurationException if an error occurs
     */
    protected ReferenceDescriptor buildReferenceDescriptor( final Configuration service )
        throws BuildException
    {
        try
        {
            final String type = service.getAttribute( "name" );
            final String versionString = service.getAttribute( "version", "1.0" );
            final Version version = buildVersion( versionString );
            return new ReferenceDescriptor( type, version );
        }
        catch( Throwable e )
        {
            final String error =
              "Error occured while attempting to build reference descriptor from legacy blockinfo configuration: "
              + ConfigurationUtil.list( service );
            throw new BuildException( error, e );
        }
    }

    /**
     * A utility method to build a {@link DependencyDescriptor}
     * object from specified configuraiton.
     *
     * @param dependency the dependency configuration
     * @return the created DependencyDescriptor
     * @throws ConfigurationException if an error occurs
     */
    protected DependencyDescriptor buildBlockDependency( final Configuration dependency )
        throws BuildException
    {
        try
        {
            Configuration serviceRef = dependency.getChild( "service" );
            final ReferenceDescriptor service =
              buildReferenceDescriptor( serviceRef );
            final boolean optional =
              dependency.getAttributeAsBoolean( "optional", false );
            final Properties attributes =
              buildAttributes( dependency.getChild( "attributes" ) );
            String role = dependency.getChild( "role" ).getValue( null );

            // default to name of service if role unspecified
            if( null == role ) role = service.getClassname();
            return new DependencyDescriptor( role, service, optional, attributes );
        }
        catch( Throwable e )
        {
            final String error =
              "Error occured while attempting to build dependency descriptor from legacy blockinfo descriptor: "
              + ConfigurationUtil.list( dependency );
            throw new BuildException( error, e );
        }
    }

}

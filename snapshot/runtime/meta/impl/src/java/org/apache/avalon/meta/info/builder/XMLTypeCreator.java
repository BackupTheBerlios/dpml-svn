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
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.avalon.meta.ConfigurationBuilder;
import org.apache.avalon.meta.info.InfoDescriptor;
import org.apache.avalon.meta.info.ContextDescriptor;
import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.EntryDescriptor;
import org.apache.avalon.meta.info.ExtensionDescriptor;
import org.apache.avalon.meta.info.CategoryDescriptor;
import org.apache.avalon.meta.info.ReferenceDescriptor;
import org.apache.avalon.meta.info.ServiceDescriptor;
import org.apache.avalon.meta.info.StageDescriptor;
import org.apache.avalon.meta.info.SecurityDescriptor;
import org.apache.avalon.meta.info.PermissionDescriptor;
import org.apache.avalon.meta.info.Type;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;
import org.apache.avalon.util.configuration.ConfigurationUtil;

import org.xml.sax.InputSource;

/**
 * Handles internalization of an XML based description of a {@link Type}
 * from a Configuration object. The format for Configuration object
 * is specified in the <a href="package-summary.html#external">package summary</a>.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: XMLTypeCreator.java 31046 2004-07-31 17:20:26Z niclas $
 */
public class XMLTypeCreator
    extends XMLServiceCreator implements TypeFactory
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( XMLTypeCreator.class );

    /**
     * Create a {@link Type} object for specified
     * classname, loaded from specified {@link InputStream}.
     *
     * @param classname The classname of Component
     * @param inputStream the InputStream to load Type from
     * @return the created Type
     * @throws Exception if an error occurs
     */
    public Type createType( String classname,
                            InputStream inputStream )
        throws Exception
    {
        if( inputStream == null )
        {
            throw new NullPointerException( "input" );
        }

        final InputSource input = new InputSource( inputStream );
        final Configuration xinfo = ConfigurationBuilder.build( input );
        return createType( classname, xinfo, (Configuration) null, null );
    }

        /**
     * Create an {@link Type} object for a specified classname from
     * specified configuration data.
     *
     * @param classname The classname of Component
     * @param info the Type configuration
     * @param defaults the default configuration
     * @return the created Type
     * @throws BuildException if an error occurs
     */
    public Type createType(
        final String classname, final Configuration info, final Configuration defaults )
        throws BuildException
    {
         return createType( classname, info, defaults, null );
    }

    /**
     * Create an {@link Type} object for a specified classname from
     * specified configuration data.
     *
     * @param classname The classname of Component
     * @param info the Type configuration
     * @param defaults the default configuration
     * @param params the default static parameters
     * @return the created Type
     * @throws BuildException if an error occurs
     */
    public Type createType( 
        final String classname, final Configuration info, final Configuration defaults,
        Parameters params )
        throws BuildException
    {
        final String topLevelName = info.getName();
         
        if( topLevelName.equals( "blockinfo" ) )
        {
            return new XMLLegacyCreator().createType( classname, info );
        }

        if( !topLevelName.equals( "type" ) )
        {
            final String message =
                REZ.getString( "builder.bad-toplevel-element.error",
                               classname,
                               topLevelName );
            throw new BuildException( message );
        }

        Configuration configuration = null;

        //
        // changed the information block from "component" to "info" to
        // avoid confusion with the "component" element definition in the
        // meta-data level - change is backward compatible
        //

        configuration = info.getChild( "info", false );
        final InfoDescriptor descriptor =
            buildInfoDescriptor( classname, configuration );

        configuration = info.getChild( "security", false );
        final SecurityDescriptor security =
            buildSecurityDescriptor( configuration );

        configuration = info.getChild( "loggers" );
        final CategoryDescriptor[] loggers = buildLoggers( configuration );

        configuration = info.getChild( "context" );
        final ContextDescriptor context = buildContext( configuration );

        configuration = info.getChild( "services" );
        final ServiceDescriptor[] services = buildServices( configuration );

        configuration = info.getChild( "dependencies" );
        final DependencyDescriptor[] dependencies = 
           buildDependencies( configuration );

        configuration = info.getChild( "stages" );
        final StageDescriptor[] phases = buildStages( configuration );

        configuration = info.getChild( "extensions" );
        final ExtensionDescriptor[] extensions = buildExtensions( configuration );

        return new Type(
          descriptor, security, loggers, context, services, dependencies, phases, 
          extensions, defaults, params );
    }

    /**
     * Utility function to create a set of phase descriptor from a configuration.
     * @param config a configuration containing 0..n phase elements
     * @return an array of phase descriptors
     * @exception BuildException if a build error occurs
     */
    protected StageDescriptor[] buildStages( Configuration config ) 
      throws BuildException
    {
        ArrayList list = new ArrayList();
        Configuration[] stages = config.getChildren( "stage" );
        for( int i = 0; i < stages.length; i++ )
        {
            StageDescriptor stage = buildPhase( stages[i] );
            list.add( stage );
        }
        return (StageDescriptor[])list.toArray( new StageDescriptor[ 0 ] );
    }

    /**
     * Utility function to create a set of phase descriptor from a configuration.
     * @param config a configuration containing 0..n phase elements
     * @return an array of phase descriptors
     * @exception BuildException if a build error occurs
     */
    protected StageDescriptor buildPhase( Configuration config ) 
      throws BuildException
    {

        try
        {
            String id = null;
            if( config.getAttribute( "type", null ) != null ) // legacy
            {
                id = config.getAttribute( "type" );
            }
            else if( config.getAttribute( "key", null ) != null ) // legacy
            {
               id = config.getAttribute( "key" );
            }

            if( id == null ) id = config.getAttribute( "id" ); // standard

            final Properties attributes =
              buildAttributes( config.getChild( "attributes" ) );
            return new StageDescriptor( id, attributes );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to construct a stage descriptor from source fragment:"
              + ConfigurationUtil.list( config );
            throw new BuildException( error, e );
        }
    }

    /**
     * A utility method to build a {@link ReferenceDescriptor}
     * object from specified configuration data.
     *
     * @param service the service Configuration
     * @return the created ReferenceDescriptor
     * @throws BuildException if an error occurs
     */
    protected ReferenceDescriptor buildReferenceDescriptor( final Configuration service )
        throws BuildException
    {
        return buildReferenceDescriptor( service, null );
    }

    /**
     * A utility method to build a {@link ReferenceDescriptor}
     * object from specified configuration data.
     *
     * @param service the service Configuration
     * @param classname the default type classname
     * @return the created ReferenceDescriptor
     * @throws BuildException if an error occurs
     */
    protected ReferenceDescriptor buildReferenceDescriptor( final Configuration service, String classname )
        throws BuildException
    {
        final String type = service.getAttribute("type", classname );
        if( type == null )
        {
            final String error = 
              "Missing 'type' attribute in configuration: " 
              + ConfigurationUtil.list( service );
            throw new BuildException( error );
        }
        if( type.indexOf( ":" ) > -1 )
        {
            return createReference( type );
        }
        final String versionString = service.getAttribute( "version", "1.0" );
        final Version version = buildVersion( versionString );
        return new ReferenceDescriptor( type, version );
    }

    /**
     * A utility method to build an array of {@link CategoryDescriptor} objects
     * from specified configuraiton.
     *
     * @param configuration the loggers configuration
     * @return the created CategoryDescriptor
     * @throws BuildException if an error occurs
     */
    protected CategoryDescriptor[] buildLoggers( final Configuration configuration )
        throws BuildException
    {
        final Configuration[] elements = configuration.getChildren( "logger" );
        final ArrayList loggers = new ArrayList();

        for( int i = 0; i < elements.length; i++ )
        {
            final CategoryDescriptor logger = buildLogger( elements[ i ] );
            loggers.add( logger );
        }

        return (CategoryDescriptor[])loggers.toArray( new CategoryDescriptor[ loggers.size() ] );
    }

    /**
     * A utility method to build a {@link CategoryDescriptor}
     * object from specified configuraiton.
     *
     * @param logger the Logger configuration
     * @return the created CategoryDescriptor
     * @throws BuildException if an error occurs
     */
    private CategoryDescriptor buildLogger( Configuration logger )
        throws BuildException
    {
        final Properties attributes = buildAttributes( logger.getChild( "attributes" ) );
        final String name = logger.getAttribute( "name", "" );
        return new CategoryDescriptor( name, attributes );
    }

    /**
     * A utility method to build an array of {@link DependencyDescriptor}
     * objects from specified configuration.
     *
     * @param configuration the dependencies configuration
     * @return the created DependencyDescriptor
     * @throws BuildException if an error occurs
     */
    public DependencyDescriptor[] buildDependencies( final Configuration configuration )
        throws BuildException
    {
        final Configuration[] elements = configuration.getChildren( "dependency" );
        final ArrayList dependencies = new ArrayList();

        for( int i = 0; i < elements.length; i++ )
        {
            final DependencyDescriptor dependency =
                buildDependency( elements[ i ] );
            dependencies.add( dependency );
        }

        return (DependencyDescriptor[])dependencies.toArray( new DependencyDescriptor[ 0 ] );
    }

    /**
     * A utility method to build a {@link DependencyDescriptor}
     * object from specified configuraiton.
     *
     * @param dependency the dependency configuration
     * @return the created DependencyDescriptor
     * @throws BuildException if an error occurs
     */
    protected DependencyDescriptor buildDependency( final Configuration dependency )
        throws BuildException
    {
        String role = dependency.getAttribute( "role", null ); // legacy
        if( role == null )
        {
            role = dependency.getAttribute( "key", null );
        }
        ReferenceDescriptor reference = 
          buildReferenceDescriptor( dependency );
        
        final boolean optional =
           dependency.getAttributeAsBoolean( "optional", false );

        final Properties attributes =
            buildAttributes( dependency.getChild( "attributes" ) );

        //
        // default to name of service if role unspecified
        //

        if( null == role )
        {
            role = reference.getClassname();
        }

        return new DependencyDescriptor( role, reference, optional, attributes );
    }

    /**
     * A utility method to build a {@link ContextDescriptor}
     * object from specified configuration.
     *
     * @param context the dependency configuration
     * @return the created ContextDescriptor
     * @throws BuildException if an error occurs
     */
    protected ContextDescriptor buildContext( final Configuration context )
        throws BuildException
    {
        final EntryDescriptor[] entrys =
          buildEntries( context.getChildren( "entry" ) );

        final Properties attributes =
          buildAttributes( context.getChild( "attributes" ) );

        String classname = context.getAttribute( "type", null );

        return new ContextDescriptor( classname, entrys, attributes );
    }


    /**
     * A utility method to build an array of {@link ServiceDescriptor}
     * objects from specified configuraiton.
     *
     * @param servicesSet the services configuration
     * @return the created ServiceDescriptor
     * @throws BuildException if an error occurs
     */
    public ServiceDescriptor[] buildServices( final Configuration servicesSet )
        throws BuildException
    {
        final Configuration[] elements = servicesSet.getChildren( "service" );
        final ArrayList services = new ArrayList();

        for( int i = 0; i < elements.length; i++ )
        {
            final ServiceDescriptor service = buildService( elements[ i ] );
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
    public ServiceDescriptor buildService( final Configuration service )
        throws BuildException
    {
        ReferenceDescriptor reference = buildReferenceDescriptor( service );
        final Properties attributes =
            buildAttributes( service.getChild( "attributes" ) );
        return new ServiceDescriptor( reference, attributes );
    }

    /**
     * A utility method to build a {@link InfoDescriptor}
     * object from specified configuraiton data and classname.
     *
     * @param classname The classname of Component (used to create descriptor)
     * @param info the component info configuration fragment
     * @return the created InfoDescriptor
     * @throws BuildException if an error occurs
     */
    public InfoDescriptor buildInfoDescriptor(
      final String classname, final Configuration info )
      throws BuildException
    {
        final String name = 
          info.getChild( "name" ).getValue( null );
        final Version version = 
          buildVersion( info.getChild( "version" ).getValue( "1.0" ) );
        final String schema = 
          info.getChild( "schema" ).getValue( null );
        final Properties attributes =
            buildAttributes( info.getChild( "attributes" ) );
        final String lifestyle = 
          buildLifestyle( info, attributes );
        final String collectionLegacy = 
          info.getChild( "lifestyle" ).getAttribute( "collection", null );
        final String collection = 
          info.getChild( "collection" ).getValue( collectionLegacy );
        return new InfoDescriptor( 
          name, classname, version, lifestyle, collection, schema, attributes );
    }

    /**
     * A utility method to build a {@link SecurityDescriptor}
     * object from specified configuration data.
     *
     * @param config the security configuration fragment
     * @return the created SecurityDescriptor
     * @throws BuildException if an error occurs
     */
    public SecurityDescriptor buildSecurityDescriptor( final Configuration config )
      throws BuildException
    {
        if( null == config ) return new SecurityDescriptor( null, null );

        try
        {
            final Properties attributes =
              buildAttributes( config.getChild( "attributes" ) );
            PermissionDescriptor[] permissions = buildPermissions( config );
            return new SecurityDescriptor( permissions, attributes );
        }
        catch( ConfigurationException e )
        {
            final String error = 
              "Cannot build secrity descriptor.";
            throw new BuildException( error, e );
        }
    }

    private PermissionDescriptor[] buildPermissions( final Configuration config )
      throws ConfigurationException
    {
        ArrayList list = new ArrayList();
        Configuration[] children = config.getChildren( "permission" );
        for( int i = 0; i < children.length; i++ )
        {			
            list.add( buildPermission( children[i] ) );
        }
        return (PermissionDescriptor[])list.toArray( new PermissionDescriptor[ 0 ] );
    }

    private PermissionDescriptor buildPermission( final Configuration config ) 
      throws ConfigurationException
    {
        final String classname = config.getAttribute( "class" );
        final String name = config.getAttribute( "name", null );
        ArrayList list = new ArrayList();
        Configuration[] children = config.getChildren( "action" );
        for( int i=0; i<children.length; i++ )
        {
            Configuration child = children[i];
            String action = child.getValue();
            list.add( action );
        }
        String[] actions = (String[])list.toArray( new String[0] );
        return new PermissionDescriptor( classname, name, actions );
    }

   /**
    * Handle the resolution of the component lifestyle.  Normally this is 
    * resolved by retrieving the &lt;lifestyle&gt; element value, however, for 
    * backward compatability - if the lifecycle element is not present, we will
    * attempt to resolve the lifestyle using the attribute value relative to the
    * key urn:avalon:lifestyle.
    *
    * @param info the info configuration fragment
    * @param attributes the component attributes
    * @return the lifestyle policy value
    */
    private String buildLifestyle( Configuration info, Properties attributes )
    {
        String lifestyle = info.getChild( "lifestyle" ).getValue( null );
        if( lifestyle != null )
        {
            return lifestyle;
        }
        else
        {
            return attributes.getProperty( "urn:avalon:lifestyle" );
        }
    }

    /**
     * Utility function to create a set of phase descriptor from a configuration.
     * @param config a configuration containing 0..n phase elements
     * @return an array of phase descriptors
     * @exception BuildException if a build error occurs
     */
    protected ExtensionDescriptor[] buildExtensions( Configuration config ) 
      throws BuildException
    {
        ArrayList list = new ArrayList();
        Configuration[] extensions = config.getChildren( "extension" );
        for( int i = 0; i < extensions.length; i++ )
        {
            list.add( buildExtension( extensions[i] ) );
        }
        return (ExtensionDescriptor[])list.toArray( new ExtensionDescriptor[ 0 ] );
    }

    /**
     * Utility function to create an extension descriptor from a configuration.
     * @param config a configuration containing the extension definition
     * @return the extension descriptor
     * @exception BuildException if a build error occurs
     */
    protected ExtensionDescriptor buildExtension( Configuration config ) 
      throws BuildException
    {
        if( config.getAttribute( "type", null ) != null ) // legacy
        {
            String urn = config.getAttribute( "type", null );
            final Properties attributes =
              buildAttributes( config.getChild( "attributes" ) );
            return new ExtensionDescriptor( urn, attributes );
        }
        else
        {
            String id = config.getAttribute( "urn", null ); // legacy
            if( id == null )
            {
               try
               {
                   id = config.getAttribute( "id" );
               }
               catch( ConfigurationException e )
               {
                   final String error = 
                     "Missing extensions identifier 'id' attribute."
                     + ConfigurationUtil.list( config );
                   throw new BuildException( error, e );
               }
            }
            final Properties attributes =
              buildAttributes( config.getChild( "attributes" ) );
            return new ExtensionDescriptor( id, attributes );
        }
    }

   /**
    * Creation of a reference descriptor from the supplied path.
    * @param path the classname
    * @return the reference descriptor
    */
    public ReferenceDescriptor createReference( String path ) 
      throws BuildException
    {
        final String type;
        final Version version;
        int index = path.indexOf(":");
        if( index > -1 )
        {
            type = path.substring( 0, index );
            version = buildVersion( path.substring( index + 1 ) );
        }
        else
        {
            type = path;
            version = buildVersion( "1.0.0" );
        }

        return new ReferenceDescriptor( type, version );
    }
}

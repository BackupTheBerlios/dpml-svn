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

package org.apache.avalon.composition.data.builder;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import org.apache.avalon.logging.data.CategoriesDirective;

import org.apache.avalon.composition.data.BlockCompositionDirective;
import org.apache.avalon.composition.data.BlockIncludeDirective;
import org.apache.avalon.composition.data.ClassLoaderDirective;
import org.apache.avalon.composition.data.ClasspathDirective;
import org.apache.avalon.composition.data.ContainmentProfile;
import org.apache.avalon.composition.data.DeploymentProfile;
import org.apache.avalon.composition.data.ExcludeDirective;
import org.apache.avalon.composition.data.FilesetDirective;
import org.apache.avalon.composition.data.IncludeDirective;
import org.apache.avalon.composition.data.LibraryDirective;
import org.apache.avalon.composition.data.MetaDataException;
import org.apache.avalon.composition.data.NamedComponentProfile;
import org.apache.avalon.composition.data.ServiceDirective;
import org.apache.avalon.composition.data.Targets;
import org.apache.avalon.composition.data.TargetDirective;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.avalon.meta.info.ServiceDescriptor;
import org.apache.avalon.meta.info.builder.XMLTypeCreator;

import org.apache.avalon.repository.Artifact;

import org.apache.avalon.util.configuration.ConfigurationUtil;

/**
 * Handles internalization of an XML based description of a {@link ContainmentProfile}
 * from a Configuration object.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: XMLContainmentProfileCreator.java 37418 2004-09-02 20:30:29Z cfieber $
 */
public class XMLContainmentProfileCreator extends XMLProfileCreator
{
    private static final XMLTypeCreator TYPE_CREATOR = 
      new XMLTypeCreator();

    private static final XMLComponentProfileCreator DEPLOYMENT_CREATOR = 
      new XMLComponentProfileCreator();

    private static final XMLTargetsCreator TARGETS_CREATOR = 
      new XMLTargetsCreator();

    /**
     * Create a {@link ContainmentProfile} from a configuration
     *
     * @param config the partitition configuration
     * @return the partition
     * @exception Exception if a error occurs during profile creation
     */
    public ContainmentProfile createContainmentProfile( Configuration config )
      throws Exception
    {
        //
        // build the containment description 
        //

        String name = null;
        if( config.getName().equals( "block" ) )
        {
            // Merlin 2.1 legacy
            name = config.getChild( "info" ).getChild( "name" ).getValue( "untitled" );
        }
        else
        {
            name = getName( null, config, "untitled" );
        }

        ServiceDirective[] exports = 
          createServiceDirectives( config.getChild( "services", false ) );

        //
        // check for any legacy "implementation" tags and if it exists
        // then run with it, otherwise continue with the container defintion
        //

        Configuration implementation = config;
        if( config.getChild( "implementation", false ) != null )
        {
            implementation = config.getChild( "implementation" );
        }

        ClassLoaderDirective classloader = null;
        if( implementation.getChild( "engine", false ) != null )
        {
            // Merlin 2.1 legacy

            Configuration engine = implementation.getChild( "engine" );
            String msg = ConfigurationUtil.list( engine );
            System.out.println("please replace 'engine' with 'classloader'\n" + msg );
            classloader = createClassLoaderDirective( engine );
        }
        else
        {
            classloader = 
              createClassLoaderDirective( 
                implementation.getChild( "classloader", false ) );
        }

        //
        // build any logging category directives
        // 

        final CategoriesDirective categories = 
          getCategoriesDirective( implementation.getChild( "categories", false ) );

        //
        // build nested profiles
        // 

        final DeploymentProfile[] profiles = createProfiles( implementation );

        //
        // return the containment profile
        // 

        return new ContainmentProfile( name, classloader, exports, categories, profiles );
    }

    private ClassLoaderDirective createClassLoaderDirective( Configuration config )
       throws ConfigurationException
    {
        if( config == null )
        {
            return null;
        }

        LibraryDirective library = 
          createLibraryDirective( config.getChild( "library", false ) );
        ClasspathDirective classpath = 
          createClasspathDirective( config.getChild( "classpath", false ) );
        return new ClassLoaderDirective( library, classpath );
    }
    
    private ClasspathDirective createClasspathDirective( Configuration config )
       throws ConfigurationException
    {
        if( config == null )
        {
            return null;
        }

        FilesetDirective[] filesets = createFilesetDirectives( config );
        Artifact[] artifacts = createArtifactDirectives( config );
        return new ClasspathDirective( filesets, artifacts );
    }

    private LibraryDirective createLibraryDirective( Configuration config )
       throws ConfigurationException
    {
        if( config == null )
        {
            return null;
        }

        Configuration[] includes = config.getChildren( "include" );
        String[] inc = new String[ includes.length ];
        for( int i = 0; i < includes.length; i++ )
        {
            inc[i] = getIncludeValue( includes[i] );
        }

        Configuration[] groups = config.getChildren( "group" );
        String[] grp = new String[ groups.length ];
        for( int i = 0; i < groups.length; i++ )
        {
            grp[i] = groups[i].getValue();
        }

        return new LibraryDirective( inc, grp );
    }

    private Artifact[] createArtifactDirectives( Configuration config )
       throws ConfigurationException
    {
        ArrayList list = new ArrayList();

        //
        // handle the legacy repository element and the contained resource
        // elements
        // 

        Configuration[] repositories = config.getChildren( "repository" );
        for( int i = 0; i < repositories.length; i++ )
        {
            Artifact[] artifacts = createResourceDirectives( repositories[i] );
            for( int j=0; j<artifacts.length; j++ )
            {
                list.add( artifacts[j] );
            }
        }

        //
        // get the artifact references
        //

        Configuration[] children = config.getChildren( "artifact" );
        for( int i = 0; i < children.length; i++ )
        {
            Artifact artifact = 
              createArtifactDirective( children[i] );
            list.add( artifact );
        }

        return (Artifact[]) list.toArray( new Artifact[0] );
    }

    private Artifact createArtifactDirective( Configuration config )
       throws ConfigurationException
    {
        String spec = config.getValue();
        String uri = getURI( spec );
        return Artifact.createArtifact( uri );
    }

    private String getURI( String path )
    {
        if( path.startsWith( "artifact:" ) ) return path;
        return "artifact:" + path;
    }

    private Artifact[] createResourceDirectives( Configuration config )
       throws ConfigurationException
    {
        if( config == null )
        {
            throw new NullPointerException( "config" );
        }

        ArrayList list = new ArrayList();
        Configuration[] resources = config.getChildren( "resource" );
        for( int i = 0; i<resources.length; i++ )
        {
            Configuration resource = resources[i];
            list.add( createResourceDirective( resource ) );
        }

        return (Artifact[]) list.toArray( new Artifact[0] );
    }


   /**
    * Convert a configuration fragement with an 'artifact' or 'id' 
    * attribute to an Artifact instance.
    */
    private Artifact createResourceDirective( Configuration config )
       throws ConfigurationException
    {
        String id = config.getAttribute( "id" );

        //
        // check for the depricated version attribute and use
        // the depricated technical of [group]:[name];[version]
        //

        final String version = config.getAttribute( "version", null );
        if( version == null )
        {
            return Artifact.createArtifact( id );
        }
        else
        {
            return Artifact.createArtifact( id + ";" + version );
        }
    }

    private FilesetDirective[] createFilesetDirectives( Configuration config )
       throws ConfigurationException
    {
        ArrayList list = new ArrayList();
        Configuration[] children = config.getChildren( "fileset" );
        for( int i = 0; i < children.length; i++ )
        {
            Configuration child = children[i];
            list.add( createFilesetDirective( child ) );
        }
        return (FilesetDirective[]) list.toArray( new FilesetDirective[0] );
    }

    /**
     * Utility method to create a new fileset descriptor from a
     * configuration instance.
     * @param config a configuration defining the fileset
     * @return the fileset descriptor
     * @exception ConfigurationException if the configuration is
     *   incomplete
     */
    public FilesetDirective createFilesetDirective( Configuration config )
       throws ConfigurationException
    {
        String base = config.getAttribute( "dir", "." );
        IncludeDirective[] includes = createIncludeDirectives( config );
        ExcludeDirective[] excludes = createExcludeDirectives( config );
        return new FilesetDirective( base, includes, excludes );
    }

    /**
     * Utility method to create a set in include directives.
     * @param config a configuration defining the fileset
     * @return the includes
     * @exception ConfigurationException if the configuration is
     *   incomplete
     */
    protected IncludeDirective[] createIncludeDirectives( Configuration config )
       throws ConfigurationException
    {
        if( config == null )
        {
            return new IncludeDirective[0];
        }

        ArrayList list = new ArrayList();
        Configuration[] children = config.getChildren( "include" );
        for( int i = 0; i < children.length; i++ )
        {
            Configuration child = children[i];
            list.add( createIncludeDirective( child ) );
        }

        return (IncludeDirective[]) list.toArray( new IncludeDirective[0] );
    }

    /**
     * Utility method to create a set in exclude directives.
     * @param config a configuration defining the fileset
     * @return the excludes
     * @exception ConfigurationException if the configuration is
     *   incomplete
     */
    protected ExcludeDirective[] createExcludeDirectives( Configuration config )
       throws ConfigurationException
    {
        if( config == null )
        {
            return new ExcludeDirective[0];
        }

        ArrayList list = new ArrayList();
        Configuration[] children = config.getChildren( "exclude" );
        for( int i = 0; i < children.length; i++ )
        {
            Configuration child = children[i];
            list.add( createExcludeDirective( child ) );
        }

        return (ExcludeDirective[]) list.toArray( new ExcludeDirective[0] );
    }

    /**
     * Utility method to create a new include directive from a
     * configuration instance.
     * @param config a configuration defining the include directive
     * @return the include directive
     * @exception ConfigurationException if the configuration does not
     *   declare the name attribute
     */
    protected IncludeDirective createIncludeDirective( Configuration config )
       throws ConfigurationException
    {
        return new IncludeDirective( getIncludeValue( config ) );
    }

    /**
     * Utility method to create a new exclude directive from a
     * configuration instance.
     * @param config a configuration defining the exclude directive
     * @return the exclude directive
     * @exception ConfigurationException if the configuration does not
     *   declare the name attribute
     */
    protected ExcludeDirective createExcludeDirective( Configuration config )
       throws ConfigurationException
    {
        return new ExcludeDirective( getExcludeValue( config ) );
    }

    private String getIncludeValue( Configuration config ) 
      throws ConfigurationException
    {
        if( config.getAttribute( "path", null ) != null )
        {
            return config.getAttribute( "path" );
        }
        else if( config.getAttribute( "name", null ) != null )
        {
            return config.getAttribute( "name" );
        }
        else
        {
            return config.getValue();
        }
    }

    private String getExcludeValue( Configuration config ) 
      throws ConfigurationException
    {
        return getIncludeValue( config );
    }

   /**
    * Return the set of profiles embedded in the supplied 
    * configuration.
    *
    * @param config a container or implementation configutation
    * @return the set of profile 
    */ 
    protected DeploymentProfile[] createProfiles( Configuration config )
      throws Exception
    {
        ArrayList list = new ArrayList();
        Configuration[] children = config.getChildren();
        for( int i=0; i<children.length; i++ )
        {
            Configuration child = children[i];
            if( !child.getName().equals( "classloader" ) )
            {
                if( child.getName().equals( "container" ) )
                {
                    list.add( createContainmentProfile( child ) );
                }
                else if( child.getName().equals( "component" ) )
                {
                    if( child.getAttribute( "profile", null ) != null )
                    {
                        list.add( createNamedComponentProfile( child ) );
                    }
                    else
                    {                    
                        list.add( 
                          DEPLOYMENT_CREATOR.createComponentProfile( child ) );
                    }
                }
                else if( child.getName().equals( "include" ) )
                {
                    list.add( createFromInclude( child ) );
                }
            }
        }
        return (DeploymentProfile[]) list.toArray( new DeploymentProfile[0] );
    }

   /**
    * Create a profile using a packaged deployment profile.
    * @param config the component configuration
    * @return the named profile
    */
    private NamedComponentProfile createNamedComponentProfile( Configuration config )
      throws Exception
    {
         final String name = config.getAttribute( "name" );
         final String classname = config.getAttribute( "class" );
         final String key = config.getAttribute( "profile" );
         final int activation = getActivationDirective( config ); 
         return new NamedComponentProfile( name, classname, key, activation );
    }

   /**
    * Resolve the logical services declared by a block directive.
    * @param config the services configuration fragment
    * @return the set of declared service descriptors
    */
    public ServiceDirective[] createServiceDirectives( Configuration config )
      throws MetaDataException
    {
        if( config == null )
        {
            return new ServiceDirective[0];
        }

        Configuration[] children = config.getChildren( "service" );
        ArrayList list = new ArrayList();
        for( int i=0; i<children.length; i++ )
        {
            list.add( createServiceDirective( children[i] ) );
        }
        return (ServiceDirective[]) list.toArray( new ServiceDirective[0] );
    }

   /**
    * Resolve a service directive declared by a block directive.
    * @param config the service configuration fragment
    * @return the set of declared services directives
    */
    private ServiceDirective createServiceDirective( Configuration config )
      throws MetaDataException
    {
        try
        {
            ServiceDescriptor service = TYPE_CREATOR.buildService( config );
            Configuration source = config.getChild( "source" );
            String path = source.getValue( null );
            return new ServiceDirective( service, path );
        }
        catch( Throwable ce )
        {
            final String error =
              "Invalid service declaration in block specification:\n"
               + ConfigurationUtil.list( config );
            throw new MetaDataException( error, ce );
        }
    }

   /**
    * Create a containment defintion for an include statement. Two variant
    * of include are supported - include by resource reference, and include
    * of a source container defintion.
    *
    * @param the include description
    * @return the containment directive
    */
    private DeploymentProfile createFromInclude( Configuration config )
      throws MetaDataException, ConfigurationException
    {
        //
        // get the name of the block to include
        //

        final String name = getBlockIncludeName( config );
        if( null != config.getAttribute( "artifact", null ) ) 
        {
            String spec = config.getAttribute( "artifact" );
            Artifact artifact = Artifact.createArtifact( "artifact:" + spec );
            TargetDirective[] targets = createTargetDirectives( config );
            return new BlockCompositionDirective( name, artifact, targets );
        }
        else if( null != config.getAttribute( "id", null ) )
        {
            System.out.println( "Using deprecated include format (see artifact attribute).");
            Artifact artifact = createResourceDirective( config );
            TargetDirective[] targets = createTargetDirectives( config );
            return new BlockCompositionDirective( name, artifact, targets );
        }
        else
        {
            final String path = getBlockIncludePath( config );
            return new BlockIncludeDirective( name, path );
        }
    }

    private TargetDirective[] createTargetDirectives( Configuration config )
      throws MetaDataException
    {
        try
        {
            Targets targets = TARGETS_CREATOR.createTargets( config );
            return targets.getTargets();
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error while attempting to build target directives.";
            throw new MetaDataException( error, e );
        }
    }

    private String getBlockIncludeName( Configuration config ) throws MetaDataException
    {
        try
        {
            return config.getAttribute( "name" );
        }
        catch( ConfigurationException e )
        {
            final String error =
              "Missing 'name' attribute in the block include statement:\n"
               + ConfigurationUtil.list( config );
            throw new MetaDataException( error, e );
        }
    }

    private String getBlockIncludePath( Configuration config ) throws MetaDataException
    {
        try
        {
            Configuration source = config.getChild( "source", false );
            if( null == source )
            {
                final String error =
                  "Missing 'source' element in the block include statement:\n"
                 + ConfigurationUtil.list( config );
                throw new MetaDataException( error );
            }
            return source.getValue();
        }
        catch( ConfigurationException e )
        {
            final String error =
              "Missing source value in the block include statement:\n"
               + ConfigurationUtil.list( config );
            throw new MetaDataException( error, e );
        }
    }
}

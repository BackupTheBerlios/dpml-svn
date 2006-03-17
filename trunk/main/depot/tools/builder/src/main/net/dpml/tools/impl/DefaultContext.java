/*
 * Copyright 2005 Stephen J. McConnell.
 *
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

package net.dpml.tools.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import net.dpml.library.info.Scope;
import net.dpml.library.Library;
import net.dpml.library.Resource;

import net.dpml.lang.Type;

import net.dpml.tools.model.Context;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;

/**
 * Default implmentation of a project context.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
final class DefaultContext implements Context
{
    private final Project m_project;
    private final Resource m_resource;
    private final Library m_library;
    
    private Path m_runtime;
    private Path m_test;
        
   /**
    * Creation of a new project build context.
    * @param resource the resource definition
    * @param library the resource library
    * @param project the Ant project
    */
    public DefaultContext( Resource resource, Library library, Project project )
    {
        m_project = project;
        m_resource = resource;
        m_library = library;
        
        String[] names = resource.getPropertyNames();
        for( int i=0; i<names.length; i++ )
        {
            String name = names[i];
            String value = resource.getProperty( name );
            project.setNewProperty( name, value );
        }
        
        project.setNewProperty( "project.name", m_resource.getName() );
        project.setNewProperty( "project.version", m_resource.getVersion() );
        if( null == m_resource.getParent() )
        {
            project.setNewProperty( "project.group", "" );
        }
        else
        {
            project.setNewProperty( "project.group", m_resource.getParent().getResourcePath() );
        }
        project.setNewProperty( "project.nl", "\n" );
        project.setNewProperty( 
          "project.line", 
          "---------------------------------------------------------------------------\n" );
        project.setNewProperty( 
          "project.info", 
          "---------------------------------------------------------------------------\n"
          + resource.getResourcePath()
          + "\n---------------------------------------------------------------------------" );
        
        project.setNewProperty( "project.src.dir", getSrcDirectory().toString() );
        project.setNewProperty( "project.src.main.dir", getSrcMainDirectory().toString() );
        project.setNewProperty( "project.src.test.dir", getSrcTestDirectory().toString() );
        project.setNewProperty( "project.etc.dir", getEtcDirectory().toString() );
        
        project.setNewProperty( "project.target.dir", getTargetDirectory().toString() );
        project.setNewProperty( "project.target.build.main.dir", getTargetBuildMainDirectory().toString() );
        project.setNewProperty( "project.target.build.test.dir", getTargetBuildTestDirectory().toString() );
        project.setNewProperty( "project.target.classes.main.dir", getTargetClassesMainDirectory().toString() );
        project.setNewProperty( "project.target.classes.test.dir", getTargetClassesTestDirectory().toString() );
        project.setNewProperty( "project.target.deliverables.dir", getTargetDeliverablesDirectory().toString() );
        project.setNewProperty( "project.target.test.dir", getTargetTestDirectory().toString() );
        project.setNewProperty( "project.target.reports.dir", getTargetReportsDirectory().toString() );
    }
    
   /**
    * Initialize the contex during which runtime anbd test path objects are 
    * established as project references.
    */
    private void init()
    {
        if( m_runtime != null )
        {
            return;
        }
        final Path compileSrcPath = new Path( m_project );
        File srcMain = getTargetBuildMainDirectory();
        compileSrcPath.createPathElement().setLocation( srcMain );
        m_project.addReference( "project.build.src.path", compileSrcPath );
        m_runtime = createPath( Scope.RUNTIME );
        m_project.addReference( "project.compile.path", m_runtime );
        m_test = createPath( Scope.TEST );
        if( m_resource.isa( "jar" ) )
        {
            File deliverables = getTargetDeliverablesDirectory();
            File jars = new File( deliverables, "jars" );
            String filename = getLayoutPath( "jar" );
            File jar = new File( jars, filename );
            m_test.createPathElement().setLocation( jar );
        }
        final File testClasses = getTargetClassesTestDirectory();
        m_test.createPathElement().setLocation( testClasses );
        m_project.addReference( "project.test.path", m_test );
    }
    
   /**
    * Return the associated project.
    * @return the ant project
    */
    public Project getProject()
    {
        return m_project;
    }
    
   /**
    * Return the value of a property.
    * @param key the property key
    * @return the property value or null if undefined
    */
    public String getProperty( String key )
    {
        return getProperty( key, null );
    }
    
   /**
    * Return the value of a property. If the project contains a declaration 
    * for the property then that value will be returned, otherwise the property
    * will be resolved relative to the current resource.
    *
    * @param key the property key
    * @param value the default value
    * @return the property value or null if undefined
    */
    public String getProperty( String key, String value )
    {
        String result = m_project.getProperty( key );
        if( null != result )
        {
            return result;
        }
        else
        {
            return getResource().getProperty( key, value );
        }
    }
    
   /**
    * Return the value of a property. If the project contains a declaration 
    * for the property then that value will be returned, otherwise the property
    * will be resolved relative to the current resource.
    *
    * @param type the type production
    * @param key the property key
    * @param value the default value
    * @return the property value or null if undefined
    */
    /*
    public String getProperty( Type type, String key, String value )
    {
        if( null == type )
        {
            return getProperty( key, value );
        }
        else
        {
            String result = type.getProperty( key );
            if( null != result )
            {
                return result;
            }
            else
            {
                return getProperty( key, value );
            }
        }
    }
    */
    
   /**
    * Return an Ant path suitable for comile or runtime usage. If the supplied scope is 
    * less than Scope.RUNTIME a runtime path is returned otherwise the test path is 
    * returned.
    * @param scope the build scope
    * @return the path object
    */
    public Path getPath( Scope scope )
    {
        if( m_runtime == null )
        {
            init();
        }
        if( scope.isLessThan( Scope.TEST ) )
        {
            return m_runtime;
        }
        else
        {
            return m_test;
        }
    }
    
   /**
    * Return the active resource.
    * @return the resource definition
    */
    public Resource getResource()
    {
        return m_resource;
    }
    
   /**
    * Return the resource library.
    * @return the library
    */
    public Library getLibrary()
    {
        return m_library;
    }
    
   /**
    * Return the project source directory.
    * @return the directory
    */
    public File getSrcDirectory()
    {
        return createFile( "src" );
    }
    
   /**
    * Return the project source main directory.
    * @return the directory
    */
    public File getSrcMainDirectory()
    {
        String path = getProperty( "project.src.main", "main" );
        return new File( getSrcDirectory(), path );
    }
    
   /**
    * Return the project source test directory.
    * @return the directory
    */
    public File getSrcTestDirectory()
    {
        String path = getProperty( "project.src.test", "test" );
        return new File( getSrcDirectory(), path );
    }
    
   /**
    * Return the project source docs directory.
    * @return the directory
    */
    public File getSrcDocsDirectory()
    {
        String path = getProperty( "project.src.docs", "docs" );
        return new File( getSrcDirectory(), path );
    }
    
   /**
    * Return the project etc directory.
    * @return the directory
    */
    public File getEtcDirectory()
    {
        String path = getProperty( "project.etc", "etc" );
        return createFile( path );
    }

   /**
    * Return the project target directory.
    * @return the directory
    */
    public File getTargetDirectory()
    {
        return createFile( "target" );
    }
    
   /**
    * Return a directory within the target directory.
    * @param path the path
    * @return the directory
    */
    public File getTargetDirectory( String path )
    {
        return new File( getTargetDirectory(), path );
    }
    
   /**
    * Return the project target temp directory.
    * @return the directory
    */
    public File getTargetTempDirectory()
    {
        return new File( getTargetDirectory(), "temp" );
    }
    
   /**
    * Return the project target build directory.
    * @return the directory
    */
    public File getTargetBuildDirectory()
    {
        return new File( getTargetDirectory(), "build" );
    }
    
   /**
    * Return the project target build main directory.
    * @return the directory
    */
    public File getTargetBuildMainDirectory()
    {
        return new File( getTargetBuildDirectory(), "main" );
    }
    
   /**
    * Return the project target build test directory.
    * @return the directory
    */
    public File getTargetBuildTestDirectory()
    {
        return new File( getTargetBuildDirectory(), "test" );
    }
    
   /**
    * Return the project target build docs directory.
    * @return the directory
    */
    public File getTargetBuildDocsDirectory()
    {
        return new File( getTargetBuildDirectory(), "docs" );
    }
    
   /**
    * Return the project target root classes directory.
    * @return the directory
    */
    public File getTargetClassesDirectory()
    {
        return new File( getTargetDirectory(), "classes" );
    }
    
   /**
    * Return the project target main classes directory.
    * @return the directory
    */
    public File getTargetClassesMainDirectory()
    {
        return new File( getTargetClassesDirectory(), "main" );
    }
    
   /**
    * Return the project target test classes directory.
    * @return the directory
    */
    public File getTargetClassesTestDirectory()
    {
        return new File( getTargetClassesDirectory(), "test" );
    }
    
   /**
    * Return the project target reports directory.
    * @return the directory
    */
    public File getTargetReportsDirectory()
    {
        return new File( getTargetDirectory(), "reports" );
    }
    
   /**
    * Return the project target test reports directory.
    * @return the directory
    */
    public File getTargetReportsTestDirectory()
    {
        return new File( getTargetReportsDirectory(), "test" );
    }
    
   /**
    * Return the project target main reports directory.
    * @return the directory
    */
    public File getTargetReportsMainDirectory()
    {
        return new File( getTargetReportsDirectory(), "main" );
    }
    
   /**
    * Return the project target javadoc reports directory.
    * @return the directory
    */
    public File getTargetReportsJavadocDirectory()
    {
        return new File( getTargetReportsDirectory(), "api" );
    }
    
   /**
    * Return the project target reports docs directory.
    * @return the directory
    */
    public File getTargetDocsDirectory()
    {
        return new File( getTargetDirectory(), "docs" );
    }
    
   /**
    * Return the project target test directory.
    * @return the directory
    */
    public File getTargetTestDirectory()
    {
        return new File( getTargetDirectory(), "test" );
    }
    
   /**
    * Return the project target deliverables directory.
    * @return the directory
    */
    public File getTargetDeliverablesDirectory()
    {
        return new File( getTargetDirectory(), "deliverables" );
    }
    
   /**
    * Return the project target deliverables directory.
    * @param type the deliverable type
    * @return the directory
    */
    public File getTargetDeliverable( String type )
    {
        String path = getLayoutPath( type );
        String types = type + "s";
        File root = new File( getTargetDeliverablesDirectory(), types );
        return new File( root, path );
    }
    
   /**
    * Create a file relative to the resource basedir.
    * @param path the relative path
    * @return the directory
    */
    public File createFile( String path )
    {
        File basedir = m_resource.getBaseDir();
        return new File( basedir, path );
    }
    
   /**
    * Return a filename using the layout strategy employed by the cache.
    * @param id the artifact type
    * @return the filename
    */
    public String getLayoutPath( String id )
    {
        Artifact artifact = m_resource.getArtifact( id );
        return Transit.getInstance().getCacheLayout().resolveFilename( artifact );
    }
    
   /**
    * Utility operation to construct a new classpath path instance.
    * @param scope the build scope
    * @return the path
    */
    public Path createPath( Scope scope )
    {
        try
        {
            Resource[] resources = m_resource.getClasspathProviders( scope );
            return createPath( resources, true, true );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while constructing path instance for the scope: " + scope;
            throw new RuntimeException( error, e );
        }
    }
    
   /**
    * Utility operation to construct a new path using a supplied array of resources.
    * @param resources the resource to use in path construction
    * @return the path
    */
    public Path createPath( Resource[] resources )
    {
        return createPath( resources, true, false );
    }
    
   /**
    * Utility operation to construct a new path using a supplied array of resources.
    * @param resources the resources to use in path construction
    * @param resolve if true force local caching of the artifact 
    * @param filter if true restrict path entries to resources that produce jars
    * @return the path
    */
    public Path createPath( Resource[] resources, boolean resolve, boolean filter )
    {
        final Path path = new Path( m_project );
        File cache = (File) m_project.getReference( "dpml.cache" );
        for( int i=0; i<resources.length; i++ )
        {
            Resource resource = resources[i];
            if( !resource.equals( getResource() ) )
            {
                if( filter && resource.isa( "jar" ) )
                {
                    Artifact artifact = resource.getArtifact( "jar" );
                    addToPath( cache, path, artifact, resolve );
                }
                else
                {
                    Type[] types = resource.getTypes();
                    for( int j=0; j<types.length; j++ )
                    {
                        Artifact artifact = resource.getArtifact( types[j].getID() );
                        addToPath( cache, path, artifact, resolve );
                    }
                }
            }
        }
        return path;
    }

    private void addToPath( File cache, Path path, Artifact artifact, boolean resolve )
    {
        String location = Transit.getInstance().getCacheLayout().resolvePath( artifact );
        File file = new File( cache, location );
        path.createPathElement().setLocation( file );
        if( resolve )
        {
            resolveArtifact( artifact );
        }
    }
    
    private void resolveArtifact( Artifact artifact )
    {
        try
        {
            URL url = artifact.toURL();
            url.openStream();
        }
        catch( IOException e )
        {
            final String error = 
              "Unable to resolve artifact [" 
              + artifact
              + "].";
            throw new BuildException( error, e );
        }
    }
}


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
import java.net.URI;
import java.util.Date;

import net.dpml.lang.Plugin;
import net.dpml.lang.Part;

import net.dpml.library.info.Scope;
import net.dpml.library.Library;
import net.dpml.library.Resource;
import net.dpml.library.Type;
import net.dpml.library.Filter;
import net.dpml.library.impl.DefaultLibrary;
import net.dpml.library.ResourceNotFoundException;

import net.dpml.tools.info.ListenerDirective;

import net.dpml.tools.Context;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;
import net.dpml.transit.link.LinkManager;

import net.dpml.util.Logger;
import net.dpml.util.DefaultLogger;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.types.Path;

/**
 * Default implmentation of a project context.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class DefaultContext implements Context
{
    private final Project m_project;
    private final Resource m_resource;
    
    private Path m_runtime;
    private Path m_test;
    
   /**
    * Creation of a new project build context.
    * @param project the unconfigured Ant project
    * @exception Exception if an error occurs during context extablishment
    */
    public DefaultContext( Project project ) throws Exception
    {
        this( 
          newResource( project.getBaseDir() ), 
          project );
    }
    
   /**
    * Creation of a new project build context.
    * @param resource the resource definition
    * @param project the Ant project
    * @exception Exception if an error occurs during context extablishment
    */
    public DefaultContext( Resource resource, Project project ) throws Exception
    {
        m_project = project;
        m_resource = resource;
        
        Library library = resource.getLibrary();
        project.addReference( "project.timestamp", new Date() );
        project.setBaseDir( resource.getBaseDir() );
        project.addReference( "project.context", this );
        
        String[] names = resource.getPropertyNames();
        for( int i=0; i<names.length; i++ )
        {
            String name = names[i];
            String value = resource.getProperty( name );
            project.setProperty( name, value );
        }
        project.setProperty( "project.name", resource.getName() );
        project.setProperty( "project.version", resource.getVersion() );
        project.setProperty( "project.resource.path", resource.getResourcePath() );
        project.setProperty( "project.basedir", resource.getBaseDir().toString() );
        
        Filter[] filters = resource.getFilters();
        for( int i=0; i<filters.length; i++ )
        {
            Filter filter = filters[i];
            String token = filter.getToken();
            try
            {
                String value = filter.getValue( resource );
                project.getGlobalFilterSet().addFilter( token, value );
            }
            catch( Exception e )
            {
                final String error =
                  "Error while attempting to setup the filter [" + token + "].";
                throw new BuildException( error, e );
            }
        }
        
        project.setNewProperty( "project.nl", "\n" );
        project.setNewProperty( 
          "project.line", 
          "---------------------------------------------------------------------------\n" );
        project.setNewProperty( 
          "project.info", 
          "---------------------------------------------------------------------------\n"
          + resource.getResourcePath()
          + "#"
          + resource.getVersion()
          + "\n---------------------------------------------------------------------------" );
        
        project.setNewProperty( "project.src.dir", getSrcDirectory().toString() );
        project.setNewProperty( "project.src.main.dir", getSrcMainDirectory().toString() );
        project.setNewProperty( "project.src.test.dir", getSrcTestDirectory().toString() );
        project.setNewProperty( "project.etc.dir", getEtcDirectory().toString() );
        project.setNewProperty( "project.etc.main.dir", getEtcMainDirectory().toString() );
        project.setNewProperty( "project.etc.test.dir", getEtcTestDirectory().toString() );
        project.setNewProperty( "project.etc.data.dir", getEtcDataDirectory().toString() );
        
        project.setNewProperty( "project.target.dir", getTargetDirectory().toString() );
        project.setNewProperty( "project.target.build.main.dir", getTargetBuildMainDirectory().toString() );
        project.setNewProperty( "project.target.build.test.dir", getTargetBuildTestDirectory().toString() );
        project.setNewProperty( "project.target.classes.main.dir", getTargetClassesMainDirectory().toString() );
        project.setNewProperty( "project.target.classes.test.dir", getTargetClassesTestDirectory().toString() );
        project.setNewProperty( "project.target.deliverables.dir", getTargetDeliverablesDirectory().toString() );
        project.setNewProperty( "project.target.test.dir", getTargetTestDirectory().toString() );
        project.setNewProperty( "project.target.reports.dir", getTargetReportsDirectory().toString() );
        
        // add listeners declared in the builder configuration
        
        ListenerDirective[] listeners = StandardBuilder.CONFIGURATION.getListenerDirectives();
        for( int i=0; i<listeners.length; i++ )
        {
            ListenerDirective directive = listeners[i];
            project.log( "adding listener: " + directive.getName(), Project.MSG_VERBOSE );
            BuildListener buildListener = loadBuildListener( directive );
            project.addBuildListener( buildListener );
            BuildEvent event = new BuildEvent( project );
            buildListener.buildStarted( event );
        }
    }
    
   /**
    * Initialize the context during which runtime and test path objects are 
    * established as project references.
    */
    public void init()
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
        return m_resource.getLibrary();
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
        String path = getProperty( "project.src.main", "src/main" );
        return createFile( path );
    }
    
   /**
    * Return the project source test directory.
    * @return the directory
    */
    public File getSrcTestDirectory()
    {
        String path = getProperty( "project.src.test", "src/test" );
        return createFile( path );
    }
    
   /**
    * Return the project source docs directory.
    * @return the directory
    */
    public File getSrcDocsDirectory()
    {
        String path = getProperty( "project.src.docs", "src/docs" );
        return createFile( path );
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
    * Return the project etc/main directory.
    * @return the directory
    */
    public File getEtcMainDirectory()
    {
        String path = getProperty( "project.etc.main", "etc/main" );
        return createFile( path );
    }

   /**
    * Return the project etc/test directory.
    * @return the directory
    */
    public File getEtcTestDirectory()
    {
        String path = getProperty( "project.etc.test", "etc/test" );
        return createFile( path );
    }

   /**
    * Return the project etc/resources directory.
    * @return the directory
    */
    public File getEtcDataDirectory()
    {
        String path = getProperty( "project.etc.data", "etc/data" );
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
        Artifact target = getTargetArtifact( artifact );
        String location = Transit.getInstance().getCacheLayout().resolvePath( target );
        File file = new File( cache, location );
        path.createPathElement().setLocation( file );
        
        if( resolve )
        {
            resolveArtifact( artifact );
        }
    }
    
    private Artifact getTargetArtifact( Artifact artifact )
    {
        String scheme = artifact.getScheme();
        if( !Artifact.LINK.equals( scheme ) )
        {
            return artifact;
        }
        else
        {
            try
            {
                LinkManager manager = Transit.getInstance().getLinkManager();
                URI uri = manager.getTargetURI( artifact.toURI() );
                return Artifact.createArtifact( uri );
            }
            catch( IOException e )
            {
                final String error = 
                  "Unable to resolve link artifact [" 
                  + artifact
                  + "].";
                throw new BuildException( error, e );
            }
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

    private static Resource newResource( File basedir ) throws Exception
    {
        Logger logger = new DefaultLogger();
        DefaultLibrary library = new DefaultLibrary( logger );
        return library.locate( basedir.getCanonicalFile() );
    }

    private static String flatternDependencies( String[] deps )
    {
        if( deps.length == 0 )
        {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        for( int i=0; i<deps.length; i++ )
        {
            if( i>0 )
            {
                buffer.append( "," );
            }
            String dep = deps[i];
            buffer.append( dep );
        }
        return buffer.toString();
    }
    
    private BuildListener loadBuildListener( ListenerDirective listener )
    {
        String name = listener.getName();
        URI uri = listener.getURI();
        try
        {
            String classname = listener.getClassname();
            Object object = loadInstance( name, uri, classname );
            return (BuildListener) object;
        }
        catch( ClassCastException e )
        {
            final String error = 
              "Build listener [" 
              + name
              + "] from uri ["
              + uri
              + "] does not implement "
              + BuildListener.class.getName();
            throw new BuilderError( error );
        }
    }
        
    private Object loadInstance( String name, URI uri, String classname )
    {
        if( null == uri )
        {
            try
            {
                ClassLoader classloader = getClass().getClassLoader();
                Class clazz = classloader.loadClass( classname );
                Object[] args = new Object[]{this};
                return Plugin.instantiate( clazz, args );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Internal error while attempting to load a local plugin."
                  + "\nClass: " + classname
                  + "\nName: " + name;
                throw new BuilderError( error, e );
            }
        }
        else
        {
            ClassLoader context = Thread.currentThread().getContextClassLoader();
            try
            {
                ClassLoader classloader = getClass().getClassLoader();
                Thread.currentThread().setContextClassLoader( classloader );
                Object[] params = new Object[]{this};
                Part part = Part.load( uri );
                if( null == classname )
                {
                    return part.instantiate( params );
                }
                else
                {
                    ClassLoader loader = part.getClassLoader();
                    Class c = loader.loadClass( classname );
                    return Plugin.instantiate( c, params );
                }
            }
            catch( Throwable e )
            {
                final String error = 
                  "Internal error while attempting to load plugin."
                  + "\nURI: " + uri
                  + "\nName: " + name;
                throw new BuilderError( error, e );
            }
            finally
            {
                Thread.currentThread().setContextClassLoader( context );
            }
        }
    }
}


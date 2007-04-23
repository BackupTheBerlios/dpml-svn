/*
 * Copyright 2005-2006 Stephen J. McConnell.
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

package dpml.tools.impl;

import dpml.build.Builder;
import dpml.build.BuildError;

import dpml.tools.BuilderError;

import dpml.library.impl.DefaultLibrary;

import dpml.tools.Context;
import dpml.tools.info.ListenerDirective;

import dpml.util.DefaultLogger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;

import net.dpml.lang.Strategy;
import net.dpml.lang.ServiceRegistry;
import net.dpml.lang.SimpleServiceRegistry;

import dpml.library.Scope;
import dpml.library.Library;
import dpml.library.Resource;
import dpml.library.Type;
import dpml.library.Filter;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;
import net.dpml.transit.Layout;
import net.dpml.transit.layout.ClassicLayout;

import net.dpml.util.Logger;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.types.Path;

/**
 * Default implementation of a project context.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class DefaultContext implements Context
{
    private static final Layout CLASSIC_LAYOUT = new ClassicLayout();
    
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
        
        File basedir = resource.getBaseDir();
        if( !basedir.equals( project.getBaseDir() ) )
        {
            project.setBaseDir( resource.getBaseDir() );
        }
        
        project.addReference( "project.context", this );
        
        String[] names = resource.getPropertyNames();
        for( int i=0; i<names.length; i++ )
        {
            String name = names[i];
            String value = resource.getProperty( name );
            setProperty( name, value );
        }
        
        setProperty( "project.name", resource.getName() );
        setProperty( "project.version", resource.getVersion() );
        setProperty( "project.resource.path", resource.getResourcePath() );
        setProperty( "project.basedir", resource.getBaseDir().toString() );
        
        File cache = Transit.getInstance().getCacheDirectory();
        String cachePath = cache.getCanonicalPath();
        setProperty( "project.cache", cachePath );
        
        Filter[] filters = resource.getFilters();
        for( int i=0; i<filters.length; i++ )
        {
            Filter filter = filters[i];
            String token = filter.getToken();
            try
            {
                String value = filter.getValue( resource );
                String resolved = resource.resolve( value );
                project.getGlobalFilterSet().addFilter( token, resolved );
            }
            catch( Exception e )
            {
                final String error =
                  "Error while attempting to setup the filter [" + token + "].";
                throw new BuilderError( error, e );
            }
        }
        
        setProperty( "project.nl", "\n" );
        setProperty( 
          "project.line", 
          "---------------------------------------------------------------------------\n", false );
        setProperty( 
          "project.info", 
          "---------------------------------------------------------------------------\n"
          + resource.getResourcePath()
          + "#"
          + resource.getVersion()
          + "\n---------------------------------------------------------------------------", false );
        
        setProperty( "project.src.dir", getSrcDirectory().toString() );
        setProperty( "project.src.main.dir", getSrcMainDirectory().toString() );
        setProperty( "project.src.test.dir", getSrcTestDirectory().toString() );
        setProperty( "project.etc.dir", getEtcDirectory().toString() );
        setProperty( "project.etc.main.dir", getEtcMainDirectory().toString() );
        setProperty( "project.etc.test.dir", getEtcTestDirectory().toString() );
        setProperty( "project.etc.data.dir", getEtcDataDirectory().toString() );
        
        setProperty( "project.target.dir", getTargetDirectory().toString() );
        setProperty( "project.target.build.main.dir", getTargetBuildMainDirectory().toString() );
        setProperty( "project.target.build.test.dir", getTargetBuildTestDirectory().toString() );
        setProperty( "project.target.classes.main.dir", getTargetClassesMainDirectory().toString() );
        setProperty( "project.target.classes.test.dir", getTargetClassesTestDirectory().toString() );
        setProperty( "project.target.deliverables.dir", getTargetDeliverablesDirectory().toString() );
        setProperty( "project.target.test.dir", getTargetTestDirectory().toString() );
        setProperty( "project.target.reports.dir", getTargetReportsDirectory().toString() );
        
        // add properties dealing with produced types
        
        addProductionProperties();
        
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
    
    private void addProductionProperties() throws IOException
    {
        Resource resource = getResource();
        Type[] types = resource.getTypes();
        for( int i=0; i<types.length; i++ )
        {
            Type type = types[i];
            String id = type.getID();
            String name = type.getCompoundName();
            File file = type.getFile( true );
            String path = file.getCanonicalPath();
            setProperty( "project.deliverable." + name + ".path", path );
            File dir = file.getParentFile();
            String spec = dir.getCanonicalPath();
            setProperty( "project.deliverable." + name + ".dir", spec );
            String base = getLayoutBase( type );
            setProperty( "project.cache." + name + ".dir", base );
            String address = getLayoutPath( type );
            setProperty( "project.cache." + name + ".path", address );
        }
    }

    private void setProperty( String key, String value )
    {
        setProperty( key, value, true );
    }
    
    private void setProperty( String key, String value, boolean verbose )
    {
        Project project = getProject();
        String v = project.getProperty( key );
        if( null == v )
        {
            if( verbose )
            {
                project.log( "setting property [" + key + "] to [" + value + "]", Project.MSG_VERBOSE );
            }
            project.setProperty( key, value ); 
        }
        else if( !value.equals( v ) )
        {
            if( verbose )
            {
                project.log( "updating property [" + key + "] to [" + value + "]", Project.MSG_VERBOSE );
            }
            project.setProperty( key, value );
        }
    }
    
   /**
    * Initialize the context during which runtime and test path objects are 
    * established as project references.
    * @exception IOException if an IO error occurs
    */
    public void init() throws IOException
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
            Type type = m_resource.getType( "jar" );
            File jar = type.getFile( true );
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
            try
            {
                init();
            }
            catch( IOException e )
            {
                final String error = 
                  "Unable to resolve path for the scope [" 
                  + scope
                  + "] in the resource ["
                  + m_resource.getResourcePath()
                  + "] (" 
                  + m_resource.getBaseDir()
                  + ").";
                throw new BuildError( error, e );
            }
        }
        if( scope.compareTo( Scope.TEST ) < 0 )
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
    * @param type the artifact type
    * @return the filename
    */
    public String getLayoutFilename( Type type )
    {
        return type.getLayoutPath();
    }

   /**
    * Return the directory path representing the module structure and type
    * using the layout strategy employed by the cache.
    * @param type the artifact type
    * @return the path from the root of the cache to the directory containing the artifact
    */
    public String getLayoutBase( Type type ) // TODO: cleanup and move to resource
    {
        if( null == type )
        {
            throw new NullPointerException( "type" );
        }
        Artifact artifact = type.getArtifact();
        return Transit.getInstance().getCacheLayout().resolveBase( artifact );
    }
    
   /**
    * Return the full path to an artifact using the layout employed by the cache.
    * @param type the artifact type
    * @return the full path including base path and filename
    */
    public String getLayoutPath( Type type ) // TODO: cleanup and move to resource
    {
        if( null == type )
        {
            throw new NullPointerException( "type" );
        }
        
        Artifact artifact = type.getArtifact();
        return Transit.getInstance().getCacheLayout().resolvePath( artifact );
    }
    
   /**
    * Utility operation to construct a new classpath path instance.
    * @param scope the build scope
    * @return the path
    * @exception IOException if an IO error occurs
    */
    public Path createPath( Scope scope ) throws IOException
    {
        //try
        //{
            Resource[] resources = m_resource.getClasspathProviders( scope );
            return createPath( resources, true, true );
        //}
        //catch( Exception e )
        //{
        //    final String error = 
        //      "Unexpected error while constructing path instance for the scope: " + scope;
        //    throw new BuilderError( error, e );
        //}
    }
    
   /**
    * Utility operation to construct a new path using a supplied array of resources.
    * @param resources the resource to use in path construction
    * @return the path
    * @exception IOException if an IO error occurs
    */
    public Path createPath( Resource[] resources ) throws IOException
    {
        return createPath( resources, true, false );
    }
    
   /**
    * Utility operation to construct a new path using a supplied array of resources.
    * @param resources the resources to use in path construction
    * @param resolve if true force local caching of the artifact 
    * @param filter if true restrict path entries to resources that produce jars
    * @return the path
    * @exception IOException if an IO error occurs
    */
    public Path createPath( Resource[] resources, boolean resolve, boolean filter ) throws IOException
    {
        final Path path = new Path( m_project );
        File cache = (File) m_project.getReference( "dpml.cache" );
        for( int i=0; i<resources.length; i++ )
        {
            Resource resource = resources[i];
            if( !resource.equals( getResource() ) )
            {
                if( filter )
                {
                    if( resource.isa( "jar" ) )
                    {
                        Type type = resource.getType( "jar" );
                        if( !type.getTest() )
                        {
                            if( resolve )
                            {
                                Artifact artifact = type.getResolvedArtifact();
                            }
                            File file = type.getFile();
                            path.createPathElement().setLocation( file );
                        }
                    }
                }
                else
                {
                    Type[] types = resource.getTypes();
                    for( int j=0; j<types.length; j++ )
                    {
                        Type type = types[j];
                        if( !type.getTest() )
                        {
                            File file = type.getFile();
                            path.createPathElement().setLocation( file );
                        }
                    }
                }
            }
        }
        return path;
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
        String classname = listener.getClassname();
        return loadBuildListener( name, uri, classname );
    }
    
    private BuildListener loadBuildListener( String name, URI uri, String classname )
    {
        ServiceRegistry registry = new SimpleServiceRegistry( this );
        if( null == uri )
        {
            try
            {
                ClassLoader classloader = getClass().getClassLoader();
                Class<?> clazz = classloader.loadClass( classname );
                Strategy strategy = Strategy.load( clazz, registry, name );
                return strategy.getInstance( BuildListener.class );
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
                ClassLoader classloader = Builder.class.getClassLoader();
                Thread.currentThread().setContextClassLoader( classloader );
                Strategy strategy = Strategy.load( classloader, registry, uri, name );
                if( null == classname )
                {
                    return strategy.getInstance( BuildListener.class );
                }
                else
                {
                    ClassLoader loader = strategy.getClassLoader();
                    Class<?> c = loader.loadClass( classname );
                    return Strategy.load( c, registry, (String) null ).getInstance( BuildListener.class );
                }
            }
            catch( Throwable e )
            {
                final String error = 
                  "Internal error while attempting to load build listener."
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


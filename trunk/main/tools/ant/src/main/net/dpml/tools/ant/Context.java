/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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

package net.dpml.tools.ant;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.beans.Expression;
import java.beans.BeanDescriptor;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.beans.Encoder;

import net.dpml.tools.info.Scope;
import net.dpml.tools.model.Library;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Builder;
import net.dpml.tools.model.Type;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;

/**
 * Project context.
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: InfoDescriptor.java 2522 2005-05-12 11:23:50Z mcconnell@dpml.net $
 */
public final class Context
{
    private final Project m_project;
    private final Resource m_resource;
    private final Library m_library;
    private final Path m_runtime;
    private final Path m_test;
        
    public Context( Resource resource, Library library, Project project )
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
        
        final Path compileSrcPath = new Path( project );
        File srcMain = getTargetBuildMainDirectory();
        compileSrcPath.createPathElement().setLocation( srcMain );
        project.addReference( "project.build.src.path", compileSrcPath );
        
        m_runtime = createPath( Scope.RUNTIME );
        project.addReference( "project.compile.path", m_runtime );
        m_test = createPath( Scope.TEST );
        if( resource.isa( "jar" ) )
        {
            File deliverables = getTargetDeliverablesDirectory();
            File jars = new File( deliverables, "jars" );
            String filename = getLayoutPath( "jar" );
            File jar = new File( jars, filename );
            m_test.createPathElement().setLocation( jar );
        }
        final File testClasses = getTargetClassesTestDirectory();
        m_test.createPathElement().setLocation( testClasses );
        
        project.addReference( "project.test.path", m_test );
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
    }
    
    public Path getPath( Scope scope )
    {
        if( scope.isLessThan( Scope.TEST ) )
        {
            return m_runtime;
        }
        else
        {
            return m_test;
        }
    }
        
    public Resource getResource()
    {
        return m_resource;
    }
    
    public Library getLibrary()
    {
        return m_library;
    }
    
    public File getSrcDirectory()
    {
        return createFile( "src" );
    }
    
    public File getSrcMainDirectory()
    {
        return new File( getSrcDirectory(), "main" );
    }
    
    public File getSrcTestDirectory()
    {
        return new File( getSrcDirectory(), "test" );
    }
    
    public File getEtcDirectory()
    {
        return createFile( "etc" );
    }

    public File getTargetDirectory()
    {
        return createFile( "target" );
    }
    
    public File getTargetDirectory( String path )
    {
        return new File( getTargetDirectory(), path );
    }
    
    public File getTargetBuildDirectory()
    {
        return new File( getTargetDirectory(), "build" );
    }
    
    public File getTargetBuildMainDirectory()
    {
        return new File( getTargetBuildDirectory(), "main" );
    }
    
    public File getTargetBuildTestDirectory()
    {
        return new File( getTargetBuildDirectory(), "test" );
    }
    
    public File getTargetClassesDirectory()
    {
        return new File( getTargetDirectory(), "classes" );
    }
    
    public File getTargetClassesMainDirectory()
    {
        return new File( getTargetClassesDirectory(), "main" );
    }
    
    public File getTargetClassesTestDirectory()
    {
        return new File( getTargetClassesDirectory(), "test" );
    }
    
    public File getTargetReportsDirectory()
    {
        return new File( getTargetDirectory(), "reports" );
    }
    
    public File getTargetReportsTestDirectory()
    {
        return new File( getTargetReportsDirectory(), "tests" );
    }
    
    public File getTargetReportsMainDirectory()
    {
        return new File( getTargetReportsDirectory(), "main" );
    }
    
    public File getTargetTestDirectory()
    {
        return new File( getTargetDirectory(), "test" );
    }
    
    public File getTargetDeliverablesDirectory()
    {
        return new File( getTargetDirectory(), "deliverables" );
    }
    
    public File createFile( String path )
    {
        File basedir = m_resource.getBaseDir();
        return new File( basedir, path );
    }
    
    public String getLayoutPath( String id )
    {
        Artifact artifact = m_resource.getArtifact( id );
        return Transit.getInstance().getCacheLayout().resolveFilename( artifact );
    }
    
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
    
    public Path createPath( Resource[] resources )
    {
        return createPath( resources, true, false );
    }
    
    public Path createPath( Resource[] resources, boolean resolve, boolean filter )
    {
        final Path path = new Path( m_project );
        File cache = (File) m_project.getReference( "dpml.cache" );
        for( int i=0; i<resources.length; i++ )
        {
            Resource resource = resources[i];
            if( filter )
            {
                Artifact artifact = resource.getArtifact( "jar" );
                addToPath( cache, path, artifact, resolve );
            }
            else
            {
                Type[] types = resource.getTypes();
                for( int j=0; j<types.length; j++ )
                {
                    Artifact artifact = resource.getArtifact( types[j].getName() );
                    addToPath( cache, path, artifact, resolve );
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


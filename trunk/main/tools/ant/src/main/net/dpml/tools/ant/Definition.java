/*
 * Copyright 2005 Stephen J. McConnell
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
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Properties;

import net.dpml.tools.info.Scope;
import net.dpml.tools.info.TypeDescriptor;
import net.dpml.tools.info.DependencyDirective;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.TypeNotFoundException;

import net.dpml.transit.Category;
import net.dpml.transit.Transit;
import net.dpml.transit.Layout;
import net.dpml.transit.Artifact;
import net.dpml.transit.Logger;
import net.dpml.transit.model.TransitModel;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

/**
 * Project definition that adapts the DPML tools Project model to a model
 * suitable for use within an Ant based build environment.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class Definition
{
    private final net.dpml.tools.model.Project m_project;
    
    public Definition( net.dpml.tools.model.Project project )
    {
        m_project = project;
    }
    
    public long getLastModified()
    {
        try
        {
            return m_project.getLastModified();
        }
        catch( RemoteException e )
        {
            throw new RuntimeException( "remote-exception", e );
        }
    }
    
    public TypeDescriptor getTypeDescriptor( String type ) throws TypeNotFoundException
    {
        try
        {
            return m_project.getTypeDescriptor( type );
        }
        catch( RemoteException e )
        {
            throw new RuntimeException( "remote-exception", e );
        }
    }
    
    public Path getPath( Project project, Scope scope )
    {
        final Path path = new Path( project );
        try
        {
            Resource[] resources = m_project.getClassPath( scope );
            for( int i=0; i<resources.length; i++ )
            {
                Resource resource = resources[i];
                String group = resource.getModule().getPath();
                String name = resource.getName();
                String version = getResourceVersion( resource );
                Artifact artifact = Artifact.createArtifact( group, name, version, "jar" );
                String location = Transit.getInstance().getCacheLayout().resolvePath( artifact );
                File cache = (File) project.getReference( "dpml.cache" );
                File file = new File( cache, location );
                path.createPathElement().setLocation( file );
            }
            return path;
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while constructing path instance for the scope: " + scope;
            throw new RuntimeException( error, e );
        }
    }
    
    private String getResourceVersion( Resource resource )
    {
        try
        {
            if( null != resource.getProject() )
            {
                return getVersion();
            }
            else
            {
                return resource.getVersion();
            }
        }
        catch( RemoteException e )
        {
            throw new RuntimeException( "remote-exception", e );
        }
    }
    
    public String getLayoutPath( String type )
    {
        Artifact artifact = getArtifact( type );
        return Transit.getInstance().getCacheLayout().resolveFilename( artifact );
    }
    
    public Artifact getArtifact( String type )
    {
        try
        {
            String group = m_project.getModule().getPath();
            String name = m_project.getName();
            String version = getVersion();
            return Artifact.createArtifact( group, name, version, type );
        }
        catch( RemoteException e )
        {
            throw new RuntimeException( "remote-exeption", e );
        }
    }
    
    public String getVersion()
    {
        return "SNAPSHOT"; // TODO - add runtime version asignment
    }
    
    public String[] getPropertyNames()
    {
        try
        {
            return m_project.getPropertyNames();
        }
        catch( RemoteException e )
        {
            throw new RuntimeException( "remote-exeption", e );
        }
    }
    
    public String getProperty( String key )
    {
        return getProperty( key, null );
    }
    
    public String getProperty( String key, String value )
    {
        try
        {
            return m_project.getProperty( key, value );
        }
        catch( RemoteException e )
        {
            throw new RuntimeException( "remote-exeption", e );
        }
    }
    
    public String getProductionProperty( String type, String key, String value )
    {
        try
        {
            return m_project.getProductionProperty( type, key, value );
        }
        catch( RemoteException e )
        {
            throw new RuntimeException( "remote-exeption", e );
        }
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
        return new File( getBase(), path );
    }
    
    public String getName()
    {
        try
        {
            return m_project.getName();
        }
        catch( RemoteException e )
        {
            throw new RuntimeException( "remote-exeption", e );
        }
    }
    
    public String getGroup()
    {
        try
        {
            return m_project.getModule().getPath();
        }
        catch( RemoteException e )
        {
            throw new RuntimeException( "remote-exeption", e );
        }
    }
    
    public File getBase()
    {
        try
        {
            return m_project.getBase();
        }
        catch( RemoteException e )
        {
            throw new RuntimeException( "remote-exeption", e );
        }
    }
    
    public String[] getTypes()
    {
        try
        {
            return m_project.getTypes();
        }
        catch( RemoteException e )
        {
            throw new RuntimeException( "remote-exeption", e );
        }
    }
    
    public String getProjectPath()
    {
        try
        {
            return m_project.getPath();
        }
        catch( RemoteException e )
        {
            throw new RuntimeException( "remote-exeption", e );
        }
    }
    
    public Resource[] getClassPath( Category category ) throws Exception
    {
        try
        {
            return m_project.getClassPath( category );
        }
        catch( RemoteException e )
        {
            throw new RuntimeException( "remote-exeption", e );
        }
    }
    public Resource toResource()
    {
        try
        {
            return m_project.toResource();
        }
        catch( RemoteException e )
        {
            throw new RuntimeException( "remote-exeption", e );
        }
    }
}

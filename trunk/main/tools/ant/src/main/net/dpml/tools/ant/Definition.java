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

import net.dpml.tools.ant.process.JarTask;
import net.dpml.tools.ant.process.PluginTask;
import net.dpml.tools.ant.Process;
import net.dpml.tools.info.Scope;
import net.dpml.tools.info.TypeDescriptor;
import net.dpml.tools.info.DependencyDirective;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.TypeNotFoundException;

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
    private final net.dpml.tools.model.Project m_model;
    private final TransitModel m_transit;
    
    public Definition( TransitModel transit, net.dpml.tools.model.Project model )
    {
        m_model = model;
        m_transit = transit;
    }
    
    public long getLastModified()
    {
        try
        {
            return m_model.getLastModified();
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
            return m_model.getTypeDescriptor( type );
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
            Resource[] resources = m_model.getClassPath( scope );
            for( int i=0; i<resources.length; i++ )
            {
                Resource resource = resources[i];
                String group = resource.getModule().getPath();
                String name = resource.getName();
                String version = getResourceVersion( resource );
                Artifact artifact = Artifact.createArtifact( group, name, version, "jar" );
                String location = Transit.getInstance().getCacheLayout().resolvePath( artifact );
                File file = new File( getCacheDirectory(), location );
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
    
    private File getCacheDirectory() throws Exception
    {
        return m_transit.getCacheModel().getCacheDirectory();
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
            String group = m_model.getModule().getPath();
            String name = m_model.getName();
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
        return "SNAPSHOT";
    }
    
    public String[] getPropertyNames()
    {
        try
        {
            return m_model.getPropertyNames();
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
            return m_model.getProperty( key, value );
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
            return m_model.getProductionProperty( type, key, value );
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
    
    public File getTargetClassesDirectory()
    {
        return new File( getTargetDirectory(), "classes" );
    }
    
    public File getTargetTestClassesDirectory()
    {
        return new File( getTargetDirectory(), "test-classes" );
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
            return m_model.getName();
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
            return m_model.getModule().getPath();
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
            return m_model.getBase();
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
            return m_model.getTypes();
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
            return m_model.getPath();
        }
        catch( RemoteException e )
        {
            throw new RuntimeException( "remote-exeption", e );
        }
    }
        
    // TODO replace this with something constructed from process definitions
    public Process[] getPluginTargets( org.apache.tools.ant.Project project )
    {
            ArrayList list = new ArrayList();
            String[] types = getTypes();
            for( int i=0; i<types.length; i++ )
            {
                String type = types[i];
                if( "jar".equals( type ) )
                {
                    list.add( createJarTarget( project ) );
                }
                else if( "plugin".equals( type ) )
                {
                    list.add( createPluginTarget( project ) );
                }
            }
            return (Process[]) list.toArray( new Process[0] );
    }

    private static Process createJarTarget( org.apache.tools.ant.Project ant )
    {
        JarTask task = new JarTask();
        task.setProject( ant );
        task.setTaskName( "jar" );
        return task;
    }
    
    private static Process createPluginTarget( org.apache.tools.ant.Project ant )
    {
        PluginTask task = new PluginTask();
        task.setProject( ant );
        task.setTaskName( "plugin" );
        return task;
    }
    
}

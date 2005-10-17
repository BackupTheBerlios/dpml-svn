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

import net.dpml.tools.model.Project;

import net.dpml.tools.ant.process.JarTask;
import net.dpml.tools.ant.process.PluginTask;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.Target;

import net.dpml.transit.Transit;
import net.dpml.transit.Layout;
import net.dpml.transit.Artifact;
import net.dpml.transit.Logger;


/**
 * Project definition that adapts the DPML tools Project model to a model
 * suitable for use within an Ant based build environment.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class Definition
{
    Project m_project;
    Target[] m_targets;
    Layout m_layout;
    
    public Definition( Project project )
    {
        m_project = project;
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
        return "SNAPSHOT";
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
    
    public File createFile( String path )
    {
        return new File( getBase(), path );
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
    
    public String getPath()
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
    
    // TODO replace this with something constructed from process definitions
    public Target[] getPluginTargets( Phase phase, org.apache.tools.ant.Project project )
    {
        if( Phase.PACKAGE.equals( phase ) )
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
            return (Target[]) list.toArray( new Target[0] );
        }
        else
        {
            return new Target[0];
        }
    }

    private static Target createJarTarget( org.apache.tools.ant.Project ant )
    {
        Target target = new Target();
        JarTask task = new JarTask();
        task.setProject( ant );
        task.setOwningTarget( target );
        task.setTaskName( "jar" );
        target.addTask( task );
        target.setName( "jar" );
        target.setProject( ant );
        return target;
    }
    
    private static Target createPluginTarget( org.apache.tools.ant.Project ant )
    {
        Target target = new Target();
        PluginTask task = new PluginTask();
        task.setProject( ant );
        task.setOwningTarget( target );
        task.setTaskName( "plugin" );
        target.addTask( task );
        target.setName( "plugin" );
        target.setProject( ant );
        target.addDependency( "jar" );
        return target;
    }
    
}

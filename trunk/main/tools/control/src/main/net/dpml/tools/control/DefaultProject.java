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

package net.dpml.tools.control;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import net.dpml.tools.info.ArtifactDirective;
import net.dpml.tools.info.DependencyDirective;
import net.dpml.tools.info.ProjectDirective;
import net.dpml.tools.info.ResourceDirective;
import net.dpml.tools.info.TypeDirective;
import net.dpml.tools.info.IncludeDirective;
import net.dpml.tools.info.Scope;
import net.dpml.tools.model.Project;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Module;
import net.dpml.tools.model.ModuleNotFoundException;
import net.dpml.tools.model.ResourceNotFoundException;
import net.dpml.tools.model.ReferentialException;
import net.dpml.tools.model.DuplicateNameException;
import net.dpml.tools.model.Library;
import net.dpml.tools.model.ModelRuntimeException;

import net.dpml.transit.util.ElementHelper;

import org.w3c.dom.Element;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class DefaultProject extends UnicastRemoteObject implements Project
{
    private final DefaultLibrary m_library;
    private final ProjectDirective m_directive;
    private final DefaultModule m_parent;
    private final String[] m_types;
    private final String m_path;
    private final DefaultResource m_resource;
    private final File m_base;
    
    DefaultProject( DefaultLibrary library, DefaultModule parent, ProjectDirective directive ) throws RemoteException
    {
        super();
        
        m_directive = directive;
        m_parent = parent;
        m_library = library;
        
        if( null == m_parent )
        {
            m_path = m_directive.getName();
            String base = directive.getBasedir();
            if( null == base )
            {
                m_base = library.getRootDirectory();
            }
            else
            {
                m_base = new File( library.getRootDirectory(), base );
            }
        }
        else
        {
            String path = m_parent.getPath();
            m_path = path + "/" + getName();
            String base = directive.getBasedir();
            if( null == base )
            {
                m_base = parent.getBase();
            }
            else
            {
                m_base = new File( parent.getBase(), base );
            }
        }
        
        ArtifactDirective[] artifacts = m_directive.getArtifactDirectives();
        m_types = new String[ artifacts.length ];
        for( int i=0; i<m_types.length; i++ )
        {
            ArtifactDirective artifact = artifacts[i];
            m_types[i] = artifact.getType();
        }
        
        m_resource = new DefaultResource( library, parent, this );
    }
    
    public String getName()
    {
        return m_directive.getName();
    }
        
    public File getBase()
    {
        return m_base;
    }
    
    public String getPath()
    {
        return m_path;
    }
    
    public String[] getTypes()
    {
        return m_types;
    }
    
    public Resource[] getProviders( Scope scope ) throws ResourceNotFoundException, ModuleNotFoundException
    {
        return getProviderResources( scope );
    }
    
    public Resource toResource()
    {
        return toLocalResource();
    }
    
    public Resource[] getResourceClassPath( Scope scope )
      throws ModuleNotFoundException, ResourceNotFoundException
    {
        return getLocalClasspath( scope );
    }
    
   /**
    * Return the set projects that are direct consumers of this project.
    * @return the sorted array of consumer projects
    */
    public Project[] getConsumers() 
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        return getConsumerProjects();
    }
    
   /**
    * Return the set projects that are consumers of this project.
    * @param depth the search depth
    * @return the sorted array of consumer projects
    */
    public Project[] getAllConsumers() 
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        return getAllConsumerProjects();
    }
    
   /**
    * Return the set of immediate consumers of this project.
    * @param collection an unsorted collection of all registered projects
    */
    DefaultProject[] getConsumerProjects() 
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        DefaultProject[] collection = m_library.getAllRegisteredProjects( false );
        return getConsumerProjects( collection );
    }
    
    DefaultProject[] getAllConsumerProjects() 
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        ArrayList list = new ArrayList();
        ArrayList visited = new ArrayList();
        DefaultProject[] projects = m_library.getAllRegisteredProjects( false );
        DefaultProject[] consumers = getConsumerProjects( projects );
        for( int i=0; i<consumers.length; i++ )
        {
            DefaultProject consumer = consumers[i];
            processConsumerProject( list, visited, consumer, projects );
        }
        return (DefaultProject[]) list.toArray( new DefaultProject[0] );
    }
    
    void processConsumerProject( ArrayList list, ArrayList visited, DefaultProject project, DefaultProject[] collection ) 
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        if( visited.contains( project ) )
        {
            return;
        }
        else
        {
            list.add( project );
            visited.add( project );
            DefaultProject[] consumers = project.getConsumerProjects( collection );
            for( int i=0; i<consumers.length; i++ )
            {
                DefaultProject consumer = consumers[i];
                processConsumerProject( list, visited, consumer, collection );
            }
        }
    }
    
   /**
    * Return the set of immediate consumers of this project.
    * @param collection an unsorted collection of all registered projects
    */
    DefaultProject[] getConsumerProjects( DefaultProject[] collection ) 
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        DefaultResource resource = toLocalResource();
        ArrayList list = new ArrayList();
        for( int i=0; i<collection.length; i++ )
        {
            DefaultProject p = collection[i];
            DefaultResource[] resources = p.getProviderResources( Scope.TEST );
            for( int j=0; j<resources.length; j++ )
            {
                DefaultResource r = resources[j];
                if( resource.equals( r ) )
                {
                    if( !list.contains( p ) )
                    {
                        list.add( p );
                    }
                }
            }
        }
        return (DefaultProject[]) list.toArray( new DefaultProject[0] );
    }
    
    IncludeDirective[] getIncludeDirectives( Scope scope )
    {
        if( scope == Scope.RUNTIME )
        {
            DependencyDirective runtime = m_directive.getDependencyDirective( Scope.RUNTIME );
            if( null != runtime )
            {
                return runtime.getIncludeDirectives();
            }
            else
            {
                return new IncludeDirective[0];
            }
        }
        else if( scope == Scope.TEST )
        {
            IncludeDirective[] runtime = getIncludeDirectives( Scope.RUNTIME );
            DependencyDirective testDependencies = m_directive.getDependencyDirective( Scope.TEST );
            if( null == testDependencies )
            {
                return runtime;
            }
            else
            {
                IncludeDirective[] test = testDependencies.getIncludeDirectives();
                IncludeDirective[] includes = new IncludeDirective[ runtime.length + test.length ];
                System.arraycopy( runtime, 0, includes, 0, runtime.length );
                System.arraycopy( test, 0, includes, runtime.length+1, test.length );
                return includes;
            }
        }
        else
        {
            final String error = 
              "Unrecognized scope: " + scope;
            throw new ModelRuntimeException( error );
        }
    }

    public String toString()
    {
        return "project:" + getPath();
    }
    
    DefaultResource[] getProviderResources( Scope scope ) throws ResourceNotFoundException, ModuleNotFoundException
    {
        IncludeDirective[] includes = getIncludeDirectives( scope );
        return m_library.resolveResourceDependencies( m_parent, includes );
    }
    
    DefaultResource[] getProviderResources( Scope scope, String type ) 
     throws ResourceNotFoundException, ModuleNotFoundException
    {
        ArrayList list = new ArrayList();
        DefaultResource[] resources = getProviderResources( scope );
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            if( resource.isa( type ) )
            {
                list.add( resource );
            }
        }
        return (DefaultResource[]) list.toArray( new DefaultResource[0] );
    }
    
    DefaultResource toLocalResource()
    {
        return m_resource;
    }

    private DefaultResource[] getLocalClasspath( Scope scope )
      throws ModuleNotFoundException, ResourceNotFoundException
    {
        ArrayList stack = new ArrayList();
        ArrayList visited = new ArrayList();
        DefaultResource[] resources = getProviderResources( scope, "jar" );
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            processClasspath( visited, stack, resource );
        }
        if( scope.equals( Scope.TEST ) )
        {
            stack.add( toLocalResource() );
        }
        return (DefaultResource[]) stack.toArray( new DefaultResource[0] );
    }
    
    private void processClasspath( ArrayList visited, ArrayList stack, DefaultResource resource )
      throws ModuleNotFoundException, ResourceNotFoundException
    {
        if( visited.contains( resource ) )
        {
            return;
        }
        else
        {
            visited.add( resource );
        }
        DefaultResource[] resources = resource.getProviderResources( "jar" );
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource r = resources[i];
            processClasspath( visited, stack, r );
        }
        stack.add( resource );
    }

}

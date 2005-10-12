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
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Hashtable;

import net.dpml.tools.info.ModuleDirective;
import net.dpml.tools.info.ResourceDirective;
import net.dpml.tools.info.ProjectDirective;
import net.dpml.tools.info.IncludeDirective;

import net.dpml.tools.model.Module;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Project;
import net.dpml.tools.model.ModuleNotFoundException;
import net.dpml.tools.model.ResourceNotFoundException;
import net.dpml.tools.model.ProjectNotFoundException;
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
public final class DefaultModule extends UnicastRemoteObject implements Module
{
    private final DefaultModule m_parent;
    private final ModuleDirective m_directive;
    
    private final Hashtable m_modules = new Hashtable();
    private final Hashtable m_resources = new Hashtable();
    private final Hashtable m_projects = new Hashtable();
    private final String m_path;
    
    private DefaultModule[] m_imports;
    
    DefaultModule( DefaultLibrary library, ModuleDirective directive ) throws RemoteException
    {
        this( library, null, directive );
    }
    
    DefaultModule( DefaultLibrary library, DefaultModule parent, ModuleDirective directive ) throws RemoteException
    {
        super();
        
        m_directive = directive;
        m_parent = parent;
        
        if( null == m_parent )
        {
            m_path = m_directive.getName();
        }
        else
        {
            String path = m_parent.getPath();
            m_path = path + "/" + getName();
        }
        
        ModuleDirective[] moduleDirectives = directive.getModuleDirectives();
        for( int i=0; i<moduleDirectives.length; i++ )
        {
            ModuleDirective moduleDirective = moduleDirectives[i];
            DefaultModule module = new DefaultModule( library, this, moduleDirective );
            String key = module.getName();
            m_modules.put( key, module );
        }

        ResourceDirective[] resourceDirectives = directive.getResourceDirectives();
        for( int i=0; i<resourceDirectives.length; i++ )
        {
            ResourceDirective resourceDirective = resourceDirectives[i];
            DefaultResource resource = new DefaultResource( library, this, resourceDirective );
            String key = resource.getName();
            m_resources.put( key, resource );
        }
        
        ProjectDirective[] projectDirectives = directive.getProjectDirectives();
        for( int i=0; i<projectDirectives.length; i++ )
        {
            ProjectDirective projectDirective = projectDirectives[i];
            DefaultProject project = new DefaultProject( library, this, projectDirective );
            String key = project.getName();
            m_projects.put( key, project );
        }
    }
    
    void init( DefaultLibrary library ) throws Exception
    {
        IncludeDirective[] includes = m_directive.getIncludeDirectives();
        for( int i=0; i<includes.length; i++ )
        {
            IncludeDirective include = includes[i];
            String includeType = include.getType();
            if( "file".equals( includeType ) )
            {
                String path = include.getValue();
                library.installLocalModule( path );
            }
            else if( "uri".equals( includeType ) )
            {
                String path = include.getValue();
                URI uri = new URI( path );
                library.installModule( uri );
            }
            else
            {
                final String error = 
                  "Unsupport include function [" + includeType + "]";
                throw new IllegalArgumentException( error );
            }
        }
        DefaultModule[] modules = (DefaultModule[]) m_modules.values().toArray( new DefaultModule[0] );
        for( int i=0; i<modules.length; i++ )
        {
            DefaultModule module = modules[i];
            module.init( library );
        }
        m_imports = modules;
    }
    
    public String getName()
    {
        return m_directive.getName();
    }
    
    public String getPath()
    {
        return m_path;
    }
    
    public Module getParent()
    {
        return m_parent;
    }
    
    public Module[] getModules()
    {
        return (Module[]) m_modules.values().toArray( new Module[0] );
    }
    
    public Module[] getImportedModules()
    {
        return (Module[]) m_imports;
    }
    
    public Module getModule( String key ) throws ModuleNotFoundException
    {
        return getLocalModule( key );
    }
    
    public Resource[] getResources()
    {
        return (Resource[]) m_resources.values().toArray( new Resource[0] );
    }
    
    public Project[] getProjects()
    {
        return (Project[]) m_projects.values().toArray( new Project[0] );
    }
    
    public Resource getResource( String key ) throws ResourceNotFoundException
    {
        return getLocalResource( key );
    }
    
    public Project getProject( String key ) throws ProjectNotFoundException
    {
        return getLocalProject( key );
    }
    
    public Resource resolveResource( String key ) throws ResourceNotFoundException
    {
        return resolveLocalResource( key );
    }
    
    
    public String toString()
    {
        return "module:" + m_path;
    }
    
    
    DefaultModule[] getLocalModules()
    {
        return (DefaultModule[]) m_modules.values().toArray( new DefaultModule[0] );
    }
    
    DefaultProject[] getLocalProjects()
    {
        return (DefaultProject[]) m_projects.values().toArray( new DefaultProject[0] );
    }

    DefaultModule getLocalModule( String key ) throws ModuleNotFoundException
    {
        DefaultModule module = (DefaultModule) m_modules.get( key );
        if( null == module )
        {
            throw new ModuleNotFoundException( key );
        }
        else
        {
            return module;
        }
    }

    DefaultResource getLocalResource( String key ) throws ResourceNotFoundException
    {
        DefaultResource resource = (DefaultResource) m_resources.get( key );
        if( null == resource )
        {
            throw new ResourceNotFoundException( key );
        }
        else
        {
            return resource;
        }
    }
    
    DefaultResource resolveLocalResource( String key ) throws ResourceNotFoundException
    {
        try
        {
            DefaultProject project = getLocalProject( key );
            return project.toLocalResource();
        }
        catch( ProjectNotFoundException e )
        {
            return getLocalResource( key );
        }
    }

    DefaultProject getLocalProject( String key ) throws ProjectNotFoundException
    {
        DefaultProject project = (DefaultProject) m_projects.get( key );
        if( null == project )
        {
            throw new ProjectNotFoundException( key );
        }
        else
        {
            return project;
        }
    }
}

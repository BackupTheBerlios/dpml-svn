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
import java.util.List;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;

import net.dpml.tools.info.ModuleDirective;
import net.dpml.tools.info.ResourceDirective;
import net.dpml.tools.info.ProjectDirective;
import net.dpml.tools.info.IncludeDirective;

import net.dpml.tools.model.Module;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Project;
import net.dpml.tools.model.ModelNotFoundException;
import net.dpml.tools.model.ModuleNotFoundException;
import net.dpml.tools.model.ResourceNotFoundException;
import net.dpml.tools.model.ProjectNotFoundException;
import net.dpml.tools.model.ReferentialException;
import net.dpml.tools.model.DuplicateNameException;
import net.dpml.tools.model.Library;
import net.dpml.tools.model.ModelRuntimeException;

import net.dpml.transit.util.ElementHelper;
import net.dpml.transit.util.PropertyResolver;

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
    private final DefaultLibrary m_library;
    
    private final Hashtable m_modules = new Hashtable();
    private final Hashtable m_resources = new Hashtable();
    private final Hashtable m_projects = new Hashtable();
    private final String m_path;
    
    private Properties m_properties;
    private File m_base;
    
    private DefaultModule[] m_imports;
    
    DefaultModule( DefaultLibrary library, ModuleDirective directive, File anchor ) throws RemoteException
    {
        this( library, null, directive, anchor );
    }
    
    DefaultModule( DefaultLibrary library, DefaultModule parent, ModuleDirective directive, File anchor ) throws RemoteException
    {
        super();
        
        m_directive = directive;
        m_parent = parent;
        m_library = library;
        
        if( null == m_parent )
        {
            m_path = m_directive.getName();
        }
        else
        {
            String path = m_parent.getPath();
            m_path = path + "/" + getName();
        }
        
        try
        {
            String base = m_directive.getBasedir();
            if( null == base )
            {
                m_base = anchor.getCanonicalFile();
            }
            else
            {
                m_base = new File( anchor, base ).getCanonicalFile();
            }
        }
        catch( IOException e )
        {
            final String error = 
               "Internal error while attempting to construct a canonical file.";
            throw new RuntimeException( error, e );
        }
        
        m_properties = setupProperties();
        ModuleDirective[] moduleDirectives = directive.getModuleDirectives();
        for( int i=0; i<moduleDirectives.length; i++ )
        {
            ModuleDirective moduleDirective = moduleDirectives[i];
            DefaultModule module = new DefaultModule( library, this, moduleDirective, m_base );
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
    
    void init( DefaultLibrary library, File anchor ) throws Exception
    {
        IncludeDirective[] includes = m_directive.getIncludeDirectives();
        DefaultModule[] modules = new DefaultModule[ includes.length ];
        for( int i=0; i<includes.length; i++ )
        {
            IncludeDirective include = includes[i];
            String includeType = include.getType();
            if( "file".equals( includeType ) )
            {
                String path = include.getValue();
                modules[i] = library.installLocalModule( anchor, path );
            }
            else if( "uri".equals( includeType ) )
            {
                String path = include.getValue();
                URI uri = new URI( path );
                modules[i] = library.installModule( uri );
            }
            else
            {
                final String error = 
                  "Unsupport include function [" + includeType + "]";
                throw new IllegalArgumentException( error );
            }
        }
        m_imports = modules;
        DefaultModule[] local = getDefaultModules();
        for( int i=0; i<local.length; i++ )
        {
            DefaultModule m = local[i];
            m.init( library, anchor );
        }
    }
    
    public String getName()
    {
        return m_directive.getName();
    }
   
    public String getVersion()
    {
        String version = m_directive.getVersion();
        if( ( null == version ) && ( null != m_parent ) )
        {
            return m_parent.getVersion();
        }
        else
        {
            return version;
        }
    }

    public String getPath()
    {
        return m_path;
    }
    
    public File getBase()
    {
        return m_base;
    }
    
    public Module getModule()
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
        return getDefaultModule( key );
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
        return getDefaultResource( key );
    }
    
    public Project getProject( String key ) throws ProjectNotFoundException
    {
        return getDefaultProject( key );
    }
    
   /**
    * Return a sorted array of all projects within this module group.
    * @return the sorted project array
    */
    public Project[] getSubsidiaryProjects()
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        ArrayList list = new ArrayList();
        aggregateProjects( list );
        DefaultProject[] projects = (DefaultProject[]) list.toArray( new DefaultProject[0] );
        return m_library.sortProjects( projects, true );
    }

    public String getProperty( String key )
    {
        return getProperty( key, null );
    }
    
    public String getProperty( String key, String value )
    {
        String result = getProperties().getProperty( key );
        return PropertyResolver.resolve( m_properties, result );
    }

    Properties getProperties()
    {
        return m_properties;
    }
    
    public String[] getPropertyNames()
    {
        ArrayList list = new ArrayList();
        Enumeration names = getProperties().propertyNames();
        while( names.hasMoreElements() )
        {
            list.add( (String) names.nextElement() );
        }
        return (String[]) list.toArray( new String[0] );
    }

    public Resource resolveResource( String key ) throws ResourceNotFoundException
    {
        return resolveDefaultResource( key );
    }
    
    public String toString()
    {
        return "module:" + m_path;
    }
    
    DefaultModule[] getDefaultModules()
    {
        return (DefaultModule[]) m_modules.values().toArray( new DefaultModule[0] );
    }
    
    DefaultProject[] getDefaultProjects()
    {
        return (DefaultProject[]) m_projects.values().toArray( new DefaultProject[0] );
    }

    DefaultModule getDefaultModule( String key ) throws ModuleNotFoundException
    {
        DefaultModule module = (DefaultModule) m_modules.get( key );
        if( null == module )
        {
            throw new ModuleNotFoundException( m_path + "/" + key );
        }
        else
        {
            return module;
        }
    }

    DefaultResource getDefaultResource( String key ) throws ResourceNotFoundException
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
    
    DefaultResource resolveDefaultResource( String key ) throws ResourceNotFoundException
    {
        try
        {
            DefaultProject project = getDefaultProject( key );
            return project.toLocalResource();
        }
        catch( ProjectNotFoundException e )
        {
            return getDefaultResource( key );
        }
    }

    DefaultProject getDefaultProject( String key ) throws ProjectNotFoundException
    {
        DefaultProject project = (DefaultProject) m_projects.get( key );
        if( null == project )
        {
            throw new ProjectNotFoundException( this, key );
        }
        else
        {
            return project;
        }
    }

    void aggregateProjects( List list )
    {
        DefaultProject[] projects = getDefaultProjects();
        for( int i=0; i<projects.length; i++ )
        {
            list.add( projects[i] );
        }
        DefaultModule[] modules = getDefaultModules();
        for( int i=0; i<modules.length; i++ )
        {
            DefaultModule module = modules[i];
            module.aggregateProjects( list );
        }
    }

    private Properties setupProperties()
    {
        Properties properties = createProperties();
        Properties local = m_directive.getProperties();
        if( null != local )
        {
            Enumeration names = local.propertyNames();
            while( names.hasMoreElements() )
            {
                String name = (String) names.nextElement();
                String value = local.getProperty( name );
                properties.setProperty( name, value );
            }
        }
        return properties;
    }
    
    private Properties createProperties()
    {
        if( null == m_parent )
        {
            Properties defaults = m_library.getProperties();
            return new Properties( defaults );
        }
        else
        {
            Properties defaults = m_parent.getProperties();
            return new Properties( defaults );
        }
    }
}

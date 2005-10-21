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
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Date;

import net.dpml.tools.info.ProductionDirective;
import net.dpml.tools.info.DependencyDirective;
import net.dpml.tools.info.ProjectDirective;
import net.dpml.tools.info.ResourceDirective;
import net.dpml.tools.info.TypeDirective;
import net.dpml.tools.info.IncludeDirective;
import net.dpml.tools.info.TaggedIncludeDirective;
import net.dpml.tools.info.Scope;
import net.dpml.tools.info.TypeDescriptor;
import net.dpml.tools.model.TypeNotFoundException;
import net.dpml.tools.model.Project;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Module;
import net.dpml.tools.model.ModuleNotFoundException;
import net.dpml.tools.model.ResourceNotFoundException;
import net.dpml.tools.model.ProjectNotFoundException;
import net.dpml.tools.model.ReferentialException;
import net.dpml.tools.model.DuplicateNameException;
import net.dpml.tools.model.Library;
import net.dpml.tools.model.ModelRuntimeException;

import net.dpml.transit.Category;
import net.dpml.transit.util.ElementHelper;
import net.dpml.transit.util.PropertyResolver;

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
    private final Properties m_properties;
    
    DefaultProject( DefaultLibrary library, DefaultModule parent, ProjectDirective directive ) throws RemoteException
    {
        super();
        
        m_directive = directive;
        m_parent = parent;
        m_library = library;
        
        try
        {
            if( null == m_parent )
            {
                m_path = m_directive.getName();
                String base = directive.getBasedir();
                if( null == base )
                {
                    m_base = library.getRootDirectory().getCanonicalFile();
                }
                else
                {
                    m_base = new File( library.getRootDirectory(), base ).getCanonicalFile();
                }
            }
            else
            {
                String path = m_parent.getPath();
                m_path = path + "/" + getName();
                String base = directive.getBasedir();
                if( null == base )
                {
                    m_base = parent.getBase().getCanonicalFile();
                }
                else
                {
                    m_base = new File( parent.getBase(), base ).getCanonicalFile();
                }
            }
        }
        catch( IOException e )
        {
            final String error = 
               "Internal error while attempting to construct a canonical file.";
            throw new RuntimeException( error, e );
        }
        
        ProductionDirective[] artifacts = m_directive.getProductionDirectives();
        m_types = new String[ artifacts.length ];
        for( int i=0; i<m_types.length; i++ )
        {
            ProductionDirective artifact = artifacts[i];
            m_types[i] = artifact.getType();
        }
        
        m_resource = new DefaultResource( library, parent, this );
        m_properties = setupProperties();
    }
    
    public long getLastModified()
    {
        return m_library.getLastModified();
    }
    
    public TypeDescriptor getTypeDescriptor( String type ) throws TypeNotFoundException
    {
        return m_library.getTypeDescriptor( type );
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

    public ProductionDirective[] getProductionDirectives()
    {
        return m_directive.getProductionDirectives();
    }

    public Module getModule()
    {
        return m_parent;
    }
        
    public File getBase()
    {
        return m_base;
    }
    
    public String getPath()
    {
        return m_path;
    }
    
    public String[] getTypeNames()
    {
        return m_types;
    }
    
    public Resource[] getProviders() throws ResourceNotFoundException, ModuleNotFoundException
    {
        return getProviderResources();
    }
    
    public Resource[] getProviders( Scope scope ) throws ResourceNotFoundException, ModuleNotFoundException
    {
        return getProviderResources( scope );
    }
    
    public Resource toResource()
    {
        return toLocalResource();
    }
    
    public Resource[] getClassPath( Scope scope )
      throws ModuleNotFoundException, ResourceNotFoundException
    {
        return getResourceClasspath( scope );
    }
    
    public Resource[] getClassPath( Category category )
      throws ModuleNotFoundException, ResourceNotFoundException
    {
        return getRuntimeClasspath( category );
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
    * @return the sorted array of consumer projects
    */
    public Project[] getAllConsumers() 
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        return getAllConsumerProjects();
    }
    
    public String getProperty( String key )
    {
        return getProperty( key, null );
    }
    
    public String getProperty( String key, String value )
    {
        String result = m_properties.getProperty( key );
        return PropertyResolver.resolve( m_properties, result );
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

    Properties getProperties()
    {
        return m_properties;
    }
    
    public String getProductionProperty( String type, String key, String value )
    {
        ProductionDirective production = m_directive.getProductionDirective( type );
        Properties properties = production.getProperties();
        if( null != properties )
        {
            return properties.getProperty( key, value );
        }
        return value;
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
            DefaultResource[] resources = p.getProviderResources();
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
        DependencyDirective directive = m_directive.getDependencyDirective( scope );
        if( null != directive )
        {
            return directive.getIncludeDirectives();
        }
        else
        {
            return new IncludeDirective[0];
        }
    }

    public String toString()
    {
        return "project:" + getPath();
    }
    
    DefaultResource[] getProviderResources() throws ResourceNotFoundException, ModuleNotFoundException
    {
        ArrayList list = new ArrayList();
        addIncludesToList( list, Scope.BUILD );
        addIncludesToList( list, Scope.RUNTIME );
        addIncludesToList( list, Scope.TEST );
        IncludeDirective[] includes = (IncludeDirective[]) list.toArray( new IncludeDirective[0] ); 
        return m_library.resolveResourceDependencies( m_parent, includes );
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
        return getFilteredResources( resources, type );
    }
    
    DefaultResource[] getFilteredResources( DefaultResource[] candidates, String type ) 
     throws ResourceNotFoundException, ModuleNotFoundException
    {
        ArrayList list = new ArrayList();
        for( int i=0; i<candidates.length; i++ )
        {
            DefaultResource resource = candidates[i];
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
    
    private DefaultResource[] getRuntimeClasspath( Category category )
      throws ModuleNotFoundException, ResourceNotFoundException
    {
        ArrayList stack = new ArrayList();
        ArrayList visited = new ArrayList();
        IncludeDirective[] includes = getIncludeDirectives( Scope.RUNTIME );
        ArrayList list = new ArrayList();
        for( int i=0; i<includes.length; i++ )
        {
            TaggedIncludeDirective include = (TaggedIncludeDirective) includes[i];
            if( category.equals( include.getCategory() ) )
            {
                list.add( include );
            }
        }
        IncludeDirective[] candidates = (IncludeDirective[]) list.toArray( new IncludeDirective[0] );
        DefaultResource[] resources = m_library.resolveResourceDependencies( m_parent, candidates );
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            processClasspath( visited, stack, resource );
        }
        return (DefaultResource[]) stack.toArray( new DefaultResource[0] );
    }
    
    private DefaultResource[] getResourceClasspath( Scope scope )
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

    private void addIncludesToList( List list, Scope scope )
    {
        IncludeDirective[] includes = getIncludeDirectives( scope );
        for( int i=0; i<includes.length; i++ )
        {
            list.add( includes[i] );
        }
    }

    private Properties setupProperties()
    {
        Properties defaults = m_parent.getProperties();
        Properties properties = new Properties( defaults );
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
        File basedir = getBase();
        try
        {
            properties.setProperty( "basedir", basedir.getCanonicalPath() );
            return properties;
        }
        catch( IOException e )
        {
            final String error = 
              "Unable to resolve canonical path from file: " + basedir;  
            throw new RuntimeException( error, e );
        }
    }
}

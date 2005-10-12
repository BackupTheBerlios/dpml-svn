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
import java.util.Hashtable;
import java.util.ArrayList;

import net.dpml.tools.info.IncludeDirective;
import net.dpml.tools.info.ModuleDirective;
import net.dpml.tools.info.Scope;
import net.dpml.tools.model.Module;
import net.dpml.tools.model.Project;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.ModelRuntimeException;
import net.dpml.tools.model.ModuleNotFoundException;
import net.dpml.tools.model.ProjectNotFoundException;
import net.dpml.tools.model.ResourceNotFoundException;
import net.dpml.tools.model.ReferentialException;
import net.dpml.tools.model.DuplicateNameException;
import net.dpml.tools.model.Library;

import net.dpml.transit.Logger;
import net.dpml.transit.util.ElementHelper;

import org.w3c.dom.Element;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class DefaultLibrary extends UnicastRemoteObject implements Library
{
    private final Hashtable m_modules = new Hashtable();
    private final File m_root;
    private final Logger m_logger;
    
    public DefaultLibrary( Logger logger, File source ) throws RemoteException, Exception
    {
        super();
        
        if( null == logger )
        {
            throw new NullPointerException( "logger" );
        }
        if( null == source )
        {
            throw new NullPointerException( "source" );
        }
        
        m_logger = logger;
        m_root = source.getParentFile();
        getLogger().info( "loading root module: " + source );
        ModuleDirective directive= ModuleDirectiveBuilder.build( source );
        DefaultModule module = new DefaultModule( this, directive );
        String path = module.getPath();
        m_modules.put( path, module );
        module.init( this );
    }
    
   /**
    * Return a sorted array of all projects within the library.
    * @return the sorted project array
    */
    public Project[] getAllProjects()
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        ArrayList list = new ArrayList();
        DefaultModule[] modules = getLocalModules();
        for( int i=0; i<modules.length; i++ )
        {
            aggregateProjects( list, modules[i] );
        }
        
        // TODO: sort projects
        
        DefaultProject[] projects = (DefaultProject[]) list.toArray( new DefaultProject[0] );
        return sortProjects( projects );
    }
    
   /**
    * Return a sorted array of projects including the dependent project of the 
    * suplied target project.
    * @param project the target project
    * @return the sorted project array
    */
    public Project[] getAllProjects( Project project )
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        DefaultProject p = (DefaultProject) project;
        return sortProjects( new DefaultProject[]{ p } );
    }
    
   /**
    * Return an array of modules registered with the library.
    * @return the module array
    */
    public Module[] getModules()
    {
        return (Module[]) m_modules.values().toArray( new Module[0] );
    }
    
   /**
    * Get a named module.
    * @param path the module address
    * @exception ModuleNotFoundException if the address is not resolvable
    */
    public Module getModule( String path ) throws ModuleNotFoundException
    {
        return getLocalModule( path );
    }
    
   /**
    * Get a named project.
    * @param path the project address
    * @exception ModuleNotFoundException if the address is not resolvable
    * @exception ProjectNotFoundException if the address is not resolvable
    */
    public Project getProject( String path ) throws ModuleNotFoundException, ProjectNotFoundException
    {
        return getLocalProject( path );
    }
    
    DefaultModule getLocalModule( String path ) throws ModuleNotFoundException
    {
        int n = path.indexOf( "/" );
        if( n > 0 )
        {
            String pre = path.substring( 0, n );
            String post = path.substring( n+1 );
            DefaultModule module = getLocalModule( pre );
            return module.getLocalModule( post );
        }
        DefaultModule module = (DefaultModule) m_modules.get( path );
        if( null == module )
        {
            throw new ModuleNotFoundException( path );
        }
        else
        {
            return module;
        }
    }

    DefaultProject getLocalProject( String path ) throws ModuleNotFoundException, ProjectNotFoundException
    {
        int n = path.lastIndexOf( "/" );
        if( n > 0 )
        {
            String pre = path.substring( 0, n );
            String post = path.substring( n+1 );
            DefaultModule module = getLocalModule( pre );
            return module.getLocalProject( post );
        }
        else
        {
            final String error =
              "Project address does not include a module name [" + path + "].";
            throw new IllegalArgumentException( error );
        }
    }
    
    DefaultResource[] resolveResourceDependencies( DefaultModule module, IncludeDirective[] includes ) 
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        DefaultResource[] resources = new DefaultResource[ includes.length ];
        for( int i=0; i<includes.length; i++ )
        {
            IncludeDirective include = includes[i];
            String type = include.getType();
            String value = include.getValue();
            if( "key".equals( type ) )
            {
                resources[i] = module.resolveLocalResource( value );
            }
            else if( "ref".equals( type ) )
            {
                resources[i] = resolveResourceRef( value );
            }
            else
            {
                final String error = 
                  "Resource dependency include directive "
                  + include
                  + "] contains an unsupported include keyword ["
                  + type
                  + "].";
                throw new ModelRuntimeException( error );
            }
        }
        return resources;
    }
    
    DefaultModule[] getLocalModules()
    {
        return (DefaultModule[]) m_modules.values().toArray( new DefaultModule[0] );
    }
    
    DefaultResource resolveResourceRef( String value ) throws ModuleNotFoundException, ResourceNotFoundException
    {
        int n = value.lastIndexOf( "/" );
        if( n < 1 )
        {
            final String error = 
             "Absolute resource reference [" 
              + value
              + "] does not include a module identifier.";
            throw new ModelRuntimeException( error );
        }
        else
        {
            String moduleName = value.substring( 0, n );
            DefaultModule module = getLocalModule( moduleName );
            String key = value.substring( n+1 );
            return module.resolveLocalResource( key );
        }
    }
    
    void installModule( URI uri ) throws Exception
    {
        throw new UnsupportedOperationException( "installModule/1" );
    }
    
    void installLocalModule( String path ) throws Exception
    {
        File file = new File( m_root, path );
        getLogger().info( "loading local module: " + file );
        ModuleDirective directive= ModuleDirectiveBuilder.build( file );
        String name = directive.getName();
        
        DefaultModule module = new DefaultModule( this, null, directive );
        if( !m_modules.containsKey( name ) )
        {
            m_modules.put( name, module );
            module.init( this );
        }
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }

    private Project[] sortProjects( DefaultProject[] projects )
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        ArrayList stack = new ArrayList();
        ArrayList visited = new ArrayList();
        for( int i=0; i<projects.length; i++ )
        {
            DefaultProject project = projects[i];
            processProject( visited, stack, project );
        }
        return (DefaultProject[]) stack.toArray( new DefaultProject[0] );
    }
    
    private void processProject( ArrayList visited, ArrayList stack, DefaultProject project ) 
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        if( visited.contains( project ) )
        {
            return;
        }
        else
        {
            visited.add( project );
        }
        DefaultProject[] projects = getDependentProjects( project );
        for( int i=0; i<projects.length; i++ )
        {
            DefaultProject p = projects[i];
            processProject( visited, stack, p );
        }
        stack.add( project );
    }
    
    private DefaultProject[] getDependentProjects( DefaultProject project ) 
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        ArrayList list = new ArrayList();
        DefaultResource[] resources = project.getLocalDependencies( Scope.TEST );
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            DefaultProject p = resource.getLocalProject();
            if( null != p )
            {
                list.add( p );
            }
        }
        return (DefaultProject[]) list.toArray( new DefaultProject[0] );
    }
    
    private void aggregateProjects( ArrayList list, DefaultModule module )
    {
        DefaultProject[] projects = module.getLocalProjects();
        for( int i=0; i<projects.length; i++ )
        {
            list.add( projects[i] );
        }
        DefaultModule[] modules = module.getLocalModules();
        for( int i=0; i<modules.length; i++ )
        {
            aggregateProjects( list, modules[i] );
        }
    }
}

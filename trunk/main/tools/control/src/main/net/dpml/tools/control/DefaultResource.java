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

import net.dpml.tools.info.IncludeDirective;
import net.dpml.tools.info.ResourceDirective;
import net.dpml.tools.info.TypeDirective;
import net.dpml.tools.info.Scope;
import net.dpml.tools.model.Module;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Project;
import net.dpml.tools.model.ResourceNotFoundException;
import net.dpml.tools.model.ModuleNotFoundException;
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
public final class DefaultResource extends UnicastRemoteObject implements Resource
{
    private final String m_name;
    private final String m_version;
    private final DefaultModule m_parent;
    private final String[] m_types;
    private final String m_path;
    private final IncludeDirective[] m_includes;
    private final DefaultLibrary m_library;
    private final DefaultProject m_project;
    
    DefaultResource( 
      DefaultLibrary library, DefaultModule parent, DefaultProject project ) throws RemoteException
    {
        m_library = library;
        m_version = null;
        m_parent = parent;
        m_name = project.getName();
        m_types = project.getTypes();
        m_path = project.getPath();
        m_project = project;
        
        TypeDirective[] typeDirectives = new TypeDirective[ m_types.length ];
        for( int i=0; i<typeDirectives.length; i++ )
        {
            typeDirectives[i] = new TypeDirective( m_types[i] );
        }
        m_includes = project.getIncludeDirectives( Scope.RUNTIME );
    }
    
    DefaultResource( 
      DefaultLibrary library, DefaultModule parent, ResourceDirective directive ) throws RemoteException
    {
        super();
        
        m_project = null;
        m_parent = parent;
        m_library = library;
        m_name = directive.getName();
        m_includes = directive.getIncludeDirectives();
        m_version = directive.getVersion();
        
        if( null == m_parent )
        {
            m_path = m_name;
        }
        else
        {
            String path = m_parent.getPath();
            m_path = path + "/" + getName();
        }
        
        TypeDirective[] typeDirectives = directive.getTypeDirectives();
        m_types = new String[ typeDirectives.length ];
        for( int i=0; i<typeDirectives.length; i++ )
        {
            TypeDirective type = typeDirectives[i];
            m_types[i] = type.getName();
        }
    }
    
    public String getName()
    {
        return m_name;
    }
    
    public String getPath()
    {
        return m_path;
    }
    
    public String getVersion()
    {
        return m_version;
    }
    
    public String[] getTypes()
    {
        return m_types;
    }
    
    public Resource[] getDependencies() throws ModuleNotFoundException, ResourceNotFoundException
    {
        return getLocalDependencies();
    }
    
    public String toString()
    {
        return "resource:" + getPath();
    }
    
    DefaultResource[] getLocalDependencies() throws ModuleNotFoundException, ResourceNotFoundException
    {
        try
        {
            return m_library.resolveResourceDependencies( m_parent, m_includes );
        }
        catch( ModuleNotFoundException e )
        {
            final String error = 
              "Unable to resolve an external module references by a dependency in the resource ["
              + getPath();
            throw new ModuleNotFoundException( error, e );
        }
        catch( ResourceNotFoundException e )
        {
            final String error = 
              "Unable to resolve an external resource references as a dependency in the resource ["
              + getPath();
            throw new ResourceNotFoundException( error, e );
        }
    }
    
    DefaultResource[] getLocalDependencies( String type )
     throws ResourceNotFoundException, ModuleNotFoundException
    {
        ArrayList list = new ArrayList();
        DefaultResource[] resources = getLocalDependencies();
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
    
    public Project getProject()
    {
        return m_project;
    }
    
    DefaultProject getLocalProject()
    {
        return m_project;
    }
    
    boolean isa( String type )
    {
        for( int i=0; i<m_types.length; i++ )
        {
            String t = m_types[i];
            if( type.equals( t ) )
            {
                return true;
            }
        }
        return false;
    }
}

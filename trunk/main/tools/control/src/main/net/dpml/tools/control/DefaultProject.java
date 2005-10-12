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
    private final Resource m_resource;
    
    DefaultProject( DefaultLibrary library, DefaultModule parent, ProjectDirective directive ) throws RemoteException
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
        
        ArtifactDirective[] artifacts = m_directive.getArtifactDirectives();
        m_types = new String[ artifacts.length ];
        for( int i=0; i<m_types.length; i++ )
        {
            ArtifactDirective artifact = artifacts[i];
            m_types[i] = artifact.getType();
        }
        
        String name = directive.getName();
        TypeDirective[] typeDirectives = new TypeDirective[ m_types.length ];
        for( int i=0; i<typeDirectives.length; i++ )
        {
            typeDirectives[i] = new TypeDirective( m_types[i] );
        }
        IncludeDirective[] includes = getIncludeDirectives( Scope.RUNTIME );
        ResourceDirective resource = new ResourceDirective( name, null, typeDirectives, includes );
        m_resource = new DefaultResource( library, parent, resource );
    }
    
    public String getName()
    {
        return m_directive.getName();
    }
        
    public String[] getTypes()
    {
        return m_types;
    }
    
    public Resource[] getDependencies( Scope scope ) throws ResourceNotFoundException, ModuleNotFoundException
    {
        IncludeDirective[] includes = getIncludeDirectives( scope );
        return m_library.resolveResourceDependencies( m_parent, includes );
    }
    
    public Resource toResource()
    {
        return m_resource;
    }
    
    public IncludeDirective[] getIncludeDirectives( Scope scope )
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

}

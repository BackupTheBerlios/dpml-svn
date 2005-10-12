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
import net.dpml.tools.model.Module;
import net.dpml.tools.model.Resource;
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
    private final DefaultLibrary m_library;
    private final ResourceDirective m_directive;
    private final DefaultModule m_parent;
    private final String[] m_types;
    private final String m_path;
    
    DefaultResource( DefaultLibrary library, DefaultModule parent, ResourceDirective directive ) throws RemoteException
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
        
        TypeDirective[] typeDirectives = m_directive.getTypeDirectives();
        m_types = new String[ typeDirectives.length ];
        for( int i=0; i<typeDirectives.length; i++ )
        {
            TypeDirective type = typeDirectives[i];
            m_types[i] = type.getName();
        }
    }
    
    public String getName()
    {
        return m_directive.getName();
    }
    
    public String getPath()
    {
        return m_path;
    }
    
    public String getVersion()
    {
        return m_directive.getVersion();
    }
    
    public String[] getTypes()
    {
        return m_types;
    }
    
    public Resource[] getDependencies() throws ModuleNotFoundException, ResourceNotFoundException
    {
        try
        {
            IncludeDirective[] includes = m_directive.getIncludeDirectives();
            return m_library.resolveResourceDependencies( m_parent, includes );
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
}

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

import net.dpml.tools.info.ModuleDirective;
import net.dpml.tools.info.IncludeDirective;
import net.dpml.tools.model.Module;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.ModelRuntimeException;
import net.dpml.tools.model.ModuleNotFoundException;
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
        int n = path.indexOf( "/" );
        if( n > 0 )
        {
            String pre = path.substring( 0, n );
            String post = path.substring( n+1 );
            Module module = getModule( pre );
            try
            {
                return module.getModule( post );
            }
            catch( RemoteException e )
            {
                final String error =
                  "Unable to fulfill request due to a remote exception.";
                throw new ModelRuntimeException( error );
            }
        }
        Module module = (Module) m_modules.get( path );
        if( null == module )
        {
            throw new ModuleNotFoundException( path );
        }
        else
        {
            return module;
        }
    }
    
    Resource[] resolveResourceDependencies( DefaultModule module, IncludeDirective[] includes ) 
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        Resource[] resources = new Resource[ includes.length ];
        for( int i=0; i<includes.length; i++ )
        {
            IncludeDirective include = includes[i];
            String type = include.getType();
            String value = include.getValue();
            if( "key".equals( type ) )
            {
                resources[i] = module.resolveResource( value );
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
    
    Resource resolveResourceRef( String value ) throws ModuleNotFoundException, ResourceNotFoundException
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
            try
            {
                Module module = getModule( moduleName );
                String key = value.substring( n+1 );
                return module.resolveResource( key );
            }
            catch( RemoteException e )
            {
                final String error =
                  "Unable to fulfill request due to a remote exception.";
                throw new ModelRuntimeException( error );
            }
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
}

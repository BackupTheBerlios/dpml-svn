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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import net.dpml.tools.info.IncludeDirective;
import net.dpml.tools.info.LibraryDirective;
import net.dpml.tools.info.ModuleDirective;
import net.dpml.tools.info.ModuleIncludeDirective;
import net.dpml.tools.info.Scope;
import net.dpml.tools.info.TypeDescriptor;
import net.dpml.tools.model.TypeNotFoundException;
import net.dpml.tools.model.Model;
import net.dpml.tools.model.Module;
import net.dpml.tools.model.Project;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.IllegalAddressRuntimeException;
import net.dpml.tools.model.ModelRuntimeException;
import net.dpml.tools.model.ModelNotFoundException;
import net.dpml.tools.model.ModuleNotFoundException;
import net.dpml.tools.model.ProjectNotFoundException;
import net.dpml.tools.model.ResourceNotFoundException;
import net.dpml.tools.model.ReferentialException;
import net.dpml.tools.model.DuplicateNameException;
import net.dpml.tools.model.Library;

import net.dpml.transit.Artifact;
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
    private final LibraryDirective m_directive;
    private final Hashtable m_modules = new Hashtable();
    private final File m_root;
    private final File m_source;
    private final Logger m_logger;
    
    public static Library load( final Logger logger, final File source ) throws RemoteException, Exception
    {
        return new DefaultLibrary( logger, source );
    }
    
    public DefaultLibrary( Logger logger ) throws RemoteException, Exception
    {
        this( logger, resolveLibrarySource() );
    }
    
    DefaultLibrary( Logger logger, File source ) throws RemoteException, Exception
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
        m_source = source;
        m_root = source.getParentFile().getCanonicalFile();
        getLogger().debug( "loading root module: " + m_root );
        System.setProperty( "dpml.library.basedir", m_root.toString() );
        m_directive = LibraryDirectiveBuilder.build( source );
        ModuleIncludeDirective[] modules = m_directive.getModuleIncludeDirectives();
        for( int i=0; i<modules.length; i++ )
        {
            ModuleIncludeDirective include = modules[i];
            String includeType = include.getType();
            if( "file".equals( includeType ) )
            {
                String path = include.getValue();
                installLocalModule( m_root, path );
            }
            else if( "uri".equals( includeType ) )
            {
                String path = include.getValue();
                URI uri = new URI( path );
                installModule( uri );
            }
            else
            {
                final String error = 
                  "Unsupport include function [" + includeType + "]";
                throw new IllegalArgumentException( error );
            }
        }
    }
    
    public Model[] select( String spec )
    {
        String[] tokens = spec.split( "/" );
        //System.out.println( "# TOKENS: " + tokens.length );
        Criteria[] criteria = new Criteria[ tokens.length ];
        for( int i=0; i<tokens.length; i++ )
        {
            String token = tokens[i];
            if( token.equals( "**" ) )
            {
                criteria[i] = new Criteria( true, null );
            }
            else
            {
                //System.out.println( "# token: [" + i + "]\t" + token );
                StringBuffer buffer = new StringBuffer();
                boolean wildcard = ( token.indexOf( "*" ) > -1 );
                if( wildcard )
                {
                    String[] blocks = token.split( "\\*", -1 );
                    buffer.append( "(" );
                    for( int j=0; j<blocks.length; j++ )
                    {
                        //System.out.println( "\t# block: [" + j + "]\t'" + blocks[j] + "'" );
                        buffer.append( "\\Q" );
                        buffer.append( blocks[j] );
                        buffer.append( "\\E" );
                        if( j < (blocks.length-1) )
                        {
                            buffer.append( ".*" );
                        }
                    }
                    buffer.append( ")" );
                }
                else
                {
                    //System.out.println( "\t# block [s]\t'" + token + "'"  );
                    buffer.append( "(" );
                    buffer.append( "\\Q" );
                    buffer.append( token );
                    buffer.append( "\\E" );
                    buffer.append( ")" );
                }
                String expression = buffer.toString();
                Pattern pattern = Pattern.compile( expression );
                criteria[i] = new Criteria( false, pattern );
            }
        }
        if( criteria.length > 0 )
        {
            //
            // matching against a top-level module name
            //
            
            Criteria c = criteria[0];
            ArrayList list = new ArrayList();
            if( c.isRecursive() )
            {
                DefaultModule[] modules = getAllDefaultModules();
                for( int i=0; i<modules.length; i++ )
                {
                    //System.out.println( "# library recursive add: " + modules[i] );
                    list.add( modules[i] );
                }
            }
            else
            {
                DefaultModule[] modules = getDefaultModules();
                Pattern pattern = c.getPattern();
                for( int i=0; i<modules.length; i++ )
                {
                    String name = modules[i].getName();
                    Matcher matcher = pattern.matcher( name );
                    boolean matches = matcher.matches();
                    //System.out.println( "# eval: " + name + ", " + matches );
                    if( matches )
                    {
                        //System.out.println( "# found: " + name );
                        list.add( modules[i] );
                    }
                }
            }
            DefaultModule[] selection = (DefaultModule[]) list.toArray( new DefaultModule[0] );
            if( criteria.length == 1 )
            {
                return selection;
            }
            else
            {
                Criteria[] set = new Criteria[ criteria.length -1 ];
                System.arraycopy( criteria, 1, set, 0, ( criteria.length -1 ) );
                ArrayList collection = new ArrayList();
                for( int i=0; i<selection.length; i++ )
                {
                    DefaultModule module = selection[i];
                    Model[] models = module.select( set );
                    for( int j=0; j<models.length; j++ )
                    {
                        collection.add( models[j] );
                    }
                }
                return (Model[]) collection.toArray( new Model[0] );
            }
        }
        else
        {
            return new Model[0];
        }
    }
    
    static class Criteria
    {
        boolean m_recursive;
        Pattern m_pattern;
        public Criteria( boolean recursive, Pattern pattern )
        {
            m_recursive = recursive;
            m_pattern = pattern;
        }
        public boolean isRecursive()
        {
            return m_recursive;
        }
        public Pattern getPattern()
        {
            return m_pattern;
        }
    }
    
    public Model lookup( File file ) 
      throws ProjectNotFoundException, ResourceNotFoundException, ModuleNotFoundException, ModelNotFoundException
    {
        DefaultProject[] projects = getAllRegisteredProjects( false );
        for( int i=0; i<projects.length; i++ )
        {
            DefaultProject project = projects[i];
            File base = project.getBase();
            if( file.equals( base ) )
            {
                return project;
            }
        }
        DefaultModule[] modules = getAllDefaultModules();
        for( int i=0; i<modules.length; i++ )
        {
            DefaultModule module = modules[i];
            File base = module.getBase();
            if( file.equals( base ) )
            {
                return module;
            }
        }
        throw new ModelNotFoundException( file );
    }
    
    public TypeDescriptor[] getTypeDescriptors()
    {
        return m_directive.getTypeDescriptors();
    }
    
    public TypeDescriptor getTypeDescriptor( String type ) throws TypeNotFoundException
    {
        TypeDescriptor[] types = getTypeDescriptors();
        for( int i=0; i<types.length; i++ )
        {
            TypeDescriptor descriptor = types[i];
            if( type.equals( descriptor.getName() ) )
            {
                return descriptor;
            }
        }
        throw new TypeNotFoundException( type );
    }

    public long getLastModified()
    {
        return m_source.lastModified();
    }
    
   /**
    * Return a sorted array of all projects within the library.
    * @return the sorted project array
    */
    public Project[] getAllProjects()
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        return getAllRegisteredProjects( true );
    }
    
   /**
    * Return an array of all module within the library.
    * @return the module array
    */
    public Module[] getAllModules()
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        return getAllDefaultModules();
    }
    
   /**
    * Return a sorted array of projects including the dependent project of the 
    * suplied target project.
    * @param project the target project
    * @param providers if TRUE sort in provider first order else consumer first
    * @return the sorted project array
    */
    public Project[] getProjectChain( Project project, boolean providers )
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        DefaultProject p = (DefaultProject) project;
        return sortProjects( new DefaultProject[]{ p }, providers );
    }
    
   /**
    * Return an array of top-level modules registered with the library.
    * @return the module array
    */
    public Module[] getModules()
    {
        return getDefaultModules();
    }
    
   /**
    * Get a named module.
    * @param path the module address
    * @exception ModuleNotFoundException if the address is not resolvable
    */
    public Module getModule( String path ) throws ModuleNotFoundException
    {
        return getDefaultModule( path );
    }
    
   /**
    * Get a named project.
    * @param path the project address
    * @exception ModuleNotFoundException if the address is not resolvable
    * @exception ProjectNotFoundException if the address is not resolvable
    */
    public Project getProject( String path ) throws ModuleNotFoundException, ProjectNotFoundException
    {
        return getDefaultProject( path );
    }
    
   /**
    * Get a named resource.
    * @param path the resource address
    * @exception ModuleNotFoundException if the address is not resolvable
    * @exception ResourceNotFoundException if the address is not resolvable
    */
    public Resource getResource( String path ) throws ModuleNotFoundException, ResourceNotFoundException
    {
        return getDefaultResource( path );
    }
    
    DefaultModule getDefaultModule( String path ) throws ModuleNotFoundException
    {
        int n = path.indexOf( "/" );
        if( n > 0 )
        {
            String pre = path.substring( 0, n );
            String post = path.substring( n+1 );
            DefaultModule module = getDefaultModule( pre );
            return module.getDefaultModule( post );
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

    DefaultProject getDefaultProject( String path ) throws ModuleNotFoundException, ProjectNotFoundException
    {
        int n = path.lastIndexOf( "/" );
        if( n > 0 )
        {
            String pre = path.substring( 0, n );
            String post = path.substring( n+1 );
            DefaultModule module = getDefaultModule( pre );
            return module.getDefaultProject( post );
        }
        else
        {
            throw new ProjectNotFoundException( null, path );
        }
    }
    
    DefaultResource getDefaultResource( String path ) throws ModuleNotFoundException, ResourceNotFoundException
    {
        int n = path.lastIndexOf( "/" );
        if( n > 0 )
        {
            String pre = path.substring( 0, n );
            String post = path.substring( n+1 );
            DefaultModule module = getDefaultModule( pre );
            return module.getDefaultResource( post );
        }
        else
        {
            final String error =
              "Resource address does not include a module name [" + path + "].";
            throw new IllegalAddressRuntimeException( error );
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
                resources[i] = module.resolveDefaultResource( value );
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
    
    DefaultModule[] getDefaultModules()
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
            DefaultModule module = getDefaultModule( moduleName );
            String key = value.substring( n+1 );
            return module.resolveDefaultResource( key );
        }
    }
    
    DefaultModule installModule( URI uri ) throws Exception
    {
        if( Artifact.isRecognized( uri ) )
        {
            URL url = Artifact.createArtifact( uri ).toURL();
            InputStream input = url.openStream();
            ModuleDirective directive = LibraryDirectiveBuilder.buildModuleDirective( input );
            return install( m_root, directive );
        }
        else
        {
            final String error = 
              "URI type not supported: " + uri;
            throw new UnsupportedOperationException( error );
        }
    }
    
    DefaultModule installLocalModule( File anchor, String path ) throws Exception
    {
        File file = new File( anchor, path );
        getLogger().debug( "loading local module: " + file );
        ModuleDirective directive= LibraryDirectiveBuilder.buildModuleDirective( file );
        File parent = file.getParentFile();
        return install( parent, directive );
    }
    
    DefaultModule install( File anchor, ModuleDirective directive ) throws Exception
    {
        String name = directive.getName();
        if( !m_modules.containsKey( name ) )
        {
            DefaultModule module = new DefaultModule( this, null, directive, anchor );
            m_modules.put( name, module );
            module.init( this, anchor );
            return module;
        }
        else
        {
            return getDefaultModule( name );
        }
    }
    
    File getRootDirectory()
    {
        return m_root;
    }
    
    DefaultProject[] sortProjects( DefaultProject[] projects, boolean policy )
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        ArrayList stack = new ArrayList();
        ArrayList visited = new ArrayList();
        for( int i=0; i<projects.length; i++ )
        {
            DefaultProject project = projects[i];
            processProject( visited, stack, project, policy, projects );
        }
        return (DefaultProject[]) stack.toArray( new DefaultProject[0] );
    }
    
    DefaultProject[] getDecendentProjects( DefaultProject project, DefaultProject[] collection ) 
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        DefaultResource resource = project.toLocalResource();
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
    
    Properties getProperties()
    {
        return m_directive.getProperties();
    }
    
   /**
    * Return a sorted array of all projects within the library.
    * @return the sorted project array
    */
    DefaultProject[] getAllRegisteredProjects( boolean sorted )
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        ArrayList list = new ArrayList();
        DefaultModule[] modules = getDefaultModules();
        for( int i=0; i<modules.length; i++ )
        {
            aggregateProjects( list, modules[i] );
        }
        DefaultProject[] projects = (DefaultProject[]) list.toArray( new DefaultProject[0] );
        if( sorted )
        {
            return sortProjects( projects, true );
        }
        else
        {
            return projects;
        }
    }

   /**
    * Return an array of all modules within the library.
    * @return the module array
    */
    DefaultModule[] getAllDefaultModules()
    {
        try
        {
            ArrayList list = new ArrayList();
            DefaultModule[] modules = getDefaultModules();
            for( int i=0; i<modules.length; i++ )
            {
                aggregateModules( list, modules[i] );
            }
            return (DefaultModule[]) list.toArray( new DefaultModule[0] );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while attempting to build module list.";
            throw new ModelRuntimeException( error );
        }
    }
    
    private void aggregateModules( ArrayList list, DefaultModule module )
    {
        list.add( module );
        DefaultModule[] modules = module.getDefaultModules();
        for( int i=0; i<modules.length; i++ )
        {
            aggregateModules( list, modules[i] );
        }
    }
    
    private void aggregateProjects( ArrayList list, DefaultModule module )
    {
        DefaultProject[] projects = module.getDefaultProjects();
        for( int i=0; i<projects.length; i++ )
        {
            list.add( projects[i] );
        }
        DefaultModule[] modules = module.getDefaultModules();
        for( int i=0; i<modules.length; i++ )
        {
            aggregateProjects( list, modules[i] );
        }
    }
    
    private DefaultProject[] getProviderProjects( DefaultProject project ) 
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        ArrayList list = new ArrayList();
        DefaultResource[] resources = project.getProviderResources();
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            DefaultProject p = resource.getDefaultProject();
            if( null != p )
            {
                list.add( p );
            }
        }
        return (DefaultProject[]) list.toArray( new DefaultProject[0] );
    }
    
    private void processProject( 
      ArrayList visited, ArrayList stack, DefaultProject project, boolean policy, DefaultProject[] collection ) 
      throws ResourceNotFoundException, ModuleNotFoundException
    {
        if( visited.contains( project ) )
        {
            return;
        }
        visited.add( project );
        if( policy )
        {
            DefaultProject[] projects = getProviderProjects( project );
            for( int i=0; i<projects.length; i++ )
            {
                DefaultProject p = projects[i];
                if( isaMember( collection, p ) )
                {
                    processProject( visited, stack, p, policy, collection );
                }
            }
            stack.add( project );
        }
        else
        {
            stack.add( project );
            DefaultProject[] projects = project.getConsumerProjects( collection );
            for( int i=0; i<projects.length; i++ )
            {
                DefaultProject p = projects[i];
                if( isaMember( collection, p ) )
                {
                    processProject( visited, stack, p, policy, collection );
                }
            }
        }
    }
    
    private boolean isaMember( DefaultProject[] projects, DefaultProject project )
    {
        for( int i=0; i<projects.length; i++ )
        {
            DefaultProject p = projects[i];
            if( project.equals( p ) )
            {
                return true;
            }
        }
        return false;
    }
    
    private Logger getLogger()
    {
        return m_logger;
    }

    //----------------------------------------------------------------------------
    // static utilities
    //----------------------------------------------------------------------------
    
    private static File resolveLibrarySource() throws FileNotFoundException
    {
        String path = System.getProperty( "user.dir" );
        File dir = new File( path );
        return resolveLibrarySource( dir );
    }
    
    private static File resolveLibrarySource( File dir ) throws FileNotFoundException
    {
        if( dir.isFile() )
        {
            throw new IllegalArgumentException( "not-a-directory" );
        }
        else
        {
            File file = new File( dir, "library.xml" );
            if( file.isFile() && file.exists() )
            {
                return file;
            }
            else
            {
                File parent = dir.getParentFile();
                if( parent != null )
                {
                    return resolveLibrarySource( parent );
                }
            }
        }
        throw new FileNotFoundException( "library.xml" );
    }
}

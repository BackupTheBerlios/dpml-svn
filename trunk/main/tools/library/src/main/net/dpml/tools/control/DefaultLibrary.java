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
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import net.dpml.tools.info.IncludeDirective;
import net.dpml.tools.info.LibraryDirective;
import net.dpml.tools.info.ModuleDirective;
import net.dpml.tools.info.ImportDirective;
import net.dpml.tools.info.Scope;
import net.dpml.tools.info.ProcessDescriptor;
import net.dpml.tools.model.ProcessorNotFoundException;
import net.dpml.tools.model.Module;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Processor;
import net.dpml.tools.model.Library;
import net.dpml.tools.model.Type;
import net.dpml.tools.model.ModuleNotFoundException;
import net.dpml.tools.model.ResourceNotFoundException;
import net.dpml.tools.model.ReferentialException;
import net.dpml.tools.model.DuplicateNameException;

import net.dpml.transit.Artifact;
import net.dpml.transit.Logger;
import net.dpml.transit.util.ElementHelper;

import org.w3c.dom.Element;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class DefaultLibrary extends DefaultDictionary implements Library
{
    private final LibraryDirective m_directive;
    private final DefaultProcessor[] m_processes;
    private final DefaultModule m_module;
    private final File m_root;
    private final Logger m_logger;
    
    public DefaultLibrary( Logger logger ) throws Exception
    {
        this( logger, resolveLibrarySource() );
    }
    
    public DefaultLibrary( Logger logger, File source ) throws Exception
    {
        super( LibraryDirectiveBuilder.build( source ) );
        
        if( null == logger )
        {
            throw new NullPointerException( "logger" );
        }
        if( null == source )
        {
            throw new NullPointerException( "source" );
        }
        
        m_logger = logger;
        m_directive = (LibraryDirective) super.getAbstractDirective();
        m_root = source.getParentFile().getCanonicalFile();
        
        // setup the processors
        
        ProcessDescriptor[] processDescriptors = m_directive.getProcessDescriptors();
        m_processes = new DefaultProcessor[ processDescriptors.length ];
        for( int i=0; i<processDescriptors.length; i++ )
        {
            ProcessDescriptor processDescriptor = processDescriptors[i];
            m_processes[i] = new DefaultProcessor( processDescriptor );
        }
        
        // handle expansion of import directives 
        
        getLogger().debug( "loaded root module: " + m_root );
        System.setProperty( "dpml.library.basedir", m_root.toString() );
        ImportDirective[] imports = m_directive.getImportDirectives();
        ModuleDirective[] moduleDirectives = new ModuleDirective[ imports.length ];
        for( int i=0; i<imports.length; i++ )
        {
            ImportDirective include = imports[i];
            ImportDirective.Mode mode = include.getMode();
            if( ImportDirective.Mode.FILE.equals( mode ) )
            {
                String path = include.getValue();
                File file = new File( m_root, path );
                getLogger().debug( "loading local module: " + file );
                moduleDirectives[i] = LibraryDirectiveBuilder.buildModuleDirective( file );
            }
            else
            {
                String path = include.getValue();
                URI uri = new URI( path );
                URL url = Artifact.createArtifact( uri ).toURL();
                InputStream input = url.openStream();
                moduleDirectives[i] = LibraryDirectiveBuilder.buildModuleDirective( input );
            }
        }
        
        // create the top-level modules
        
        DefaultModule[] modules = new DefaultModule[ moduleDirectives.length ];
        m_module = new DefaultModule( this, m_directive, modules );
        for( int i=0; i<moduleDirectives.length; i++ )
        {
            ModuleDirective moduleDirective = moduleDirectives[i];
            modules[i] = new DefaultModule( this, m_module, moduleDirective );
        }
    }
    
    //----------------------------------------------------------------------------
    // Library
    //----------------------------------------------------------------------------
    
   /**
    * Utility operation to sort a collection of resources.
    * @param resources the resources to sort
    * @return the sorted resource array
    */
    public Resource[] sort( Resource[] resources )
    {
        DefaultResource[] array = new DefaultResource[ resources.length ];
        for( int i=0; i<resources.length; i++ )
        {
            array[i] = (DefaultResource) resources[i];
        }
        return m_module.sortDefaultResources( array, Scope.TEST );
    }
    
   /**
    * Return an array of all registered processes.
    * @return the processor array
    */
    public Processor[] getProcessors()
    {
        return m_processes;
    }
    
   /**
    * Return the sequence of process defintions supporting production of a 
    * supplied resource.  The implementation constructs a sequence of process
    * instances based on the types declared by the resource combined with 
    * dependencies declared by respective process defintions. Clients may
    * safely invoke processes sequentially relative to the returned process
    * sequence.
    * 
    * @param resource the resource to be produced
    * @return a sorted array of processor definitions supporting resource production
    */
    public Processor[] getProcessorSequence( Resource resource ) throws ProcessorNotFoundException
    {
        Type[] types = resource.getTypes();
        try
        {
            return getDefaultProcessorSequence( types );
        }
        catch( InvalidProcessorNameException e )
        {
            final String error = 
              "Internal error due to an invalid processor name ["
              + e.getMessage()
              + "] in the resource ["
              + resource 
              + "].";
            throw new IllegalStateException( e.getMessage() );
        }
    }

   /**
    * Return the processor defintions matching a supplied type.  
    * 
    * @param type the type declaration
    * @return the processor definition
    * @exception ProcessorNotFoundException if no processor is registered 
    *   for the supplied type
    */
    public Processor getProcessor( Type type ) throws ProcessorNotFoundException
    {
        try
        {
            return getDefaultProcessor( type );
        }
        catch( InvalidProcessorNameException e )
        {
            final String message = e.getMessage();
            throw new ProcessorNotFoundException( message );
        }
    }
    
   /**
    * Return a array of the top-level modules within the library.
    * @return module array
    */
    public Module[] getModules()
    {
        return m_module.getModules();
    }
    
   /**
    * Return a array of all modules in the library.
    * @return module array
    */
    public Module[] getAllModules()
    {
        return m_module.getAllModules();
    }
    
   /**
    * Return a named module.
    * @param ref the fully qualified module name
    * @return the module
    * @exception ModuleNotFoundException if the module cannot be found
    */
    public Module getModule( String ref ) throws ModuleNotFoundException
    {
        return m_module.getModule( ref );
    }

   /**
    * Recursively lookup a resource using a fully qualified reference.
    * @param ref the fully qualified resource name
    * @return the resource instance
    * @exception ResourceNotFoundException if the resource cannot be found
    */
    public Resource getResource( String ref ) throws ResourceNotFoundException
    {
        return m_module.getResource( ref );
    }
    
   /**
    * <p>Select a set of resource matching a supplied a resource selection 
    * constraint.  The constraint may contain the wildcards '**' and '*'.
    * @param criteria the selection criteria
    * @param sort if true the returned array will be sorted relative to dependencies
    *   otherwise the array will be sorted alphanumerically with respect to the resource
    *   path
    * @return an array of resources matching the selction criteria
    */
    public Resource[] select( String criteria, boolean sort )
    {
        return m_module.select( criteria, false, sort );
    }
    
   /**
    * <p>Select a set of resource matching a supplied a resource selection 
    * constraint.  The constraint may contain the wildcards '**' and '*'.
    * @param criteria the selection criteria
    * @param local if true restrict selection to local projects
    * @param sort if true the returned array will be sorted relative to dependencies
    *   otherwise the array will be sorted alphanumerically with respect to the resource
    *   path
    * @return an array of resources matching the selction criteria
    */
    public Resource[] select( String criteria, boolean local, boolean sort )
    {
        return m_module.select( criteria, local, sort );
    }
    
   /**
    * Select all local projects with a basedir equal to or deeper than the supplied 
    * directory.
    * @param base the reference basedir
    * @return an array of projects within or lower than the supplied basedir
    */
    public Resource[] select( File base )
    {
        String root = base.toString();
        ArrayList list = new ArrayList();
        DefaultResource[] resources = m_module.selectDefaultResources( true, "**/*" );
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            File basedir = resource.getBaseDir();
            if( null != basedir )
            {
                String path = basedir.toString();
                if( path.startsWith( root ) )
                {
                    list.add( resource );
                }
            }
            else
            {
                final String error = 
                  "Local project list returned a resource with a null basedir ["
                  + resource.getResourcePath() 
                  + "].";
                throw new IllegalStateException( error );
            }
        }
        DefaultResource[] selection = (DefaultResource[]) list.toArray( new DefaultResource[0] );
        return m_module.sortDefaultResources( selection );
    }
    
   /**
    * Locate a resource relative to a base directory.
    * @param base the base directory
    * @return a resource with a matching basedir
    * @exception ResourceNotFoundException if resource match  relative to the supplied base
    */
    public Resource locate( File base ) throws ResourceNotFoundException
    {
        return m_module.locate( base );
    }
    
    //----------------------------------------------------------------------------
    // internals
    //----------------------------------------------------------------------------
    
    File getRootDirectory()
    {
        return m_root;
    }
    
    DefaultProcessor getDefaultProcessor( Type type ) throws InvalidProcessorNameException
    {
        String id = type.getName();
        for( int i=0; i<m_processes.length; i++ )
        {
            DefaultProcessor processor = m_processes[i];
            String name = processor.getName();
            if( name.equals( id ) )
            {
                return processor;
            }
        }
        throw new InvalidProcessorNameException( id );
    }
    
    DefaultProcessor[] getDefaultProcessorSequence( Type[] types )
    {
        DefaultProcessor[] processors = new DefaultProcessor[ types.length ];
        for( int i=0; i<types.length; i++ )
        {
            Type type = types[i];
            processors[i] = getDefaultProcessor( type );
        }
        return processors;
    }
    
    String[] expandTypeNames( String[] types )
    {
        ProcessDescriptor[] descriptors = getProcessDescriptors( types );
        ProcessDescriptor[] sorted = sortProcessDescriptors( descriptors );
        String[] names = new String[ sorted.length ];
        for( int i=0; i<sorted.length; i++ )
        {
            ProcessDescriptor descriptor = sorted[i];
            names[i] = descriptor.getName();
        }
        return names;
    }
    
   /**
    * Return the array of top-level modules.
    * @return the top-level module array
    */
    DefaultModule[] getDefaultModules()
    {
        return m_module.getDefaultModules();
    }
    
   /**
    * Recursively lookup a resource using a fully qualified reference.
    * @param ref the fully qualified resource name
    * @return the resource instance
    */
    DefaultResource getDefaultResource( String ref )
    {
        return m_module.getDefaultResource( ref );
    }
    
   /**
    * Return a named module.
    * @param ref the fully qualified resource name
    * @return the module
    */
    DefaultModule getDefaultModule( String ref )
    {
        return m_module.getDefaultModule( ref );
    }
    
    //----------------------------------------------------------------------------
    // selection
    //----------------------------------------------------------------------------
    
    DefaultResource[] selectDefaultResources( String spec )
    {
        return m_module.selectDefaultResources( spec );
    }
    
    //----------------------------------------------------------------------------
    // internal ProcessDescriptor sorting
    //----------------------------------------------------------------------------
    
    private ProcessDescriptor[] getProcessDescriptors( String[] names )
      throws InvalidProcessorNameException
    {
        ProcessDescriptor[] result = new ProcessDescriptor[ names.length ];
        for( int i=0; i<names.length; i++ )
        {
            String name = names[i];
            result[i] = getProcessDescriptor( name );
        }
        return result;
    }
    
    private ProcessDescriptor getProcessDescriptor( String name ) throws InvalidProcessorNameException
    {
        ProcessDescriptor[] processes = m_directive.getProcessDescriptors();
        for( int i=0; i<processes.length; i++ )
        {
            ProcessDescriptor process = processes[i];
            if( process.getName().equals( name ) )
            {
                return process;
            }
        }
        throw new InvalidProcessorNameException( name );
    }
    
    private ProcessDescriptor[] getProcessDescriptors()
    {
        return m_directive.getProcessDescriptors();
    }
    
    private ProcessDescriptor[] sortProcessDescriptors( ProcessDescriptor[] descriptors )
    {
        ArrayList visited = new ArrayList();
        ArrayList stack = new ArrayList();
        for( int i=0; i<descriptors.length; i++ )
        {
            ProcessDescriptor descriptor = descriptors[i];
            processProcessDescriptor( visited, stack, descriptor );
        }
        return (ProcessDescriptor[]) stack.toArray( new ProcessDescriptor[0] );
    }
    
    private void processProcessDescriptor( 
      List visited, List stack, ProcessDescriptor descriptor )
    {
        if( visited.contains( descriptor ) )
        {
            return;
        }
        else
        {
            visited.add( descriptor );
            String[] deps = descriptor.getDependencies();
            ProcessDescriptor[] providers = getProcessDescriptors( deps );
            for( int i=0; i<providers.length; i++ )
            {
                processProcessDescriptor( visited, stack, providers[i] );
            }
            stack.add( descriptor );
        }
    }
    
    //----------------------------------------------------------------------------
    // other internals
    //----------------------------------------------------------------------------
    
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

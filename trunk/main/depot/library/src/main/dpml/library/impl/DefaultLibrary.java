/*
 * Copyright 2005-2006 Stephen J. McConnell
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

package dpml.library.impl;

import dpml.library.info.LibraryDecoder;
import dpml.library.info.ImportDirective;
import dpml.library.info.LibraryDirective;
import dpml.library.info.ModuleDirective;
import dpml.library.info.ResourceDirective;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Hashtable;

import dpml.library.Scope;
import dpml.library.Library;
import dpml.library.Module;
import dpml.library.ModuleNotFoundException;
import dpml.library.Resource;
import dpml.library.ResourceNotFoundException;

import net.dpml.transit.Artifact;

import net.dpml.util.Logger;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class DefaultLibrary extends DefaultDictionary implements Library
{
    private static final LibraryDecoder LIBRARY_DECODER = new LibraryDecoder();
    
    private final LibraryDirective m_directive;
    private final DefaultModule m_module;
    private final File m_root;
    private final Logger m_logger;
    private final Hashtable<String,DefaultResource> m_anonymous = 
      new Hashtable<String,DefaultResource>();
    
    private static LibraryDirective buildLibraryDirective( File source ) throws Exception
    {
        return LIBRARY_DECODER.build( source );
    }
    
   /**
    * Creation of a new library.  The definition of the indexwill 
    * be resolved by search up the file system for a file named index.xml.
    * @param logger the assigned logging channel
    * @exception Exception if an error occurs during defintion loading
    */
    public DefaultLibrary( Logger logger ) throws Exception
    {
        this( logger, resolveLibrarySource() );
    }
    
   /**
    * Creation of a new library.
    * @param logger the assigned logging channel
    * @param source the index source defintion
    * @exception Exception if an error occurs during defintion loading
    */
    public DefaultLibrary( Logger logger, File source ) throws Exception
    {   
        super( null, buildLibraryDirective( source ) );
        
        if( null == logger )
        {
            throw new NullPointerException( "logger" );
        }
        
        m_logger = logger;
        m_directive = (LibraryDirective) super.getAbstractDirective();
        m_root = source.getParentFile().getCanonicalFile();
        m_module = new DefaultModule( this, m_directive );
        
        getLogger().debug( "loaded root module: " + m_root );
        System.setProperty( "dpml.library.basedir", m_root.toString() );
        
        // handle expansion of import directives 
        
        ImportDirective[] imports = m_directive.getImportDirectives();
        ModuleDirective[] importModuleDirectives = new ModuleDirective[ imports.length ];
        for( int i=0; i<imports.length; i++ )
        {
            ImportDirective include = imports[i];
            ImportDirective.Mode mode = include.getMode();
            if( ImportDirective.Mode.FILE.equals( mode ) )
            {
                throw new UnsupportedOperationException( "file" );
            }
            else
            {
                String path = include.getValue();
                URI uri = new URI( path );
                getLogger().debug( "loading external import: " + uri );
                ResourceDirective resource = LIBRARY_DECODER.buildResource( uri );
                if( resource instanceof ModuleDirective )
                {
                    ModuleDirective moduleDirective = (ModuleDirective) resource;
                    importModuleDirectives[i] = moduleDirective;
                }
                else
                {
                    final String error = 
                      "Not yet equipped to import resource of the type [" 
                      + resource.getClass().getName() 
                      + ".";
                    throw new IllegalArgumentException( error );
                } 
            }
        }
        
        for( int i=0; i<importModuleDirectives.length; i++ )
        {
            ModuleDirective importModuleDirective = importModuleDirectives[i];
            m_module.addResource( importModuleDirective );
        }
        
        // create the top-level modules
        
        ArrayList<ResourceDirective> primaryDirectives = new ArrayList<ResourceDirective>();
        ResourceDirective[] directives = m_directive.getResourceDirectives();
        for( int i=0; i<directives.length; i++ )
        {
            ResourceDirective directive = directives[i];
            primaryDirectives.add( directive );
        }
        ResourceDirective[] values = primaryDirectives.toArray( new ResourceDirective[0] );
        for( int i=0; i<values.length; i++ )
        {
            ResourceDirective d = values[i];
            m_module.addResource( d );
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
    * Return an array of the top-level modules within the library.
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
        try
        {
            return m_module.getResource( ref );
        }
        catch( InvalidNameException e )
        {
            throw new ResourceNotFoundException( e.getMessage(), e.getCause() );
        }
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
        return select( base, true );
    }

   /**
    * Select all local projects relative to the supplied basedir.
    * @param base the reference basedir
    * @param self if true and the basedir resolves to a project then include the project
    *   otherwise the project will be expluded from selection
    * @return an array of projects relative to the basedir
    */
    public Resource[] select( final File base, boolean self )
    {
        String root = base.toString();
        ArrayList<DefaultResource> list = new ArrayList<DefaultResource>();
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
                    if( path.equals( root ) )
                    {
                        if( self )
                        {
                            list.add( resource );
                        }
                    }
                    else
                    {
                        list.add( resource );
                    }
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
        DefaultResource[] selection = list.toArray( new DefaultResource[0] );
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
    // Dictionary
    //----------------------------------------------------------------------------
    
   /**
    * Return the property names associated with the dictionary.
    * @return the array of property names
    */
    public String[] getPropertyNames()
    {
        return m_module.getPropertyNames();
    }
    
   /**
    * Return a property value.
    * @param key the property key
    * @return the property value
    */
    public String getProperty( String key )
    {
        return m_module.getProperty( key );
    }
    
   /**
    * Return a property value.
    * @param key the property key
    * @param value the default value
    * @return the property value
    */
    public String getProperty( String key, String value )
    {
        return m_module.getProperty( key, value );
    }
    //----------------------------------------------------------------------------
    // internals
    //----------------------------------------------------------------------------
    
   /**
    * Construct a new locally referenced anonymouse resource.
    * @param urn the resource urn
    * @param properties properties assigned to the resource
    * @return the resource
    * @exception URISyntaxException if the include urn is invaid
    */
    DefaultResource getAnonymousResource( String urn, Properties properties ) 
    throws URISyntaxException
    {
        if( m_anonymous.containsKey( urn ) )
        {
            return m_anonymous.get( urn );
        }
        
        Artifact artifact = Artifact.createArtifact( urn );
        String scheme = artifact.getScheme();
        String group = artifact.getGroup();
        String name = artifact.getName();
        String version = artifact.getVersion();
        String type = artifact.getType();
        
        ResourceDirective resourceDirective = 
          ResourceDirective.createAnonymousResource( scheme, name, version, type, properties );

        ModuleDirective enclosing = null;
        String[] elements = group.split( "/", -1 );
        for( int i = ( elements.length-1 ); i>-1; i-- )
        {
            String elem = elements[i];
            if( i == ( elements.length-1 ) )
            {
                enclosing = new ModuleDirective( elem, version, resourceDirective );
            }
            else
            {
                enclosing = new ModuleDirective( elem, version, enclosing );
            }
        }
        try
        {
            DefaultModule root = new DefaultModule( this, m_directive );
            root.addResource( enclosing );
            DefaultResource resource =  root.getDefaultResource( group + "/" + name );
            m_anonymous.put( urn, resource );
            return resource;
        }
        catch( Exception e )
        {
            final String error = 
              "Internal error while creating an ANONYMOUS resource: "
              + urn
              + "].";
            throw new RuntimeException( error, e );
        }
    }
    
    File getRootDirectory()
    {
        return m_root;
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
            File file = new File( dir, INDEX_FILENAME );
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
        throw new FileNotFoundException( "index.xml" );
    }
}

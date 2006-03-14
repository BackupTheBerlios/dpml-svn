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

package net.dpml.library.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Hashtable;

import net.dpml.library.info.ImportDirective;
import net.dpml.library.info.IncludeDirective;
import net.dpml.library.info.LibraryDirective;
import net.dpml.library.info.ModuleDirective;
import net.dpml.library.info.ResourceDirective;
import net.dpml.library.info.Scope;
import net.dpml.library.model.Library;
import net.dpml.library.model.Module;
import net.dpml.library.model.ModuleNotFoundException;
import net.dpml.library.model.Resource;
import net.dpml.library.model.ResourceNotFoundException;

import net.dpml.transit.Artifact;
import net.dpml.transit.Logger;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class DefaultLibrary extends DefaultDictionary implements Library
{
    private static final LibraryBuilder BUILDER = new LibraryBuilder();
    
    private final LibraryDirective m_directive;
    private final DefaultModule m_module;
    private final DefaultModule m_imports;
    private final File m_root;
    private final Logger m_logger;
    private final Hashtable m_anonymous = new Hashtable();
    
    private static LibraryDirective buildLibraryDirective( File source ) throws Exception
    {
        LibraryBuilder builder = new LibraryBuilder();
        return builder.build( source );
    }
    
   /**
    * Creation of a new library.  The definition of the library will 
    * be resolved by search up the file system for a file named library.xml.
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
    * @param source the library source defintion
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
                ResourceDirective resource = BUILDER.buildResource( uri );
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
        DefaultModule[] importModules = new DefaultModule[ importModuleDirectives.length ];
        m_imports = new DefaultModule( this, m_directive, importModules );
        for( int i=0; i<importModuleDirectives.length; i++ )
        {
            ModuleDirective importModuleDirective = importModuleDirectives[i];
            importModules[i] = new DefaultModule( this, m_imports, importModuleDirective );
        }
        
        // create the top-level modules
        
        ArrayList moduleDirectives = new ArrayList();
        ResourceDirective[] directives = m_directive.getResourceDirectives();
        for( int i=0; i<directives.length; i++ )
        {
            ResourceDirective directive = directives[i];
            if( directive instanceof ModuleDirective )
            {
                ModuleDirective md = (ModuleDirective) directive;
                moduleDirectives.add( md );
            }
            else
            {
                final String error = 
                  "No support in place for non-module top-level resources.";
                throw new IllegalArgumentException( error );
            }
        }
        ModuleDirective[] values = (ModuleDirective[]) moduleDirectives.toArray( new ModuleDirective[0] );
        DefaultModule[] modules = new DefaultModule[ values.length ];
        m_module = new DefaultModule( this, m_directive, modules );
        for( int i=0; i<modules.length; i++ )
        {
            ModuleDirective md = values[i];
            modules[i] = new DefaultModule( this, m_module, md );
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
        try
        {
            return m_module.getModule( ref );
        }
        catch( ModuleNotFoundException e )
        {
            return m_imports.getModule( ref );
        }
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
        catch( ResourceNotFoundException e )
        {
            return m_imports.getResource( ref );
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
    * @param include the dependency include
    * @return the resource
    * @exception IllegalArgumentException if the include mode is not URN mode
    * @exception URISyntaxException if the include urn is invaid
    */
    DefaultResource getAnonymousResource( IncludeDirective include ) 
    throws URISyntaxException
    {
        if( !include.getMode().equals( IncludeDirective.URN ) )
        {
            throw new IllegalArgumentException( 
              "Invalid include mode: " 
              + include.getMode() );
        }
        
        String urn = include.getValue();
        if( m_anonymous.containsKey( urn ) )
        {
            return (DefaultResource) m_anonymous.get( urn );
        }
        
        Properties properties = include.getProperties();
        Artifact artifact = Artifact.createArtifact( urn );
        String group = artifact.getGroup();
        String name = artifact.getName();
        String version = artifact.getVersion();
        String type = artifact.getType();
        
        ResourceDirective resourceDirective = 
          new ResourceDirective( name, version, type, properties );
        ModuleDirective enclosing = null;
        String[] elements = group.split( "/", -1 );
        for( int i = ( elements.length-1 ); i>-1; i-- )
        {
            String elem = elements[i];
            if( i == ( elements.length-1 ) )
            {
                enclosing = new ModuleDirective( elem, resourceDirective );
            }
            else
            {
                enclosing = new ModuleDirective( elem, enclosing );
            }
        }
        try
        {
            DefaultModule module = new DefaultModule( this, m_module, enclosing );
            DefaultModule root = new DefaultModule( this, m_directive, new DefaultModule[]{module} );
            DefaultResource resource =  root.getDefaultResource( group + "/" + name );
            m_anonymous.put( urn, resource );
            return resource;
        }
        catch( Exception pnfe )
        {
            final String error = 
              "Internal error while creating an ANONYMOUS resource: "
              + urn
              + "].";
            throw new RuntimeException( error, pnfe );
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
        try
        {
            return m_module.getDefaultResource( ref );
        }
        catch( InvalidNameException e )
        {
            return m_imports.getDefaultResource( ref );
        }
    }
    
   /**
    * Return a named module.
    * @param ref the fully qualified resource name
    * @return the module
    */
    DefaultModule getDefaultModule( String ref )
    {
        try
        {
            return m_module.getDefaultModule( ref );
        }
        catch( InvalidNameException e )
        {
            return m_imports.getDefaultModule( ref );
        }
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

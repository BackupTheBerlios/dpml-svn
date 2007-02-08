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

import dpml.library.info.AbstractDirective;
import dpml.library.info.ModuleDirective;
import dpml.library.info.ResourceDirective;
import dpml.library.info.DependencyDirective;
import dpml.library.info.TypeDirective;
import dpml.library.info.InfoDirective;
import dpml.library.info.LibraryEncoder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Map;

import dpml.library.Scope;
import dpml.library.Classifier;
import dpml.library.Module;
import dpml.library.Resource;
import dpml.library.ResourceNotFoundException;
import dpml.library.ModuleNotFoundException;

import dpml.util.Category;
import net.dpml.lang.DuplicateKeyException;

/**
 * A Module is a collection of resources.  It serves to establish a 
 * namespace and a framework for sharing properties and characteristics 
 * defined within within the module with resources contained within the
 * module.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class DefaultModule extends DefaultResource implements Module
{
    private final boolean m_root;
    private final ModuleDirective m_directive;
    private final Map<String,DefaultResource> m_map = new Hashtable<String,DefaultResource>();
    
   /**
    * Constructor used by the library to create a virtual root module os shared 
    * resource references.
    * @param library the library
    * @param directive the directive from which common properties are established
    */
    DefaultModule( final DefaultLibrary library, final AbstractDirective directive ) 
    {
        super( library, directive );
        
        m_root = true;
        m_directive = null;
    }
    
   /**
    * Creation of a new nested module.
    * @param library the library
    * @param module the parent module
    * @param directive the directive from which common properties are established
    */
    DefaultModule( 
      final DefaultLibrary library, final DefaultModule module, final ModuleDirective directive ) 
      throws DuplicateKeyException
    {
        super( library, module, directive );
        
        m_root = false;
        m_directive = directive;
        ResourceDirective[] directives = directive.getResourceDirectives();
        for( int i=0; i<directives.length; i++ )
        {
            ResourceDirective res = directives[i];
            addResource( res ); 
        }
    }
    
   /**
    * Add a resource to the module.
    * @param directive the resource directive to add to the module
    * @throws DuplicateKeyException if a resource name is already bound
    */
    DefaultResource addResource( final ResourceDirective directive ) throws DuplicateKeyException
    {
        if( null == directive )
        {
            throw new NullPointerException( "directive" );
        }
        
        synchronized( m_map )
        {
            String key = directive.getName();
            if( m_map.containsKey( key ) )
            {
                if( directive instanceof ModuleDirective )
                {
                    DefaultModule module = (DefaultModule) m_map.get( key );
                    
                    // check basedir values
                    
                    if( null != directive.getBasedir() )
                    {
                        if( null != module.getBaseDir() )
                        {
                            File base = new File( getBaseDir(), directive.getBasedir() );
                            if( !module.getBaseDir().equals( base ) )
                            {
                                final String error = 
                                  "Cannot merge modules with different base directories."
                                  + "\nModule: " + module
                                  + "\nPrimary base: " + module.getBaseDir()
                                  + "\nSecondary base: " + base;
                                throw new IllegalStateException( error );
                            }
                        }
                    }
                    
                    // check versions
                    
                    if( null != directive.getVersion() )
                    {
                        if( module.getVersion().equals( directive.getVersion() ) )
                        {
                            final String error = 
                              "Cannot merge modules with different versions."
                              + "\nModule: " + module
                              + "\nPrimary version: " + module.getVersion()
                              + "\nSecondary version: " + directive.getVersion();
                            throw new IllegalStateException( error );
                        }
                    }
                    
                    // check types
                    
                    if( directive.getTypeDirectives().length > 0 ) 
                    {
                        final String error = 
                          "Cannot merge a module with type production directives."
                          + "\nModule: " + module;
                        throw new IllegalStateException( error );
                    }
                    
                    // check dependencies
                    
                    if( directive.getDependencyDirectives().length > 0 ) 
                    {
                        final String error = 
                          "Cannot merge a module with dependency directives."
                          + "\nModule: " + module;
                        throw new IllegalStateException( error );
                    }
                    
                    // add additional resources
                    
                    ModuleDirective d = (ModuleDirective) directive;
                    ResourceDirective[] resources = d.getResourceDirectives();
                    for( int i=0; i<resources.length; i++ )
                    {
                        ResourceDirective r = resources[i];
                        module.addResource( r );
                    }
                    return module;
                }
                else
                {
                    throw new DuplicateKeyException( key );
                }
            }
            else
            {
                DefaultLibrary library = getDefaultLibrary();
                if( directive instanceof ModuleDirective )
                {
                    ModuleDirective d = (ModuleDirective) directive;
                    DefaultModule module = new DefaultModule( library, this, d );
                    m_map.put( key, module );
                    return module;
                }
                else
                {
                    DefaultResource resource = new DefaultResource( library, this, directive );
                    m_map.put( key, resource );
                    return resource;
                }
            }
        }
    }
    
    //----------------------------------------------------------------------------
    // Module
    //----------------------------------------------------------------------------
    
   /**
    * Return an array of immediate resources contained within the
    * module.
    * @return the resource array
    */
    public Resource[] getResources()
    {
        return getDefaultResources();
    }
    
   /**
    * Return a resource using a supplied name.
    * @param ref a path relative to the module
    * @return the resource array
    * @exception ResourceNotFoundException if the resource does not exist
    */
    public Resource getResource( final String ref ) throws ResourceNotFoundException
    {
        try
        {
            return getDefaultResource( ref );
        }
        catch( InvalidNameException e )
        {
            final String error = 
              "Resource reference ["
              + ref
              + "] is undefined.";
            throw new ResourceNotFoundException( error, e );
        }
    }
    
   /**
    * Return the array of modules that are direct children of this module.
    * @return the child modules
    */
    public Module[] getModules()
    {
        return getDefaultModules();
    }
    
   /**
    * Return a module using a supplied reference.
    * @param ref a path relative to the module
    * @return the module array
    * @exception ModuleNotFoundException if the module does not exist
    */
    public Module getModule( final String ref ) throws ModuleNotFoundException
    {
        try
        {
            return getDefaultModule( ref );
        }
        catch( InvalidNameException e )
        {
            final String error = 
              "Cannot locate module [" 
              + ref
              + "] within module ["
              + getResourcePath()
              + "]";
            throw new ModuleNotFoundException( error, e );
        }
    }
    
   /**
    * Return the array of modules that are descendants of this module.
    * @return the descendants module array
    */
    public Module[] getAllModules()
    {
        return getAllDefaultModules();
    }
    
   /**
    * <p>Select a set of resource matching a supplied a resource selection 
    * constraint.  The constraint may contain the wildcards '**' and '*'.
    * @param criteria the selection criteria
    * @param local if true limit the selection to local projects
    * @param sort if true the returned array will be sorted relative to dependencies
    *   otherwise the array will be sorted alphanumerically
    * @return an array of resources matching the selection criteria
    */
    public Resource[] select( final String criteria, final boolean local, final boolean sort )
    {
        DefaultResource[] resources = selectDefaultResources( local, criteria );
        if( sort )
        {
            return sortDefaultResources( resources, Scope.TEST );
        }
        else
        {
            Arrays.sort( resources );
            return resources;
        }
        
    }
    
   /**
    * Locate a resource relative to a base directory.
    * @param base the base directory
    * @return a resource with a matching basedir
    * @exception ResourceNotFoundException if resource match  relative to the supplied base
    */
    public Resource locate( final File base ) throws ResourceNotFoundException
    {
        DefaultResource[] resources = selectDefaultResources( true, "**/*" );
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            File basedir = resource.getBaseDir();
            if( ( null != basedir ) && base.equals( basedir ) )
            {
                return resource;
            }
        }
        throw new ResourceNotFoundException( base.toString() );
    }
    
   /**
    * Publish the module to the output stream.
    * @param output the output stream
    */
    public void export( OutputStream output ) throws IOException
    {
        ModuleDirective directive = exportDirective();
        final LibraryEncoder encoder = new LibraryEncoder();
        encoder.export( directive, output );
    }

    public ModuleDirective exportDirective()
    {
        if( null == m_directive )
        {
            final String error = 
              "Cannot export from the virtual root.";
            throw new UnsupportedOperationException( error );
        }
        
        DefaultModule parent = getDefaultParent();
        if( null == parent )
        {
            //
            // exporting a top-level module
            //
            
            return (ModuleDirective) exportResource( this );
        }
        else
        {
            //
            // exporting the nested module
            //
            
            String path = getResourcePath();
            return exportModule( this, path );
        }
    }
    
   /**
    * Return a directive suitable for publication as an external description.
    * @param module the enclosing module
    * @return the resource directive
    * @exception IllegalArgumentException if the module is not a top-level module
    */
    ResourceDirective exportResource( final DefaultModule module ) throws IllegalArgumentException
    {
        String name = getName();
        return exportModule( module, name );
    }
    
   /**
    * Return a directive suitable for publication as an external description.
    * @param module the enclosing module
    * @return the resource directive
    * @exception IllegalArgumentException if the module is not a top-level module
    */
    ModuleDirective exportModule( final DefaultModule module, final String name ) throws IllegalArgumentException
    {
        DefaultResource[] resources = getDefaultResources();
        ResourceDirective[] directives = new ResourceDirective[ resources.length ];
        for( int i=0; i<directives.length; i++ )
        {
            DefaultResource resource = resources[i];
            directives[i] = resource.exportResource( module );
        }
        String version = getVersion();
        String basedir = null;
        InfoDirective info = m_directive.getInfoDirective();
        TypeDirective[] types = m_directive.getTypeDirectives();
        Properties properties = getExportProperties();
        return new ModuleDirective( 
          name, version, Classifier.EXTERNAL, basedir,
          info, types, new DependencyDirective[0], 
          directives, properties, null );
    }
    
    //----------------------------------------------------------------------------
    // Object
    //----------------------------------------------------------------------------
    
   /**
   * Return the string representation of the module.
   * @return the string value
   */
    public String toString()
    {
        return toString( "module" );
    }
    
    //----------------------------------------------------------------------------
    // DefaultResource (overriding)
    //----------------------------------------------------------------------------

    DefaultResource[] getLocalDefaultProviders( final Scope scope, final Category category )
    {
        DefaultResource[] local = super.getLocalDefaultProviders( scope, category );
        if( Scope.BUILD.equals( scope ) )
        {
            ArrayList<DefaultResource> stack = new ArrayList<DefaultResource>();
            for( int i=0; i<local.length; i++ )
            {
                DefaultResource resource = local[i];
                stack.add( resource );
            }
            return expandLocalDefaultProviders( stack );
        }
        else if( Scope.RUNTIME.equals( scope ) )
        {
            ArrayList<DefaultResource> stack = new ArrayList<DefaultResource>();
            for( int i=0; i<local.length; i++ )
            {
                DefaultResource resource = local[i];
                stack.add( resource );
            }
            
            DefaultResource[] resources = getDefaultResources();
            for( int i=0; i<resources.length; i++ )
            {
                DefaultResource resource = resources[i];
                if( !stack.contains( resource ) )
                {
                    stack.add( resource );
                }
            }
            return stack.toArray( new DefaultResource[0] );
        }
        else
        {
            return local;
        }
    }
    
    DefaultResource[] expandLocalDefaultProviders( final List<DefaultResource> stack )
    {
        DefaultResource[] resources = getDefaultResources();
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            if( resource instanceof DefaultModule )
            {
                DefaultModule module = (DefaultModule) resource;
                String path = module.getResourcePath();
                if( !path.startsWith( getResourcePath() ) )
                {
                    DefaultResource[] providers = module.expandLocalDefaultProviders( stack );
                    for( int j=0; j<providers.length; j++ )
                    {
                        DefaultResource r = providers[j];
                        if( !stack.contains( r ) )
                        {
                            stack.add( r );
                        }
                    }
                }
            }
            else
            {
                DefaultResource[] providers = 
                  resource.getAggregatedDefaultProviders( Scope.TEST, true, false );
                getParentModules( stack, providers );
            }
        }
        return stack.toArray( new DefaultResource[0] );
    }
    
    private void getParentModules( final List<DefaultResource> stack, final DefaultResource[] resources )
    {
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            if( !resource.isAnonymous() )
            {
                DefaultModule parent = resource.getDefaultParent();
                if( null != parent )
                {
                    String path = parent.getResourcePath();
                    if( !path.startsWith( getResourcePath() ) && !stack.contains( parent ) )
                    {
                        stack.add( parent );
                    }
                }
            }
        }
    }

    void sortDefaultResource( 
      final List<DefaultResource> visited, final List<DefaultResource> stack, 
      final Scope scope, final DefaultResource[] resources )
    {
        if( visited.contains( this ) )
        {
            return;
        }
        
        visited.add( this );
        DefaultResource[] providers = 
          getAggregatedDefaultProviders( scope, false, false );
        for( int i=0; i<providers.length; i++ )
        {
            DefaultResource provider = providers[i];
            if( isaMember( resources, provider ) )
            {
                provider.sortDefaultResource( visited, stack, scope, resources );
            }
        }

        DefaultModule[] modules = getProviderModules( scope );
        for( int i=0; i<modules.length; i++ )
        {
            DefaultResource module = modules[i];
            if( isaMember( resources, module ) )
            {
                module.sortDefaultResource( visited, stack, scope, resources );
            }
        }
        
        if( !stack.contains( this ) )
        {
            stack.add( this );
        }
    }
    
    //----------------------------------------------------------------------------
    // root semantics
    //----------------------------------------------------------------------------
    
    boolean isRoot()
    {
        return m_root;
    }
    
    //----------------------------------------------------------------------------
    // resource and module lookup
    //----------------------------------------------------------------------------
    
    DefaultResource[] getDefaultResources()
    {
        return m_map.values().toArray( new DefaultResource[0] );
    }
    
    DefaultResource getDefaultResource( final String ref )
    {
        if( null == ref )
        {
            throw new NullPointerException( "ref" );
        }
        int n = ref.indexOf( "/" );
        if( n > 0 )
        {
            String pre = ref.substring( 0, n );
            String post = ref.substring( n+1 );
            DefaultModule module = getDefaultModule( pre );
            return module.getDefaultResource( post );
        }
        else
        {
            DefaultResource[] resources = getDefaultResources();
            for( int i=0; i<resources.length; i++ )
            {
                DefaultResource resource = resources[i];
                if( ref.equals( resource.getName() ) )
                {
                    return resource;
                }
            }
            final String error = 
              "The reference to ["
                + ref
                + "] within the module ["
                + getResourcePath()
                + "] could not be resolved.";
            throw new InvalidNameException( error );
        }
    }
    
    DefaultModule getDefaultModule( final String ref )
    {
        if( null == ref )
        {
            throw new NullPointerException( "ref" );
        }
        int n = ref.indexOf( "/" );
        if( n == 0 )
        {
            String newRef = ref.substring( 1 );
            return getDefaultModule( newRef );
        }
        else if( n > 0 )
        {
            String pre = ref.substring( 0, n );
            String post = ref.substring( n+1 );
            DefaultModule module = getDefaultModule( pre );
            return module.getDefaultModule( post );
        }
        else
        {
            DefaultResource resource = null;
            try
            {
                resource = getDefaultResource( ref );
            }
            catch( InvalidNameException e )
            {
                final String error = 
                  "The reference to module ["
                    + ref
                    + "] within the module ["
                    + getResourcePath()
                    + "] does not exist.";
                throw new InvalidNameException( error, e );
            }
            if( resource instanceof DefaultModule )
            {
                return (DefaultModule) resource;
            }
            else
            {
                final String error = 
                  "A reference to module ["
                  + ref
                  + "] within the module ["
                  + getResourcePath()
                  + "] returned a reference to a non-module resource.";
                throw new InvalidNameException( error );
            }
        }
    }
    
    DefaultModule[] getDefaultModules()
    {
        ArrayList<DefaultModule> stack = new ArrayList<DefaultModule>();
        DefaultResource[] resources = getDefaultResources();
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            if( resource instanceof DefaultModule )
            {
                stack.add( (DefaultModule) resource );
            }
        }
        return stack.toArray( new DefaultModule[0] );
    }
    
    DefaultModule[] getAllDefaultModules()
    {
        return getAllDefaultModules( true, false );
    }
    
    DefaultModule[] getAllDefaultModules( final boolean sort, final boolean self )
    {
        if( sort )
        {
            DefaultModule[] modules = getAllDefaultModules( false, self );
            return sortDefaultModules( modules );
        }
        else
        {
            DefaultModule[] modules = getDefaultModules();
            ArrayList<DefaultModule> visited = new ArrayList<DefaultModule>();
            ArrayList<DefaultModule> stack = new ArrayList<DefaultModule>();
            for( int i=0; i<modules.length; i++ )
            {
                DefaultModule module = modules[i];
                collectChildModules( visited, stack, module );
            }
            if( self )
            {
                stack.add( this );
            }
            return (DefaultModule[]) stack.toArray( new DefaultModule[0] );
        }
    }
    
    private void collectChildModules( 
      final List<DefaultModule> visited, final List<DefaultModule> stack, 
      final DefaultModule module )
    {
        if( visited.contains( module ) )
        {
            return;
        }
        visited.add( module );
        DefaultModule[] children = module.getDefaultModules();
        for( int i=0; i<children.length; i++ )
        {
            collectChildModules( visited, stack, children[i] );
        }
        stack.add( module );
    }
    
    //----------------------------------------------------------------------------
    // Selection logic
    //----------------------------------------------------------------------------
        
    DefaultResource[] selectDefaultResources( final String spec )
    {
        return selectDefaultResources( false, spec );
    }
    
    DefaultResource[] selectDefaultResources( final boolean local, final String spec )
    {
        DefaultResource[] resources = doSelectDefaultResources( false, spec );
        if( local )
        {
            return filterProjects( resources );
        }
        else
        {
            return resources;
        }
    }
    
    DefaultResource[] doSelectDefaultResources( final boolean wild, final String spec )
    {
        String[] tokens = spec.split( "/" );
        if( tokens.length == 0 )
        {
            return new DefaultResource[0];
        }
        else if( tokens.length == 1 )
        {
            String token = tokens[0];
            if( "**".equals( token ) )
            {
                return getAllDefaultModules( true, !m_root );
            }
            else if( "*".equals( token ) )
            {
                if( wild && !m_root )
                {
                    DefaultResource[] resources = getDefaultResources();
                    DefaultResource[] result = new DefaultResource[ resources.length + 1 ];
                    System.arraycopy( resources, 0, result, 0, resources.length );
                    result[ resources.length ] = this;
                    return result;
                }
                else
                {
                    return getDefaultResources();
                }
            }
            else
            {
                Pattern pattern = createSelectionPattern( token );
                DefaultResource[] resources = getDefaultResources();
                DefaultResource[] selection = selectUsingPattern( resources, pattern );
                return selection;
            }
        }
        else
        {
            String token = tokens[0];
            boolean wildcard = ( token.indexOf( "**" ) > -1 );
            String remainder = getRemainderOfSelection( spec, "/", token );
            DefaultModule[] modules = selectDefaultModules( token );
            ArrayList<DefaultResource> list = new ArrayList<DefaultResource>();
            for( int i=0; i<modules.length; i++ )
            {
                DefaultModule module = modules[i];
                DefaultResource[] selection = module.doSelectDefaultResources( wildcard, remainder );
                aggregate( list, selection );
            }
            if( wildcard )
            {
                DefaultResource[] selection = doSelectDefaultResources( wildcard, remainder );
                aggregate( list, selection );
            }
            return list.toArray( new DefaultResource[0] );
        }
    }

    DefaultModule[] selectDefaultModules( final String token )
    {
        if( "**".equals( token ) )
        {
            return getAllDefaultModules( true, !m_root );
        }
        else if( "*".equals( token ) )
        {
            return getDefaultModules();
        }
        else
        {
            Pattern pattern = createSelectionPattern( token );
            DefaultModule[] modules = getDefaultModules();
            DefaultResource[] selection = selectUsingPattern( modules, pattern );
            DefaultModule[] result = new DefaultModule[ selection.length ];
            for( int i=0; i<selection.length; i++ )
            {
                result[i] = (DefaultModule) selection[i];
            }
            return result;
        }
    }
    
    private DefaultResource[] selectUsingPattern( final DefaultResource[] resources, final Pattern pattern )
    {
        ArrayList<DefaultResource> list = new ArrayList<DefaultResource>();
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            String name = resource.getName();
            Matcher matcher = pattern.matcher( name );
            boolean matches = matcher.matches();
            if( matches )
            {
                list.add( resource );
            }
        }
        return list.toArray( new DefaultResource[0] );
    }
    
    private String getRemainderOfSelection( final String spec, final String delimiter, final String token )
    {
        int n = token.length() + delimiter.length();
        return spec.substring( n );
    }
    
    private void aggregate( final List<DefaultResource> list, final DefaultResource[] resources )
    {
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            if( !list.contains( resource ) )
            {
                list.add( resources[i] );
            }
        }
    }
    
    private Pattern createSelectionPattern( final String token )
    {
        StringBuffer buffer = new StringBuffer();
        boolean wildcard = ( token.indexOf( "*" ) > -1 );
        if( wildcard )
        {
            String[] blocks = token.split( "\\*", -1 );
            buffer.append( "(" );
            for( int j=0; j<blocks.length; j++ )
            {
                buffer.append( "\\Q" );
                buffer.append( blocks[j] );
                buffer.append( "\\E" );
                if( j < ( blocks.length-1 ) )
                {
                    buffer.append( ".*" );
                }
            }
            buffer.append( ")" );
        }
        else
        {
            buffer.append( "(\\Q" );
            buffer.append( token );
            buffer.append( "\\E)" );
        }
        String expression = buffer.toString();
        return Pattern.compile( expression );
    }
    
    //----------------------------------------------------------------------------
    // Module sorting
    //----------------------------------------------------------------------------

    private DefaultModule[] sortDefaultModules( final DefaultModule[] modules )
    {
        Scope scope = Scope.TEST;
        ArrayList<DefaultResource> visited = new ArrayList<DefaultResource>();
        ArrayList<DefaultResource> stack = new ArrayList<DefaultResource>();
        for( int i=0; i<modules.length; i++ )
        {
            DefaultModule module = modules[i];
            module.sortDefaultResource( visited, stack, scope, modules );
        }
        return (DefaultModule[]) stack.toArray( new DefaultModule[0] );
    }
    
   /**
    * Returns a sorted array of the provider modules this this module depends on.  
    * The notion of dependency is ressolved via (a) direct depedencies declared
    * by the module, (b) subsidiary module which are considered as sippliers to 
    * this module, and (c) module referenced by any resources contained within 
    * this or any subsidiary module.
    */
    private DefaultModule[] getProviderModules( final Scope scope )
    {
        ArrayList<DefaultResource> visited = new ArrayList<DefaultResource>();
        ArrayList<DefaultModule> stack = new ArrayList<DefaultModule>();
        processModuleDependencies( visited, stack, scope, this );
        return (DefaultModule[]) stack.toArray( new DefaultModule[0] );
    }
    
    private void processModuleDependencies( 
      final List<DefaultResource> visited, final List<DefaultModule> stack, 
      final Scope scope, final DefaultResource resource ) 
    {
        if( visited.contains( resource ) )
        {
            return;
        }
        
        visited.add( resource );
        final boolean expansion = false;
        final boolean filtering = false;
        
        Classifier classifier = resource.getClassifier();
        if( classifier.equals( Classifier.ANONYMOUS ) )
        {
            return;
        }
        
        if( resource instanceof DefaultModule )
        {
            DefaultModule module = (DefaultModule) resource;
            DefaultResource[] providers = module.getAggregatedDefaultProviders( scope, expansion, filtering );
            for( int i=0; i<providers.length; i++ )
            {
                processModuleDependencies( visited, stack, scope, providers[i] );
            }
            DefaultResource[] children = module.getDefaultModules();
            for( int i=0; i<children.length; i++ )
            {
                processModuleDependencies( visited, stack, scope, children[i] );
            }
            if( !resource.equals( this ) )
            {
                stack.add( module );
            }
        }
        else
        {
            DefaultResource[] resources = 
              resource.getAggregatedDefaultProviders( scope, expansion, filtering );
            for( int i=0; i<resources.length; i++ )
            {
                DefaultModule m = resources[i].getDefaultParent();
                processModuleDependencies( visited, stack, scope, m );
            }
        }
    }

    private DefaultResource[] filterProjects( final DefaultResource[] resources )
    {
        ArrayList<DefaultResource> list = new ArrayList<DefaultResource>();
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            if( resource.isLocal() )
            {
                list.add( resource );
            }
        }
        return list.toArray( new DefaultResource[0] );
    }
}

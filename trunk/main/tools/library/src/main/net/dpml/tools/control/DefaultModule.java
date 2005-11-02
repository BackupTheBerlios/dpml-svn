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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import net.dpml.tools.info.Scope;
import net.dpml.tools.info.AbstractDirective;
import net.dpml.tools.info.ModuleDirective;
import net.dpml.tools.info.ResourceDirective;
import net.dpml.tools.info.ResourceDirective.Classifier;
import net.dpml.tools.info.LibraryDirective;
import net.dpml.tools.model.Module;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.ResourceNotFoundException;
import net.dpml.tools.model.ModuleNotFoundException;
import net.dpml.tools.model.DuplicateNameException;

import net.dpml.transit.Category;


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
    private final DefaultResource[] m_resources;
    private final ModuleDirective m_directive;
    
   /**
    * Constructor used by the library to create a virtual root module os shared 
    * resource references.
    * @param library the library
    * @param directive the library directive from which common properties are established
    * @param resources the array of top-level modules
    */
    DefaultModule( DefaultLibrary library, AbstractDirective directive, DefaultResource[] resources ) 
      throws Exception
    {
        super( library, directive );
        m_resources = resources;
        m_root = true;
        m_directive = null;
    }
    
    DefaultModule( DefaultLibrary library, DefaultModule module, ModuleDirective directive ) 
      throws Exception
    {
        super( library, module, directive );
        
        m_root = false;
        m_directive = directive;
        ResourceDirective[] directives = directive.getResourceDirectives();
        DefaultResource[] resources = new DefaultResource[ directives.length ];
        for( int i=0; i<directives.length; i++ )
        {
            ResourceDirective res = directives[i];
            if( res instanceof ModuleDirective )
            {
                ModuleDirective d = (ModuleDirective) res;
                resources[i] = new DefaultModule( library, this, d );
            }
            else
            {
                resources[i] = new DefaultResource( library, this, res );
            }
        }
        m_resources = resources;
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
    public Resource getResource( String ref ) throws ResourceNotFoundException
    {
        try
        {
            return getDefaultResource( ref );
        }
        catch( InvalidNameException e )
        {
            final String message = e.getMessage();
            throw new ResourceNotFoundException( message );
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
    public Module getModule( String ref ) throws ModuleNotFoundException
    {
        try
        {
            return getDefaultModule( ref );
        }
        catch( InvalidNameException e )
        {
            final String message = e.getMessage();
            throw new ModuleNotFoundException( message );
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
    public Resource[] select( String criteria, boolean local, boolean sort )
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
    public Resource locate( File base ) throws ResourceNotFoundException
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
    * Return a directive suitable for publication as an external
    * module description.
    * @return the module directive
    */
    public ModuleDirective export()
    {
        if( null == m_directive )
        {
            final String error = 
              "Cannot export from the root module.";
            throw new UnsupportedOperationException( error );
        }
        else
        {
            String version = getVersion();
            return (ModuleDirective) m_directive.export( version );
        }
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
        return "module:" + getResourcePath();
    }
    
    //----------------------------------------------------------------------------
    // DefaultResource (overriding)
    //----------------------------------------------------------------------------

    DefaultResource[] getLocalDefaultProviders( Scope scope, Category category )
    {
        if( Scope.BUILD.equals( scope ) )
        {
            ArrayList stack = new ArrayList();
            DefaultResource[] local = super.getLocalDefaultProviders( scope, category );
            for( int i=0; i<local.length; i++ )
            {
                DefaultResource resource = local[i];
                stack.add( resource );
            }
            return getLocalDefaultProviders( stack, true );
        }
        else
        {
            return super.getLocalDefaultProviders( scope, category );
        }
    }
    
    DefaultResource[] getLocalDefaultProviders( List stack, boolean flag )
    {
        DefaultResource[] resources = getDefaultResources();
        if( flag )
        {
            for( int i=0; i<resources.length; i++ )
            {
                DefaultResource resource = resources[i];
                if( !stack.contains( resource ) )
                {
                    stack.add( resource );
                }
            }
        }
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            if( resource instanceof DefaultModule )
            {
                DefaultModule module = (DefaultModule) resource;
                DefaultResource[] providers = module.getLocalDefaultProviders( stack, false );
                for( int j=0; j<providers.length; j++ )
                {
                    DefaultResource r = providers[j];
                    if( !stack.contains( r ) )
                    {
                        stack.add( r );
                    }
                }
            }
            else
            {
                //System.out.println( "$      START GET PARENTS: " + this );
                DefaultResource[] providers = 
                  resource.getAggregatedDefaultProviders( Scope.TEST, true, false );
                getParentModules( stack, providers );
                //System.out.println( "$      END GET PARENTS: " + this );
            }
        }
        return (DefaultResource[]) stack.toArray( new DefaultResource[0] );
    }
    
    private void getParentModules( List stack, DefaultResource[] resources )
    {
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            if( !resource.isAnonymous() )
            {
                DefaultModule parent = resource.getDefaultParent();
                if( !stack.contains( parent ) && !this.equals( parent ) )
                {
                    stack.add( parent );
                }
            }
        }
    }
    
    void sortDefaultResource( 
      List visited, List stack, Scope scope, DefaultResource[] resources )
    {
        if( visited.contains( this ) )
        {
            return;
        }
        visited.add( this );
        DefaultModule[] providers = getProviderModules( scope );
        for( int i=0; i<providers.length; i++ )
        {
            DefaultResource provider = providers[i];
            if( isaMember( resources, provider ) )
            {
                provider.sortDefaultResource( visited, stack, scope, resources );
            }
        }
        DefaultResource[] children = getDefaultResources();
        for( int i=0; i<children.length; i++ )
        {
            DefaultResource child = children[i];
            if( isaMember( resources, child ) )
            {
                child.sortDefaultResource( visited, stack, scope, resources );
            }
        }
        stack.add( this );
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
        return m_resources;
    }
    
    DefaultResource getDefaultResource( String ref )
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
            String path = getResourcePath() + "/" + ref;
            throw new InvalidNameException( path );
        }
    }
    
    DefaultModule getDefaultModule( String ref )
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
            return module.getDefaultModule( post );
        }
        else
        {
            DefaultResource resource = getDefaultResource( ref );
            if( resource instanceof DefaultModule )
            {
                return (DefaultModule) resource;
            }
            else
            {
                String path = getResourcePath() + "/" + ref;
                final String error = 
                  "Resource [" 
                  + path 
                  + "] is not a module.";
                throw new IllegalArgumentException( error );
            }
        }
    }
    
    DefaultModule[] getDefaultModules()
    {
        ArrayList stack = new ArrayList();
        DefaultResource[] resources = getDefaultResources();
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            if( resource instanceof DefaultModule )
            {
                stack.add( resource );
            }
        }
        return (DefaultModule[]) stack.toArray( new DefaultModule[0] );
    }
    
    DefaultModule[] getAllDefaultModules()
    {
        return getAllDefaultModules( true, false );
    }
    
    DefaultModule[] getAllDefaultModules( boolean sort, boolean self )
    {
        if( sort )
        {
            DefaultModule[] modules = getAllDefaultModules( false, self );
            return sortDefaultModules( modules );
        }
        else
        {
            DefaultModule[] modules = getDefaultModules();
            ArrayList visited = new ArrayList();
            ArrayList stack = new ArrayList();
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
      List visited, List stack, DefaultModule module )
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
        
    DefaultResource[] selectDefaultResources( String spec )
    {
        return selectDefaultResources( false, spec );
    }
    
    DefaultResource[] selectDefaultResources( boolean local, String spec )
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
    
    DefaultResource[] doSelectDefaultResources( boolean wild, String spec )
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
                if( wild )
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
            ArrayList list = new ArrayList();
            for( int i=0; i<modules.length; i++ )
            {
                DefaultModule module = modules[i];
                DefaultResource[] selection = module.doSelectDefaultResources( wildcard, remainder );
                aggregate( list, selection );
            }
            return (DefaultResource[]) list.toArray( new DefaultResource[0] );
        }
    }

    DefaultModule[] selectDefaultModules( String token )
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
    
    private DefaultResource[] selectUsingPattern( DefaultResource[] resources, Pattern pattern )
    {
        ArrayList list = new ArrayList();
        for( int i=0; i<resources.length; i++ )
        {
            Resource resource = resources[i];
            String name = resource.getName();
            Matcher matcher = pattern.matcher( name );
            boolean matches = matcher.matches();
            //System.out.println( "# eval: " + name + ", " + matches );
            if( matches )
            {
                //System.out.println( "# found: " + name );
                list.add( resource );
            }
        }
        return (DefaultResource[]) list.toArray( new DefaultResource[0] );
    }
    
    private String getRemainderOfSelection( String spec, String delimiter, String token )
    {
        int n = token.length() + delimiter.length();
        return spec.substring( n );
    }
    
    private void aggregate( List list, DefaultResource[] resources )
    {
        for( int i=0; i<resources.length; i++ )
        {
            list.add( resources[i] );
        }
    }
    
    private Pattern createSelectionPattern( String token )
    {
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
                if( j < ( blocks.length-1 ) )
                {
                    buffer.append( ".*" );
                }
            }
            buffer.append( ")" );
        }
        else
        {
            //System.out.println( "\t# block [s]\t'" + token + "'"  );
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

    private DefaultModule[] sortDefaultModules( DefaultModule[] modules )
    {
        Scope scope = Scope.TEST;
        ArrayList visited = new ArrayList();
        ArrayList stack = new ArrayList();
        for( int i=0; i<modules.length; i++ )
        {
            DefaultModule module = modules[i];
            //processDefaultModule( visited, stack, module, modules );
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
    private DefaultModule[] getProviderModules( Scope scope )
    {
        ArrayList visited = new ArrayList();
        ArrayList stack = new ArrayList();
        processModuleDependencies( visited, stack, scope, this );
        return (DefaultModule[]) stack.toArray( new DefaultModule[0] );
    }
    
    private void processModuleDependencies( List visited, List stack, Scope scope, DefaultResource resource ) 
    {
        if( visited.contains( resource ) )
        {
            return;
        }
        visited.add( resource );
        final boolean expansion = true;
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
                //System.out.println( "   add module provider: " + module + " to " + this );
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
                //System.out.println( "   add resource parent: " + m );
                processModuleDependencies( visited, stack, scope, m );
            }
        }
    }

    private DefaultResource[] filterProjects( DefaultResource[] resources )
    {
        ArrayList list = new ArrayList();
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            if( resource.isLocal() )
            {
                list.add( resource );
            }
        }
        return (DefaultResource[]) list.toArray( new DefaultResource[0] );
    }
}

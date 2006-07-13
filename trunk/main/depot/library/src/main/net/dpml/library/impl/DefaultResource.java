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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.TimeZone;
import java.util.Properties;
import java.util.Map;
import java.util.Hashtable;

import net.dpml.lang.Category;
import net.dpml.lang.Version;

import net.dpml.library.Info;
import net.dpml.library.Filter;
import net.dpml.library.Library;
import net.dpml.library.Module;
import net.dpml.library.Resource;
import net.dpml.library.Type;
import net.dpml.library.Data;
import net.dpml.library.ResourceNotFoundException;
import net.dpml.library.info.InfoDirective;
import net.dpml.library.info.TypeDirective;
import net.dpml.library.info.ResourceDirective;
import net.dpml.library.info.ResourceDirective.Classifier;
import net.dpml.library.info.IncludeDirective;
import net.dpml.library.info.DependencyDirective;
import net.dpml.library.info.AbstractDirective;
import net.dpml.library.info.ValidationException;
import net.dpml.library.info.FilterDirective;
import net.dpml.library.info.Scope;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;

import net.dpml.util.Resolver;


/**
 * Implementation of a resource.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultResource extends DefaultDictionary implements Resource, Resolver, Comparable
{
   /**
    * Timestamp.
    */
    public static final String TIMESTAMP = getTimestamp();
    
   /**
    * Constant SNAPSHOT symbol.
    */
    public static final String SNAPSHOT = "SNAPSHOT";
    
    private final DefaultLibrary m_library;
    private final ResourceDirective m_directive;
    private final DefaultModule m_parent;
    private final Type[] m_types;
    private final String[] m_typeNames;
    private final String m_path;
    private final File m_basedir;
    private final Map m_filters = new Hashtable();
    private final Data[] m_data;
    
   /**
    * Creation of a new default resource.
    * @param logger the assigned logging channel
    * @param library the reference library
    * @param directive the directive
    */
    DefaultResource( final DefaultLibrary library, final AbstractDirective directive )
    {
        super( null, directive );
        
        m_library = library;
        m_directive = null;
        m_parent = null;
        m_types = new Type[0];
        m_typeNames = new String[0];
        m_path = "";
        m_basedir = null;
        m_data = new Data[0];
    }
    
   /**
    * Creation of a new default resource.
    * @param logger the assigned logging channel
    * @param library the reference library
    * @param module the parent module
    * @param directive the resource directive
    */
    DefaultResource( 
      final DefaultLibrary library, final DefaultModule module, final ResourceDirective directive ) 
    {
        super( module, directive );
        if( null == directive )
        {
            throw new NullPointerException( "directive" );
        }
        
        m_library = library;
        m_directive = directive;
        m_parent = module;
        
        if( module.isRoot() )
        {
            m_path = directive.getName();
        }
        else
        {
            m_path = module.getResourcePath() + "/" + directive.getName();
        }
        
        // setup produced types
        
        //m_types = buildTypes( directive.getTypeDirectives() );
        m_types = directive.getTypeDirectives();
        m_typeNames = new String[ m_types.length ];
        for( int i=0; i<m_types.length; i++ )
        {
            Type type = m_types[i];
            m_typeNames[i] = type.getID();
        }
        
        // setup production data
        
        m_data = new Data[0];
        
        // setup the resource basedir
        
        File anchor = getAnchor();
        String filename = m_directive.getBasedir();
        if( null != filename )
        {
            File file = new File( filename );
            if( file.isAbsolute() )
            {
                m_basedir = getCanonicalFile( file );
            }
            else
            {
                File basedir = new File( anchor, filename );
                m_basedir = getCanonicalFile( basedir );
                setProperty( "basedir", m_basedir.toString() );
            }
        }
        else
        {
            if( !m_directive.getClassifier().equals( Classifier.LOCAL ) )
            {
                m_basedir = null;
            }
            else
            {
                final String error = 
                  "Missing base directory declaration in resource ["
                  + m_path
                  + "].";
                throw new ValidationException( error );
            }
        }
        
        // setup the default properties
        
        setProperty( "project.name", getName() );
        if( null != m_parent )
        {
            setProperty( "project.group", m_parent.getResourcePath() );
        }
        else
        {
            setProperty( "project.group", "" );
        }
        String version = getVersion();
        if( null != version )
        {
            setProperty( "project.version", getVersion() );
        }
        
        FilterDirective[] filters = directive.getFilterDirectives();
        for( int i=0; i<filters.length; i++ )
        {
            FilterDirective filter = filters[i];
            String token = filter.getToken();
            m_filters.put( token, filter );
        }
    }
    
    //----------------------------------------------------------------------------
    // Resource
    //----------------------------------------------------------------------------
    
   /**
    * Return a data directives.
    * @return the associated production data
    */
    public Data[] getData()
    {
        return m_data;
    }

   /**
    * Return the singleton library.
    * @return the library
    */
    public Library getLibrary()
    {
        return m_library;
    }

   /**
    * Return the name of the resource.
    * @return the resource name
    */
    public String getName()
    {
        if( null != m_directive )
        {
            return m_directive.getName();
        }
        else
        {
            return null;
        }
    }
    
   /**
    * Return the resource version.
    * @return the version
    */
    public String getVersion()
    {
        if( null == m_directive )
        {
            return getStandardVersion();
        }
        String version = m_directive.getVersion();
        if( ResourceDirective.ANONYMOUS.equals( getClassifier() ) )
        {
            return version;
        }
        if( null == version )
        {
            if( null != m_parent ) 
            {
                return m_parent.getVersion();
            }
            else
            {
                return getStandardVersion();
            }
        }
        else
        {
            return version;
        }
    }
    
   /**
    * Return the fully qualified path to the resource.
    * @return the path
    */
    public String getResourcePath()
    {
        return m_path;
    }
    
   /**
    * Return the basedir for this resource.
    * @return the base directory (possibly null)
    */
    public File getBaseDir()
    {
        return m_basedir;
    }

   /**
    * Return the resource classifier.
    * @return the classifier (LOCAL, EXTERNAL or ANONYMOUS)
    */
    public Classifier getClassifier()
    {
        if( null != m_directive )
        {
            return m_directive.getClassifier();
        }
        else
        {
            return ResourceDirective.ANONYMOUS;
        }
    }
    
   /**
    * Return the info block.
    * @return the info block
    */
    public Info getInfo()
    {
        return m_directive.getInfoDirective();
    }
    
   /**
    * Return the expanded array of types associated with the resource.
    * The returned array is a function of the types declared by a resource
    * expanded relative to any types implied by processor dependencies.
    * @return the type array
    */
    public Type[] getTypes()
    {
        return m_types;
    }
    
   /**
    * Test if this resource is associated with a type of the supplied name.
    * @param type the type id
    * @return TRUE if this resource produces an artifact of the supplied type
    */
    public boolean isa( final String type )
    {
        for( int i=0; i<m_types.length; i++ )
        {
            Type someType = m_types[i];
            String name = someType.getID();
            if( name.equals( type ) )
            {
                return true;
            }
        }
        return false;
    }
    
   /**
    * Return a resource type relative to a supplied type id.
    * @param id the type name to retrieve
    * @return the type instance
    * @exception IllegalArgumentException if the id value does not match
    * a type produced by the resource.
    */
    public Type getType( final String id ) throws IllegalArgumentException
    {
        for( int i=0; i<m_types.length; i++ )
        {
            Type type = m_types[i];
            if( type.getID().equals( id ) )
            {
                return type;
            }
        }
        final String error = 
          "Type name ["
          + id
          + "] not recognized with the scope of resource ["
          + getResourcePath()
          + "].";
        throw new IllegalArgumentException( error );
    }
    
   /**
    * Construct an link artifact for the supplied type.
    * @param id the resource type id
    * @return the link artifact
    */
    public Artifact getLinkArtifact( final String id )
    {
        if( null == m_directive )
        {
            final String error = 
              "Method not supported on virtual root.";
            throw new UnsupportedOperationException( error );
        }
        if( null == id )
        {
            throw new NullPointerException( "id" );
        }
        String group = getGroupName();
        String name = getName();
        Type type = getType( id );
        Version version = type.getVersion();
        if( null == version )
        {
            final String error = 
              "Resource does not declare production of an alias for the requested type."
              + "\nResource: " + this
              + "\nType: " + id;
            throw new IllegalArgumentException( error );
        }
        try
        {
            String spec = "link:" + id;
            if( null != group )
            {
                spec = spec + ":" + group + "/" + name;
            }
            else
            {
                spec = spec + ":" + name;
            }
            if( !Version.NULL_VERSION.equals( version ) )
            {
                int major = version.getMajor();
                int minor = version.getMinor();
                spec = spec + "#"
                  + major
                  + "."
                  + minor;
            }
            return Artifact.createArtifact( spec );
        }
        catch( Throwable e )
        {
            final String error = 
              "Failed to construct link artifact for resource ["
              + getResourcePath()
              + "].";
            throw new RuntimeException( error, e );
        }
    }

   /**
    * Construct an artifact for the supplied type.
    * @param id the resource type identifier
    * @return the artifact
    */
    public Artifact getArtifact( final String id )
    {
        if( null == m_directive )
        {
            final String error = 
              "Method not supported on virtual root.";
            throw new UnsupportedOperationException( error );
        }
        if( null == id )
        {
            throw new NullPointerException( "id" );
        }
        
        String group = getGroupName();
        String name = getName();
        String version = getVersion();
        String scheme = m_directive.getScheme();
        
        try
        {
            return Artifact.createArtifact( scheme, group, name, version, id );
        }
        catch( Throwable e )
        {
            final String error = 
              "Failed to construct artifact for resource ["
              + getResourcePath()
              + "].";
            throw new RuntimeException( error, e );
        }
    }
    
   /**
    * Return the enclosing parent module.
    * @return the enclosing module of null if this a top-level module.
    */
    public Module getParent()
    {
        return getDefaultParent();
    }
    
   /**
    * Return an array of filters associated with the resource.
    * @return the array of filters
    */
    public Filter[] getFilters()
    {
        DefaultModule module = getDefaultParent();
        if( null != module )
        {
            Map map = new Hashtable();
            Filter[] filters = module.getFilters();
            for( int i=0; i<filters.length; i++ )
            {
                Filter filter = filters[i];
                String token = filter.getToken();
                map.put( token, filter );
            }
            Filter[] local = getLocalFilters();
            for( int i=0; i<local.length; i++ )
            {
                Filter filter = local[i];
                String token = filter.getToken();
                map.put( token, filter );
            }
            return (Filter[]) map.values().toArray( new Filter[0] );
        }
        else
        {
            return getLocalFilters();
        }
    }
    
    //----------------------------------------------------------------------------
    // Resolver
    //----------------------------------------------------------------------------

   /**
    * Utility function supporting resolution of uris containing 'resource' or 
    * 'alias' schemes.  If the supplied uri schem is 'resource' or 'alias' the 
    * reference is resolved to a artifact type, group and name from which a 
    * resource is resolved and the uri returned.  If the scheme is resource
    * the usri of the resource is returned. If the scheme is 'alias' a 
    * linkn alias is returned.  If the scheme is not 'resource' or 'alias' 
    * the argument will be evaluated as a normal transit artifact uri 
    * specification.
    * 
    * @param ref the uri argument
    * @return the uri value
    */
    public URI toURI( final String ref ) throws URISyntaxException
    {
        Artifact spec = Artifact.createArtifact( ref );
        if( spec.isRecognized() )
        {
            return spec.toURI();
        }
        else if( ref.startsWith( "resource:" ) || ref.startsWith( "alias:" ) )
        {
            String type = spec.getType();
            String group = spec.getGroup();
            String name = spec.getName();
            String path = group + "/" + name;
            Library library = getLibrary();
            try
            {
                Resource resource = library.getResource( path );
                if( ref.startsWith( "resource:" ) )
                {
                    Artifact artifact = resource.getArtifact( type );
                    return artifact.toURI();
                }
                else
                {
                    Artifact artifact = resource.getLinkArtifact( type );
                    return artifact.toURI();
                }
            }
            catch( ResourceNotFoundException e )
            {
                final String error = 
                  "Unresolvable resource reference: " + path;
                IllegalArgumentException iae = new IllegalArgumentException( error );
                iae.initCause( e );
                throw iae;
            }
        }
        else
        {
            return spec.toURI();
        }
    }
    
    //----------------------------------------------------------------------------
    // implementation
    //----------------------------------------------------------------------------

    Map getFilterMap()
    {
        return m_filters;
    }
    
    Filter[] getLocalFilters()
    {
        return (Filter[]) getFilterMap().values().toArray( new Filter[0] );
    }
    
   /**
    * Return an array of resource that are providers to this resource.
    * @param scope the operational scope
    * @param expand if true include transitive dependencies
    * @param sort if true the array will sorted relative to dependencies
    * @return the resource providers
    */
    public Resource[] getProviders( final Scope scope, final boolean expand, final boolean sort )
    {
        return getDefaultProviders( scope, expand, sort );
    }
    
   /**
    * Return an array of resource that are providers to this resource. If
    * the supplied scope is BUILD the returned resource array is equivalent
    * <src>getProviders( Scope.BUILD, .. )</src>.  If the scope is RUNTIME
    * the returned resource array includes BUILD and RUNTIME resources. If 
    * the scope is TEST the returned array includes BUILD, RUNTIME and TEST
    * resources.
    * @param scope the scope of aggregation to be applied to the selection
    * @param expand if TRUE include transitive dependencies
    * @param sort if true the array will sorted relative to dependencies
    * @return the resource providers
    */
    public Resource[] getAggregatedProviders( final Scope scope, final boolean expand, final boolean sort )
    {
        return getAggregatedDefaultProviders( scope, expand, sort, false );
    }
    
   /**
    * Return a sorted and filtered array of providers. Resources not declaring
    * the "jar" type as a produced type are excluded from selection.  The 
    * resource array will include transitive dependencies.  The method is 
    * suitable for the construction of build and test phase classloaders.
    *
    * @param scope the aggregation scope
    * @return the scoped resource chain
    */
    public Resource[] getClasspathProviders( final Scope scope )
    {
        final boolean expanded = true;
        final boolean sorted = true;
        final boolean filtered = true;
        return getAggregatedDefaultProviders( scope, expanded, sorted, filtered );
    }

   /**
    * Return an array of runtime providers filtered relative to a supplied
    * classloading category.  Resources not declaring the "jar" type as a 
    * produced type are excluded from selection.  The resource array returned 
    * from this operation is a sorted transitive sequence excluding all 
    * resource references by any category higher than the supplied category.
    * This method is typically used to construct information suitable for 
    * the gerneration of plugin metadata.
    *
    * @param category the classloader category
    * @return the category scoped resource chain
    */
    public Resource[] getClasspathProviders( final Category category )
    {
        DefaultResource[] resources = getClasspathDefaultProviders( category );
        return sortDefaultResources( resources, Scope.RUNTIME );
    }
    
   /**
    * Return an array of resources that are consumers of this resource.
    * @param expand if true the returned array includes consumers associated
    *   through transitive dependency relationships, otherwise the array is 
    *   limited to direct consumers
    * @param sort if true the array is sorted relative to depenency relationships
    * @return the array of consumer projects
    */
    public Resource[] getConsumers( final boolean expand, final boolean sort )
    {
        return getDefaultConsumers( expand, sort );
    }
    
   /**
    * Return the underlying resource defintion.
    * @return the resource directive
    */
    public ResourceDirective getResourceDirective()
    {
        return m_directive;
    }

   /**
    * Return a filename using the layout strategy employed by the cache.
    * @param id the artifact type
    * @return the filename
    */
    public String getLayoutPath( final String id )
    {
        Artifact artifact = getArtifact( id );
        return Transit.getInstance().getCacheLayout().resolveFilename( artifact );
    }

   /**
    * Return a directive suitable for publication as an external description.
    * @param module the enclosing module
    * @return the resource directive
    */
    ResourceDirective exportResource( final DefaultModule module )
    {
        if( null == m_directive )
        {
            final String error = 
              "Cannot export from the root module.";
            throw new UnsupportedOperationException( error );
        }
        String name = getName();
        String version = getVersion();
        String basedir = null;
        InfoDirective info = m_directive.getInfoDirective();
        TypeDirective[] types = m_directive.getTypeDirectives();
        TypeDirective[] exportedTypes = createExportedTypes( types );
        DependencyDirective[] dependencies = createDeps( module );
        Properties properties = getExportProperties();
        return ResourceDirective.createResourceDirective( 
          name, version, Classifier.EXTERNAL, basedir,
          info, exportedTypes, dependencies, properties, null );
    }
    
    TypeDirective[] createExportedTypes( final TypeDirective[] types )
    {
        TypeDirective[] export = new TypeDirective[ types.length ]; 
        for( int i=0; i<export.length; i++ )
        {
            TypeDirective type = types[i];
            String id = type.getID();
            Version version = type.getVersion();
            export[i] = new TypeDirective( id, version );
        }
        return export;
    }
    
    private DependencyDirective[] createDeps( final DefaultModule module )
    {
        ArrayList list = new ArrayList();
        createIncludeDirectives( module, list, Category.SYSTEM );
        createIncludeDirectives( module, list, Category.PUBLIC );
        createIncludeDirectives( module, list, Category.PROTECTED );
        createIncludeDirectives( module, list, Category.PRIVATE );
        if( list.size() == 0 )
        {
            return new DependencyDirective[0];
        }
        else
        {
            IncludeDirective[] includes = 
             (IncludeDirective[]) list.toArray( new IncludeDirective[0] );
            DependencyDirective runtime = 
              new DependencyDirective( Scope.RUNTIME, includes );
            return new DependencyDirective[]{runtime};
        }
    }
    
    boolean isaDescendant( final DefaultModule module )
    {
        if( module == this )
        {
            return true;
        }
        if( m_parent == null )
        {
            return false;
        }
        else
        {
            if( m_parent == module )
            {
                return true;
            }
            else
            {
                return m_parent.isaDescendant( module );
            }
        }
    }

    private void createIncludeDirectives(
      final DefaultModule module, final List list, final Category category )
    {
        DefaultResource[] providers = 
          getDefaultProviders( Scope.RUNTIME, true, category );
        for( int i=0; i<providers.length; i++ )
        {
            DefaultResource provider = providers[i];
            if( provider.isaDescendant( module ) )
            {
                // create a ref
                String path = provider.getResourcePath();
                IncludeDirective include = 
                  new IncludeDirective( 
                    IncludeDirective.REF,
                    category,
                    path,
                    null );
                list.add( include );
            }
            else
            {
                // create a urn
                
                Type[] types = provider.getTypes();
                for( int j=0; j<types.length; j++ )
                {
                    Type type = types[j];
                    String label = type.getID();
                    Artifact artifact = provider.getArtifact( label );
                    String urn = artifact.toString();
                    IncludeDirective include = 
                      new IncludeDirective( 
                        IncludeDirective.URI,
                        category,
                        urn,
                        null );
                    list.add( include );
                }
            }
        }
    }
    
    //----------------------------------------------------------------------------
    // Object
    //----------------------------------------------------------------------------
    
   /**
    * Return a string representation of the resource in the form 'resource:[path]'.
    * @return the string value
    */
    public String toString()
    {
        if( null != m_directive )
        {
            if( m_directive.isLocal() )
            {
                return toString( "project" );
            }
        }
        return toString( "resource" );
    }
    
    String toString( final String type )
    {
        return type + ":" + getResourcePath() + "#" + getVersion();
    }
    
   /**
    * Compare this object with another.
    * @param other the other object
    * @return the comparitive index
    */
    public int compareTo( final Object other )
    {
        if( other instanceof DefaultResource )
        {
            DefaultResource resource = (DefaultResource) other;
            return getResourcePath().compareTo( resource.m_path );
        }
        else
        {
            return -1;
        }
    }
    
    //----------------------------------------------------------------------------
    // internals
    //----------------------------------------------------------------------------
    
   /**
    * Return the singlton library.
    * @return the library
    */
    DefaultLibrary getDefaultLibrary()
    {
        return m_library;
    }
    
    boolean isAnonymous()
    {
        if( null != m_directive )
        {
            return m_directive.isAnonymous();
        }
        return false;
    }
    
    boolean isLocal()
    {
        if( null != m_directive )
        {
            return m_directive.isLocal();
        }
        return false;
    }
    
    DefaultModule getDefaultParent()
    {
        if( null != m_parent )
        {
            if( m_parent.isRoot() )
            {
                return null;
            }
        }
        return m_parent;
    }
    
    DefaultResource[] getAggregatedDefaultProviders( 
      final Scope scope, final boolean expanded, final boolean sort, final boolean filtered )
    {
        DefaultResource[] resources = 
          getAggregatedDefaultProviders( scope, expanded, filtered );
        if( sort )
        {
            return sortDefaultResources( resources, scope );
        }
        else
        {
            Arrays.sort( resources );
            return resources;
        }
    }
    
    DefaultResource[] getAggregatedDefaultProviders( 
      final Scope scope, final boolean expanded, final boolean filtered )
    {
        ArrayList list = new ArrayList();
        if( !filtered )
        {
            aggregateProviders( list, Scope.BUILD );
        }
        if( scope.isGreaterThan( Scope.BUILD ) )
        {
            aggregateProviders( list, Scope.RUNTIME );
        }
        if( scope.isGreaterThan( Scope.RUNTIME ) )
        {
            aggregateProviders( list, Scope.TEST );
        }
        DefaultResource[] result = (DefaultResource[]) list.toArray( new DefaultResource[0] );
        if( expanded )
        {
            List visited = new ArrayList();
            List stack = new ArrayList();
            for( int i=0; i<result.length; i++ )
            {
                DefaultResource resource = result[i];
                processDefaultResource( visited, stack, scope, true, resource );
            }
            result = (DefaultResource[]) stack.toArray( new DefaultResource[0] );
        }
        if( filtered )
        {
            List stack = new ArrayList();
            for( int i=0; i<result.length; i++ )
            {
                DefaultResource resource = result[i];
                if( resource.isa( "jar" ) )
                {
                    stack.add( resource );
                }
            }
            result = (DefaultResource[]) stack.toArray( new DefaultResource[0] );
        }
        return result;
    }

    private void aggregateProviders( final List list, final Scope scope )
    {
        DefaultResource[] resources = getDefaultProviders( scope, false, null );
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            if( !list.contains( resource ) )
            {
                list.add( resource );
            }
        }
    }
    
    DefaultResource[] getDefaultProviders( 
      final Scope scope, final boolean expanded, final boolean sort ) 
    {
        DefaultResource[] resources = getDefaultProviders( scope, expanded, null );
        if( sort )
        {
            return sortDefaultResources( resources, scope );
        }
        else
        {
            Arrays.sort( resources );
            return resources;
        }
    }
    
    DefaultResource[] getDefaultProviders( 
      final Scope scope, final boolean expand, final Category category )
    {
        ArrayList visited = new ArrayList();
        ArrayList stack = new ArrayList();
        DefaultResource[] providers = getLocalDefaultProviders( scope, category );
        for( int i=0; i<providers.length; i++ )
        {
            DefaultResource provider = providers[i];
            if( expand )
            {
                processDefaultResource( visited, stack, scope, false, provider );
            }
            else
            {
                stack.add( provider );
            }
        }
        return (DefaultResource[]) stack.toArray( new DefaultResource[0] );
    }
    
    DefaultResource[] getLocalDefaultProviders( final Scope scope, final Category category ) 
    {
        if( null == m_directive )
        {
            return new DefaultResource[0];
        }
        IncludeDirective[] includes = getLocalIncludes( scope, category );
        DefaultResource[] resources = new DefaultResource[ includes.length ];
        for( int i=0; i<includes.length; i++ )
        {
            IncludeDirective include = includes[i];
            if( include.getMode().equals( IncludeDirective.URI ) )
            {
                try
                {
                    String value = include.getValue();
                    String urn = resolve( value );
                    Properties properties = include.getProperties();
                    resources[i] = m_library.getAnonymousResource( urn, properties );
                }
                catch( URISyntaxException e )
                {
                    final String error = 
                      "Invalid uri value: " + include.getValue();
                    throw new RuntimeException( error, e );
                }
                catch( InvalidNameException e )
                {
                    final String error = 
                      "An anonomous dependency include reference to ["
                      + include
                      + "] within the resource ["
                      + getResourcePath()
                      + "] could not be resolved.";
                    throw new InvalidNameException( error, e );
                }
                catch( Exception e )
                {
                    final String error = 
                      "Unexpected error during dynamic resource creation.";
                    throw new RuntimeException( error, e );
                }
            }
            else
            {
                String ref = getIncludeReference( include );
                try
                {
                    DefaultResource resource = m_library.getDefaultResource( ref );
                    resources[i] = resource;
                }
                catch( InvalidNameException e )
                {
                    if( null == category )
                    {
                        final String error = 
                          "A dependency include ["
                          + ref
                          + "] within ["
                          + this
                          + "] referencing ["
                          + ref
                          + "] under the scope ["
                          + scope
                          + "] is unknown.";
                        throw new InvalidNameException( error );
                    }
                    else
                    {
                        final String error = 
                          "A dependency include within ["
                          + this
                          + "] referencing ["
                          + ref
                          + "] under the scope ["
                          + scope
                          + "] and category ["
                          + category
                          + "] is unknown.";
                        throw new InvalidNameException( error );
                    }
                }
            }
        }
        return resources;
    }
    
    private IncludeDirective[] getLocalIncludes( final Scope scope, final Category category )
    {
        DependencyDirective dependency = m_directive.getDependencyDirective( scope );
        if( null == category )
        {
            return dependency.getIncludeDirectives();
        }
        else
        {
            return dependency.getIncludeDirectives( category );
        }
    }
    
    private void processDefaultResource( 
      final List visited, final List stack, final Scope scope, final boolean expand, 
      final DefaultResource resource )
    {
        if( visited.contains( resource ) )
        {
            return;
        }
        else
        {
            visited.add( resource );
            DefaultResource[] providers = resource.getAggregatedDefaultProviders( scope, false, false );
            for( int i=0; i<providers.length; i++ )
            {
                processDefaultResource( visited, stack, scope, expand, providers[i] );
            }
            stack.add( resource );
        }
    }
    
    private String getIncludeReference( final IncludeDirective directive )
    {
        if( null == m_parent )
        {
            return directive.getValue();
        }
        else
        {
            if( IncludeDirective.REF.equals( directive.getMode() ) )
            {
                return directive.getValue();
            }
            else
            {
                String path = m_parent.getResourcePath();
                if( "".equals( path ) )
                {
                    return directive.getValue();
                }
                else
                {
                    String key = directive.getValue();
                    return path + "/" + key;
                }
            }
        }
    }
    
    //----------------------------------------------------------------------------
    // consumer concerns
    //----------------------------------------------------------------------------
    
    boolean isaConsumer( final DefaultResource resource )
    {
        DefaultResource[] resources = getAggregatedDefaultProviders( Scope.TEST, false, false );
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource provider = resources[i];
            if( resource.equals( provider ) )
            {
                return true;
            }
        }
        return false;
    }
    
    DefaultResource[] getDefaultConsumers( final boolean expand, final boolean sort )
    {
        DefaultResource[] consumers = getDefaultConsumers( expand );
        if( sort )
        {
            return sortDefaultResources( consumers, Scope.TEST );
        }
        else
        {
            return consumers;
        }
    }
    
    DefaultResource[] getDefaultConsumers( final boolean expand )
    {
        if( !expand )
        {
            ArrayList list = new ArrayList();
            DefaultResource[] resources = m_library.selectDefaultResources( "**/*" );
            for( int i=0; i<resources.length; i++ )
            {
                DefaultResource resource = resources[i];
                if( !list.contains( resource ) && resource.isaConsumer( this ) )
                {
                    list.add( resource );
                }
            }
            return (DefaultResource[]) list.toArray( new DefaultResource[0] );
        }
        else
        {
            ArrayList visited = new ArrayList();
            ArrayList stack = new ArrayList();
            DefaultResource[] consumers = getDefaultConsumers( false );
            for( int i=0; i<consumers.length; i++ )
            {
                DefaultResource consumer = consumers[i];
                processConsumer( visited, stack, consumer );
            }
            return (DefaultResource[]) stack.toArray( new DefaultResource[0] );
        }
    }
    
    void processConsumer( final List visited, final List stack, final DefaultResource consumer )
    {
        if( visited.contains( consumer ) )
        {
            return;
        }
        visited.add( consumer );
        stack.add( consumer );
        DefaultResource[] resources = consumer.getDefaultConsumers( false, false );
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            processConsumer( visited, stack, resource );
        }
    }
    
    //----------------------------------------------------------------------------
    // classpath stuff
    //----------------------------------------------------------------------------
    
   /**
    * Construct an array of resources based on the RUNTIME scoped dependencies
    * associated with the supplied category.  The implementation builds a list
    * of all preceeding categories as a basis for filtering the returned list ensuring
    * no duplicate references are returned.
    * @param category the runtime classloader category
    * @return the array of resources the define a classloader for the category
    */
    private DefaultResource[] getClasspathDefaultProviders( final Category category )
    {
        ArrayList list = new ArrayList();
        for( int i=0; i<category.getValue(); i++ )
        {   
            Category c = Category.parse( i );
            DefaultResource[] collection = 
              getDefaultProviders( Scope.RUNTIME, true, c );
            for( int j=0; j<collection.length; j++ )
            {
                list.add( collection[j] );
            }
        }
        DefaultResource[] resources = 
          getDefaultProviders( Scope.RUNTIME, true, category );
        ArrayList stack = new ArrayList();
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            if( !list.contains( resource ) )
            {
                stack.add( resources[i] );
            }
        }
        
        return (DefaultResource[]) stack.toArray( new DefaultResource[0] );
    }
    
    //----------------------------------------------------------------------------
    // sorting relative to dependencies
    //----------------------------------------------------------------------------
    
    DefaultResource[] sortDefaultResources( final DefaultResource[] resources )
    {
        return sortDefaultResources( resources, Scope.TEST );
    }
    
    DefaultResource[] sortDefaultResources( final DefaultResource[] resources, final Scope scope )
    {
        ArrayList visited = new ArrayList();
        ArrayList stack = new ArrayList();
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            resource.sortDefaultResource( visited, stack, scope, resources );
        }
        return (DefaultResource[]) stack.toArray( new DefaultResource[0] );
    }
    
    void sortDefaultResource( 
      final List visited, final List stack, final Scope scope, final DefaultResource[] resources )
    {
        if( visited.contains( this ) )
        {
            return;
        }
        else
        {
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
            if( !stack.contains( this ) )
            {
                stack.add( this );
            }
        }
    }
    
    boolean isaMember( final DefaultResource[] resources, final DefaultResource resource )
    {
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource r = resources[i];
            if( resource == r )
            {
                return true;
            }
        }
        return false;
    }
    
    //----------------------------------------------------------------------------
    // version utilities
    //----------------------------------------------------------------------------

    private String getStandardVersion()
    {
        String system = System.getProperty( "build.signature", SNAPSHOT );
        String value = getProperty( "build.signature", system );
        if( value.equals( "project.timestamp" ) )
        {
            return TIMESTAMP;
        }
        else
        {
            return value;
        }
    }

   /**
    * Return the UTC YYMMDD.HHMMSSS signature of a date.
    * @return the UTC date-stamp signature
    */
    public static String getTimestamp()
    {
        return getTimestamp( new Date() );
    }
    
   /**
    * Return the UTC YYMMDD.HHMMSSS signature of a date.
    * @param date the date
    * @return the UTC date-stamp signature
    */
    public static String getTimestamp( final Date date )
    {
        final SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd.HHmmss" );
        sdf.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        return sdf.format( date );
    }
    
    //----------------------------------------------------------------------------
    // other utilities
    //----------------------------------------------------------------------------

    private File getAnchor()
    {
        if( null != m_parent )
        {
            File anchor = m_parent.getBaseDir();
            if( null != anchor )
            {
                return anchor;
            }
        }
        return m_library.getRootDirectory();
    }
    
    File getCanonicalFile( final File file )
    {
        try
        {
            return file.getCanonicalFile();
        }
        catch( IOException e )
        {
            final String error = 
              "internal error while attempting to convert the file ["
              + file
              + "] to its canonical representation.";
            throw new RuntimeException( error, e );
        }
    }
    
    private String getGroupName()
    {
        if( m_parent.isRoot() )
        {
            return null;
        }
        else
        {
            return m_parent.getResourcePath();
        }
    }
}

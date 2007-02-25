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

package dpml.library.impl;

import dpml.library.info.InfoDirective;
import dpml.library.info.TypeDirective;
import dpml.library.info.ResourceDirective;
import dpml.library.info.IncludeDirective;
import dpml.library.info.DependencyDirective;
import dpml.library.info.AbstractDirective;
import dpml.library.info.ValidationException;
import dpml.library.info.FilterDirective;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.TimeZone;
import java.util.Properties;
import java.util.Map;
import java.util.Hashtable;

import dpml.util.Category;
import net.dpml.lang.Version;

import dpml.library.Info;
import dpml.library.Filter;
import dpml.library.Library;
import dpml.library.Module;
import dpml.library.Resource;
import dpml.library.Type;
import dpml.library.Classifier;
import dpml.library.Scope;
import dpml.library.ResourceNotFoundException;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;
import net.dpml.transit.Layout;

import net.dpml.util.Resolver;


/**
 * Implementation of a resource.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultResource extends DefaultDictionary implements Resource, Resolver, Comparable
{
    private static final String LEGACY_DECIMAL_PREFIX_KEY = 
      "project.version-prefix.enabled";
    
   /**
    * Timestamp.
    */
    public static final String TIMESTAMP = getTimestamp();
    
   /**
    * Constant SNAPSHOT symbol.
    */
    public static final String SNAPSHOT = "SNAPSHOT";
    
   /**
    * Constant BOOTSTRAP symbol.
    */
    public static final String BOOTSTRAP = "BOOTSTRAP";
    
   /**
    * Constant RELEASE symbol.
    */
    public static final String RELEASE = "RELEASE";
    
   /**
    * Constant ANONYMOUS version symbol.
    */
    public static final String ANONYMOUS = "ANONYMOUS";
    
    private final DefaultLibrary m_library;
    private final ResourceDirective m_directive;
    private final DefaultModule m_parent;
    private final Type[] m_types;
    private final String[] m_typeNames;
    private final String m_path;
    private final File m_basedir;
    private final Map<String,Filter> m_filters = new Hashtable<String,Filter>();
    
   /**
    * Creation of a new default resource.
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
    }
    
   /**
    * Creation of a new default resource.
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
        
        TypeDirective[] types = directive.getTypeDirectives();
        m_types = new Type[types.length];
        m_typeNames = new String[ types.length ];
        for( int i=0; i<types.length; i++ )
        {
            TypeDirective type = types[i];
            m_types[i] = new DefaultType( this, type );
            m_typeNames[i] = type.getID();
        }
        
        // setup the resource basedir
        
        File anchor = getAnchor();
        String filename = m_directive.getBasedir();
        if( null != filename )
        {
            String spec = resolve( filename );
            File file = new File( spec );
            if( file.isAbsolute() )
            {
                m_basedir = getCanonicalFile( file );
            }
            else
            {
                File basedir = new File( anchor, spec );
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
            setProperty( "project.version", version );
        }
        
        // setup filters
        
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
    /*
    public String getVersion( Type type )
    {
        Version version = type.getTypeVersion();
        if( null != version )
        {
            return version.toString();
        }
        else
        {
            return getVersion();
        }
    }
    */
    
   /**
    * Return the resource version.
    * @return the version
    */
    public String getVersion()
    {
        String version = getStatutoryVersion();
        if( null != version )
        {
            return version;
        }
        else if( m_directive.isAnonymous() )
        {
            return version;
        }
        else
        {
            return getStandardVersion();
        }
    }
    
   /**
    * Return the declard resource version.
    * @return the statutory version
    */
    public String getStatutoryVersion()
    {
        if( null == m_directive )
        {
            return null;
        }
        else
        {
            String version = m_directive.getVersion();
            if( ( null != version ) || m_directive.isAnonymous() )
            {
                return version;
            }
            else
            {
                if( null != m_parent )
                {
                    return m_parent.getStatutoryVersion();
                }
                else if( !m_directive.getClassifier().equals( Classifier.LOCAL ) )
                {
                    return ANONYMOUS;
                }
                else
                {
                    return null;
                }
            }
        }
    }

   /**
    * Return the decimal version.
    *
    * @return the version
    */
    public Version getDecimalVersion()
    {
        int major = getMajorVersion();
        int minor = getMinorVersion();
        int micro = getMicroVersion();
        return new Version( major, minor, micro );
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
        boolean flag = id.indexOf( '#' ) > -1;
        if( flag )
        {
            for( int i=0; i<m_types.length; i++ )
            {
                Type type = m_types[i];
                if( type.getCompoundName().equals( id ) )
                {
                    return type;
                }
            }
        }
        else
        {
            for( int i=0; i<m_types.length; i++ )
            {
                Type type = m_types[i];
                if( type.getID().equals( id ) )
                {
                    return type;
                }
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
    /*
    public Artifact getLinkArtifact( final Type type )
    {
        if( null == m_directive )
        {
            final String error = 
              "Method not supported on virtual root.";
            throw new UnsupportedOperationException( error );
        }
        if( null == type )
        {
            throw new NullPointerException( "type" );
        }
        
        String id = type.getID();
        String group = getGroupName();
        String name = getName( type );
        
        //Version version = type.getVersion();
        //if( null == version )
        //{
        //    final String error = 
        //      "Resource does not declare production of an alias for the requested type."
        //      + "\nResource: " + this
        //      + "\nType: " + id;
        //    throw new IllegalArgumentException( error );
        //}
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
            //if( !Version.NULL_VERSION.equals( version ) )
            //{
            //    int major = version.getMajor();
            //    int minor = version.getMinor();
            //    spec = spec + "#"
            //      + major
            //      + "."
            //      + minor;
            //}
            
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
    */

   /**
    * Construct an artifact for the supplied type.
    * @param id the resource type identifier
    * @return the artifact
    */
    /*
    public Artifact getArtifact( final Type type, boolean resolve )
    {
        if( null == m_directive )
        {
            final String error = 
              "Method not supported on virtual root.";
            throw new UnsupportedOperationException( error );
        }
        if( null == type )
        {
            throw new NullPointerException( "type" );
        }
        
        String id = type.getID();
        String group = getGroupName();
        String name = getName( type );
        String version = getVersion( type );
        String scheme = m_directive.getScheme();
        
        if( !resolve )
        {
            return Artifact.createArtifact( scheme, group, name, version, id );
        }
        else
        {
            try
            {
                Artifact artifact = Artifact.createArtifact( scheme, group, name, version, id );
                URL url = artifact.toURL();
                url.openConnection().connect();
                return artifact;
            }
            catch( IOException ioe )
            {
                final String error = 
                  "Unable to resolve the artifact for the type ["
                  + type.getID()
                  + "] on the resource ["
                  + getResourcePath()
                  + "].";
                throw new IllegalStateException( error, ioe );
            }
        }
    }
    */
    
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
            Map<String,Filter> map = new Hashtable<String,Filter>();
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
            return map.values().toArray( new Filter[0] );
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
    * @exception URISyntaxException if an error occurs during uri creation
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
                Type t = resource.getType( type );
                if( ref.startsWith( "resource:" ) )
                {
                    Artifact artifact = t.getArtifact();
                    return artifact.toURI();
                }
                else
                {
                    Artifact artifact = t.getLinkArtifact();
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
        return (Filter[]) m_filters.values().toArray( new Filter[0] );
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
        DefaultResource[] result = getAggregatedDefaultProviders( scope, true, true, true );
        List<DefaultResource> stack = new ArrayList<DefaultResource>();
        for( int i=0; i<result.length; i++ )
        {
            DefaultResource resource = result[i];
            if( resource.isa( "jar" ) )
            {
                stack.add( resource );
            }
        }
        return stack.toArray( new DefaultResource[0] );
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
    /*
    public String getLayoutPath( final String id ) // <-- what about test types?
    {
        Type type = getType( id );
        Artifact artifact = getArtifact( type, false );
        return Transit.getInstance().getCacheLayout().resolveFilename( artifact );
    }
    */

   /**
    * Return a filename of a produced type.
    * @param id the type id
    * @return the filename
    */
    
    /*
    public File getFile( final Type type )
    {
        return getFile( type, false );
    }
    
    public File getFile( final Type type, boolean local ) // <!-- TODO: policy concerning versions on test types
    {
        if( null == type )
        {
            throw new NullPointerException( "type" );
        }
        try
        {
            if( type.getTest() )
            {   
                String id = type.getID();
                String name = getName( type );
                String path = "target/test/" + name + "." + id;
                File basedir = getBaseDir();
                return new File( basedir, path );
            }
            else
            {
                if( local )
                {
                    String id = type.getID();
                    String name = getName( type );
                    String version = getVersion( type );
                    String path = "target/deliverables/" + id +  "s/" + name;
                    if( null != version )
                    {
                        path = path + "-" + version;
                    }
                    path = path + "." + id;
                    File basedir = getBaseDir();
                    return new File( basedir, path );
                }
                else
                {
                    Artifact artifact = getArtifact( type, false );
                    Layout layout = Transit.getInstance().getCacheLayout();
                    File cache = Transit.getInstance().getCacheDirectory();
                    String path = layout.resolvePath( artifact );
                    return new File( cache, path );
                }
            }
        }
        catch( Exception e )
        {
            final String error = 
              "Unable to resolve resource path.";
            throw new RuntimeException( error, e );
        }
    }
    */
    
   /**
    * Return a filename of a produced type.
    * @param id the type id
    * @return the filename
    */
    /*
    public String getName( final Type type )
    {
        if( null == type )
        {
            throw new NullPointerException( "type" );
        }
        String name = type.getName();
        if( null == name )
        {
            return getName();
        }
        else
        {
            return name;
        }
    }
    */
    
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
        List<TypeDirective> list = new ArrayList<TypeDirective>();
        for( int i=0; i<types.length; i++ )
        {
            TypeDirective type = types[i];
            boolean export = type.getExport();
            if( export )
            {
                boolean test = type.getTest();
                if( !test )
                {
                    list.add( type );
                }
            }
        }
        return list.toArray( new TypeDirective[0] );
    }
    
    private DependencyDirective[] createDeps( final DefaultModule module )
    {
        ArrayList<IncludeDirective> list = new ArrayList<IncludeDirective>();
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
            IncludeDirective[] includes = list.toArray( new IncludeDirective[0] );
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
      final DefaultModule module, final List<IncludeDirective> list, final Category category )
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
                if( !list.contains( include ) )
                {
                    list.add( include );
                }
            }
            else
            {
                // create a urn
                
                Type[] types = provider.getTypes();
                for( int j=0; j<types.length; j++ )
                {
                    Type type = types[j];
                    String label = type.getID();
                    Artifact artifact = type.getArtifact();
                    String urn = artifact.toString();
                    IncludeDirective include = 
                      new IncludeDirective( 
                        IncludeDirective.URI,
                        category,
                        urn,
                        null );
                    if( !list.contains( include ) )
                    {
                        list.add( include );
                    }
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
        if( null != getVersion() )
        {
            return type + ":" + getResourcePath() + "#" + getVersion();
        }
        else
        {
            return type + ":" + getResourcePath();
        }
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
      final Scope scope, final boolean expanded, final boolean sort, final boolean flag )
    {
        DefaultResource[] resources = 
          getAggregatedDefaultProviders( scope, expanded, flag );
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
      final Scope scope, final boolean expanded, final boolean flag )
    {
        ArrayList<DefaultResource> list = new ArrayList<DefaultResource>();
        if( !flag )
        {
            aggregateProviders( list, Scope.BUILD );
        }
        if( scope.compareTo( Scope.BUILD ) > 0 )
        {
            aggregateProviders( list, Scope.RUNTIME );
        }
        if( scope.compareTo( Scope.RUNTIME ) > 0 )
        {
            aggregateProviders( list, Scope.TEST );
        }
        DefaultResource[] result = list.toArray( new DefaultResource[0] );
        if( expanded )
        {
            List<DefaultResource> visited = new ArrayList<DefaultResource>();
            List<DefaultResource> stack = new ArrayList<DefaultResource>();
            for( int i=0; i<result.length; i++ )
            {
                DefaultResource resource = result[i];
                expandDefaultResource( visited, stack, scope, resource );
            }
            result = stack.toArray( new DefaultResource[0] );
        }
        return result;
    }

    private void aggregateProviders( final List<DefaultResource> list, final Scope scope )
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
        ArrayList<DefaultResource> visited = new ArrayList<DefaultResource>();
        ArrayList<DefaultResource> stack = new ArrayList<DefaultResource>();
        DefaultResource[] providers = getLocalDefaultProviders( scope, category );
        for( int i=0; i<providers.length; i++ )
        {
            DefaultResource provider = providers[i];
            if( expand )
            {
                expandDefaultResource( visited, stack, scope, provider );
            }
            else if( !stack.contains( provider ) )
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
    
    private void expandDefaultResource( 
      final List<DefaultResource> visited, final List<DefaultResource> stack, 
      final Scope scope, final DefaultResource resource )
    {
        if( visited.contains( resource ) )
        {
            return;
        }
        else
        {
            visited.add( resource );
            boolean flag = !scope.equals( Scope.BUILD );
            DefaultResource[] providers = resource.getAggregatedDefaultProviders( scope, false, flag );
            for( int i=0; i<providers.length; i++ )
            {
                DefaultResource provider = providers[i];
                expandDefaultResource( visited, stack, scope, provider );
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
            ArrayList<DefaultResource> list = new ArrayList<DefaultResource>();
            DefaultResource[] resources = m_library.selectDefaultResources( "**/*" );
            for( int i=0; i<resources.length; i++ )
            {
                DefaultResource resource = resources[i];
                if( !list.contains( resource ) && resource.isaConsumer( this ) )
                {
                    list.add( resource );
                }
            }
            return list.toArray( new DefaultResource[0] );
        }
        else
        {
            ArrayList<DefaultResource> visited = new ArrayList<DefaultResource>();
            ArrayList<DefaultResource> stack = new ArrayList<DefaultResource>();
            DefaultResource[] consumers = getDefaultConsumers( false );
            for( int i=0; i<consumers.length; i++ )
            {
                DefaultResource consumer = consumers[i];
                processConsumer( visited, stack, consumer );
            }
            return (DefaultResource[]) stack.toArray( new DefaultResource[0] );
        }
    }
    
    void processConsumer( 
      final List<DefaultResource> visited, final List<DefaultResource> stack, 
      final DefaultResource consumer )
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
        ArrayList<DefaultResource> list = new ArrayList<DefaultResource>();
        for( int i=0; i<category.ordinal(); i++ )
        {   
            Category c = Category.valueOf( i );
            DefaultResource[] collection = 
              getDefaultProviders( Scope.RUNTIME, true, c );
            for( int j=0; j<collection.length; j++ )
            {
                list.add( collection[j] );
            }
        }
        DefaultResource[] resources = 
          getDefaultProviders( Scope.RUNTIME, true, category );
        ArrayList<DefaultResource> stack = new ArrayList<DefaultResource>();
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            if( resource.isa( "jar" ) && !list.contains( resource ) )
            {
                stack.add( resource );
            }
        }
        
        return stack.toArray( new DefaultResource[0] );
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
        ArrayList<DefaultResource> visited = new ArrayList<DefaultResource>();
        ArrayList<DefaultResource> stack = new ArrayList<DefaultResource>();
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            resource.sortDefaultResource( visited, stack, scope, resources );
        }
        return stack.toArray( new DefaultResource[0] );
    }
    
    void sortDefaultResource( 
      final List<DefaultResource> visited, final List<DefaultResource> stack, 
      final Scope scope, final DefaultResource[] resources )
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
        String signature = getBuildSignature();
        if( BOOTSTRAP.equals( signature ) )
        {
            return BOOTSTRAP;
        }
        
        if( isDecimal() )
        {
            Version decimal = getDecimalVersion();
            String spec = decimal.toString();
            if( null == signature )
            {
                return spec;
            }
            else
            {
                return spec + "-" + signature;
            }
        }
        else
        {
            if( null == signature )
            {
                return SNAPSHOT;
            }
            else
            {
                return signature;
            }
        }
    }
    
    private boolean isDecimal()
    {
        boolean isDecimal = Boolean.getBoolean( DECIMAL_VERSIONING_KEY );
        isDecimal = getBooleanProperty( DECIMAL_VERSIONING_KEY, isDecimal );
        return getBooleanProperty( LEGACY_DECIMAL_PREFIX_KEY, isDecimal );
    }
    
    private String getBuildSignature()
    {
        String system = System.getProperty( "build.signature", null );
        return getProperty( "build.signature", system );
    }
    
    /*
    private String getBuildSignature()
    {
        String system = System.getProperty( "build.signature", null );
        String value = getProperty( "build.signature", system );
        if( null == value )
        {
            return SNAPSHOT;
        }
        else if( value.equals( "project.timestamp" ) )
        {
            return TIMESTAMP;
        }
        else
        {
            return value;
        }
    }
    */
    
    private int getMajorVersion()
    {
        return getIntegerProperty( "project.major.version", 0 );
    }
    
    private int getMinorVersion()
    {
        return getIntegerProperty( "project.minor.version", 0 );
    }
    
    private int getMicroVersion()
    {
        return getIntegerProperty( "project.micro.version", 0 );
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
    
    String getGroupName()
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
    
    String getScheme()
    {
        return m_directive.getScheme();
    }
}

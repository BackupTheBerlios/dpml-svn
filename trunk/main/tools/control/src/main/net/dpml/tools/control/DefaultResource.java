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
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Type;
import net.dpml.tools.model.Module;
import net.dpml.tools.model.TypeNotFoundException;
import net.dpml.tools.model.ResourceNotFoundException;
import net.dpml.tools.model.ProcessorNotFoundException;
import net.dpml.tools.info.TypeDirective;
import net.dpml.tools.info.TypeUnknownException;
import net.dpml.tools.info.ResourceDirective;
import net.dpml.tools.info.ResourceDirective.Classifier;
import net.dpml.tools.info.IncludeDirective;
import net.dpml.tools.info.DependencyDirective;
import net.dpml.tools.info.AbstractDirective;
import net.dpml.tools.info.ValidationException;
import net.dpml.tools.info.Scope;

import net.dpml.transit.Artifact;
import net.dpml.transit.Category;

/**
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultResource extends DefaultDictionary implements Resource, Comparable
{
    public static final String TIMESTAMP = getTimestamp();
    public static final String SNAPSHOT = "SNAPSHOT";
    
    private final DefaultLibrary m_library;
    private final ResourceDirective m_directive;
    private final DefaultModule m_parent;
    private final DefaultType[] m_types;
    private final String[] m_typeNames;
    private final String m_path;
    private final File m_basedir;
    
    DefaultResource( DefaultLibrary library, AbstractDirective directive )
    {
        super( library, directive );
        
        m_library = library;
        m_directive = null;
        m_parent = null;
        m_types = new DefaultType[0];
        m_typeNames = new String[0];
        m_path = "";
        m_basedir = null;
    }
    
    DefaultResource( DefaultLibrary library, DefaultModule module, ResourceDirective directive ) 
      throws ProcessorNotFoundException, ValidationException
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
        
        try
        {
            m_types = resolveTypes( directive );
            m_typeNames = new String[ m_types.length ];
            for( int i=0; i<m_types.length; i++ )
            {
                Type type = m_types[i];
                m_typeNames[i] = type.getName();
            }
        }
        catch( InvalidProcessorNameException e )
        {
            final String error = 
              "Unable to create resource ["
              + directive.getName() 
              + "] in ["
              + this
              + "] due to a reference to the resource type ["
              + e.getMessage()
              + "] that does not have a corresponding processor type definition.";
            throw new ProcessorNotFoundException( error, e );
        }
        
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
            if( m_directive.getClassifier().equals( Classifier.EXTERNAL ) )
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
    }
    
    //----------------------------------------------------------------------------
    // Resource
    //----------------------------------------------------------------------------
    
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
    public boolean isa( String type )
    {
        for( int i=0; i<m_types.length; i++ )
        {
            Type someType = m_types[i];
            String name = someType.getName();
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
    public Type getType( String id )
    {
        for( int i=0; i<m_types.length; i++ )
        {
            Type type = m_types[i];
            if( type.getName().equals( id ) )
            {
                return type;
            }
        }
        throw new InvalidTypeNameException( id );
    }
    
   /**
    * Construct an artifact for the supplied type.
    * @param id the resource type identifier
    * @return the artifact
    */
    public Artifact getArtifact( String id )
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
        if( null == m_parent )
        {
            final String error = 
              "Artifact protocol does not support null groups.";
            throw new UnsupportedOperationException( error );
        }
        String group = getParent().getResourcePath();
        String name = getName();
        String version = getVersion();
        return Artifact.createArtifact( group, name, version, id );
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
    * Return an array of resource that are providers to this resource.
    * @param scope the operational scope
    * @param expand if true include transitive dependencies
    * @param sort if true the array will sorted relative to dependencies
    * @return the resource providers
    */
    public Resource[] getProviders( Scope scope, boolean expand, boolean sort )
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
    public Resource[] getAggregatedProviders( Scope scope, boolean expand, boolean sort )
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
    public Resource[] getClasspathProviders( Scope scope )
    {
        boolean expanded = true;
        boolean sorted = true;
        boolean filtered = true;
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
    public Resource[] getClasspathProviders( Category category )
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
    public Resource[] getConsumers( boolean expand, boolean sort )
    {
        return getDefaultConsumers( expand, sort );
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
                return "project:" + getResourcePath();
            }
        }
        return "resource:" + getResourcePath();
    }
    
    public int compareTo( Object other )
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
      Scope scope, boolean expanded, boolean sort, boolean filtered )
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
    
    DefaultResource[] getAggregatedDefaultProviders( Scope scope, boolean expanded, boolean filtered )
    {
        ArrayList list = new ArrayList();
        if( !filtered )
        {
            aggregateProviders( list, Scope.BUILD, expanded, filtered );
        }
        if( scope.isGreaterThan( Scope.BUILD ) )
        {
            aggregateProviders( list, Scope.RUNTIME, expanded, filtered );
        }
        if( scope.isGreaterThan( Scope.RUNTIME ) )
        {
            aggregateProviders( list, Scope.TEST, expanded, filtered );
        }
        return (DefaultResource[]) list.toArray( new DefaultResource[0] ); 
    }

    DefaultResource[] getDefaultProviders( Scope scope, boolean expanded, boolean sort ) 
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
    
    DefaultResource[] getDefaultProviders( Scope scope, boolean expand, Category category )
    {
        DefaultResource[] resources = getLocalDefaultProviders( scope, category );
        if( expand )
        {
            ArrayList visited = new ArrayList();
            ArrayList stack = new ArrayList();
            for( int i=0; i<resources.length; i++ )
            {
                DefaultResource resource = resources[i];
                processDefaultResource( visited, stack, scope, resource );
            }
            return (DefaultResource[]) stack.toArray( new DefaultResource[0] );
        }
        else
        {
            return resources;
        }
    }
    
    DefaultResource[] getLocalDefaultProviders( Scope scope, Category category ) 
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
            String ref = getIncludeReference( include );
            DefaultResource resource = m_library.getDefaultResource( ref );
            resources[i] = resource;
        }
        return resources;
    }
    
    private IncludeDirective[] getLocalIncludes( Scope scope, Category category )
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
      List visited, List stack, Scope scope, DefaultResource resource )
    {
        if( visited.contains( resource ) )
        {
            return;
        }
        else
        {
            visited.add( resource );
            //DefaultResource[] providers = resource.getDefaultProviders( scope, false, null );
            DefaultResource[] providers = resource.getAggregatedDefaultProviders( scope, false, false );
            for( int i=0; i<providers.length; i++ )
            {
                processDefaultResource( visited, stack, scope, providers[i] );
            }
            stack.add( resource );
        }
    }
    
    private void aggregateProviders( List list, Scope scope, boolean expanded, boolean filter )
    {
        DefaultResource[] resources = getDefaultProviders( scope, expanded, null );
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource resource = resources[i];
            if( !filter )
            {
                list.add( resource );
            }
            else if( resource.isa( Type.JAR ) )
            {
                list.add( resource );
            }
        }
    }
    
    private String getIncludeReference( IncludeDirective directive )
    {
        if( IncludeDirective.REF.equals( directive.getMode() ) || ( null == m_parent ) )
        {
            return directive.getValue();
        }
        else
        {
            String path = m_parent.getResourcePath();
            String key = directive.getValue();
            return path + "/" + key;
        }
    }
    
    //----------------------------------------------------------------------------
    // consumer concerns
    //----------------------------------------------------------------------------
    
    boolean isaConsumer( DefaultResource resource )
    {
        //System.out.println( "  ## checking " + this );
        DefaultResource[] resources = getAggregatedDefaultProviders( Scope.TEST, false, false );
        for( int i=0; i<resources.length; i++ )
        {
            DefaultResource provider = resources[i];
            //System.out.println( "     ## provider == " + provider );
            if( resource.equals( provider ) )
            {
                //System.out.println( "     ## <---------------------- " + this );
                return true;
            }
        }
        return false;
    }
    
    DefaultResource[] getDefaultConsumers( boolean expand, boolean sort )
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
    
    DefaultResource[] getDefaultConsumers( boolean expand )
    {
        if( !expand )
        {
            ArrayList list = new ArrayList();
            DefaultResource[] resources = m_library.selectDefaultResources( "**/*" );
            for( int i=0; i<resources.length; i++ )
            {
                DefaultResource resource = resources[i];
                //System.out.println( "# eval: " + resource );
                if( !list.contains( resource ) && resource.isaConsumer( this ) )
                {
                    //System.out.println( "#   --> is a consumer: " + resource );
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
    
    void processConsumer( List visited, List stack, DefaultResource consumer )
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
    private DefaultResource[] getClasspathDefaultProviders( Category category )
    {
        ArrayList list = new ArrayList();
        for( int i=0; i<category.getValue(); i++ )
        {
            DefaultResource[] collection = 
              getDefaultProviders( Scope.RUNTIME, true, Category.values()[i] );
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
    
    DefaultResource[] sortDefaultResources( DefaultResource[] resources, Scope scope )
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
      List visited, List stack, Scope scope, DefaultResource[] resources )
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
            stack.add( this );
        }
    }
    
    boolean isaMember( DefaultResource[] resources, DefaultResource resource )
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
    // type utilities
    //----------------------------------------------------------------------------
    
   /**
    * Internal utility that resolves an array of types based on a supplied resource
    * directive.  The implementation expands type names based on processor declared 
    * dependencies and as such a resource may expose more types than it declares
    * directly. In the case of a implied type, an empty type instance is assigned
    * otherwise the type instance is based on the declared type and any associated
    * properties.
    */
    private DefaultType[] resolveTypes( ResourceDirective directive )
    {
        String[] names = resolveTypeNames( directive );
        DefaultType[] types = new DefaultType[ names.length ];
        for( int i=0; i<names.length; i++ )
        {
            String name = names[i];
            try
            {
                TypeDirective type = directive.getTypeDirective( name );
                types[i] = new DefaultType( this, type );
            }
            catch( TypeUnknownException e )
            {
                TypeDirective type = new TypeDirective( name );
                types[i] = new DefaultType( this, type );
            }
        }
        return types;
    }
    
   /**
    * Utility operation to resolved the expanded set of type names.
    */
    private String[] resolveTypeNames( ResourceDirective directive )
    {
        TypeDirective[] types = directive.getTypeDirectives();
        String[] names = new String[ types.length ];
        for( int i=0; i<types.length; i++ )
        {
            TypeDirective type = types[i];
            names[i] = type.getName();
        }
        return m_library.expandTypeNames( names );
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
    
    File getCanonicalFile( File file )
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
    
}

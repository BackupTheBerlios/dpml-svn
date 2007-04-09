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

package dpml.library.info;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Properties;

import dpml.library.Classifier;
import dpml.library.Scope;

import net.dpml.transit.Artifact;

/**
 * The ResourceDirective class describes an available resource.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ResourceDirective extends AbstractDirective
{
   /**
    * External resource constant identifier.
    */
    public static final Classifier EXTERNAL = Classifier.EXTERNAL;
    
   /**
    * Local resource constant identifier.
    */
    public static final Classifier LOCAL = Classifier.LOCAL;
     
   /**
    * Anonymous resource constant identifier.
    */
    public static final Classifier ANONYMOUS = Classifier.ANONYMOUS;
    
    private final String m_scheme;
    private final String m_name;
    private final String m_version;
    private final String m_basedir;
    private final TypeDirective[] m_types;
    private final DependencyDirective[] m_dependencies;
    private final Classifier m_classifier;
    private final FilterDirective[] m_filters;
    private final InfoDirective m_info;
    private final boolean m_export;
    
   /**
    * Creation of a new anonymous resource directive.
    * @param scheme the schema name
    * @param name the resource name
    * @param version the resource version
    * @param type the resource type
    * @param properties suppliementary properties
    * @return the resource directive
    */
    public static ResourceDirective createAnonymousResource( 
      String scheme, String name, String version, String type, Properties properties )
    {
        return new ResourceDirective( scheme, name, type, version, properties, true );
    }

   /**
    * Creation of a new resource directive.  If the resource name if composite
    * then the resource directive will be a module directive instance that either 
    * encloses the resource or enclosed a resource containing the resource.
    *
    * @param name the resource name
    * @param version the resource version
    * @param classifier LOCAL or EXTERNAL classifier
    * @param basedir the project basedir
    * @param info info descriptor
    * @param types types produced by the resource
    * @param dependencies resource dependencies
    * @param properties suppliementary properties
    * @param filters project filters
    * @return the immediate enclosing resource
    */
    public static ResourceDirective createResourceDirective( 
      String name, String version, Classifier classifier, String basedir, 
      InfoDirective info, TypeDirective[] types, 
      DependencyDirective[] dependencies, Properties properties, 
      FilterDirective[] filters, boolean export )
    {
        int n = name.indexOf( "/" );
        if( n > -1 )
        {
            ResourceDirective resource = null;
            String[] elements = name.split( "/", -1 );
            for( int i = ( elements.length-1 ); i>-1; i-- )
            {
                String elem = elements[i];
                if( i == ( elements.length-1 ) )
                {
                    resource =  
                      new ResourceDirective(
                        elem, version, classifier, basedir, info, types, dependencies,
                        properties, filters, export );
                }
                else
                {
                    resource = 
                      new ModuleDirective(
                        elem, null, Classifier.EXTERNAL, null, null,
                        new TypeDirective[0], new DependencyDirective[0],
                        new ResourceDirective[]{resource}, null, null, true );
                }
            }
            return resource;
        }
        else
        {
            return new ResourceDirective(
              name, version, classifier, basedir, info, types, 
              dependencies, properties, filters, export );
        }
    }

   /**
    * Creation of a new annonomous resource directive.
    * @param scheme the resource schema (artifact or link)
    * @param name the resource name
    * @param version the resource version
    * @param properties suppliementary properties
    */
    private ResourceDirective( 
      String scheme, String name, String type, String version, Properties properties, boolean export )
    {
        super( properties );
        
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        
        m_name = name;
        m_types = new TypeDirective[]{new TypeDirective( type )};
        m_dependencies = new DependencyDirective[0];
        m_classifier = Classifier.ANONYMOUS;
        m_version = version;
        m_basedir = ".";
        m_info = new InfoDirective( null, null );
        m_filters = new FilterDirective[0];
        m_scheme = scheme;
        m_export = export;
    }
    
   /**
    * Creation of a new resource directive.
    * @param name the resource name
    * @param version the resource version
    * @param classifier LOCAL or EXTERNAL classifier
    * @param basedir the project basedir
    * @param info info descriptor
    * @param data type production data
    * @param dependencies resource dependencies
    * @param properties suppliementary properties
    * @param filters suppliementary filters
    */
    ResourceDirective( 
      String name, String version, Classifier classifier, String basedir, 
      InfoDirective info, TypeDirective[] types, 
      DependencyDirective[] dependencies, Properties properties, 
      FilterDirective[] filters, boolean export )
    {
        super( properties );
        
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        if( null == types )
        {
            throw new NullPointerException( "types" );
        }
        if( null == dependencies )
        {
            throw new NullPointerException( "dependencies" );
        }
        if( null == classifier )
        {
            throw new NullPointerException( "classifier" );
        }
        
        m_name = name;
        m_version = version;
        m_basedir = basedir;
        m_classifier = classifier;
        m_scheme = Artifact.ARTIFACT;
        
        if( null == info )
        {
            m_info = new InfoDirective( null, null );
        }
        else
        {
            m_info = info;
        }
        
        m_dependencies = dependencies;
        
        if( null == filters )
        {
            m_filters = new FilterDirective[0];
        }
        else
        {
            m_filters = filters;
        }
        
        m_types = types;
        m_export = export;
    }
    
   /**
    * Return the resource name.
    * @return the name
    */
    public String getName()
    {
        return m_name;
    }
    
   /**
    * Return the resource version.
    * @return the version
    */
    public String getVersion()
    {
        return m_version;
    }
    
    
   /**
    * Return the export policy.
    * @return the export policy value
    */
    public boolean getExportPolicy()
    {
        return m_export;
    }
    
   /**
    * Return the resource basedir.
    * @return the basedir
    */
    public String getBasedir()
    {
        return m_basedir;
    }
    
   /**
    * Return the info descriptor.
    * @return the info descriptor
    */
    public InfoDirective getInfoDirective()
    {
        return m_info;
    }
    
   /**
    * Return the resource classifier.
    * @return the classifier (LOCAL, EXTERNAL or ANONYMOUS)
    */
    public Classifier getClassifier()
    {
        return m_classifier;
    }
    
   /**
    * Return true if this represents a local project.
    * @return true if local
    */
    public boolean isLocal()
    {
        return LOCAL.equals( m_classifier );
    }
    
   /**
    * Return true if this represents a anonymous resource.
    * @return true if anonymous
    */
    public boolean isAnonymous()
    {
        return ANONYMOUS.equals( m_classifier );
    }
    
   /**
    * Return the scheme to use when declaring the published artifact (used
    * in conjunction with anonymous resources.
    * @return the scheme
    */
    public String getScheme()
    {
        return m_scheme;
    }
    
   /**
    * Return an array of types representing artifacts associated
    * with the resource.
    * @return the type directives
    */
    public TypeDirective[] getTypeDirectives()
    {
        return m_types;
    }
    
   /**
    * Return an array of supplimentary filter directives.
    * @return the filter directives
    */
    public FilterDirective[] getFilterDirectives()
    {
        return m_filters;
    }
    
   /**
    * Return an named type.
    * @param name the type name
    * @return the type directives
    * @exception TypeUnknownException if the type name if not recornized within
    *   the scope of the resource
    */
    public TypeDirective getTypeDirective( String name ) throws TypeUnknownException
    {
        for( int i=0; i<m_types.length; i++ )
        {
            TypeDirective type = m_types[i];
            if( name.equals( type.getID() ) )
            {
                return type;
            }
        }
        throw new TypeUnknownException( name );
    }
    
   /**
    * Return an array of dependency directives.
    * @return the dependency directive array
    */
    public DependencyDirective[] getDependencyDirectives()
    {
        return m_dependencies;
    }
    
   /**
    * Return an dependency directive matching a supplied scope.
    * @param scope the scope
    * @return the dependency directive matching the supplied scope
    */
    public DependencyDirective getDependencyDirective( Scope scope )
    {
        for( int i=0; i<m_dependencies.length; i++ )
        {
            DependencyDirective directive = m_dependencies[i];
            if( scope.equals( directive.getScope() ) )
            {
                return directive;
            }
        }
        return new DependencyDirective( scope, new IncludeDirective[0] );
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ResourceDirective ) )
        {
            ResourceDirective object = (ResourceDirective) other;
            if( m_export != object.m_export )
            {
                return false;
            }
            if( !equals( m_name, object.m_name ) )
            {
                return false;
            }
            else if( !equals( m_scheme, object.m_scheme ) )
            {
                return false;
            }
            else if( !equals( m_version, object.m_version ) )
            {
                return false;
            }
            else if( !equals( m_basedir, object.m_basedir ) )
            {
                return false;
            }
            else if( !equals( m_info, object.m_info ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_types, object.m_types ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( m_dependencies, object.m_dependencies );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hash value.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= super.hashValue( m_name );
        hash ^= super.hashValue( m_scheme );
        hash ^= super.hashValue( m_version );
        hash ^= super.hashValue( m_basedir );
        hash ^= super.hashValue( m_info );
        hash ^= super.hashArray( m_types );
        hash ^= super.hashArray( m_dependencies );
        hash ^= super.hashArray( m_filters );
        if( m_export )
        {
            hash ^= 42;
        }
        return hash;
    }
}

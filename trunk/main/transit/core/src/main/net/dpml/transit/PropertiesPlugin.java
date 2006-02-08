/*
 * Copyright 2004-2005 Stephen J. McConnell.
 * Copyright 2004 Niclas Hedhman.
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

package net.dpml.transit;

import java.io.Serializable;

import java.net.URI;

import java.util.ArrayList;
import java.util.Properties;

/**
 * An implementation of a plugin descriptor based the original properties
 * external format.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class PropertiesPlugin
    implements Plugin, Serializable
{
    static final long serialVersionUID = 1L;

    //-----------------------------------------------------------
    // static
    //-----------------------------------------------------------

   /**
    * key to designate the namespace of the plugin descriptor.
    */
    static final String PP_NAMESPACE_KEY = "dpml.plugin.meta.namespace";

   /**
    * key to designate the version of the plugin descriptor.
    */
    static final String PP_META_VERSION_KEY = "dpml.plugin.meta.version";

   /**
    * key to designate an api entry.
    */
    static final String PP_SYS_KEY = "dpml.artifact.dependency.sys";

   /**
    * key to designate an api entry.
    */
    static final String PP_API_KEY = "dpml.artifact.dependency.api";

   /**
    * key to designate an spi entry.
    */
    static final String PP_SPI_KEY = "dpml.artifact.dependency.spi";

   /**
    * key to designate an impl entry.
    */
    static final String PP_IMP_KEY = "dpml.artifact.dependency";

   /**
    * key to designate w32 native libraries
    */
    static final String PP_NATIVE_W32_KEY = "dpml.artifact.native.w32";

   /**
    * key to designate nix native libraries
    */
    static final String PP_NATIVE_NIX_KEY = "dpml.artifact.native.nix";

   /**
    * key to designate the plugin classname.
    */
    static final String PP_FACTORY_KEY = "dpml.plugin.class";

   /**
    * key to designate a service export.
    */
    static final String PP_EXPORT_KEY = "dpml.artifact.export"; // <-- check me

   /**
    * key to designate the artifact group.
    */
    static final String PP_GROUP_KEY = "dpml.artifact.group";

   /**
    * key to designate the artifact name.
    */
    static final String PP_NAME_KEY = "dpml.artifact.name";

   /**
    * key to designate the artifact version.
    */
    static final String PP_VERSION_KEY = "dpml.artifact.version";

   /**
    * key to designate the artifact a resource path.
    */
    static final String PP_RESOURCE_KEY = "dpml.plugin.resource";

   /**
    * key to designate the artifact urn.
    */
    static final String PP_URN_KEY = "dpml.plugin.urn";

    //-----------------------------------------------------------
    // immutable state
    //-----------------------------------------------------------

   /**
    * The specification namespace identifier.
    */
    private final String m_specificationNamespace;

   /**
    * The specification namespace version.
    */
    private final String m_specificationVersion;

   /**
    * The set of uris representing the api.
    */
    private final URI[] m_sys;

   /**
    * The set of uris representing the api.
    */
    private final URI[] m_api;

   /**
    * The set of uris representing the spi.
    */
    private final URI[] m_spi;

   /**
    * The set of uris representing the implementation.
    */
    private final URI[] m_imp;

   /**
    * The set of uris representing the native w32 libraries.
    */
    private final URI[] m_w32;

   /**
    * The set of uris representing the native nix libraries.
    */
    private final URI[] m_nix;

   /**
    * Plugin group.
    */
    private final String m_group;

   /**
    * Plugin name.
    */
    private final String m_name;

   /**
    * Plugin version.
    */
    private final String m_version;

   /**
    * Plugin plugin uri.
    */
    private final URI m_uri;

   /**
    * Plugin plugin classname.
    */
    private final String m_factory;

   /**
    * Plugin plugin service.
    */
    private final String m_interface;

   /**
    * Plugin plugin resource path.
    */
    private final String m_resource;

   /**
    * Plugin plugin urn.
    */
    private final String m_urn;

    //-----------------------------------------------------------
    // constructor
    //-----------------------------------------------------------

    /**
     * Creates a new PropertiesPlugin.
     *
     * @param attributes the plugin descriptor attributes
     * @exception NullArgumentException if the supplied attributes are null
     * @exception RepositoryException if an error occurs reading the properties
     */
    PropertiesPlugin( final Properties attributes )
      throws RepositoryException, NullArgumentException
    {
        if( null == attributes )
        {
          throw new NullArgumentException( "attributes" );
        }

        m_specificationNamespace = attributes.getProperty( PP_NAMESPACE_KEY );
        if( null == m_specificationNamespace )
        {
            final String error = "Missing attribute: " + PP_NAMESPACE_KEY;
            throw new RepositoryException( error );
        }

        m_specificationVersion = attributes.getProperty( PP_META_VERSION_KEY );
        if( null == m_specificationVersion )
        {
            final String error = "Missing attribute: " + PP_META_VERSION_KEY;
            throw new RepositoryException( error );
        }

        m_group = attributes.getProperty( PP_GROUP_KEY, "" );
        m_name = attributes.getProperty( PP_NAME_KEY, "" );
        m_version = attributes.getProperty( PP_VERSION_KEY, "" );

        m_uri = Artifact.createArtifact( m_group, m_name, m_version, "plugin" ).toURI();

        m_w32 = buildDependents( attributes, PP_NATIVE_W32_KEY );
        m_nix = buildDependents( attributes, PP_NATIVE_NIX_KEY );
        m_sys = buildDependents( attributes, PP_SYS_KEY );
        m_api = buildDependents( attributes, PP_API_KEY );
        m_spi = buildDependents( attributes, PP_SPI_KEY );
        m_imp = buildDependents( attributes, PP_IMP_KEY );

        m_factory = attributes.getProperty( PP_FACTORY_KEY );
        m_interface = attributes.getProperty( PP_EXPORT_KEY );
        m_resource = attributes.getProperty( PP_RESOURCE_KEY );
        m_urn = attributes.getProperty( PP_URN_KEY );

        if( ( m_factory == null ) && ( m_resource == null ) )
        {
            final String error =
              "Unable to resolve either the plugin attribute ["
              + PP_FACTORY_KEY
              + "] or ["
              + PP_RESOURCE_KEY
              + "].";
            throw new RepositoryException( error );
        }
        if( ( m_resource != null ) && ( m_urn == null ) )
        {
            attributes.list( System.out );
            final String error =
              "Unable to resolve the plugin attribute ["
              + PP_URN_KEY
              + "].";
            throw new RepositoryException( error );
        }
    }

    //-----------------------------------------------------------
    // public
    //-----------------------------------------------------------

   /**
    * Return the uri referencing the desriptor.
    * @return the uri
    */
    public URI getURI()
    {
        return m_uri;
    }

   /**
    * Return the plugin specification value.
    * @return the namespace of the specification
    */
    public String getSpecificationNamespace()
    {
        return m_specificationNamespace;
    }

   /**
    * Return the plugin specification version
    * @return the version
    */
    public String getSpecificationVersion()
    {
        return m_specificationVersion;
    }

   /**
    * Test is the supplied object is equal to this object.
    * @param other the object to compare this object with
    * @return true if the objects are equivalent
    */
    public boolean equals( Object other )
    {
        boolean isEqual = other instanceof Plugin;
        if ( isEqual )
        {
            Plugin plugin = (Plugin) other;
            isEqual = isEqual && m_specificationNamespace.equals( plugin.getSpecificationNamespace() );
            isEqual = isEqual && m_specificationVersion.equals( plugin.getSpecificationVersion() );
        }
        return isEqual;
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = m_specificationNamespace.hashCode();
        hash ^= m_specificationVersion.hashCode();
        hash ^= m_group.hashCode();
        hash ^= m_version.hashCode();
        return hash;
    }

   /**
    * Return the factory classname.
    * @return the classname
    */
    public String getClassname()
    {
        return m_factory;
    }

   /**
    * Return the factory interface.
    * @return the interface classname
    */
    public String getInterface()
    {
        return m_interface;
    }

   /**
    * Return the resource path.
    * @return the resource path
    */
    public String getResource()
    {
        return m_resource;
    }

   /**
    * Return the antlib urn
    * @return the urn
    */
    public String getURN()
    {
        return m_urn;
    }

   /**
    * Return the implementation dependencies
    * @param key one of classloader group identifiers (api, spi, impl)
    * @return the set of URIs
    */
    public URI[] getDependencies( Category key )
    {
        //if( key == Category.ANY )
        //{
        //    return getDependencies();
        //}
        if( key == Category.SYSTEM )
        {
            return m_sys;
        }
        else if( key == Category.PUBLIC )
        {
            return m_api;
        }
        else if( key == Category.PROTECTED )
        {
            return m_spi;
        }
        else if( key == Category.PRIVATE )
        {
            return m_imp;
        }
        else
        {
            final String error =
              "Invalid dependency key: " + key;
            throw new IllegalArgumentException( error );
        }
    }
    
   /**
    * Return the native dependencies
    *
    * @return the uris to native libraries
    */
    public URI[] getNativeDependencies()
    {
        if( Environment.isUnix() )
        {
            return m_nix;
        }
        else
        {
            return m_w32;
        }
    }
    
   /**
    * Return all dependencies
    * @return the set of URIs
    */
    public URI[] getDependencies()
    {
        int j = m_sys.length + m_api.length + m_spi.length + m_imp.length;
        URI[] all = new URI[ j ];
        int q = 0;
        for( int i=0; i < m_sys.length; i++ )
        {
            all[q] = m_sys[i];
            q++;
        }
        for( int i=0; i < m_api.length; i++ )
        {
            all[q] = m_api[i];
            q++;
        }
        for( int i=0; i < m_spi.length; i++ )
        {
            all[q] = m_spi[i];
            q++;
        }
        for( int i=0; i < m_imp.length; i++ )
        {
            all[q] = m_imp[i];
            q++;
        }
        return all;
    }

   /**
    * Return a stringified representation of the instance.
    * @return the string representation
    */
    public String toString()
    {
        return "[plugin: " + getSpecificationNamespace()
          + "::" + getSpecificationVersion() + "]";
    }

    //-----------------------------------------------------------
    // private
    //-----------------------------------------------------------

   /**
    * Return the set of dependencies relative to a supplied category.
    * @param attributes the properties source
    * @param key the category key
    * @return the set of uris matching the category
    */
    private URI[] buildDependents( Properties attributes, final String key )
    {
        ArrayList result = new ArrayList();
        int counter = 0;
        while( true )
        {
            String lookup = key + "." + counter;
            counter++;
            final String spec = attributes.getProperty( lookup );
            if( spec == null )
            {
                break;
            }
            URI uri = URI.create( spec );
            Artifact.createArtifact( uri ); // validate spec
            result.add( uri );
        }
        URI[] dependencies = new URI[ result.size() ];
        result.toArray( dependencies );
        return dependencies;
    }
    
}

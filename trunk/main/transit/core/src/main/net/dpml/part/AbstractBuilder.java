/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.part;

import java.net.URI;
import java.util.Map;
import java.util.Hashtable;

import net.dpml.lang.Builder;
import net.dpml.lang.BuilderException;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;
import net.dpml.transit.Repository;

import org.w3c.dom.Element;
import org.w3c.dom.TypeInfo;

/**
 * Utility class supporting resolution of element builders.
 */
public class AbstractBuilder
{
    static final String PART_XSD_URI = "@PART-XSD-URI@";
    
    static final URI LOCAL_URI = createURI( "local:dpml" );
    
    private final Map m_map; // maps namespace uris to builders
    
   /**
    * Creation of a new abstract builder.
    * @param map the naespace to builder uri map
    */
    public AbstractBuilder( Map map )
    {
        if( null == map )
        {
            m_map = new Hashtable();
        }
        else
        {
            m_map = map;
        }
        if( !m_map.containsKey( "dpml/lang/dpml-part" ) )
        {
            m_map.put( "dpml/lang/dpml-part", LOCAL_URI );
        }
    }
    
   /**
    * Return the namespace to builder uri map.
    * @return the map
    */
    protected Map getMap()
    {
        return m_map;
    }
    
   /**
    * Get the builder based on the namespace declared by the supplied element.
    * @param element the DOM element
    * @return the builder
    * @exception Exception if an eror occurs
    */
    public Builder getBuilder( Element element ) throws Exception
    {
        return loadBuilder( element );
    }

   /**
    * Get a strategy builder based on the namespace declared by the supplied element.
    * @param element the DOM element
    * @return the strategy builder
    * @exception Exception if an eror occurs
    */
    protected StrategyBuilder getStrategyBuilder( Element element ) throws Exception
    {
        Builder builder = loadBuilder( element );
        if( builder instanceof StrategyBuilder )
        {
            return (StrategyBuilder) builder;
        }
        else
        {
            final String error = 
              "Builder ["
              + builder.getClass().getName()
              + "] is not an instance of "
              + StrategyBuilder.class.getName()
              + ".";
            throw new BuilderException( element, error );
        }
    }
    
   /**
    * Get a strategy builder based on the namespace declared by the supplied element.
    * @param element the DOM element
    * @return the strategy builder
    * @exception Exception if an eror occurs
    */
    protected Builder loadBuilder( Element element ) throws Exception
    {
        URI uri = getBuilderURI( element );
        if( LOCAL_URI.equals( uri ) )
        {
            TypeInfo info = element.getSchemaTypeInfo();
            String name = info.getTypeName();
            if( "plugin".equals( name ) || "resource".equals( name ) )
            {
                return new PartStrategyBuilder( m_map );
            }
            else if( "part".equals( name ) )
            {
                return new PartBuilder( m_map );
            }
        }
        return new DelegatingBuilder( uri );
    }
    
   /**
    * Internal utility to load a builder using the supplied uri and validation
    * that the object established from the uri is type assignable to the supplied class.
    *
    * @param uri the builder part uri
    * @param clazz the class to test the resolved instance for assignability
    * @return the object assignable to the supplied class
    * @exception Exception if an eror occurs
    */
    protected Object loadObjectFromURI( URI uri, Class clazz ) throws Exception
    {
        if( LOCAL_URI.equals( uri ) )
        {
            if( StrategyBuilder.class.isAssignableFrom( clazz ) )
            {
                return new PartStrategyBuilder( m_map );
            }
            else
            {
                final String error = 
                  "Unexpected request to load a local object."
                  + "\nClass: " + clazz.getName();
                throw new IllegalArgumentException( error );
            }
        }
        ClassLoader classloader = getClassLoader();        
        Repository repository = Transit.getInstance().getRepository();
        Object object = repository.getPlugin( classloader, uri, new Object[0] );
        if( clazz.isAssignableFrom( object.getClass() ) )
        {
            return object;
        }
        else
        {
            final String error = 
              "Plugin ["
              + uri
              + "] is not assignable to "
              + clazz.getName()
              + ".";
            throw new IllegalArgumentException( error );
        }
    }
    
   /**
    * Resolve the builder uri from a supplied element.
    *
    * @param element the DOM element
    * @return the builder uri
    * @exception Exception if an eror occurs
    */
    protected URI getBuilderURI( Element element ) throws Exception
    {
        TypeInfo info = element.getSchemaTypeInfo();
        String namespace = info.getTypeNamespace();
        if( PART_XSD_URI.equals( namespace ) )
        {
            return LOCAL_URI;
        }
        else if( m_map.containsKey( namespace ) )
        {
            return (URI) m_map.get( namespace );
        }
        else
        {
            String name = info.getTypeName();
            String path = namespace + "#" + name;
            if( m_map.containsKey( path ) )
            {
                return (URI) m_map.get( path );
            }
            else
            {
                final String error = 
                  "Unable to resolve part builder."
                  + "\nNamespace: " + namespace;
                throw new BuilderException( element, error );
            }
        }
    }
   
   /**
    * Delegating builder that defers resolution until required.
    */
    private class DelegatingBuilder implements Builder
    {
        private final URI m_uri;
        private Object m_delegate = null;
        
       /**
        * Creation of a new delegating builder instance.
        * @param uri the uri of the builder that operations will be delegated to
        */
        DelegatingBuilder( URI uri )
        {
            m_uri = uri;
        }
        
       /**
        * Delegating implementation of the generic element build operation.
        * @param classloader the base classloader
        * @param element the subject element
        * @return the resulting object
        */
        public Object build( ClassLoader classloader, Element element ) throws Exception
        {
            Object delegate = getDelegate();
            if( delegate instanceof Builder )
            {
                Builder builder = (Builder) delegate;
                return builder.build( classloader, element );
            }
            else
            {
                final String error = 
                  "Builder delegate does not implement build operations."
                  + "\nURI: " + m_uri;
                throw new UnsupportedOperationException( error );
            }
        }
        
        private Object getDelegate()
        {
            if( null != m_delegate )
            {
                return m_delegate;
            }
            else
            {
                try
                {
                    ClassLoader classloader = getClassLoader();
                    Repository repository = Transit.getInstance().getRepository();
                    return repository.getPlugin( classloader, m_uri, new Object[0] );
                }
                catch( Exception e )
                {
                    final String error = 
                      "Unable to establish delegate builder."
                      + "\nURI: " + m_uri;
                    throw new RuntimeException( error );
                }
            }
        }
        
        private ClassLoader getClassLoader()
        {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            if( null != classloader )
            {
                return classloader;
            }
            else
            {
                return getClass().getClassLoader();
            }
        }
    }
    
    private ClassLoader getClassLoader()
    {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        if( null != classloader )
        {
            return classloader;
        }
        else
        {
            return StrategyBuilder.class.getClassLoader();
        }
    }
    
    private URI getBuilderFromNamespaceURI( String urn ) throws Exception
    {
        URI raw = new URI( urn );
        Artifact artifact = Artifact.createArtifact( raw );
        String scheme = artifact.getScheme();
        String group = artifact.getGroup();
        String name = artifact.getName();
        String type = artifact.getType();
        String version = artifact.getVersion();
        
        String path = "link:part:" + group + "/" + name;
        Artifact link = Artifact.createArtifact( path );
        return link.toURI();
    }

    private static URI createURI( String spec )
    {
        try
        {
            return new URI( spec );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            return null;
        }
    }
}

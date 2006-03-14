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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Hashtable;

import javax.xml.XMLConstants;

import net.dpml.lang.Category;
import net.dpml.lang.Classpath;
import net.dpml.lang.Builder;
import net.dpml.lang.BuilderException;
import net.dpml.lang.Type;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.util.ElementHelper;
import net.dpml.transit.artifact.ArtifactNotFoundException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.TypeInfo;

/**
 * Utility class supporting resolution of element builders.
 */
public class AbstractBuilder
{
    static final String PART_XSD_URI = "@PART-XSD-URI@";
    
    static final URI LOCAL_URI = createURI( "local:dpml" );
    
    private final Map m_map; // maps namespace uris to a builders
    
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
    
    protected Map getMap()
    {
        return m_map;
    }
    
    public Builder getBuilder( Element element ) throws Exception
    {
        return loadBuilder( element );
    }

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
    
    protected Object loadObjectFromURI( URI uri, Class clazz ) throws Exception
    {
        if( LOCAL_URI.equals( uri ) )
        {
            if( StrategyBuilder.class.isAssignableFrom( clazz ) )
            {
                return new PartStrategyBuilder(m_map );
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
        Object handler = repository.getPlugin( classloader, uri, new Object[0] );
        if( clazz.isAssignableFrom( handler.getClass() ) )
        {
            return handler;
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
                /*
                try
                {
                    URI uri = new URI( namespace );
                    if( Artifact.isRecognized( uri ) )
                    {
                        Artifact artifact = Artifact.createArtifact( uri );
                        String group = artifact.getGroup();
                        String artifactName = artifact.getName();
                        String spec = group + "/" + artifactName;
                        if( m_map.containsKey( spec ) )
                        {
                            return (URI) m_map.get( spec );
                        }
                        else
                        {
                            return getBuilderFromNamespaceURI( namespace );
                        }
                    }
                    else
                    {
                        final String error = 
                          "Namespace format is not a recognized artifact [" 
                          + namespace 
                          + "].";
                        throw new IllegalArgumentException( error );
                    }
                }
                catch( URISyntaxException e )
                {
                    final String error = 
                      "Namespace format is not recognized [" 
                      + namespace 
                      + "].";
                    throw new BuilderException( element, error );
                }
                */
            }
        }
    }
    
    private class DelegatingBuilder implements Builder
    {
        private final URI m_uri;
        private Object m_delegate = null;
        
        DelegatingBuilder( URI uri )
        {
            m_uri = uri;
        }
        
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

        /*
        public String getID()
        {
            Object delegate = getDelegate();
            if( delegate instanceof TypeBuilder )
            {
                TypeBuilder builder = (TypeBuilder) delegate;
                return builder.getID();
            }
            else
            {
                final String error = 
                  "Builder delegate does not implement type builder operations."
                  + "\nURI: " + m_uri;
                throw new UnsupportedOperationException( error );
            }
        }

        public Type buildType( ClassLoader classloader, Element element ) throws Exception
        {
            Object delegate = getDelegate();
            if( delegate instanceof TypeBuilder )
            {
                TypeBuilder builder = (TypeBuilder) delegate;
                return builder.buildType( classloader, element );
            }
            else
            {
                final String error = 
                  "Builder delegate does not implement type builder operations."
                  + "\nURI: " + m_uri;
                throw new UnsupportedOperationException( error );
            }
        }
        */
        
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
        
       /*
        try
        {
            String path = "link:part:" + group + "/" + name;
            Artifact link = Artifact.createArtifact( path );
            URL linkUrl = link.toURL();
            linkUrl.getContent( new Class[]{File.class} ); // test for existance
            return link.toURI();
        }
        catch( ArtifactNotFoundException e )
        {
            Artifact result = Artifact.createArtifact( group, name, version, "part" );
            try
            {
                URL url = result.toURL();
                url.getContent( new Class[]{File.class} );
                return result.toURI();
            }
            catch( ArtifactNotFoundException anfe )
            {
                final String error = 
                  "Unable to locate a builder for the urn ["
                  + urn
                  + "].";
                throw new UnresolvableHandlerException( error, result.toURI() );
            }
        }
        */
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

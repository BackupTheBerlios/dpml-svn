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

import java.io.IOException;
import java.net.URI;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Hashtable;

import net.dpml.lang.Encoder;
import net.dpml.lang.DecodingException;
import net.dpml.lang.Decoder;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;
import net.dpml.transit.Repository;

import org.w3c.dom.Element;
import org.w3c.dom.TypeInfo;

/**
 * Utility class supporting resolution of element decoders.
 */
public final class DecoderFactory
{
   /**
    * Constant uri used to refernce the internal package encoder.
    */
    static final URI LOCAL_URI = createURI( "local:dpml" );
    
    static final String PART_XSD_URI = "@PART-XSD-URI@";
        
    private static final DecoderFactory INSTANCE = new DecoderFactory();

    private final Map m_map; // maps namespace uris to element handlers
    
   /**
    * Creation of a new helper factory using default mappings.
    */
    public DecoderFactory()
    {
        this( null );
    }
    
   /**
    * Creation of a new decoder factory. The supplied map contains the mapping
    * of namespace urn to decoder plugin uris.  If the "dpml/lang/dpml-part" namespace
    * is not included in the map a special uri will be assigned associating the namespace
    * with this package implementation.
    *
    * @param map the namespace to helper uri map
    */
    public DecoderFactory( Map map )
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
    * Internal utility to load an encoder using the supplied uri.
    *
    * @param uri the encoder uri
    * @return the encoder
    * @exception IOException if an IO error occurs
    * @exception InvocationTargetException if the encoder plugin raises an instantiation error
    */
    public static Encoder loadEncoder( URI uri ) throws IOException, InvocationTargetException
    {
        if( LOCAL_URI.equals( uri ) )
        {
            return new PartEncoder();
        }
        else
        {
            ClassLoader classloader = getClassLoader();
            Repository repository = Transit.getInstance().getRepository();
            Object[] args = new Object[]{INSTANCE};
            Object object = repository.getPlugin( classloader, uri, args );
            if( object instanceof Encoder )
            {
                return (Encoder) object;
            }
            else
            {
                final String error = 
                  "URI ["
                  + uri
                  + "] does not resolve to an object assignable to "
                  + Encoder.class.getName()
                  + ".";
                throw new IllegalArgumentException( error );
            }
        }
    }
    
   /**
    * Get an element helper based on the namespace declared by the supplied element. If 
    * the element namespace is the dpml/part namespace then a local uri is returned, 
    * otherwise evaluation is based on namespace to hanlder mappings supplied to 
    * the factory constructor.  If a map entry is resolved, a delegating builder is 
    * established with the resolved helper uri, otherwise a helper uri is resolved 
    * by substituting the namespace uri artifact type for "part" on the assumption that 
    * a part implemenation will be available.
    *
    * @param element the DOM element
    * @return the associated helper instance
    * @exception Exception if an eror occurs
    */
    public Decoder loadDecoder( Element element ) throws Exception
    {
        URI uri = getDecoderURI( element );
        if( LOCAL_URI.equals( uri ) )
        {
            return new PartDecoder( this );
        }
        else
        {
            return new DelegatingDecoder( this, uri );
        }
    }
    
   /**
    * Resolve the element helper uri from a supplied element.
    *
    * @param element the DOM element
    * @return the builder uri
    * @exception Exception if an eror occurs
    */
    public URI getDecoderURI( Element element ) throws Exception
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
            return getDecoderURIFromNamespaceURI( namespace );
        }
    }
    
    private URI getDecoderURIFromNamespaceURI( String urn ) throws Exception
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
    
   
   /**
    * Delegating builder that defers resolution until required.
    */
    private class DelegatingDecoder implements Decoder
    {
        private final DecoderFactory m_factory;
        private final URI m_uri;
        private Decoder m_delegate = null;
        
       /**
        * Creation of a new delegating builder instance.
        * @param uri the uri of the builder that operations will be delegated to
        */
        DelegatingDecoder( DecoderFactory factory, URI uri )
        {
            m_uri = uri;
            m_factory = factory;
        }
        
       /**
        * Delegating implementation of the decode operation.
        * @param classloader the base classloader
        * @param element the subject element
        * @return the resulting object
        */
        public Object decode( ClassLoader classloader, Element element ) throws DecodingException
        {
            Decoder decoder = getDelegateDecoder();
            return decoder.decode( classloader, element );
        }
        
        private Decoder getDelegateDecoder()
        {
            if( null != m_delegate )
            {
                return m_delegate;
            }
            else
            {
                Object instance = getInstance();
                if( instance instanceof Decoder )
                {
                    m_delegate = (Decoder) instance;
                    return m_delegate;
                }
                else
                {
                    final String error = 
                      "Object resolved from the uri does not provide decoding services."
                      + "\nDelegate URI: " + m_uri
                      + "\nDelegate Class: " + instance.getClass().getName();
                    throw new IllegalStateException( error );
                }
            }
        }
        
        private Object getInstance()
        {
            try
            {
                ClassLoader classloader = getClassLoader();
                Repository repository = Transit.getInstance().getRepository();
                Object[] args = new Object[]{m_factory};
                return repository.getPlugin( classloader, m_uri, args );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error occured while attempting to establish the delegate decoder."
                  + "\nDelegate URI: " + m_uri;
                throw new RuntimeException( error, e ); // change to a factory exception
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
    
    static ClassLoader getClassLoader()
    {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        if( null != classloader )
        {
            return classloader;
        }
        else
        {
            return DecoderFactory.class.getClassLoader();
        }
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

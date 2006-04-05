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
import java.util.Map;
import java.util.Hashtable;
import java.lang.ref.WeakReference;

import net.dpml.lang.Classpath;
import net.dpml.lang.Logger;
import net.dpml.lang.DefaultLogger;
import net.dpml.lang.Value;

import net.dpml.transit.Artifact;
import net.dpml.transit.util.ElementHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.TypeInfo;

/**
 * Construct a part.
 */
public final class PartDecoder implements Decoder
{
   /**
    * Part ZSD uri.
    */
    public static final String PART_XSD_URI = "@PART-XSD-URI@";

    private static final DOM3DocumentBuilder DOCUMENT_BUILDER = 
      new DOM3DocumentBuilder();
    
    private static final ValueDecoder VALUE_DECODER = new ValueDecoder();
    
    private static final PartDecoder DECODER = new PartDecoder();
    
   /**
    * Get the singleton instance of the part decoder.
    * @return the decoder instance.
    */
    public static PartDecoder getInstance()
    {
        return DECODER;
    }
    
    private Map m_map = new Hashtable();
    
    private Logger m_logger;
    
    private PartDecoder()
    {
        m_logger = new DefaultLogger();
    }
    
   /**
    * Load a part from a uri.
    * @param uri the part uri
    * @return the part definition
    * @exception IOException if an IO error occurs
    */
    public Part loadPart( URI uri ) throws IOException
    {
        if( null == uri )
        {
            throw new NullPointerException( "uri" );
        }
        String key = buildKey( uri );
        WeakReference ref = (WeakReference) m_map.get( key );
        if( null != ref )
        {
            Part part = (Part) ref.get();
            if( null != part )
            {
                if( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "loading part [" + uri + "] from cache." );
                }
                //System.out.println( "CACHED PART: " + key );
                //Exception e = new Exception( "trace" );
                //e.printStackTrace();
                return part;
            }
        }
        try
        {
            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "loading part [" + uri + "] from source." );
            }
            //System.out.println( "LOADING PART: " + key );
            //Exception e = new Exception( "trace" );
            //e.printStackTrace();
            final Document document = DOCUMENT_BUILDER.parse( uri );
            final Element root = document.getDocumentElement();
            Part value = decodePart( uri, root );
            WeakReference reference = new WeakReference( value );
            m_map.put( key, reference );
            return value;
        }
        catch( Throwable e )
        {
            final String error =
              "An error while attempting to load a part."
              + "\nURI: " + uri;
            IOException exception = new IOException( error );
            exception.initCause( e );
            throw exception;
        }
    }
    
    private String buildKey( URI uri )
    {
        ClassLoader classloader = getAnchorClassLoader();
        int n = System.identityHashCode( classloader );
        return "" + n + "#" + uri.toASCIIString();
    }
    
   /**
    * Resolve a object from a DOM element.
    * @param element the dom element
    * @return the resolved object
    * @exception IOException if an error occurs during element evaluation
    */
    public Object decode( Element element ) throws IOException
    {
        return decodePart( null, element );
    }
    
   /**
    * Resolve a part from a DOM element.
    * @param uri the part uri
    * @param element element part definition
    * @return the resoplved part datastructure
    * @exception IOException if an error occurs during element evaluation
    */
    public Part decodePart( URI uri, Element element ) throws IOException
    {
        TypeInfo info = element.getSchemaTypeInfo();
        String namespace = info.getTypeNamespace();
        if( PART_XSD_URI.equals( namespace ) )
        {
            boolean alias = ElementHelper.getBooleanAttribute( element, "alias", false );
            Info information = getInfo( uri, element );
            Classpath classpath = getClasspath( element );
            Element strategy = getStrategyElement( element );
            return build( information, classpath, strategy );
        }
        else
        {
            final String error = 
              "Part namespace not recognized."
              + "\nExpecting: " + PART_XSD_URI
              + "\nFound: " + namespace;
            throw new DecodingException( element, error );
        }
    }
    
   /**
    * Resolve a part plugin or resource strategy.
    * @param information the part info definition
    * @param classpath the part classpath definition
    * @param element element part definition
    * @return the resolved part
    * @exception IOException if an error occurs during element evaluation
    */
    public Part build( Info information, Classpath classpath, Element strategy ) throws IOException
    {
        TypeInfo info = strategy.getSchemaTypeInfo();
        String namespace = info.getTypeNamespace();
        if( PART_XSD_URI.equals( namespace ) )
        {
            // this is either a plugin or a resource
            
            Logger logger = getLogger();
            String name = info.getTypeName();
            if( "plugin".equals( name ) )
            {
                String classname = ElementHelper.getAttribute( strategy, "class" );
                Element[] elements = ElementHelper.getChildren( strategy );
                Value[] values = VALUE_DECODER.decodeValues( elements );
                return new Plugin( logger, information, classpath, classname, values );
            }
            else if( "resource".equals( name ) )
            {
                String urn = ElementHelper.getAttribute( strategy, "urn" );
                String path = ElementHelper.getAttribute( strategy, "path" );
                return new Resource( logger, information, classpath, urn, path );
            }
            else
            {
                final String error = 
                  "Element type name ["
                  + name
                  + "] is not a recognized element type within the "
                  + PART_XSD_URI
                  + " namespace.";
                throw new DecodingException( strategy, error );
            }
        }
        else
        {
            // this is a foreign part
            
            try
            {
                URI uri = getDecoderURIFromNamespaceURI( strategy, namespace );
                Builder builder = loadForeignBuilder( uri );
                return builder.build( information, classpath, strategy );
            }
            catch( Exception ioe )
            {
                final String error = 
                  "Internal error while attempting to load foreign part builder.";
                throw new DecodingException( strategy, error, ioe );
            }
        }
    }
    
   /**
    * Get the assigned logging channel.
    * @return the logging channel
    */
    protected Logger getLogger()
    {
        return m_logger;
    }
    
    private URI getDecoderURIFromNamespaceURI( Element element, String urn ) throws DecodingException
    {
        try
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
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error while resolving builder uri. "
              + "\nNamespace: " + urn;
            throw new DecodingException( element, error, e );
        }
    }
    
    private Builder loadForeignBuilder( URI uri ) throws DecodingException, Exception
    {
        Part part = loadPart( uri );
        if( part instanceof Plugin )
        {
            Plugin plugin = (Plugin) part;
            Logger logger = getLogger();
            Object[] args = new Object[]{logger};
            Object object = plugin.instantiate( args );
            if( object instanceof Builder )
            {
                return (Builder) object;
            }
            else
            {
                final String error = 
                  "Plugin does not implement the "
                  + Builder.class.getName()
                  + " interface."
                  + "\nURI: " + uri 
                  + "\nClass: " + object.getClass().getName();
                throw new PartHandlerException( error );
            }
        }
        else
        {
            final String error = 
              "Cannot resolve an instance from a part that is not derived from "
              + Plugin.class.getName()
              + "."
              + "\nURI: " + uri 
              + "\nPart Type: " + part.getClass().getName();
            throw new PartHandlerException( error );
        }
    }
    
    private Element getStrategyElement( Element root ) throws DecodingException
    {
        Element[] children = ElementHelper.getChildren( root );
        if( children.length != 3 )
        {
            final String error = 
              "Illegal number of child elements in <part>. Expecting 3, found " 
              + children.length
              + ".";
            throw new DecodingException( root, error );
        }
        return children[1];
    }
    
    
    private Info getInfo( URI uri, Element root )
    {
        Element element = ElementHelper.getChild( root, "info" );
        String title = ElementHelper.getAttribute( element, "title", "Unknown" );
        Element descriptionElement = ElementHelper.getChild( element, "description" );
        String description = ElementHelper.getValue( descriptionElement );
        return new Info( uri, title, description );
    }
    
   /**
    * Construct the classpath defintion.
    * @param root the element containing a 'classpath' element.
    * @return the classpath definition
    * @exception DecodingException if an error occurs during element evaluation
    */
    protected Classpath getClasspath( Element root ) throws DecodingException
    {
        Element classpath = ElementHelper.getChild( root, "classpath" );
        if( null == classpath )
        {
            final String error = 
              "Required classpath element is not present in plugin descriptor.";
            throw new DecodingException( root, error );
        }
        
        try
        {
            Element[] children = ElementHelper.getChildren( classpath );
            URI[] sys = buildURIs( classpath, "system" );
            URI[] pub = buildURIs( classpath, "public" );
            URI[] prot = buildURIs( classpath, "protected" );
            URI[] priv = buildURIs( classpath, "private" );
            Classpath cp = new Classpath( sys, pub, prot, priv );
            return cp;
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to decode classpath due to an unexpected error.";
            throw new DecodingException( classpath, error, e );
        }
    }
    
    private URI[] buildURIs( Element classpath, String key ) throws Exception
    {
        Element category = ElementHelper.getChild( classpath, key );
        if( null == category )
        {
            return new URI[0];
        }
        else
        {
            Element[] children = ElementHelper.getChildren( category, "uri" );
            URI[] uris = new URI[ children.length ];
            for( int i=0; i<children.length; i++ )
            {
                Element child = children[i];
                String value = ElementHelper.getValue( child );
                uris[i] = new URI( value );
            }
            return uris;
        }
    }
    
    private ClassLoader getAnchorClassLoader()
    {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        if( null == classloader )
        {
            return Part.class.getClassLoader();
        }
        else
        {
            return classloader;
        }
    }
    
}

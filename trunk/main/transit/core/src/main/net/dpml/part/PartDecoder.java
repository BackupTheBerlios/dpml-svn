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

import net.dpml.lang.Classpath;
import net.dpml.lang.Decoder;
import net.dpml.lang.DecodingException;

import net.dpml.transit.util.ElementHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.TypeInfo;

/**
 * Construct a part.
 */
public final class PartDecoder implements Decoder
{
    private static final DOM3DocumentBuilder DOCUMENT_BUILDER = 
      new DOM3DocumentBuilder();
    
    private static final PartStrategyDecoder STRATEGY_DECODER = new PartStrategyDecoder();
    
    private final DecoderFactory m_factory;
    
   /**
    * Creation of a new part builder.
    * @param factory the decoder factory
    */
    public PartDecoder( DecoderFactory factory )
    {
        m_factory = factory;
    }
    
   /**
    * Load a part from a uri.
    * @param uri the part uri
    * @return the part definition
    * @exception IOException if an IO error occurs
    */
    public Part loadPart( URI uri ) throws IOException
    {
        ClassLoader base = Part.class.getClassLoader();
        return loadPart( base, uri );
    }
    
   /**
    * Load a part from a uri.
    * @param base the base classloader
    * @param uri the part uri
    * @return the part definition
    * @exception IOException if an IO error occurs
    */
    public Part loadPart( ClassLoader base, URI uri ) throws IOException
    {
        if( null == uri )
        {
            throw new NullPointerException( "uri" );
        }
        if( null == base )
        {
            throw new NullPointerException( "base" );
        }
        
        try
        {
            final Document document = DOCUMENT_BUILDER.parse( uri );
            final Element root = document.getDocumentElement();
            return decodePart( base, root );
        }
        catch( Throwable e )
        {
            final String error =
              "An error while attempting to load a part."
              + "\nPart URI: " + uri;
            IOException exception = new IOException( error );
            exception.initCause( e );
            throw exception;
        }
    }
    
   /**
    * Resolve a part from a DOM element.
    * @param classloader the classloader
    * @param element the dom element
    * @return the part definition
    * @exception DecodingException if an error occurs during element evaluation
    */
    public Object decode( ClassLoader classloader, Element element ) throws DecodingException
    {
        TypeInfo info = element.getSchemaTypeInfo();
        String name = info.getTypeName();
        if( "plugin".equals( name ) || "resource".equals( name ) )
        {
            return STRATEGY_DECODER.decode( classloader, element );
        }
        else if( "part".equals( name ) )
        {
            return decodePart( classloader, element );
        }
        else
        {
            final String error = 
              "Element type name ["
              + name
              + "] is not recognized.";
            throw new DecodingException( element, error );
        }
    }
    
   /**
    * Resolve a part from a DOM element.
    * @param base the classloader
    * @param root the dom element
    * @return the part definition
    * @exception DecodingException if an error occurs during element evaluation
    */
    public Part decodePart( ClassLoader base, Element root ) throws DecodingException
    {
        if( null == root )
        {
            throw new NullPointerException( "root" );
        }
        Info info = getInfo( root );
        Strategy strategy = getStrategy( base, root );
        Classpath classpath = getClasspath( root );
        return new Part( info, strategy, classpath );
    }
    
    private Strategy getStrategy( ClassLoader loader, Element root ) throws DecodingException
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
        
        Element strategy = children[1];
        Decoder decoder = getDocoder( strategy );
        Object result = decoder.decode( loader, strategy );
        if( result instanceof Strategy )
        {
            return (Strategy) result;
        }
        else
        {
            final String error = 
              "Decoded object is not assignable to "
              + Strategy.class.getName()
              + "."
              + "\nDecoder: " + decoder.getClass().getName()
              + "\nObject: " + result.getClass().getName();
            throw new DecodingException( strategy, error );
        }
    }
    
    private Decoder getDocoder( Element element ) throws DecodingException
    {
        try
        {
            return m_factory.loadDecoder( element );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error while attempting to load decoder.";
            throw new DecodingException( element, error );
        }
    }
    
    private Info getInfo( Element root )
    {
        Element element = ElementHelper.getChild( root, "info" );
        String title = ElementHelper.getAttribute( element, "title", "Unknown" );
        Element descriptionElement = ElementHelper.getChild( element, "description" );
        String description = ElementHelper.getValue( descriptionElement );
        return new Info( title, description );
    }
    
   /**
    * Construct the classpath defintion.
    * @param root the element containing a 'classpath' element.
    * @return the classpath defintion
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
    
    private Element getSingleNestedElement( Element parent ) throws Exception
    {
        if( null == parent )
        {
            throw new NullPointerException( "parent" );
        }
        else
        {
            Element[] children = ElementHelper.getChildren( parent );
            if( children.length == 1 )
            {
                return children[0];
            }
            else
            {
                final String error = 
                  "Parent element does not contain a single child.";
                throw new IllegalArgumentException( error );
            }
        }
    }
}

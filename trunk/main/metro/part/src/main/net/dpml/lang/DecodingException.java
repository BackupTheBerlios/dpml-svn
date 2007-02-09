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

package net.dpml.lang;

import dpml.util.ElementHelper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import net.dpml.transit.Artifact;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Exception related to data decoding from a DOM element.
 */
public class DecodingException extends IOException
{
    private final Element m_element;
    
   /**
    * Create a new decoding exception.
    * @param element the element representing the source of the error
    * @param message the exception message
    */
    public DecodingException( String message, Element element )
    {
        this( message, null, element );
    }
    
   /**
    * Create a new decoding exception.
    * @param element the element representing the source of the error
    * @param message the exception message
    * @param cause the causal exception
    */
    public DecodingException( String message, Throwable cause, Element element )
    {
        super( message );
        if( null != cause )
        {
            super.initCause( cause );
        }
        m_element = element;
    }
    
   /**
    * Get the element that is the subject of this exception.
    * @return the subject element
    */
    public Element getElement()
    {
        return m_element;
    }
    
   /**
    * Return a string representation of the exception.
    * @return the string value
    */
    public String getMessage()
    {
        try
        {
            String message = super.getMessage();
            StringBuffer buffer = new StringBuffer( message );
            Element element = getElement();
            if( null != element )
            {
                Document document = element.getOwnerDocument();
                String uri = document.getDocumentURI();
                if( null != uri )
                {
                    buffer.append( "\nSource: " + uri  );
                    String filename = getSourceFilePath( uri );
                    if( null != filename )
                    {
                        buffer.append( "\nFile: " + filename );
                    }
                    buffer.append( "\n" );
                }
                String listing = list( element );
                buffer.append( listing );
            }
            return buffer.toString();
        }
        catch( Throwable e )
        {
            return super.getMessage();
        }
    }
    
   /**
    * Static utility operation that returns a string representation of a DOM element.
    * @param element the element to stringify
    * @return the string value
    */
    public static String list( Element element )
    {
        return list( element, "" );
    }
    
   /**
    * Static utility operation that returns a syring representation of a DOM element.
    * @param element the element to stringify
    * @param pad padding offset
    * @return the string value
    */
    public static String list( Element element, String pad )
    {
        if( null == element )
        {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        String tag = element.getTagName();
        buffer.append( pad + "<" );
        buffer.append( tag );
        NamedNodeMap map = element.getAttributes();
        for( int i=0; i<map.getLength(); i++ )
        {
            Node item = map.item( i );
            buffer.append( " " + item.getNodeName() + "=\"" );
            buffer.append( item.getNodeValue() );
            buffer.append( "\"" );
        }
        
        Element[] children = ElementHelper.getChildren( element );
        if( children.length > 0 )
        {
            buffer.append( ">" );
            for( int i=0; i<children.length; i++ )
            {
                Element child = children[i];
                String listing = list( child, pad + "  " );
                String tagName = child.getTagName();
                buffer.append( "\n" + listing );
            }
            buffer.append( "\n" + pad + "</" + tag + ">" );
        }
        else
        {
            buffer.append( "/>" );
        }
        return buffer.toString();
    }
    
    private static String getSourceFilePath( String value )
    {
        try
        {
            URI uri = URI.create( value );
            if( Artifact.isRecognized( uri ) )
            {
                URL url = Artifact.toURL( uri );
                File file = (File) url.getContent( new Class[]{ File.class } );
                return file.getCanonicalPath();
            }
            return null;
        }
        catch( Throwable e )
        {
            return null;
        }
    }
    
}

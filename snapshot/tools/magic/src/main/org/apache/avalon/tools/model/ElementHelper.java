/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.tools.model;

import org.apache.tools.ant.BuildException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Utility class supporting the translation of DOM content into local child, children, 
 * attribute and value values.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ElementHelper
{
   /**
    * Return the root element of the supplied file.
    * @param definition the file to load
    * @exception BuildException if the error occurs during root element establishment
    */
    public static Element getRootElement( final File definition )
      throws BuildException
    {
        if( !definition.exists() ) 
        {
            throw new BuildException( 
              new FileNotFoundException( definition.toString() ) );
        }

        if( !definition.isFile() ) 
        {
            final String error = 
              "Source is not a file: " + definition;
            throw new BuildException( 
              new IllegalArgumentException( error ) );
        }

        try 
        {
            final DocumentBuilderFactory factory =
              DocumentBuilderFactory.newInstance();
            factory.setValidating( false );
            factory.setNamespaceAware( false );
            final Document document =
              factory.newDocumentBuilder().parse( definition );
            return document.getDocumentElement();
        }
        catch( SAXException sxe )
        {
            // Error generated during parsing
            if( sxe.getException() != null) 
            {
                throw new BuildException( sxe.getException() );
            }
            throw new BuildException( sxe );
        }
        catch( ParserConfigurationException pce ) 
        {
            // Parser with specified options can't be built
            throw new BuildException( pce );
        }
        catch( IOException ioe ) 
        {
            // I/O error
            throw new BuildException(ioe);
        }
    }

   /**
    * Return the root element of the supplied input stream.
    * @param input the input stream containing a XML definition
    * @exception BuildException if the error occurs during root element establishment
    */
    public static Element getRootElement( final InputStream input )
      throws Exception
    {
        try
        {
            final DocumentBuilderFactory factory =
              DocumentBuilderFactory.newInstance();
            factory.setValidating( false );
            factory.setNamespaceAware( false );
            final Document document =
              factory.newDocumentBuilder().parse( input );
            return document.getDocumentElement();
        }
        catch( ParserConfigurationException pce ) 
        {
            // Parser with specified options can't be built
            throw new BuildException( pce );
        }
        catch( IOException ioe ) 
        {
            // I/O error
            throw new BuildException(ioe);
        }
    }

   /**
    * Return a named child relative to a supplied element.
    * @param root the parent DOM element
    * @param name the name of a child element
    * @return the child element of null if the child does not exist
    */
    public static Element getChild( final Element root, final String name )
    {
        if( null == root ) return null;
        final NodeList list = root.getElementsByTagName( name );
        final int n = list.getLength();
        if( n < 1 ) return null;
        return (Element) list.item( 0 );
    }

   /**
    * Return all children matching the supplied element name.
    * @param root the parent DOM element
    * @param name the name against which child element will be matched
    * @return the array of child elements with a matching name
    */
    public static Element[] getChildren( final Element root, final String name )
    {
        if( null == root ) return new Element[0];
        final NodeList list = root.getElementsByTagName( name );
        final int n = list.getLength();
        final ArrayList result = new ArrayList();
        for( int i=0; i<n; i++ )
        {
            final Node item = list.item( i );
            if( item instanceof Element )
            {
                result.add( item );
            }
        }
        return (Element[]) result.toArray( new Element[0] );
    }

   /**
    * Return all children of the supplied parent.
    * @param root the parent DOM element
    * @return the array of all children
    */
    public static Element[] getChildren( final Element root )
    {
        if( null == root ) return new Element[0];
        final NodeList list = root.getChildNodes();
        final int n = list.getLength();
        if( n < 1 ) return new Element[0];
        final ArrayList result = new ArrayList();
        for( int i=0; i<n; i++ )
        {
            final Node item = list.item( i );
            if( item instanceof Element )
            {
                result.add( item );
            }
        }
        return (Element[]) result.toArray( new Element[0] );
    }

   /**
    * Return the value of an element.
    * @param node the DOM node
    * @return the node value
    */
    public static String getValue( final Element node )
    {
        if( null == node ) 
            return null;
        String value;
        if( node.getChildNodes().getLength() > 0 )
        {
            value = node.getFirstChild().getNodeValue();
        }
        else
        {
            value = node.getNodeValue(); 
        }
        return normalize( value );
    }

   /**
    * Return the value of an element attribute.
    * @param node the DOM node
    * @param key the attribute key
    * @return the attribute value or null if the attribute is undefined
    */
    public static String getAttribute( final Element node, final String key )
    {
        return getAttribute( node, key, null );
    }

   /**
    * Return the value of an element attribute.
    * @param node the DOM node
    * @param key the attribute key
    * @param def the default value if the attribute is undefined
    * @return the attribute value or the default value if undefined
    */
    public static String getAttribute( final Element node, final String key, final String def )
    {
        if( null == node ) 
            return def;
        final String value = node.getAttribute( key );
        if( null == value ) 
            return def;
        return normalize( value );
    }

   /**
    * Return the value of an element attribute as a boolean
    * @param node the DOM node
    * @param key the attribute key
    * @return the attribute value as a boolean or false if undefined
    */
    public static boolean getBooleanAttribute( final Element node, final String key )
    {
        return getBooleanAttribute( node, key, false );
    }

   /**
    * Return the value of an element attribute as a boolean.
    * @param node the DOM node
    * @param key the attribute key
    * @param def the default value if the attribute is undefined
    * @return the attribute value or the default value if undefined
    */
    public static boolean getBooleanAttribute( final Element node, final String key, final boolean def )
    {
        if( null == node ) 
            return def;
        String value = node.getAttribute( key );
        value = normalize( value );
        
        if( null == value ) 
            return def;
        if( value.equals( "" ) ) 
            return def;
        if( value.equals( "true" ) ) 
            return true;
        if( value.equals( "false" ) ) 
            return false;
        final String error = 
          "Boolean argument [" + value + "] not recognized.";
        throw new BuildException( error );
    }
    
    static String normalize( String value )
    {
        return normalize( value, System.getProperties() );
    }
    
    static String normalize( String value, Properties props )
    {
        return PropertyResolver.resolve( props, value );
    }
}

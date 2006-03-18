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

import net.dpml.lang.Classpath;
import net.dpml.lang.Builder;
import net.dpml.lang.BuilderException;

import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.util.ElementHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Construct a part.
 */
public class PartBuilder extends PartWriter implements Builder
{
    private static final DOM3DocumentBuilder BUILDER = 
      new DOM3DocumentBuilder();
    
   /**
    * Creation of a new part builder.
    */
    public PartBuilder()
    {
        this( null );
    }
    
   /**
    * Creation of a new part builder.
    * @param map the namespace to part build uri map
    */
    public PartBuilder( Map map )
    {
        super( map );
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
        
        //System.out.println( "# LOAD PART: " + uri );

        try
        {
            final Document document = BUILDER.parse( uri );
            final Element root = document.getDocumentElement();
            return buildPart( base, root );
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
    * @exception Exception if an error occurs
    */
    public Object build( ClassLoader classloader, Element element ) throws Exception
    {
        return buildPart( classloader, element );
    }
    
   /**
    * Resolve a part from a DOM element.
    * @param base the classloader
    * @param root the dom element
    * @return the part definition
    * @exception Exception if an error occurs
    */
    public Part buildPart( ClassLoader base, Element root ) throws Exception
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
    
    private Strategy getStrategy( ClassLoader loader, Element root ) throws Exception
    {
        Element[] children = ElementHelper.getChildren( root );
        if( children.length != 3 )
        {
            final String error = 
              "Illegal number of child elements in <part>. Expecting 3, found " 
              + children.length
              + ".";
            throw new BuilderException( root, error );
        }
        
        Element strategy = children[1];
        Builder builder = getBuilder( strategy );
        Object result = builder.build( loader, strategy );
        if( result instanceof Strategy )
        {
            return (Strategy) result;
        }
        else
        {
            final String error = 
              "Object returned from builder is not a strategy instance.";
            throw new BuilderException( strategy, error );
        }
    }
    
    private ClassLoader createClassLoader( 
      ClassLoader base, URI uri, Classpath classpath ) throws IOException
    {
        Repository repository = Transit.getInstance().getRepository();
        return repository.createClassLoader( base, uri, classpath );
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
    * @exception Exception if an error occurs
    */
    protected Classpath getClasspath( Element root ) throws Exception
    {
        Element classpath = ElementHelper.getChild( root, "classpath" );
        if( null == classpath )
        {
            final String error = 
              "Required classpath element is not present in plugin descriptor.";
            throw new IllegalStateException( error );
        }
        
        Element[] children = ElementHelper.getChildren( classpath );
        URI[] sys = buildURIs( classpath, "system" );
        URI[] pub = buildURIs( classpath, "public" );
        URI[] prot = buildURIs( classpath, "protected" );
        URI[] priv = buildURIs( classpath, "private" );
        Classpath cp = new Classpath( sys, pub, prot, priv );
        return cp;
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

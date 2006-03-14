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

import javax.xml.XMLConstants;

import net.dpml.lang.Category;
import net.dpml.lang.Classpath;
import net.dpml.lang.Builder;
import net.dpml.lang.BuilderException;

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
 * Construct a part.
 */
public class PartBuilder extends PartWriter implements Builder
{
    private static final DOM3DocumentBuilder BUILDER = 
      new DOM3DocumentBuilder();
    
    public PartBuilder()
    {
        this( null );
    }
    
    public PartBuilder( Map map )
    {
        super( map );
    }
    
    public Part loadPart( URI uri ) throws IOException
    {
        ClassLoader base = Part.class.getClassLoader();
        return loadPart( base, uri );
    }
    
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
    
    public Object build( ClassLoader classloader, Element element ) throws Exception
    {
        return buildPart( classloader, element );
    }
    
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
        //StrategyBuilder builder = getStrategyBuilder( strategy );
        //return builder.buildStrategy( loader, strategy );
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

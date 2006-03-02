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

package dpmlx.schema;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import dpmlx.lang.Part;
import dpmlx.lang.StrategyBuilder;
import dpmlx.lang.Info;
import dpmlx.lang.Strategy;

import net.dpml.lang.Classpath;

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
public class PartBuilder
{
    private static final String SCHEMA_INSTANCE_NS = 
      "http://www.w3.org/2001/XMLSchema-instance";
    
    private static DOMDocumentBuilder BUILDER = new DOMDocumentBuilder();
    
    public Part loadPart( URI uri ) throws Exception
    {
        ClassLoader base = Part.class.getClassLoader();
        return loadPart( base, uri );
    }
    
    public Part loadPart( ClassLoader base, URI uri ) throws Exception
    {
        final Document document = BUILDER.parse( uri );
        final Element root = document.getDocumentElement();
        return buildPart( base, root );
    }
    
    public Part buildPart( ClassLoader base, Element root ) throws Exception
    {
        final String namespace = root.getNamespaceURI();
        if( !isRecognized( namespace ) )
        {
            final String error = 
              "Element namespace is not recognized.";
            throw new IllegalArgumentException( error );
        }
        
        Info info = getInfo( root );
        Strategy strategy = getStrategy( base, root );
        Classpath classpath = getClasspath( root );
        return new Part( info, strategy, classpath );
    }
    
    private URI getStrategyBuilderURI( Element element ) throws Exception
    {
        TypeInfo info = element.getSchemaTypeInfo();
        String namespace = info.getTypeNamespace();
        return getBuilderURI( namespace );
    }
    
    private Strategy getStrategy( ClassLoader loader, Element root ) throws Exception
    {
        Element strategy = ElementHelper.getChild( root, "strategy" );
        if( null == strategy )
        {
            final String error = "Missing strategy element in: " + root;
            throw new IllegalArgumentException( error );
        }
        TypeInfo info = strategy.getSchemaTypeInfo();
        String namespace = info.getTypeNamespace();
        Artifact artifact = Artifact.createArtifact( namespace );
        if( "dpmlx".equals( artifact.getGroup() ) && "dpmlx-part".equals( artifact.getName() ) )
        {
            PluginStrategyBuilder builder = new PluginStrategyBuilder();
            return builder.buildStrategy( strategy );
        }
        else
        {
            URI uri = getBuilderURI( namespace );
            StrategyBuilder builder = loadStrategyBuilder( uri );
            return builder.buildStrategy( strategy );
        }
    }
    
    private ClassLoader createClassLoader( 
      ClassLoader base, URI uri, Classpath classpath ) throws IOException
    {
        Repository repository = Transit.getInstance().getRepository();
        return repository.createClassLoader( base, uri, classpath );
    }
    
    private boolean isRecognized( String namespace )
    {
        return true; // to be done
    }
    
    private Info getInfo( Element root )
    {
        Element element = ElementHelper.getChild( root, "info" );
        Element titleElement = ElementHelper.getChild( element, "title" );
        Element descriptionElement = ElementHelper.getChild( element, "description" );
        String title = ElementHelper.getValue( titleElement );
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
        
        URI[] sys = buildURIs( classpath, "system" );
        URI[] pub = buildURIs( classpath, "public" );
        URI[] prot = buildURIs( classpath, "protected" );
        URI[] priv = buildURIs( classpath, "private" );
        return new Classpath( sys, pub, prot, priv );
    }
    
    private URI[] buildURIs( Element classpath, String key ) throws Exception
    {
        Element category = ElementHelper.getChild( classpath, key );
        if( null == category )
        {
            return null;
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
    
    private URI getBuilderURI( String urn ) throws Exception
    {
        URI raw = new URI( urn );
        if( Artifact.isRecognized( raw ) )
        {
            Artifact artifact = Artifact.createArtifact( raw );
            String scheme = artifact.getScheme();
            String group = artifact.getGroup();
            String name = artifact.getName();
            String type = artifact.getType();
            String version = artifact.getVersion();
            
            try
            {
                String path = "link:plugin:" + group + "/" + name;
                Artifact link = Artifact.createArtifact( path );
                System.out.println( "  checking: " + link );
                URL linkUrl = link.toURL();
                linkUrl.getContent( new Class[]{File.class} ); // test for existance
                return link.toURI();
            }
            catch( ArtifactNotFoundException e )
            {
                Artifact result = Artifact.createArtifact( group, name, version, "plugin" );
                try
                {
                    System.out.println( "  checking: " + result );
                    URL url = result.toURL();
                    url.getContent( new Class[]{File.class} );
                    return result.toURI();
                }
                catch( ArtifactNotFoundException anfe )
                {
                    System.out.println( "  unresolved" );
                    final String error = 
                      "Unable to resolve a strategy handler for the urn ["
                      + urn
                      + "].";
                    throw new UnresolvableHandlerException( error, result.toURI() );
                }
            }
        }
        else
        {
            final String error = 
              "Namespace urn ["
              + urn
              + "] is not a recognized artifact protocol.";
            throw new IllegalArgumentException( error );
        }
    }
    
    private StrategyBuilder loadStrategyBuilder( URI uri ) throws Exception
    {
        ClassLoader classloader = StrategyBuilder.class.getClassLoader();
        Repository repository = Transit.getInstance().getRepository();
        Object[] args = new Object[0];
        Object handler = repository.getPlugin( classloader, uri, args );
        if( handler instanceof StrategyBuilder )
        {
            return (StrategyBuilder) handler;
        }
        else
        {
            final String error = 
              "Plugin ["
              + uri
              + "] does not implement the "
              + StrategyBuilder.class.getName()
              + " interface.";
            throw new IllegalArgumentException( error );
        }
    }
}

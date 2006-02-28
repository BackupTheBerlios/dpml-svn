
package dpmlx.schema.test;

import java.io.File;
import java.net.URI;
import java.net.URL;

import dpmlx.schema.StandardBuilder;
import dpmlx.schema.UnresolvableHandlerException;

import net.dpml.transit.Artifact;
import net.dpml.transit.artifact.ArtifactNotFoundException;
import net.dpml.transit.Transit;
import net.dpml.transit.util.ElementHelper;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DocumentType;

/**
 * Test the demo class..
 */
public class SchemaTestCase extends TestCase
{
    StandardBuilder m_builder;
    
   /**
    * Test the demo class.
    */
    public void setUp() throws Exception
    {
        ClassLoader classloader = Transit.class.getClassLoader();
        File file = new File( "target/deliverables/plugins/dpmlx-schema-test-@VERSION@.plugin" );
        URI uri = file.toURI();
        Object[] args = new Object[0];
        m_builder = 
          (StandardBuilder) Transit.getInstance().getRepository().getPlugin( 
            classloader, uri, args );
    }
    
   /**
    * Test the demo class.
    */
    public void testPlugin() throws Exception
    {
        evaluateDocument( "target/test/plugin.xml" );
    }
    
    public void testComponent() throws Exception
    {
        try
        {
            evaluateDocument( "target/test/component.xml" );
        }
        catch( UnresolvableHandlerException e )
        {
            // expected
        }
    }
    
    private void evaluateDocument( String path ) throws Exception
    {
        System.out.println( "source: " + path );
        Document doc = m_builder.parse( path );
        final Element root = doc.getDocumentElement();
        final String namespace = root.getNamespaceURI();
        
        Element plugin = ElementHelper.getChild( root, "plugin" );
        if( null != plugin )
        {
            // this is a standard transit plugin containing either
            // a <class> element or a <resource> element as such is
            // a concern private to the transit implementation to 
            // resolve the plugin strategy
            
            System.out.println( "plugin (namespace): " + namespace );
        }
        else
        {
            // this is a custom deployment strategy defined under 
            // a <strategy> element
            
            Element strategy = ElementHelper.getChild( root, "strategy" );
            Element implementation = getSingleNestedElement( strategy );
            String urn = implementation.getNamespaceURI();
            System.out.println( "strategy (namespace): " + urn );
            URI uri = getImplementationHandler( urn );
            System.out.println( "handler: " + uri );
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
    
    private URI getImplementationHandler( String urn ) throws Exception
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
            
            Artifact result = Artifact.createArtifact( group, name, version, "plugin" );
            try
            {
                System.out.println( "  checking: " + result );
                URL url = result.toURL();
                url.getContent( new Class[]{File.class} );
                return result.toURI();
            }
            catch( ArtifactNotFoundException e )
            {
                String path = "link:plugin:" + group + "/" + name;
                Artifact link = Artifact.createArtifact( path );
                System.out.println( "  checking: " + link );
                URI linkUri = link.toURI();
                try
                {
                    URL linkUrl = link.toURL();
                    linkUrl.getContent( new Class[]{File.class} );
                    return link.toURI();
                }
                catch( ArtifactNotFoundException anfe )
                {
                    System.out.println( "  unresolved" );
                    final String error = 
                      "Unable to resolve a strategy handler for the urn ["
                      + urn
                      + "].";
                    throw new UnresolvableHandlerException( error, linkUri );
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
}

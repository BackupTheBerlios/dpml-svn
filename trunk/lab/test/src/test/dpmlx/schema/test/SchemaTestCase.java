
package dpmlx.schema.test;

import java.io.File;
import java.net.URI;
import java.net.URL;

import dpmlx.lang.Part;
import dpmlx.schema.PartBuilder;
import dpmlx.schema.UnresolvableHandlerException;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DocumentType;

/**
 * Test the demo class..
 */
public class SchemaTestCase extends TestCase
{
    static
    {
        System.setProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );
    }
    
    PartBuilder m_builder;
    
   /**
    * Test the demo class.
    */
    public void setUp() throws Exception
    {
        m_builder = new PartBuilder();
    }
    
   /**
    * Test the demo class.
    */
    public void testPlugin() throws Exception
    {
        evaluateDocument( "plugin.xml" );
    }
    
    public void testComponent() throws Exception
    {
        evaluateDocument( "component.xml" );
    }
    
    private void evaluateDocument( String path ) throws Exception
    {
        String base = System.getProperty( "project.test.dir" );
        File test = new File( base );
        File file = new File( test, path );
        System.out.println( "source: " + file );
        Part part = m_builder.loadPart( file.toURI() );
        System.out.println( "# PART: " + part );
        
        /*
        Document doc = m_builder.parse( path );
        final Element root = doc.getDocumentElement();
        final String namespace = root.getNamespaceURI();
        
        // construct the classloader using the <classpath> element
        
        // check if we are a classic plugin or if a custom strategy
        // has been declared
        
        Element plugin = ElementHelper.getChild( root, "plugin" );
        if( null != plugin )
        {
            // This is a standard transit plugin containing either
            // a <class> element or a <resource> element as such is
            // a concern private to the transit implementation to 
            // resolve the plugin strategy.
            
            System.out.println( "plugin (namespace): " + namespace );
        }
        else
        {
            // This is a custom deployment strategy defined under 
            // a <strategy> element and all we know is its namespace
            // and that the nested element is the argument to a 
            // custom instantiation handler. On the grounds that the
            // namespace is declared in the form of an artifact with
            // the type 'xsd', we construct a new plugin artifact using  
            // the same group, name and version.  If such a plugin 
            // exists using the link protocol we use otherwise we 
            // attempt to locate a plugin using the artifact protocol.
            
            Element strategy = ElementHelper.getChild( root, "strategy" );
            Element implementation = getSingleNestedElement( strategy );
            String urn = implementation.getNamespaceURI();
            System.out.println( "strategy (namespace): " + urn );
            URI uri = getImplementationHandler( urn );
            System.out.println( "resolved: " + uri );
            
            PartHandler handler = loadPartHandler( uri, implementation );
            //Object instance = handler.getInstance( classloader, new Object[0] );
            System.out.println( "handler: " + handler.getClass().getName() );
        }
        */
    }
    
    /*
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
    
    private PartHandler loadPartHandler( URI uri, Element implementation ) throws Exception
    {
        ClassLoader classloader = PartHandler.class.getClassLoader();
        Repository repository = Transit.getInstance().getRepository();
        Object[] args = new Object[]{ implementation };
        Object handler = repository.getPlugin( classloader, uri, args );
        if( handler instanceof PartHandler )
        {
            return (PartHandler) handler;
        }
        else
        {
            final String error = 
              "Part handler plugin ["
              + uri
              + "] does not implement the "
              + PartHandler.class.getName()
              + " interface.";
            throw new IllegalArgumentException( error );
        }
    }
    */
}

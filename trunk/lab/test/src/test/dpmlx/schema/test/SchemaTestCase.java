
package dpmlx.schema.test;

import java.io.File;
import java.net.URI;
import java.net.URL;

import dpmlx.lang.Part;
import dpmlx.lang.Strategy;
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
        Strategy strategy = part.getStrategy();
        System.out.println( "  Controller: " + strategy.getControllerURI() );
        System.out.println( "  Data: " + strategy.getDeploymentData().getClass().getName() );
    }
}

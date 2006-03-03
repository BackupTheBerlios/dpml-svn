
package dpmlx.schema.test;

import java.io.File;
import java.net.URI;
import java.net.URL;

import dpmlx.lang.Part;
import dpmlx.lang.Strategy;
import dpmlx.lang.PartDirective;
import dpmlx.schema.PartBuilder;
import dpmlx.schema.UnresolvableHandlerException;
import dpmlx.schema.PartHandlerFactory;

import net.dpml.part.PartHandler;

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
        PartDirective directive = strategy.getPartDirective();
        URI uri = directive.getURI();
        Object data = strategy.getDeploymentData();
        
        System.out.println( "  Controller: " + uri );
        System.out.println( "  Data: " + data.getClass().getName() );
        
        try
        {
            PartHandler handler = PartHandlerFactory.getInstance().getPartHandler( directive );
            System.out.println( "  Handler: " + handler );
        }
        catch( Throwable e )
        {
            System.out.println( "  " + e.toString() );
        }
    }
}

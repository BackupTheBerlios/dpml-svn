
package dpmlx.schema.test;

import java.io.File;
import java.net.URI;

import dpmlx.schema.StandardBuilder;

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
        File file = new File( "target/deliverables/plugins/dpmlx-schema-test-SNAPSHOT.plugin" );
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
        Document doc = m_builder.parse( "examples/plugin.xml" );
        final Element root = doc.getDocumentElement();
        final String uri = root.getNamespaceURI();
        //System.out.println( "NS: [" + uri + "]" ); 
        //System.out.println( "ROOT: [" + root + "]" );
        Element strategy = ElementHelper.getChild( root, "strategy" );
        String handlerUri = ElementHelper.getAttribute( strategy, "uri" );
        if( null == handlerUri )
        {
            System.out.println( "DEFAULT STRATEGY" );
        }
        else
        {
            System.out.println( "CUSTOM STRATEGY: " + handlerUri );
        }
    }
    
    public void testComponent() throws Exception
    {
        Document doc = m_builder.parse( "examples/component.xml" );
        final Element root = doc.getDocumentElement();
        final String uri = root.getNamespaceURI();
        //System.out.println( "NS: [" + uri + "]" ); 
        //System.out.println( "ROOT: [" + root + "]" );
        Element strategy = ElementHelper.getChild( root, "strategy" );
        String handlerUri = ElementHelper.getAttribute( strategy, "uri" );
        if( null == handlerUri )
        {
            System.out.println( "DEFAULT STRATEGY" );
        }
        else
        {
            System.out.println( "CUSTOM STRATEGY: " + handlerUri );
        }
    }
}


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
        Document doc = m_builder.parse( "target/test/plugin.xml" );
        evaluateDocument( doc );
    }
    
    public void testComponent() throws Exception
    {
        Document doc = m_builder.parse( "target/test/component.xml" );
        evaluateDocument( doc );
    }
    
    private void evaluateDocument( Document doc ) throws Exception
    {
        final Element root = doc.getDocumentElement();
        final String namespace = root.getNamespaceURI();
        //assertEquals( "namespace", "dpml:transit/part", namespace );
        
        Element plugin = ElementHelper.getChild( root, "plugin" );
        if( null != plugin )
        {
            // this is a standard transit plugin containing either
            // a <class> element or a <resource> element as such is
            // a concern private to the transit implementation to 
            // resolve the plugin strategy
            
            System.out.println( "PLUGIN STRATEGY " + namespace );
        }
        else
        {
            // this is a custom deployment strategy defined under 
            // a <strategy> element
            
            Element strategy = ElementHelper.getChild( root, "strategy" );
            Element[] children = ElementHelper.getChildren( strategy );
            if( children.length == 1 )
            {
                Element thing = children[0];
                System.out.println( "CUSTOM STRATEGY " + thing.getNamespaceURI() );
            }
            else
            {
                fail( "Strategy must contain a single element." );
            }
        }
    }
    
}

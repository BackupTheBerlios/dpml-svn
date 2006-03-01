
package dpmlx.schema.test;

import java.io.File;
import java.net.URI;
import java.net.URL;

import dpmlx.schema.StandardBuilder;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test the demo class..
 */
public class LibraryTestCase extends TestCase
{
    static
    {
        System.setProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );
    }
    
    StandardBuilder m_builder;
    
   /**
    * Test the demo class.
    */
    public void setUp() throws Exception
    {
        m_builder = new StandardBuilder();
    }
    
   /**
    * Test the demo class.
    */
    public void testLibrary() throws Exception
    {
        String base = System.getProperty( "project.test.dir" );
        File test = new File( base );
        File file = new File( test, "library.xml" );
        m_builder.parse( file.toURI().toASCIIString() );
    }
    
}

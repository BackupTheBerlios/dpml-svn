package acme.test;

import java.io.File;
import java.net.URI;
import java.util.logging.Logger;

import junit.framework.TestCase;

import net.dpml.component.Component;
import net.dpml.component.Disposable;
import net.dpml.component.Provider;
import net.dpml.lang.Part;

import acme.ContainerImpl;

public class ExampleTest extends TestCase 
{
    private Logger m_logger;
    private Component m_component;
    
   /**
    * Load up the part from the tasrget/deliverables directory, 
    * and get the component handler which we will use to (a) 
    * establish an instance and (b) handle the formal end-of-life 
    * processing.
    *
    * @exception Exception if not everything goes according to plan
    */
    public void setUp() throws Exception 
    {
        m_logger = Logger.getLogger( "test" );
        m_logger.info( "commissioning" );
        URI uri = getPartURI();
        Part part = Part.load( uri );
        m_component = (Component) part.getContent( new Class[]{Component.class} );
    }
    
   /**
    * Handle formal decommissioning of the component.
    *
    * @exception Exception if not everything goes according to plan
    */
    public void tearDown() throws Exception
    {
        m_logger.info( "decommissioning" );
        Disposable disposable = (Disposable) m_component;
        disposable.dispose();
    }
   
   /**
    * The Component instance exposes a bunch of management operations.  One
    * of these is the operation getProvider() which gives us access to 
    * the manager of one instance of the class specificed by the component 
    * type. The provider exposes the operation getValue( boolean ) which 
    * is where we actually get a fully initialized instance (and keep
    * in mind here that full-initialized means that all initialization 
    * actions (such as state transitions) have alrady been successfully 
    * completed.
    *
    * @exception Exception if a test related error occurs
    */
    public void testExampleComponent() throws Exception 
    {
        m_logger.info( "testing" );
        Provider provider = m_component.getProvider();
        ContainerImpl container = (ContainerImpl) provider.getValue( false );
        
        // Any test assertions go here.
    }
    
   /**
    * Internal utility to get a URI to the part definition in the 
    * project target/deliverables directory.
    *
    * @return the part uri
    * @exception Exception if an error occurs
    */
    private URI getPartURI() throws Exception
    {
        String path = System.getProperty( "project.deliverable.part.path" );
        File file = new File( path );
        return file.toURI();
    }
}

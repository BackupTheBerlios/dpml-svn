/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.part.test;

import java.io.File;
import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.DefaultPersistenceDelegate;
import java.net.URI;
import java.util.Properties;

import junit.framework.TestCase;

import net.dpml.part.Part;
import net.dpml.part.PartBuilder;
import net.dpml.part.PartHeader;
import net.dpml.part.Directive;


/**
 * Part datastructure testcase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartTestCase extends TestCase
{
   /**
    * Validate that the constructor throws a NPE in the event of 
    * a null controller uri argument.
    * @exception Exception if an error occurs
    */
    public void testNullControllerURI() throws Exception
    {
        try
        {
            Part part = new Part( null, new Properties(), new DemoDirective() );
            fail( "No NPE on null controller uri" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Validate that the constructor throws a NPE in the event of 
    * a null properties argument.
    * @exception Exception if an error occurs
    */
    public void testNullProperties() throws Exception
    {
        try
        {
            Part part = new Part( new URI( "test:controller" ), null, new DemoDirective() );
            fail( "No NPE on null properties" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Test controller accessor.
    * @exception Exception if an error occurs
    */
    public void testControllerURI() throws Exception
    {
        URI uri = new URI( "link:plugin:abc/def" );
        Part part = new Part( uri, new Properties(), new DemoDirective() );
        assertEquals( "uri", uri, part.getControllerURI() );
    }
    
   /**
    * Test properties accessor.
    * @exception Exception if an error occurs
    */
    public void testProperties() throws Exception
    {
        URI uri = new URI( "link:plugin:abc/def" );
        Properties properties = new Properties();
        properties.setProperty( "test", "something" );
        Part part = new Part( uri, properties, new DemoDirective() );
        assertEquals( "properties", properties, part.getProperties() );
    }

   /**
    * Test directive features.
    * @exception Exception if an error occurs
    */
    public void testDirective() throws Exception
    {
        URI uri = new URI( "link:plugin:abc/def" );
        Properties properties = new Properties();
        properties.setProperty( "test", "something" );
        Directive directive = new DemoDirective();
        Part part = new Part( uri, properties, directive );
        assertEquals( "directive", directive, part.getDirective() );
    }

   /**
    * Test part builder.
    * @exception Exception if an error occurs
    */
    public void testPartBuildReadWrite() throws Exception
    {
        String base = System.getProperty( "project.test.dir" );
        File test = new File( base );
        File destination = new File( test, "test.part" );
        URI uri = new URI( "link:plugin:abc/def" );
        Properties properties = new Properties();
        properties.setProperty( "test", "something" );
        Directive directive = new DemoDirective();
        Part part = new Part( uri, properties, directive );
        PartBuilder.write( part, destination );
        URI dest = destination.toURI();
        PartHeader header = PartBuilder.readPartHeader( dest );
        assertEquals( "controller", uri, header.getControllerURI() );
        assertEquals( "properties", properties, header.getProperties() );
        Part p = PartBuilder.readPart( dest );
        assertEquals( "part", part, p );
    }
    
   /**
    * Mock directive.
    */
    public static final class DemoDirective implements Directive
    {
       /**
        * Compare this mock object with another for equality.
        * @param other the other object
        * @return true if equal
        */
        public boolean equals( Object other )
        {
            return ( other instanceof DemoDirective );
        }
        
       /**
        * Return the instance hashcode.
        * @return the hash value
        */
        public int hashCode()
        {
            return getClass().hashCode();
        }
    }
   
   /**
    * Utility demo directive persistence delegate.
    */
    public static final class DemoDirectiveBeanInfo extends SimpleBeanInfo
    {
        private static final BeanDescriptor BEAN_DESCRIPTOR = setupBeanDescriptor();
    
       /**
        * Return the bean descriptor.
        * @return the bean descriptor
        */
        public BeanDescriptor getBeanDescriptor()
        {
            return BEAN_DESCRIPTOR;
        }
    
        private static BeanDescriptor setupBeanDescriptor()
        {
            BeanDescriptor descriptor = new BeanDescriptor( DemoDirective.class );
              descriptor.setValue( 
              "persistenceDelegate", 
               new DefaultPersistenceDelegate( new String[0] ) );
            return descriptor;
        }
    }
}

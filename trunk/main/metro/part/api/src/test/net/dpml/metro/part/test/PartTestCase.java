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

package net.dpml.metro.part.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.beans.Encoder;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.DefaultPersistenceDelegate;
import java.net.URI;
import java.util.Properties;

import junit.framework.TestCase;

import net.dpml.metro.part.Part;
import net.dpml.metro.part.PartBuilder;
import net.dpml.metro.part.PartHeader;
import net.dpml.metro.part.Directive;


/**
 * Validation of the Part datatype.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartTestCase extends TestCase
{
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
    
    /*
    public void testNullDirective() throws Exception
    {
        try
        {
            Part part = new Part( new URI( "test:controller" ), new Properties(), null );
            fail( "No NPE on null directive" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    */
    
    public void testControllerURI() throws Exception
    {
        URI uri = new URI( "link:plugin:abc/def" );
        Part part = new Part( uri, new Properties(), new DemoDirective() );
        assertEquals( "uri", uri, part.getControllerURI() );
    }
    
    public void testProperties() throws Exception
    {
        URI uri = new URI( "link:plugin:abc/def" );
        Properties properties = new Properties();
        properties.setProperty( "test", "something" );
        Part part = new Part( uri, properties, new DemoDirective() );
        assertEquals( "properties", properties, part.getProperties() );
    }

    public void testDirective() throws Exception
    {
        URI uri = new URI( "link:plugin:abc/def" );
        Properties properties = new Properties();
        properties.setProperty( "test", "something" );
        Directive directive = new DemoDirective();
        Part part = new Part( uri, properties, directive );
        assertEquals( "directive", directive, part.getDirective() );
    }

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
    
    public static final class DemoDirective implements Directive
    {
        public boolean equals( Object other )
        {
            return ( other instanceof DemoDirective );
        }
    }
    
    public static final class DemoDirectiveBeanInfo extends SimpleBeanInfo
    {
        private static final BeanDescriptor BEAN_DESCRIPTOR = setupBeanDescriptor();
    
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

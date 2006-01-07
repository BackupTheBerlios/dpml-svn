/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.library.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.beans.Encoder;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.beans.ExceptionListener;
import java.beans.Expression;

import junit.framework.TestCase;

import net.dpml.library.model.Resource;
import net.dpml.library.info.ModuleDirective;
import net.dpml.library.info.Scope;

import net.dpml.transit.Logger;
import net.dpml.transit.monitor.LoggingAdapter;

/**
 * Abstract library testcase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
abstract class AbstractTestCase extends TestCase
{
    private Logger m_logger = new LoggingAdapter( "test" );
    private DefaultLibrary m_library;
    
   /**
    * Return the library established by setup.
    * @return the library
    */
    public DefaultLibrary getLibrary()
    {
        return m_library;
    }
    
   /**
    * Testcase setup using library.xml.
    * @exception Exception if an error in library setup occurs
    */
    public void setUp() throws Exception
    {
        setUp( "library.xml" );
    }

   /**
    * Testcase setup.
    * @param path file path relative to the target/test directory
    * @exception Exception if an error in library setup occurs
    */
    public void setUp( String path ) throws Exception
    {
        String testPath = System.getProperty( "project.test.dir" );
        File test = new File( testPath );
        File example = new File( test, path );
        m_library = new DefaultLibrary( m_logger, example );
    }
    
   /**
    * Test that the supplied object can be serialized and the deserialization
    * process returns an equivalent object to the supplied object.
    * @param object the object to test
    * @exception Exception if an test exception occurs
    */
    public void doSerializationTest( Object object )
        throws Exception
    {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream( byteOutputStream );
        output.writeObject( object );
        output.close();

        ByteArrayInputStream byteInputStream = new ByteArrayInputStream( byteOutputStream.toByteArray() );
        ObjectInputStream input = new ObjectInputStream( byteInputStream );
        Object serialized = input.readObject();
        input.close();

        assertTrue( object != serialized ); // Ensure this is not the same instance
        assertEquals( object, serialized );
        assertEquals( object.hashCode(), serialized.hashCode() );
    }

   /**
    * Test that the supplied object can be encoded and the decoding
    * process returns an equivalent object to the supplied object.
    * @param object the object to encode/decode
    * @exception Exception if an test exception occurs
    */
    public Object doEncodingTest( Object object, String filename ) throws Exception
    {
        String base = System.getProperty( "project.test.dir" );
        File test = new File( base );
        File encoding = new File( test, "encoding" );
        File destination = new File( encoding, filename );
        encoding.mkdirs();
        FileOutputStream output = new FileOutputStream( destination );
        BufferedOutputStream buffer = new BufferedOutputStream( output );
        XMLEncoder encoder = new XMLEncoder( buffer );
        encoder.setExceptionListener( 
          new ExceptionListener()
          {
            public void exceptionThrown( Exception e )
            {
                e.printStackTrace();
                fail( "encoding exception: " + e.toString() );
            }
          }
        );
        encoder.writeObject( object );
        encoder.close();
        FileInputStream input = new FileInputStream( destination );
        XMLDecoder decoder = new XMLDecoder( new BufferedInputStream( input ) );
        Object result = decoder.readObject();
        assertTrue( object != result ); // Ensure this is not the same instance
        assertEquals( "encoding", object, result );
        assertEquals( object.hashCode(), result.hashCode() );
        return result;
    }

    void doProviderTest( 
      String path, boolean expand, int build, int runtime, int test )
      throws Exception
    {
        doProviderTest( path, expand, false, build, runtime, test, true );
        doProviderTest( path, expand, true, build, runtime, test, true );
    }
    
    void doProviderTest( 
      String path, boolean expand, boolean sort, int build, int runtime, int test )
      throws Exception
    {
        doProviderTest( path, expand, sort, build, runtime, test, true );
    }
    
    void doProviderTest( 
      String path, boolean expand )
      throws Exception
    {
        doProviderTest( path, expand, true, 0, 0, 0, false );
    }
    
    void doProviderTest( 
      String path, boolean expand, boolean sort, int build, int runtime, int test, boolean check )
      throws Exception
    {
        Resource resource = m_library.getResource( path );
        Resource[] buildProviders = resource.getProviders( Scope.BUILD, expand, sort );
        Resource[] runtimeProviders = resource.getProviders( Scope.RUNTIME, expand, sort  );
        Resource[] testProviders = resource.getProviders( Scope.TEST, expand, sort );
        if( check )
        {
            assertEquals( 
              path + " build-deps (sorted:" + sort + ") (expanded:" + expand + ")", 
              build, 
              buildProviders.length );
            assertEquals( 
              path + " runtime-deps (sorted:" + sort + ") (expanded:" + expand + ")", 
              runtime, 
              runtimeProviders.length );
            assertEquals( 
              path + " test-deps (sorted:" + sort + ") (expanded:" + expand + ")", 
              test, 
              testProviders.length );
        }
        else
        {
            System.out.println( "PATH: " + path + " " + expand + ", " + sort 
              + "(" 
              + buildProviders.length + ", " 
              + runtimeProviders.length 
              + ", " + testProviders.length 
              + ")"
            );
        }
    }
    
    void doAggregatedProviderTest( 
      String path, boolean expand, int build, int runtime, int test )
      throws Exception
    {
        doAggregatedProviderTest( path, expand, false, build, runtime, test, true );
        doAggregatedProviderTest( path, expand, true, build, runtime, test, true );
    }
    
    void doAggregatedProviderTest( 
      String path, boolean expand, boolean sort, int build, int runtime, int test )
      throws Exception
    {
        doAggregatedProviderTest( path, expand, sort, build, runtime, test, true );
    }
    
    void doAggregatedProviderTest( 
      String path, boolean expand )
      throws Exception
    {
        doAggregatedProviderTest( path, expand, true, 0, 0, 0, false );
    }
    
    void doAggregatedProviderTest( 
      String path, boolean expand, boolean sort, int build, int runtime, int test, boolean check )
      throws Exception
    {
        Resource resource = m_library.getResource( path );
        Resource[] buildProviders = resource.getAggregatedProviders( Scope.BUILD, expand, sort );
        Resource[] runtimeProviders = resource.getAggregatedProviders( Scope.RUNTIME, expand, sort  );
        Resource[] testProviders = resource.getAggregatedProviders( Scope.TEST, expand, sort );
        
        if( check )
        {
            assertEquals( 
              path + " build-deps (sorted:" + sort + ") (expanded:" + expand + ") ", 
              build, 
              buildProviders.length );
            assertEquals( 
              path + " runtime-deps (sorted:" + sort + ") (expanded:" + expand + ") ", 
              runtime, 
              runtimeProviders.length );
            assertEquals( 
              path + " test-deps (sorted:" + sort + ") (expanded:" + expand + ") ", 
              test, 
              testProviders.length );
        }
        else
        {
            System.out.println( "CHECK: " + path + " " + expand + ", " + sort 
              + "(" 
              + buildProviders.length + ", " 
              + runtimeProviders.length + ", " 
              + testProviders.length 
              + ")"
            );
        }
    }

    static
    {
        System.setProperty( 
          "java.util.prefs.PreferencesFactory", 
          "net.dpml.transit.store.LocalPreferencesFactory" );
        System.setProperty( 
          "java.util.logging.config.class", 
          System.getProperty( 
            "java.util.logging.config.class", 
            "net.dpml.transit.util.ConfigurationHandler" ) );
    }
}

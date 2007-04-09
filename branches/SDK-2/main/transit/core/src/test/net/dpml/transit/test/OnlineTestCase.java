/*
 * Copyright 2004 Niclas Hedhman.
 * Copyright 2004-2006 Stephen J. McConnell.
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

package net.dpml.transit.test;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileOutputStream;

import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

import dpml.transit.info.CacheDirective;
import dpml.transit.info.TransitDirective;
import dpml.transit.info.HostDirective;

import net.dpml.transit.Transit;

/**
 * URL Handler TestCase for online operations.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class OnlineTestCase extends TestCase
{
    private static final Transit TRANSIT = setupTransit();
    
    private static final Transit setupTransit()
    {
        try
        {
            File basedir = new File( System.getProperty( "basedir" ) );
            new File( basedir, "target/test/cache" ).delete();
            CacheDirective cache =
              new CacheDirective(
                "${user.dir}/target/test/cache",
                CacheDirective.CACHE_LAYOUT,
                "file:${user.dir}/target/test/trusted",
                CacheDirective.LOCAL_LAYOUT,
                new HostDirective[0] );
            TransitDirective directive = new TransitDirective( null, cache );
            File config = new File( basedir, "target/test/online.config" );
            FileOutputStream out = new FileOutputStream( config );
            TransitDirective.encode( directive, out );
            System.setProperty( "dpml.transit.profile", config.toURI().toASCIIString() );
            return Transit.getInstance();
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            return null;
        }
    }

   /**
    * Test stack jar url.
    * @exception Exception if an error occurs
    */
    public void testStackedJarUrl() throws Exception
    {
        URL url = new URL( "jar:artifact:jar:dpml/test/dpml-test-testb!/net/dpml/test/testb/B.class" );
        Object obj = url.getContent();
        //System.out.println( obj.getClass() );
    }

   /**
    * Test non-versioned artifact download.
    * @exception Exception if an error occurs
    */
    public void testDownloadNoVersion() throws Exception
    {
        URL url = new URL( "artifact:testfile:dpml/test/1" );
        String content = getContent( url );
        assertEquals( "Content not retrieved.", "abc\n", content );
    }

   /**
    * Test versioned artifact download.
    * @exception Exception if an error occurs
    */
    public void testDownloadWithVersion() throws Exception
    {
        URL url = new URL( "artifact:testfile:dpml/test/abc#1.0.1" );
        String content = getContent( url );
        assertEquals( "Content not retrieved.", "abc\ndef\n", content );
    }

   /**
    * Test classloader creation.
    * @exception Exception if an error occurs
    */
    public void testClassLoader1() throws Exception
    {
        URL url = new URL( "artifact:jar:dpml/test/dpml-test-testa" );
        ClassLoader classloader = new URLClassLoader( new URL[]{url} );
        classloader.loadClass( "net.dpml.test.testa.A" );
    }

   /**
    * Test classloader creation.
    * @exception Exception if an error occurs
    */
    public void testClassLoader2() throws Exception
    {
        URL url = new URL( "artifact:jar:dpml/test/dpml-test-testb" );
        ClassLoader classloader = new URLClassLoader( new URL[]{url} );
        classloader.loadClass( "net.dpml.test.testb.B" );
    }

   /**
    * Test internal resource.
    * @exception Exception if an error occurs
    */
    public void testInternalResource() throws Exception
    {
        URL url = new URL( "artifact:jar:dpml/test/dpml-test-testb" );
        ClassLoader classloader = new URLClassLoader( new URL[]{url} );
        Class clazz = classloader.loadClass( "net.dpml.test.testb.B" );
        URL resourceUrl = clazz.getResource( "TestB.xinfo" );
        String external =
          "artifact:jar:dpml/test/dpml-test-testb!/net/dpml/test/testb/TestB.xinfo";
        assertEquals( "external", external, resourceUrl.toExternalForm() );
        resourceUrl.openStream();
    }

   /**
    * Test content conversion.
    * @exception Exception if an error occurs
    */
    public void testContentConversionA() throws Exception
    {
        URL url = new URL( "artifact:jar:dpml/test/dpml-test-testa" );
        Object content = url.getContent();
        assertTrue( "Content type not correct.", content instanceof InputStream );
    }

   /**
    * Test content conversion.
    * @exception Exception if an error occurs
    */
    public void testContentConversionB() throws Exception
    {
        URL url = new URL( "artifact:png:dpml/test/sample" );
        Object content = url.getContent();
        assertTrue( "Content type not correct.", content instanceof java.awt.image.ImageProducer );
    }

    private String getContent( URL url )
        throws Exception
    {
        InputStream stream = url.openStream();
        try
        {
            InputStreamReader isr = new InputStreamReader( stream, "ISO8859-1" );
            BufferedReader in = new BufferedReader( isr );
            StringBuffer buf = new StringBuffer( 100 );
            String line = in.readLine();
            while( line != null )
            {
                buf.append( line );
                buf.append( '\n' );
                line = in.readLine();
            }
            return buf.toString();
        } 
        finally
        {
            stream.close();
        }
    }
}

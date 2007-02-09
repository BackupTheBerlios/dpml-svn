/*
 * Copyright 2005 Niclas Hedhman
 * Copyright 2005-2007 Stephen J. McConnell.
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

import dpml.transit.StreamUtils;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;

import net.dpml.transit.ArtifactNotFoundException;
import net.dpml.transit.LinkManager;
import net.dpml.transit.Transit;

import dpml.transit.info.CacheDirective;
import dpml.transit.info.TransitDirective;
import dpml.transit.info.HostDirective;

/**
 * LinkTestCase.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class LinkTestCase extends TestCase
{
    static final Transit TRANSIT = setupTransit();
    static final LinkManager LINK = TRANSIT.getLinkManager();
    
   /**
    * Static Transit setup.
    */
    private static Transit setupTransit()
    {
        System.setProperty( "java.protocol.handler.pkgs", "dpml.transit" );
        try
        {
            new File( "target/test/cache" ).delete();
            CacheDirective cache =
              new CacheDirective(
                "${user.dir}/target/test/cache",
                CacheDirective.CACHE_LAYOUT,
                "file:${user.dir}/target/test/trusted",
                "classic",
                new HostDirective[0] );
            TransitDirective directive = new TransitDirective( null, cache );
            File basedir = new File( System.getProperty( "user.dir" ) );
            File config = new File( basedir, "target/test/link.config" );
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
    * Test-case setup.
    * @exception Exception if an error occurs
    */
    public void setUp() throws Exception
    {
        if( null == TRANSIT )
        {
            throw new IllegalStateException( "TRANSIT" );
        }
    }
    
   /**
    * Test default target value.
    * @exception Exception if an error occurs
    */
    public void testLinkCreation() throws Exception
    {
        URI link = URI.create( "link:blob:transit/test/aaa/bbb" );
        URI target = URI.create( "artifact:bla:abc/def#142" );
        LINK.setTargetURI( link, target );
        URI value = LINK.getTargetURI( link );
        assertEquals( "target", target, value );
    }

   /**
    * Validate that a content request on a link with the URL class 
    * argument returns the link URI value.
    * @exception Exception if an error in test execution occurs
    */
    public void testURIContent() throws Exception
    {
        URI link = URI.create( "link:blob:transit/test/aaa/bbb" );
        URI target = URI.create( "artifact:bla:abc/def#142" );
        LINK.setTargetURI( link, target );
        URL url = link.toURL();
        Class[] args = new Class[]{URI.class};
        URI content = (URI) url.getContent( args );
        assertEquals( "content", link, content );
    }

   /**
    * Test link target reset.
    * @exception Exception if an error occurs
    */
    public void testResetValue() throws Exception
    {
        // create initial link
        
        URI link = URI.create( "link:blob:transit/test/example" );
        URI target = URI.create( "artifact:testfile:dpml/test/abc#1.0" );
        LINK.setTargetURI( link, target );
        URI value = LINK.getTargetURI( link );
        assertEquals( "target", target, value );
        
        // reset the link to a new target
        
        URI alt = URI.create( "artifact:testfile:dpml/test/abc#2.0" );
        LINK.setTargetURI( link, alt );
        URI newValue = LINK.getTargetURI( link );
        assertEquals( "target", alt, newValue );
    }

   /**
    * Test get value.
    * @exception Exception if an error occurs
    */
    public void testInputStrreamValue() throws Exception
    {
        URI link = URI.create( "link:blob:transit/test/aaa/xxx" );
        URI targetUri = URI.create( "artifact:testfile:dpml/test/abc#1.0.1" );
        LINK.setTargetURI( link, targetUri );
        
        URL linkURL = link.toURL();
        URL targetURL = targetUri.toURL();
        
        InputStream linkIs = linkURL.openConnection().getInputStream();
        InputStream targetIS = targetURL.openConnection().getInputStream();
        assertTrue( "different content", StreamUtils.compareStreams( linkIs, targetIS ) );
    }
    
   /**
    * Test illegal link artifact.
    * @exception Exception if an error occurs
    */
    public void testIllegalArtifact()
        throws Exception
    {
        URI link = URI.create( "link:blob:transit/test/errors/aaa" );
        URI targetUri = URI.create( "artifact:abc:does/not/exist" );
        LINK.setTargetURI( link, targetUri );
        URL url = link.toURL();
        try
        {
            url.getContent();
            fail( "ArtifactNotFoundException was not thrown." );
        } 
        catch( ArtifactNotFoundException e )
        {
            // success
        }
    }
}

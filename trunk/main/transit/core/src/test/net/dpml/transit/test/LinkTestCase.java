/*
 * Copyright 2005 Niclas Hedhman
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
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.prefs.Preferences;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.TestCase;

import net.dpml.transit.artifact.ArtifactNotFoundException;
import net.dpml.transit.link.Link;
import net.dpml.transit.util.StreamUtils;
import net.dpml.transit.info.CacheDirective;
import net.dpml.transit.info.TransitDirective;
import net.dpml.transit.info.LayoutDirective;
import net.dpml.transit.info.HostDirective;
import net.dpml.transit.info.ContentDirective;
import net.dpml.transit.monitor.LoggingAdapter;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.HostModel;
import net.dpml.transit.Transit;
import net.dpml.transit.DefaultTransitModel;

/**
 *
 * @author <a href="http://www.dpml.net/">The Digital Product Meta Library</a>
 * @version $Id$
 */
public class LinkTestCase extends TestCase
{
    static Transit TRANSIT = setupTransit();
    
    private static Transit setupTransit()
    {
        System.setProperty( "java.protocol.handler.pkgs", "net.dpml.transit" );
        try
        {
            new File( "target/test/cache" ).delete();
            CacheDirective cache =
              new CacheDirective(
                "${user.dir}/target/test/cache",
                CacheDirective.CACHE_LAYOUT,
                "file:${user.dir}/target/test/trusted",
                CacheDirective.LOCAL_LAYOUT,
                new LayoutDirective[0],
                new HostDirective[0],
                new ContentDirective[0] );
            TransitDirective directive = new TransitDirective( null, cache );
            LoggingAdapter logger = new LoggingAdapter( "test" );
            TransitModel model = new DefaultTransitModel( logger, directive );
            return Transit.getInstance( model );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public void setUp() throws Exception
    {
        if( null == TRANSIT )
        {
            throw new IllegalStateException( "TRANSIT" );
        }
    } 

    /**
     * Constructor
     * @param name the test name
     */
    public LinkTestCase( String name )
    {
        super( name );
    }

    public void testDefaultValue()
        throws Exception
    {
        URL url = new URL( "link:blob:transit/test/aaa/bbb" );
        Class[] args = new Class[] { Link.class };
        Link link = (Link) url.getContent( args );
        URI defUri = URI.create( "artifact:bla:abc/def#142" );
        URI uri = link.getTargetURI( defUri );
        assertEquals( "default", defUri, uri );
    }

    public void testContentUri()
        throws Exception
    {
        URI orig = URI.create( "link:blob:transit/test/aaa/bbb" );
        URL url = orig.toURL();
        Class[] args = new Class[] { URI.class };
        URI found = (URI) url.getContent( args );
        assertEquals( "default", orig, found );
    }

    public void testSetValue()
        throws Exception
    {
        URL url = new URL( "link:blob:transit/test/aaa/ccc" );
        Class[] args = new Class[] { Link.class };
        Link link = (Link) url.getContent( args );
        URI newUri = URI.create( "artifact:testfile:dpml/test/abc#1.0.1" );
        link.setTargetURI( newUri );

        url = new URL( "link:blob:transit/test/aaa/ccc" );
        link = (Link) url.getContent( args );
        URI defUri = URI.create( "artifact:bla:ccc/ddd#222" );
        URI uri = link.getTargetURI( defUri );

        assertEquals( "setting", newUri, uri );
    }

    public void testResetValue()
        throws Exception
    {
        URL url = new URL( "link:blob:transit/test/example" );
        Class[] args = new Class[] { Link.class };
        Link link = (Link) url.getContent( args );
        URI newUri = URI.create( "artifact:testfile:dpml/test/abc#1.0" );
        link.setTargetURI( newUri );
        URI newerUri = URI.create( "artifact:testfile:dpml/test/abc#2.0" );
        link.setTargetURI( newerUri );
    }

    public void testGetValue()
        throws Exception
    {
        URL url = new URL( "link:blob:transit/test/aaa/xxx" );
        Class[] args = new Class[] { Link.class };
        Link link = (Link) url.getContent( args );
        URI newUri = URI.create( "artifact:testfile:dpml/test/abc#1.0.1" );
        link.setTargetURI( newUri );

        url = new URL( "link:blob:transit/test/aaa/xxx" );
        URL original = newUri.toURL();
        InputStream in1 = url.openConnection().getInputStream();
        InputStream in2 = original.openConnection().getInputStream();
        assertTrue( "different content", StreamUtils.compareStreams( in1, in2 ) );
    }

    public void testIllegalArtifact()
        throws Exception
    {
        URL url = new URL( "link:blob:transit/test/errors/aaa" );
        Class[] args = new Class[] { Link.class };
        URI newUri = URI.create( "artifact:abc:does/not/exist" );
        try
        {
            Link link = (Link) url.getContent( args );
            link.setTargetURI( newUri );
            url.getContent();
            fail( "ArtifactNotFoundException was not thrown." );
        } 
        catch( ArtifactNotFoundException e )
        {
            // should throw an exception if we try to read against a
            // non existent artifact.
        }
    }
}

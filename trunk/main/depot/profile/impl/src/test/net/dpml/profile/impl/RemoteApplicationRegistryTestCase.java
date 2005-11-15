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

package net.dpml.profile.impl;

import java.net.URL;
import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import net.dpml.profile.info.ApplicationDescriptor;
import net.dpml.profile.info.StartupPolicy;
import net.dpml.profile.info.ValueDescriptor;
import net.dpml.profile.model.RegistryListener;
import net.dpml.profile.model.RegistryEvent;

import net.dpml.transit.Artifact;
import net.dpml.transit.Logger;
import net.dpml.transit.monitor.LoggingAdapter;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.DuplicateKeyException;


/**
 * The RemoteApplicationRegistryTestCase class validates the application registry 
 * implementation.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class RemoteApplicationRegistryTestCase extends TestCase
{
    private URL m_url;
    private Logger m_logger = new LoggingAdapter( "registry" );
    
    public void setUp() throws Exception
    {
        Artifact artifact = Artifact.createArtifact( "local:xml:dpml/station/registry-test" );
        URL url = artifact.toURL();
        File file = (File) url.getContent( new Class[]{ File.class } );
        if( file.exists() )
        {
            file.delete();
        }
        m_url = url;
    }
    
    public void tearDown() throws Exception
    {
        File file = (File) m_url.getContent( new Class[]{ File.class } );
        file.deleteOnExit();
    }
    
    public void testRegistryInitialCount() throws Exception
    {
        RemoteApplicationRegistry registry = 
          new RemoteApplicationRegistry( m_logger, m_url );
        int n = registry.getApplicationDescriptorCount();
        assertEquals( "inital-count", 0, n );
    }
    
    public void testApplicationAddition() throws Exception
    {
        ApplicationDescriptor profile = 
          new ApplicationDescriptor( 
            "link:plugin:acme/widget", "widget", new ValueDescriptor[0], ".", 
            StartupPolicy.MANUAL, 10, 20, new Properties(), null );
            
        RemoteApplicationRegistry registry = 
          new RemoteApplicationRegistry( m_logger, m_url );
        registry.addApplicationDescriptor( "test", profile );
        int n = registry.getApplicationDescriptorCount();
        assertEquals( "single-count", 1, n );
    }
    
    public void testApplicationRemoval() throws Exception
    {
        ApplicationDescriptor profile = 
          new ApplicationDescriptor( 
            "link:plugin:acme/widget", "widget", new ValueDescriptor[0], ".", 
            StartupPolicy.MANUAL, 10, 20, new Properties(), null );

        RemoteApplicationRegistry registry = 
          new RemoteApplicationRegistry( m_logger, m_url );
        registry.addApplicationDescriptor( "test", profile );
        registry.removeApplicationDescriptor( "test" );
        int n = registry.getApplicationDescriptorCount();
        assertEquals( "empty-count", 0, n );
    }
    
    public void testRegistryStorage() throws Exception
    {
        ApplicationDescriptor profile = 
          new ApplicationDescriptor( 
            "link:plugin:acme/widget", "widget", new ValueDescriptor[0], ".", 
            StartupPolicy.MANUAL, 10, 20, new Properties(), null );
            
        RemoteApplicationRegistry registry = 
          new RemoteApplicationRegistry( m_logger, m_url );
        registry.addApplicationDescriptor( "test-1", profile );
        registry.addApplicationDescriptor( "test-2", profile );
        int n = registry.getApplicationDescriptorCount();
        assertEquals( "double-count", 2, n );
        registry.flush();
            
        registry = 
          new RemoteApplicationRegistry( m_logger, m_url );
        n = registry.getApplicationDescriptorCount();
        assertEquals( "double-count-after-restore", 2, n );
    }

    public void testRegistryUpdate() throws Exception
    {
        ApplicationDescriptor profileOne = 
          new ApplicationDescriptor( 
            "link:plugin:acme/widget", "widget", new ValueDescriptor[0], ".", 
            StartupPolicy.MANUAL, 10, 20, new Properties(), null );
            
        ApplicationDescriptor profileTwo = 
          new ApplicationDescriptor( 
            "link:plugin:acme/widget", "widget", new ValueDescriptor[0], ".", 
            StartupPolicy.DISABLED, 10, 20, new Properties(), null );
        
        RemoteApplicationRegistry registry = 
          new RemoteApplicationRegistry( m_logger, m_url );
        registry.addApplicationDescriptor( "test", profileOne );
        registry.updateApplicationDescriptor( "test", profileTwo );
        int n = registry.getApplicationDescriptorCount();
        assertEquals( "updated-count", 1, n );
        ApplicationDescriptor updated = registry.getApplicationDescriptor( "test" );
        assertEquals( "updated profile", profileTwo, updated );
    }

    public void testGetApplicationDescriptors() throws Exception
    {
        ApplicationDescriptor profileOne = 
          new ApplicationDescriptor( 
            "link:plugin:acme/widget", "widget", new ValueDescriptor[0], ".", 
            StartupPolicy.MANUAL, 10, 20, new Properties(), null );
            
        ApplicationDescriptor profileTwo = 
          new ApplicationDescriptor( 
            "link:plugin:acme/widget", "widget", new ValueDescriptor[0], ".", 
            StartupPolicy.DISABLED, 10, 20, new Properties(), null );
        
        RemoteApplicationRegistry registry = 
          new RemoteApplicationRegistry( m_logger, m_url );
        registry.addApplicationDescriptor( "test-one", profileOne );
        registry.addApplicationDescriptor( "test-two", profileTwo );
        int n = registry.getApplicationDescriptorCount();
        assertEquals( "double-count", 2, n );
        ApplicationDescriptor[] entries = registry.getApplicationDescriptors();
        assertEquals( "count", 2, entries.length );
    }

    public void testInvalidKey() throws Exception
    {
        ApplicationDescriptor profile = 
          new ApplicationDescriptor( 
            "link:plugin:acme/widget", "widget", new ValueDescriptor[0], ".", 
            StartupPolicy.MANUAL, 10, 20, new Properties(), null );
            
        try
        {
            RemoteApplicationRegistry registry = 
              new RemoteApplicationRegistry( m_logger, m_url );
            registry.addApplicationDescriptor( "test", profile );
            registry.getApplicationDescriptor( "zzz" );
            fail( "Did not throw an UnknownKeyException" );
        }
        catch( UnknownKeyException e )
        {
            // success
        }
    }

    public void testDuplicateKey() throws Exception
    {
        ApplicationDescriptor profile = 
          new ApplicationDescriptor( 
            "link:plugin:acme/widget", "widget", new ValueDescriptor[0], ".", 
            StartupPolicy.MANUAL, 10, 20, new Properties(), null );
            
        try
        {
            RemoteApplicationRegistry registry = 
              new RemoteApplicationRegistry( m_logger, m_url );
            registry.addApplicationDescriptor( "test", profile );
            registry.addApplicationDescriptor( "test", profile );
            fail( "Did not throw an DuplicateKeyException" );
        }
        catch( DuplicateKeyException e )
        {
            // success
        }
    }
    
    public void testListeners() throws Exception
    {
        ApplicationDescriptor profile = 
          new ApplicationDescriptor( 
            "link:plugin:acme/widget", "widget", new ValueDescriptor[0], ".", 
            StartupPolicy.MANUAL, 10, 20, new Properties(), null );
        
        RemoteApplicationRegistry registry = 
          new RemoteApplicationRegistry( m_logger, m_url );
        RegistryListener listener = new MockRegistryListener();
        registry.addRegistryListener( listener );
        registry.addApplicationDescriptor( "batman", profile );
        registry.addApplicationDescriptor( "robin", profile );
        registry.removeApplicationDescriptor( "robin" );
        registry.updateApplicationDescriptor( "batman", profile );
        registry.removeRegistryListener( listener );
        registry.removeApplicationDescriptor( "batman" );
    }
    
    private class MockRegistryListener implements RegistryListener
    {
        public void profileAdded( RegistryEvent event )
        {
        }
        
        public void profileRemoved( RegistryEvent event )
        {
        }
    }
}

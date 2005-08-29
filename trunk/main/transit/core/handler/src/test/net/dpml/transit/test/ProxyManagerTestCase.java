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

package net.dpml.transit.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.PasswordAuthentication; 
import java.rmi.RemoteException;
import java.util.Date;
import java.util.prefs.Preferences;
import java.net.URL;

import junit.framework.TestCase;

import net.dpml.transit.Transit;
import net.dpml.transit.monitor.LoggingAdapter;
import net.dpml.transit.model.ProxyEvent;
import net.dpml.transit.model.ProxyListener;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.ProxyModel;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.DefaultTransitModel;

/**
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ProxyManagerTestCase extends TestCase
{
    static
    {
        System.setProperty( 
           "java.util.prefs.PreferencesFactory", 
           "net.dpml.transit.store.LocalPreferencesFactory" );
        System.setProperty( "dpml.transit.profile", "test-proxy" );
        System.setProperty( "dpml.data", "target/test/data" );
    }

    private TransitModel m_model;
    private File m_data;

    public void setUp() throws Exception
    {
        m_data = new File( System.getProperty( "dpml.data" ) );
        m_model = new DefaultTransitModel();
    }

    public void testProxyAuthenticationListener() throws Exception
    {
        String username = "peter";
        String password = "rabbit";
        PasswordAuthentication auth = 
          new PasswordAuthentication( username, password.toCharArray() );
        
        TestProxyAuthListener listener = new TestProxyAuthListener( auth );
        m_model.getProxyModel().addProxyListener( listener );
        m_model.getProxyModel().update( null, auth, null );
    }

    public void testProxyExcludes() throws Exception
    {
        TestProxyExcludesListener listener = new TestProxyExcludesListener();
        m_model.getProxyModel().addProxyListener( listener );
        String[] excludes = new String[]{ "http://www.osm.net", "http://repository.dpml.net" };
        m_model.getProxyModel().update( null, null, excludes );
    }

    public class TestProxyExcludesListener implements ProxyListener
    {
        public void proxyChanged( ProxyEvent event )
        {
            try
            {
                event.getProxyModel().removeProxyListener( this );
                String[] result = event.getExcludes();
                assertNotNull( "excludes-not-null", result );
                assertEquals( "excludes", 2, result.length );
            }
            catch( RemoteException e )
            {
                fail( e.toString() );
            }

        }
    }

    public class TestProxyAuthListener implements ProxyListener
    {
        private PasswordAuthentication m_auth;
        public TestProxyAuthListener( PasswordAuthentication auth )
        {
            m_auth = auth;
        }
        public void proxyChanged( ProxyEvent event )
        {
            try
            {
                event.getProxyModel().removeProxyListener( this );
                PasswordAuthentication auth = event.getPasswordAuthentication();
                assertNotNull( "auth", auth );
                assertEquals( "username", m_auth.getUserName(), auth.getUserName() );
                assertEquals( "password", 
                  new String( m_auth.getPassword() ), 
                  new String( auth.getPassword() ) );
            }
            catch( RemoteException e )
            {
                fail( e.toString() );
            }
        }
    }
}


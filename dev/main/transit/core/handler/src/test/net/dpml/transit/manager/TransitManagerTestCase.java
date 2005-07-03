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

package net.dpml.transit.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.DefaultTransitModel;

/**
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class TransitManagerTestCase extends TestCase
{
    static
    {
        System.setProperty( 
           "java.util.prefs.PreferencesFactory", 
           "net.dpml.transit.unit.LocalPreferencesFactory" );
        System.setProperty( "dpml.transit.profile", "test-cache" );
        System.setProperty( "dpml.data", "target/test/data" );
    }

    private TransitModel m_model;
    private File m_data;

    public void setUp() throws Exception
    {
        m_data = new File( System.getProperty( "dpml.data" ) );
        m_model = new DefaultTransitModel();
    }

    public void testCacheDirectory() throws Exception
    {
        File dir = new File( m_data, "cache" ).getAbsoluteFile();
        m_model.getCacheModel().setCacheDirectory( dir );
        File cache = m_model.getCacheModel().getCacheDirectory();
        assertEquals( "cache", dir, cache );
    }
}


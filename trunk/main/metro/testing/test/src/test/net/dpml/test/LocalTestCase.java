/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.test;

import dpml.util.DefaultLogger;

import java.net.URI;
import java.net.URL;
import java.rmi.RMISecurityManager;

import junit.framework.TestCase;

import net.dpml.appliance.Appliance;

import net.dpml.transit.Artifact;

import net.dpml.util.Logger;

/**
 * Appliance testcase.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class LocalTestCase extends TestCase
{
    private Logger m_logger = new DefaultLogger( "test" );
    
   /**
    * Test appliance deployment via appliance uri content resolution.
    * @exception Exception if an error occurs
    */
    public void testClassicAppliance() throws Exception
    {
        URI uri = URI.create( "link:appliance:dpml/metro/demo" );
        URL url = Artifact.toURL( uri );
        Appliance appliance = (Appliance) url.getContent( new Class[]{Appliance.class} );
        try
        {
            appliance.commission();
        }
        finally
        {
            appliance.decommission();
        }
    }

    static
    {
        System.setSecurityManager( new RMISecurityManager() );
    }
}

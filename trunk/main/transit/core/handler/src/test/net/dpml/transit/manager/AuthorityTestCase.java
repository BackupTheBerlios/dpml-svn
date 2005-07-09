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

import java.net.URL;

import junit.framework.TestCase;

import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.HostModel;
import net.dpml.transit.model.DefaultTransitModel;
import net.dpml.transit.unit.TransitStorageUnit;

/**
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class AuthorityTestCase extends TestCase
{
    public void testAuthority() throws Exception
    {
        TransitStorageUnit store = new TransitStorageUnit( new URL( "http://staging.dpml.net/test/" ) );
        TransitModel model = new DefaultTransitModel( store );
        HostModel[] hosts = model.getCacheModel().getHostModels();
        assertEquals( "hosts", 2, hosts.length );
    }
}


/*
 * Copyright 2004 Niclas Hedhman.
 * Copyright 2004 Stephen J. McConnell.
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

import junit.framework.TestCase;

import net.dpml.transit.DefaultTransitModel;
import net.dpml.transit.Transit;
import net.dpml.transit.model.TransitModel;

/**
 * Test execution of the disposal of the Transit model.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: OnlineTestCase.java 2926 2005-06-27 10:53:17Z mcconnell@dpml.net $
 */
public class DisposalTestCase extends TestCase
{
    private DefaultTransitModel m_model;
    
    public void testTransitLifecycle() throws Exception
    {
        m_model = DefaultTransitModel.getDefaultModel();
        Transit.getInstance( m_model );
        m_model.dispose();
    }
}

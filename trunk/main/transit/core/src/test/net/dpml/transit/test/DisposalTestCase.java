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

package net.dpml.transit.test;

import junit.framework.TestCase;

import net.dpml.transit.DefaultTransitModel;
import net.dpml.transit.Transit;

/**
 * Test execution of the disposal of the Transit model.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DisposalTestCase extends TestCase
{
    private DefaultTransitModel m_model;
    
    public void setUp() throws Exception
    {
        m_model = DefaultTransitModel.getDefaultModel();
        Transit.getInstance( m_model );
    }
    
    public void testSetupAndTearDown()
    {
        // trigger setup and teardown
    }

    public void tearDown()
    {
        m_model.dispose();
    }
}

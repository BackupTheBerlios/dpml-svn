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
 
package net.dpml.runtime.lifecycle;

import net.dpml.runtime.AbstractTestCase;

import net.dpml.runtime.Component;
import net.dpml.runtime.Provider;

import org.acme.StateWidget;

/**
 * Lifecycle testcase.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StateTestCase extends AbstractTestCase
{
   /**
    * Validate component lifecycle.
    * @exception Exception if an error occurs
    */
    public void testLifecycle() throws Exception
    {
        Component component = load( Component.class, "state.xml", "state" );
        Provider provider = component.getProvider();
        StateWidget widget = provider.getInstance( StateWidget.class );
        assertEquals( "started", 1, widget.getState() );
        component.release( provider );
        component.terminate();
        assertEquals( "stopped", 2, widget.getState() );
    }
}

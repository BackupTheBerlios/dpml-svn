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

package net.dpml.runtime.lifestyle;

import net.dpml.annotation.LifestylePolicy;
import net.dpml.runtime.AbstractTestCase;

import net.dpml.runtime.Component;
import net.dpml.runtime.ComponentStrategy;

import org.acme.DefaultWidget;
import org.acme.Widget;

/**
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TransientTestCase extends AbstractTestCase
{
    public void testEquality() throws Exception
    {
        ComponentStrategy component = load( ComponentStrategy.class, "transient.xml", "transient" );
        assertEquals( "lifestyle", component.getLifestylePolicy(), LifestylePolicy.TRANSIENT );
        Widget w1 = component.getProvider().getInstance( DefaultWidget.class );
        Widget w2 = component.getProvider().getInstance( DefaultWidget.class );
        if( w1 == w2 )
        {
            fail( "transients" );
        }
    }
}

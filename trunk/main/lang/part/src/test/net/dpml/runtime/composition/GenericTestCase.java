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

package net.dpml.runtime.composition;

import java.awt.Color;
import java.io.File;

import net.dpml.runtime.AbstractTestCase;

import net.dpml.runtime.Provider;
import net.dpml.runtime.Component;

import org.acme.Widget;
import org.acme.Gizmo;
import org.acme.GenericComponent;

/**
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class GenericTestCase extends AbstractTestCase
{
    public void testComponent() throws Exception
    {
        GenericComponent component = 
          load( GenericComponent.class, "generics.xml", "generic" );
          
        assertNotNull( "widget", component.getParts().getWidget( Provider.class ) );
        assertNotNull( "gizmo", component.getParts().getGizmo( Component.class ) );
        Component c = component.getParts().getWidget( Component.class );
        Provider provider = c.getProvider();
        Widget w1 = provider.getInstance( Widget.class );
        Widget w2 = component.getParts().getWidget( Widget.class );
        assertEquals( "equality", w1, w2 );
        Gizmo gizmo = component.getParts().getGizmo( Gizmo.class );
        int number = gizmo.getNumber();
        assertEquals( "gizmo", 42, number );
    }
}

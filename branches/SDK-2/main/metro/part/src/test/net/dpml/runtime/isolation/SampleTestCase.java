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

package net.dpml.runtime.isolation;

import java.lang.reflect.Proxy;

import net.dpml.runtime.AbstractTestCase;

import org.acme.DefaultWidget;
import org.acme.Widget;

/**
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class SampleTestCase extends AbstractTestCase
{
    public void testNormalUsage() throws Exception
    {
        Widget widget = 
          load( Widget.class, "component.xml", "normal" );

        String message = widget.getMessage();
        assertEquals( "message", "Hello", message );
    }
    
    public void testNoIsolation() throws Exception
    {
        Widget widget = 
          load( DefaultWidget.class, "component.xml", "isolation" );
        assertFalse( "isa-proxy", Proxy.isProxyClass( widget.getClass() ) );
    }

    public void testWithIsolation() throws Exception
    {
        Widget widget = 
          load( Widget.class, "component.xml", "no-isolation" );
        assertTrue( "isa-proxy", Proxy.isProxyClass( widget.getClass() ) );
    }
}

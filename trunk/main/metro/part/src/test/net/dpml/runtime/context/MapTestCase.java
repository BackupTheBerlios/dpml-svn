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

package net.dpml.runtime.context;

import java.util.Map;

import net.dpml.runtime.AbstractTestCase;

import org.acme.MapWidget;
import org.acme.Widget;

/**
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class MapTestCase extends AbstractTestCase
{
    public void testEquality() throws Exception
    {
        MapWidget widget = 
          load( MapWidget.class, "map.xml", "map" );
        Map map = widget.getContext().getDemo();
        int n = map.size();
        String message = (String) map.get( "message" );
        String value = (String) map.get( "foo" );
        
        assertEquals( "message", "Hello from the map entry", message );
        assertEquals( "foo", "bar", value );
    }
}

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

import java.awt.Color;
import java.util.Map;
import java.util.SortedMap;

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
    public void testSimpleMap() throws Exception
    {
        MapWidget widget = 
          load( MapWidget.class, "map.xml", "map" );
        Map<String,String> map = widget.getContext().getPrimary();
        String message = map.get( "message" );
        String value = map.get( "foo" );
        assertEquals( "message", "Hello from the map entry", message );
        assertEquals( "foo", "bar", value );
    }
    
    public void testQualifiedMap() throws Exception
    {
        MapWidget widget = 
          load( MapWidget.class, "map.xml", "map" );
        SortedMap<String,String> map = widget.getContext().getSecondary();
        String foo = map.get( "foo" );
        String ping = map.get( "ping" );
        assertEquals( "foo", "bar", foo );
        assertEquals( "ping", "pong", ping );
    }
    
    public void testColors() throws Exception
    {
        MapWidget widget = 
          load( MapWidget.class, "map.xml", "map" );
        Map<String,Color> map = widget.getContext().getColors();
        Color red = map.get( "red" );
        Color green = map.get( "green" );
        Color blue = map.get( "blue" );
        assertEquals( "red", Color.RED, red );
        assertEquals( "green", Color.GREEN, green );
        assertEquals( "blue", Color.BLUE, blue );
    }
}

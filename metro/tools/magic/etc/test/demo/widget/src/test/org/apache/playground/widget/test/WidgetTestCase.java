/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.playground.widget.test;

import junit.framework.TestCase;

import org.apache.playground.gizmo.Gizmo;
import org.apache.playground.widget.Widget;

/**
 * Demo class. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revisio:-)n: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class WidgetTestCase extends TestCase
{
   /**
    * Test widget creation.
    */
    public void testWidgetCreation()
    {
        Widget widget = new Widget();
        String info = widget.getInfo();
        assertNotNull( info );
    }

   /**
    * Test widget type.
    */
    public void testWidgetType()
    {
        Widget widget = new Widget();
        assertTrue( widget instanceof Gizmo );
        assertTrue( widget instanceof Widget );
    }

   /**
    * Test widget info.
    */
    public void testWidgetContent()
    {
        Widget widget = new Widget();
        String info = widget.getInfo();
        assertEquals( info, "Hello World from Gizmo\nHello World from Widget" );
    }

}

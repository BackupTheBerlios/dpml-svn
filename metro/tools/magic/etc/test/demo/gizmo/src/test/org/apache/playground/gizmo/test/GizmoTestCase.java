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

package org.apache.playground.gizmo.test;

import junit.framework.TestCase;

import org.apache.playground.gizmo.Gizmo;

/**
 * Demo class. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revisio:-)n: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class GizmoTestCase extends TestCase
{
   /**
    * Test gizmo creation.
    */
    public void testGizmoCreation()
    {
        Gizmo gizmo = new Gizmo();
        String info = gizmo.getInfo();
        assertNotNull( info );
    }

   /**
    * Test gizmo creation.
    */
    public void testGizmoContent()
    {
        Gizmo gizmo = new Gizmo();
        String info = gizmo.getInfo();
        assertEquals( info, "Hello World from Gizmo" );
    }

}

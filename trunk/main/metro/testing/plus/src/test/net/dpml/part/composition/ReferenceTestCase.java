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

package net.dpml.part.composition;

import org.acme.reference.AggregatingComponent;

/**
 * Test part-by-reference semantics.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ReferenceTestCase extends AbstractTestCase
{
   /**
    * Test deployment of a component configured using part-by-reference.
    * @exception Exception if an error occurs
    */
    public void testComponent() throws Exception
    {
        AggregatingComponent component = 
          load( AggregatingComponent.class, "reference.xml", null );
        String message = component.getMessage();
        assertEquals( "message", "OK", message );
    }
}

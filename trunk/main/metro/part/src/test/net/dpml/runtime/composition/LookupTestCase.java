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

import net.dpml.runtime.AbstractTestCase;

import org.acme.CompositeComponent;

/**
 * Lookup testcase.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class LookupTestCase extends AbstractTestCase
{
   /**
    * Validate the lookup.xml example.
    * @exception Exception if an error occurs
    */
    public void testComponent() throws Exception
    {
        CompositeComponent component = 
          load( CompositeComponent.class, "lookup.xml", "lookup" );
        
        assertNotNull( "widget", component.getParts().getWidget() );
        assertNotNull( "gizmo", component.getParts().getGizmo() );
    }
}

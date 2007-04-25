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

import net.dpml.runtime.AbstractTestCase;

import org.acme.ContextualWidget;

/**
 * Context handling test case.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ContextualTestCase extends AbstractTestCase
{
   /**
    * Validate context entries in contextual.xml.
    * @exception Exception if an error occurs
    */
    public void testEquality() throws Exception
    {
        ContextualWidget widget = 
          load( ContextualWidget.class, "contextual.xml", "contextual" );
    
        assertEquals( "message", "Batman", widget.getMessage() );
        assertEquals( "primary", Color.RED, widget.getPrimary() );
        assertEquals( "secondary", Color.BLUE, widget.getSecondary() );
        assertEquals( "numbers.length", 3, widget.getNumbers().length );
        assertEquals( "numbers[0]", 1, widget.getNumbers()[0] );
        assertEquals( "numbers[1]", 2, widget.getNumbers()[1] );
        assertEquals( "numbers[2]", 3, widget.getNumbers()[2] );
    }
}

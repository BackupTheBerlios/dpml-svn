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

package net.dpml.runtime.service;

import net.dpml.runtime.AbstractTestCase;

import net.dpml.lang.Strategy;

import org.acme.Widget;
import org.acme.ForeignWidget;

/**
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ForeignTestCase extends AbstractTestCase
{
    public void testComponentViaPart() throws Exception
    {
        Widget widget = load( Widget.class, "foreign.xml", "foreign" );
        assertEquals( "message", "42", widget.getMessage() );
    }
    
    public void testComponentViaClass() throws Exception
    {
        Strategy strategy = Strategy.load( ForeignWidget.class, null, "foreign" );
        Widget widget = strategy.getContentForClass( Widget.class );
        assertEquals( "message", "42", widget.getMessage() );
    }
}

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

package net.dpml.runtime.profile;

import java.util.ServiceLoader;

import junit.framework.TestCase;

import org.acme.Widget;

/**
 * Profile test case.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ProfileTestCase extends TestCase
{
   /**
    * Validate service loading.
    * @exception Exception if an error occurs
    */
    public void testServiceLoading() throws Exception
    {
        Widget widget = getWidget();
        assertNotNull( "widget", widget );
        String message = widget.getMessage();
        assertEquals( "message", "Batman", message );
    }
    
   /**
    * Utility to load a Widget service.
    * @return the widget
    * @exception Exception if an error occurs
    */
    public Widget getWidget() throws Exception
    {
        ServiceLoader<Widget> loaders = ServiceLoader.load( Widget.class );
        for( Widget widget : loaders )
        {
            return widget;
        }
        return null;
    }
}

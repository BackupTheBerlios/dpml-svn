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

import net.dpml.runtime.AbstractTestCase;

import org.acme.ArrayWidget;
import org.acme.Widget;

/**
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ArrayTestCase extends AbstractTestCase
{
    public void testEquality() throws Exception
    {
        ArrayWidget widget = 
          load( ArrayWidget.class, "array.xml", "array" );
        
        char[] chars = new char[]{ 'h','e','l','l','o'};
        char[] result = widget.getContext().getMessage();
        assertEquals( "length", chars.length, result.length );
        for( int i=0; i<chars.length; i++ )
        {
            String assertion = "character-" + i;
            assertEquals( assertion, chars[i], result[i] );
        }
    }
}

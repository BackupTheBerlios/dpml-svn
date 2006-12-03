/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.tools.test;

import junit.framework.TestCase;

/**
 * PropertiesTestCase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PropertiesTestCase extends TestCase
{
    public void testTheTestProperties()
    {
        System.out.println( "${test.alpha}: " + System.getProperty( "test.alpha" ) );
        System.out.println( "${user.name}: " + System.getProperty( "user.name" ) );
        System.out.println( "${user.dir}: " + System.getProperty( "user.dir" ) );
        System.out.println( "${project.test.dir}: " + System.getProperty( "project.test.dir" ) );
        System.out.println( "${test.dir}: " + System.getProperty( "test.dir" ) );
        System.out.println( "${test.delta}: " + System.getProperty( "test.delta" ) );
    }
}

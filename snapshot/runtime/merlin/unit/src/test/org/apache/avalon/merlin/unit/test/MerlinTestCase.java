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

package org.apache.avalon.merlin.unit.test;

import org.apache.avalon.merlin.unit.AbstractMerlinTestCase;

/**
 * Test case that usages the repository builder to deploy the 
 * Merlin default application factory.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: MerlinTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class MerlinTestCase extends AbstractMerlinTestCase
{
    //----------------------------------------------------------
    // constructor
    //----------------------------------------------------------

    /**
     * Constructor for MerlinTestCase.
     * @param name the name of the testcase
     */
    public MerlinTestCase( String name )
    {
        super( name );
    }

    //----------------------------------------------------------
    // testcase
    //----------------------------------------------------------

    public void testHelloAquisition() throws Exception
    {
        Object hello = super.resolve( "/tutorial/hello" );
        assertNotNull( "hello", hello );

        Object hello2 = super.resolve( "/tutorial/hello" );
        assertNotNull( "hello-2", hello2 );
    }
}

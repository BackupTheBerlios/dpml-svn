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

package org.apache.avalon.finder.impl.test;

import junit.framework.TestCase;

import org.apache.avalon.finder.Finder;
import org.apache.avalon.finder.impl.DefaultFinder;

/**
 * DefaultFinder testcase.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/04/08 08:35:15 $
 */
public class DefaultFinderTestCase extends TestCase
{
    //--------------------------------------------------------
    // constructors
    //--------------------------------------------------------

   /**
    * @param name the name of the test case
    */
    public DefaultFinderTestCase( String name )
    {
        super( name );
    }

    //--------------------------------------------------------
    // testcase
    //--------------------------------------------------------

    public void testFinderConstructor() throws Exception
    {
        try
        {
            Finder finder = new DefaultFinder( null, null );
        }
        catch( NullPointerException npe )
        {
            // good
        }
        catch( Throwable bad )
        {
            fail( "unexpected constructor error: " + bad.toString() );
        }
    }

}

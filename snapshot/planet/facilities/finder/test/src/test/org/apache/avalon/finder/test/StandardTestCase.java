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

package org.apache.avalon.finder.test;

import org.apache.avalon.merlin.unit.AbstractMerlinTestCase;

import org.apache.avalon.finder.Finder;
import org.apache.avalon.finder.FinderException;

/**
 * DefaultFinder testcase.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.1 $ $Date: 2004/04/04 15:00:56 $
 */
public class StandardTestCase extends AbstractMerlinTestCase
{
    //--------------------------------------------------------
    // constructors
    //--------------------------------------------------------

   /**
    * @param name the name of the test case
    */
    public StandardTestCase( String name )
    {
        super( name );
    }

    //--------------------------------------------------------
    // testcase
    //--------------------------------------------------------

    public void testWidgetResolution() throws Exception
    {
        Finder finder = (Finder) resolve( "/test/finder" );
        assertNotNull( "finder", finder );
        try
        {
            Widget widget = (Widget) finder.find( Widget.class );
            System.out.println( "located widget: " + widget );
        }
        catch( Throwable notOk )
        {
            fail( "Unexpected error: " + notOk );
        }

        try
        {
            Object object = finder.find( StandardTestCase.class );
        }
        catch( FinderException ok )
        {
            // ok
        }
        catch( Throwable e )
        {
            fail( "Unexpected error: " + e );
        }
    }
}

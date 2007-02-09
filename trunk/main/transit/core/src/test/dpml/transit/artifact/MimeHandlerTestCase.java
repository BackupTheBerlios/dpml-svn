/*
 * Copyright 2004 Niclas Hedhman.
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

package dpml.transit.artifact;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * MimeHandlerTestCase.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class MimeHandlerTestCase extends TestCase
{
    private static final String[][] TESTS = new String[][]
    {
        {"plugin", "text/x-dpml-plugin"},
        {"conf", "text/x-dpml-conf"},
        {"jar", "application/x-jar"},
        {"zip", "application/x-zip"},
        {"pdf", "application/pdf"},
        {"png", "image/png"},
        {"jpg", "image/jpg"},
        {"gif", "image/gif"},
        {"link", "application/x-dpml-link"},
        {"part", "application/x-dpml-part"}
    };

   /**
    * Return the test suite.
    * @return the test
    */
    public static Test suite()
    {
        TestSuite tests = new TestSuite();
        for( int i = 0; i<TESTS.length; i++ )
        {
            String lookup = TESTS[i][0];
            String expected = TESTS[i][1];
            String name = "testLookup";
            TestCase testcase = new MimeHandlerTestCase( name, lookup, expected );
            tests.addTest( testcase );
        }
        String name = "testSize";
        TestCase testcase = new MimeHandlerTestCase( name, null, null );
        tests.addTest( testcase );
        return tests;
    }

    private String m_lookup;
    private String m_expected;

    private MimeHandlerTestCase( String name, String lookup, String expected )
    {
        super( name );
        m_lookup = lookup;
        m_expected = expected;
    }

   /**
    * Test mime type lookup.
    * @exception Exception if an error occurs
    */
    public void testLookup()
        throws Exception
    {
        String mime = MimeTypeHandler.getMimeType( m_lookup );
        assertEquals( m_lookup, m_expected, mime );
    }

   /**
    * Test mime map size.
    */
    public void testSize()
    {
        assertEquals( "Map Size", TESTS.length, MimeTypeHandler.getMimeTypesSize() );
    }
}

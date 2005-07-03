/*
 * Copyright 2004 Niclas Hedhman.
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

package net.dpml.transit.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import net.dpml.transit.util.MimeTypeHandler;

public class MimeHandlerTestCase extends TestCase
{
    static private String[][] TESTS = new String[][]
    {
        { "block", "text/x-dpml-block" },
        { "plugin", "text/x-dpml-plugin" },
        { "conf", "text/x-dpml-conf" },
        { "jar", "application/x-jar" },
        { "zip", "application/x-zip" },
        { "pdf", "application/pdf" },
        { "png", "image/png" },
        { "jpg", "image/jpg" },
        { "gif", "image/gif" },
        { "link", "application/x-dpml-link" }
    };

    static public Test suite()
    {
        TestSuite tests = new TestSuite();

        for( int i = 0 ; i < TESTS.length ; i++ )
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

    private String m_Lookup;
    private String m_Expected;

    private MimeHandlerTestCase( String name, String lookup, String expected )
    {
        super( name );
        m_Lookup = lookup;
        m_Expected = expected;
    }

    public void testLookup()
        throws Exception
    {
        String mime = MimeTypeHandler.getMimeType( m_Lookup );
        assertEquals( m_Lookup, m_Expected, mime );
    }

    public void testSize()
    {
        assertEquals( "Map Size", TESTS.length, MimeTypeHandler.getMimeTypesSize() );
    }
}

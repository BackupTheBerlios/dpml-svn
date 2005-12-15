/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.state.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import net.dpml.state.*;
import net.dpml.state.impl.*;

/**
 * State testcase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultOperationTestCase extends AbstractEncodingTestCase
{
    private String m_name;
    private Operation m_operation;
    
    public void setUp() throws Exception
    {
        m_name = "test";
        m_operation = new DefaultOperation( m_name );
    }
    
    public void testName() throws Exception
    {
        assertEquals( "name", m_name, m_operation.getName() );
    }
    
    public void testNullName() throws Exception
    {
        try
        {
            new DefaultOperation( null );
            fail( "NO NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testEncoding() throws Exception
    {
        Operation operation = (Operation) executeEncodingTest( m_operation, "simple-operation-encoded.xml" );
        assertEquals( "original-equals-encoded", m_operation, operation );
    }
}




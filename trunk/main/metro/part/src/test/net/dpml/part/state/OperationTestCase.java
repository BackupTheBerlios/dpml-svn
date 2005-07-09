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

package net.dpml.part.state;

import java.net.URI;

import junit.framework.TestCase;

/**
 * Test creation of the default operation implentation.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: AbstractDescriptorTestCase.java 1556 2005-01-22 12:43:42Z niclas $
 */
public class OperationTestCase extends TestCase
{
    private URI m_uri;
    private Operation m_operation; 

   /**
    * Setup the Operation
    */
    public void setUp() throws Exception
    {
        m_uri = new URI( "method:null" );
        m_operation = new Operation( m_uri );
    }

    public void testURI() throws Exception
    {
        assertEquals( "uri", m_uri, m_operation.getHandlerURI() );
    }

    public void testConstructorWithNullURI() throws Exception
    {
        try
        {
            new Operation( null );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
}




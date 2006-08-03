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

package net.dpml.job.impl;

import junit.framework.TestCase;

/**
 * FIFO list validation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class FIFOTestCase extends TestCase
{
   /**
    * Test the FIFO inital size.
    * @exception Exception if an error occurs
    */
    public void testFIFO() throws Exception
    {
        FIFO fifo = new FIFO();
        assertEquals( "size", 0, fifo.size() );
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();
        fifo.put( a );
        fifo.put( b );
        fifo.put( c );
        assertEquals( "size", 3, fifo.size() );
        fifo.clear();
        assertEquals( "size", 0, fifo.size() );
    }
    
   /**
    * Test the FIFO semantics.
    * @exception Exception if an error occurs
    */
    public void testFIFOSemantics() throws Exception
    {
        FIFO fifo = new FIFO();
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();
        fifo.put( a );
        fifo.put( b );
        fifo.put( c );
        Object first = fifo.get();
        assertEquals( "first", a, first );
        assertEquals( "size", 2, fifo.size() );
        Object second = fifo.get();
        assertEquals( "second", b, second );
        assertEquals( "size", 1, fifo.size() );
        Object third = fifo.get();
        assertEquals( "third", c, third );
        assertEquals( "size", 0, fifo.size() );
    }
    
}

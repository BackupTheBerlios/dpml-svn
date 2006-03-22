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

import java.util.ArrayList;

/** 
 * First-in-first-out queue.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
*/
class FIFO
{
    private ArrayList m_queue;
    
   /**
    * Creation of a new FIFO queue.
    */
    FIFO()
    {
        m_queue = new ArrayList();
    }
    
   /**
    * Clear the queue.
    */
    void clear()
    {
        synchronized( this )
        {
            m_queue.clear();
        }
    }
    
   /**
    * Add an object to the queue.
    */
    void put( Object obj )
    {
        synchronized( this )
        {
            m_queue.add( obj );
            notifyAll();
        }
    }
    
   /**
    * Get the first object entered in the queue.
    * @return the first object
    * @exception InterruptedException if interrupted
    */
    Object get() throws InterruptedException
    {
        synchronized( this )
        {
            while( m_queue.size() == 0 )
            {
                wait( 100 );
            }
            return m_queue.remove( 0 );
        }
    }
    
   /**
    * Get the number of objects on the queue.
    * @return the number of objects
    */
    int size()
    {
        synchronized( this )
        {
            return m_queue.size();
        }
    }
}

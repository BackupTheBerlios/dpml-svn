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

package org.apache.avalon.composition.model.impl;

import java.util.ArrayList;

/** This class implements a simple thread-safe FirstIn-FirstOut queue.
 * 
 * It is not intended to be used outside this package,
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SimpleFIFO.java 30977 2004-07-30 08:57:54Z niclas $
*/
class SimpleFIFO
{
    private ArrayList m_Queue;
    
    SimpleFIFO()
    {
        m_Queue = new ArrayList();
    }
    
    void clear()
    {
        synchronized( this )
        {
            m_Queue.clear();
        }
    }
    
    void put( Object obj )
    {
        synchronized( this )
        {
            m_Queue.add( obj );
            notifyAll();
        }
    }
    
    Object get()
        throws InterruptedException
    {
        synchronized( this )
        {
            while( m_Queue.size() == 0 )
                wait(100);
            return m_Queue.remove(0);
        }
    }
    
    int size()
    {
        synchronized( this )
        {
            return m_Queue.size();
        }
    }
} 

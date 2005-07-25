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

package net.dpml.transit.monitor;
 
import java.util.ArrayList;

/**
 * A repository monitor router handles mutlicast distribution of monitor events to 
 * a set of subscribed monitors.
 */
public class AbstractMonitorRouter implements Router
{
    //--------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------

   /**
    * An empty set of monitors.
    */
    private static final Monitor[] EMPTY_MONITORS = new Monitor[0];

    //--------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------

   /**
    * List of attached monitors.
    */
    private ArrayList m_monitors;

    //--------------------------------------------------------------------
    // Router
    //--------------------------------------------------------------------

   /**
    * Add a monitor to the set of monitors managed by this router.
    * @param monitor the monitor to add
    */
    public void addMonitor( Monitor monitor )
    {
        synchronized( this )
        {
            ArrayList list;
            if( m_monitors == null )
            {
                list = new ArrayList();
            }
            else
            {
                list = (ArrayList) m_monitors.clone();
            }
            list.add( monitor );
            m_monitors = list;
        }
    }
    
   /**
    * Remove a monitor from the set of monitors managed by this router.
    * @param monitor the monitor to remove
    */
    public void removeMonitor( Monitor monitor )
    {
        synchronized( this )
        {
            if( m_monitors == null )
            {
                return;
            }
            ArrayList list = (ArrayList) m_monitors.clone();
            list.remove( monitor );
            if( list.size() == 0 )
            {
                m_monitors = null;
            }
            else
            {
                m_monitors = list;
            }
        }
    }

   /**
    * Return the list of monitors.
    * @return an array of connected monitors
    */
    Monitor[] getMonitors()
    {
        if( null == m_monitors ) 
        {
            return EMPTY_MONITORS;
        }
        else
        {
            return (Monitor[]) m_monitors.toArray( new Monitor[0] );
        }
    }
}
 

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

package net.dpml.test.state;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.dpml.logging.Logger;

/**
 * Component implementation that exposes an active management operation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ManagedComponent implements Service
{
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

   /**
    * The logging channel.
    */
    private final Logger m_logger;
    
    private int m_count = 0;
    
    private DefaultMonitor m_monitor;

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new <tt>ManagedComponent</tt>.
    * 
    * @param logger the assigned logging channel
    */
    public ManagedComponent( final Logger logger )
    {
        m_logger = logger;
        
        m_monitor = new DefaultMonitor();
    }
    
    //------------------------------------------------------------------
    // Service
    //------------------------------------------------------------------

   /**
    * Ping the object.
    */
    public void ping()
    {
        m_count++;
    }
    
    //------------------------------------------------------------------
    // operations
    //------------------------------------------------------------------
    
   /**
    * Return a monitor.
    * @return the monitor
    */
    public Monitor getMonitor()
    {
        return m_monitor;
    }
    
    //------------------------------------------------------------------
    // internal
    //------------------------------------------------------------------
    
   /**
    * Return the assigned logging channel.
    * @return the logging channel
    */
    private Logger getLogger()
    {
        return m_logger;
    }
    
   /**
    * Interface implemented by an internal monitor returned
    * by the getMonitor operation.
    */
    public interface Monitor extends Remote
    {
       /**
        * Monitor the number of times the component ping operation 
        * has been invoked.
        * @return the access count
        * @exception RemoteException if a remote I/O error occurs
        */
        int getAccessCount() throws RemoteException;
    }
    
   /**
    * The default monitor implementation.
    */
    class DefaultMonitor implements Monitor
    {
       /**
        * Return the access count.
        * @return the access count
        */
        public int getAccessCount()
        {
            return m_count;
        }
    }
}

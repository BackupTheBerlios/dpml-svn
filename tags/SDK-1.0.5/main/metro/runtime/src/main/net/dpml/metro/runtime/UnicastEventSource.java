/*
 * Copyright 2005-2006 Stephen J. McConnell.
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

package net.dpml.metro.runtime;

import java.rmi.RemoteException;

import net.dpml.component.Disposable;

import net.dpml.util.EventQueue;
import net.dpml.util.Logger;

/**
 * A abstract base class that established an event queue and handles event dispatch 
 * operations for listeners declared in a class extending this base class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class UnicastEventSource extends net.dpml.util.UnicastEventSource implements Disposable
{
    private boolean m_disposed = false;

   /**
    * Creation of a new <tt>UnicastEventSource</tt>.
    * @param queue the system event queue
    * @param logger the assigned logging channel
    * @exception RemoteException if a remote I/O exception occurs
    */
    protected UnicastEventSource( EventQueue queue, Logger logger ) throws RemoteException
    {
        super( queue, logger );
    }
    
    //--------------------------------------------------------------------------
    // Disposable
    //--------------------------------------------------------------------------
    
   /**
    * Retun the disposed state of this event source.
    * @return true if disposed
    */
    protected boolean isDisposed()
    {
        return m_disposed;
    }
   
   /**
    * Handle instance disposal.
    */
    public void dispose()
    {
        if( !m_disposed )
        {
            super.terminate();
            m_disposed = true;
        }
    }
    
    //--------------------------------------------------------------------------
    // internal
    //--------------------------------------------------------------------------
    
   /**
    * Return the logging channel assigned to the event source.
    * @return the logging channel
    */
    Logger getLogger()
    {
        return super.getLocalLogger();
    }
}

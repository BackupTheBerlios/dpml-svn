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

package net.dpml.transit;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.EventObject;
import java.util.EventListener;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;

import net.dpml.transit.monitor.LoggingAdapter;

import net.dpml.util.Logger;
import net.dpml.util.UnicastEventSource;
import net.dpml.util.EventQueue;

/**
 * A abstract base class that established an event queue and handles event dispatch 
 * operations for listeners declared in classes extending this base class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class DefaultModel extends UnicastEventSource implements Disposable
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private Map m_listeners = new WeakHashMap();

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new model.
    * @param name the name used to construct a logging channel
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultModel( EventQueue queue, String name ) 
      throws RemoteException
    {
        this( queue, getLoggerForCategory( name ) );
    }

   /**
    * Creation of a new model.
    * @param logger the assigned logging channel
    * @exception NullPointerException if the supplied logging channel is null
    * @exception RemoteException if a remote exception occurs
    */
    public DefaultModel( EventQueue queue, Logger logger ) 
      throws NullPointerException, RemoteException
    {
        super( queue, logger );
    }

    // ------------------------------------------------------------------------
    // DefaultModel
    // ------------------------------------------------------------------------

    protected Logger getLogger()
    {
        return super.getLocalLogger();
    }
    
   /**
    * Dispose of the model.
    */
    public synchronized void dispose()
    {
        EventListener[] listeners = getEventListeners();
        for( int i=0; i < listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            removeListener( listener );
        }
        super.terminate();
        getLogger().debug( "disposed" );
    }
    
    /**
     * Enqueue an event for delivery to registered
     * listeners unless there are no registered
     * listeners.
     *
     * @param event the event object to add to the queue
     * @param waitForCompletion if TRUE the implementation will apply
     *   the event to the event source event handler and return on 
     *   copmpletion of evetn delivery
     */
    protected void enqueueEvent( EventObject event, boolean waitForCompletion )
    {
        if( m_listeners.size() > 0 )
        {
            getEventQueue().enqueueEvent( event, waitForCompletion );
        }
    }
    
   /**
    * Return a logging channel for the supplied name.
    * @param name the name to use in construction of the logging channel
    * @return the logging channel
    */
    static Logger getLoggerForCategory( String name )
    {
        if( null == name )
        {
            return new LoggingAdapter( "" );
        }
        else
        {
            return new LoggingAdapter( name );
        }
    }
}

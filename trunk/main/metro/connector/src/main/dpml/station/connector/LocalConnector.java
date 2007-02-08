/*
 * Copyright 2007 Stephen J. McConnell.
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

package dpml.station.connector;

import net.dpml.appliance.Appliance;
import net.dpml.appliance.ApplianceConnector;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.TimeUnit;

/**
 * Utility class used to handler the establishment of a connection between 
 * a spawned sub-process and a parent process.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class LocalConnector extends UnicastRemoteObject implements ApplianceConnector
{
    private final Object m_lock = new Object();
    private Appliance m_appliance;
    private Monitor m_monitor;
    
    public LocalConnector() throws RemoteException
    {
        super();
    }
    
   /**
    * Connect an appliance to the station.
    * @param appliance the appliance
    * @exception RemoteException if a remote error occurs
    */
    public void connect( Appliance appliance ) throws RemoteException
    {
        synchronized( m_lock )
        {
            m_appliance = appliance;
            if( null != m_monitor )
            {
                m_monitor.interrupt();
            }
        }
    }
    
    public Appliance getAppliance( TimeUnit units, int timeout ) throws IOException
    {
        synchronized( m_lock )
        {
            if( null != m_appliance )
            {
                return m_appliance;
            }
            else
            {
                m_monitor = new Monitor( units, timeout );
                m_monitor.setName( "DPML Connection Monitor " 
                  + System.identityHashCode( this ) );
                m_monitor.start();
            }
        }
        
        try
        {
            m_monitor.join();
        }
        catch( InterruptedException e )
        {
        }
        
        synchronized( m_lock )
        {
            m_monitor = null;
            if( null != m_appliance )
            {
                return m_appliance;
            }
            else
            {
                throw new IOException( "Appliance connection timeout (" + timeout + ")." );
            }
        }
    }
    
    private class Monitor extends Thread
    {
        private int m_timeout;
        private TimeUnit m_units;
        
        public Monitor( TimeUnit units, int timeout )
        {
            m_timeout = timeout;
            m_units = units;
        }
        
        public void run()
        {
            try
            {
                m_units.sleep( m_timeout );
                //Thread.currentThread().sleep( m_timeout );
            }
            catch( InterruptedException e )
            {
            }
        }
    }
}

/*
 * Copyright 2006-2007 Stephen J. McConnell.
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

package dpml.appliance;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.InstanceAlreadyExistsException;

import net.dpml.appliance.Appliance;
import net.dpml.appliance.ApplianceListener;
import net.dpml.appliance.ApplianceEvent;
import net.dpml.appliance.ApplianceException;
import net.dpml.appliance.ApplianceManager;

import net.dpml.lang.Strategy;

import net.dpml.runtime.Component;
import net.dpml.runtime.ComponentEvent;
import net.dpml.runtime.ComponentListener;
import net.dpml.runtime.Provider;
import net.dpml.runtime.ProviderEvent;
import net.dpml.runtime.Status;

import net.dpml.state.State;
import net.dpml.state.StateEvent;
import net.dpml.state.StateListener;

import net.dpml.util.Logger;

/**
 * Remote adapter to a component.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class AbstractAppliance extends UnicastRemoteObject
{
    protected void setShutdownHook( final Appliance appliance )
    {
        Runtime.getRuntime().addShutdownHook(
          new Thread()
          {
              public void run()
              {
                  try
                  {
                      appliance.decommission();
                      UnicastRemoteObject.unexportObject( appliance, false );
                  }
                  catch( Throwable e )
                  {
                  }
              }
          }
        );
    }
    
    private final Logger m_logger;
    private final URI m_codebase;
    private final Set<ApplianceListener> m_listeners = new CopyOnWriteArraySet<ApplianceListener>();
    private final ExecutorService m_queue = Executors.newSingleThreadExecutor();

    private boolean m_commissioned = false;
    
    public AbstractAppliance( final Logger logger, final URI uri ) throws IOException
    {
        super();
        
        if( null == uri )
        {
            String spec = System.getProperty( "dpml.appliance.codebase.uri", null );
            if( null != spec )
            {
                m_codebase = URI.create( spec );
            }
            else
            {
                m_codebase = null;
            }
        }
        else
        {
            m_codebase = uri;
        }
        m_logger = logger;
    }
    
    public String getCodebaseURI()
    {
        return m_codebase.toASCIIString();
    }
    
   /**
    * Return an array of subsidiary appliance instances managed by this appliance.
    * @return an array of subsidiary appliance instances
    */
    public Appliance[] getChildren()
    {
        return new Appliance[0];
    }

   /**
    * Add a listener to the component.
    * @param listener the component listener
    */
    public void addApplianceListener( ApplianceListener listener )
    {
        m_listeners.add( listener );
    }
    
   /**
    * Remove a listener from the component.
    * @param listener the component listener
    */
    public void removeApplianceListener( ApplianceListener listener )
    {
        m_listeners.remove( listener );
    }
    
    public boolean isCommissioned()
    {
        synchronized( this )
        {
            return m_commissioned;
        }
    }
    
    public void commission() throws IOException
    {
        synchronized( this )
        {
            m_commissioned = true;
        }
    }
    
    public void decommission() throws RemoteException
    {
        decommission( 10, TimeUnit.SECONDS );
    }
    
    protected void decommission( long timeout, TimeUnit units ) throws RemoteException
    {
        synchronized( this )
        {
            if( !m_commissioned )
            {
                return;
            }
            try
            {
                m_queue.shutdown();
                boolean ok = m_queue.awaitTermination( timeout, units );
                if( !ok )
                {
                    final String message = 
                      "A decommissioning timout (some events from may not have been processed).";
                    getLogger().warn( message );
                }
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
            finally
            {
                m_commissioned = false;
            }
        }
    }
    
    protected Logger getLogger()
    {
        return m_logger;
    }
    
    protected void processEvent( ApplianceEvent event )
    {
        Logger logger= getLogger();
        for( ApplianceListener listener : m_listeners )
        {
            m_queue.execute( new ApplianceEventDistatcher( logger, listener, event ) );
        }
    }
    
    private static class ApplianceEventDistatcher implements Runnable
    {
        private Logger m_logger;
        private ApplianceListener m_listener;
        private ApplianceEvent m_event;
        
        ApplianceEventDistatcher( Logger logger, ApplianceListener listener, ApplianceEvent event )
        {
            m_logger = logger;
            m_listener = listener;
            m_event = event;
        }
        
        public void run()
        {
            try
            {
                m_listener.applianceChanged( m_event );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Event distatch error.";
                m_logger.error( error, e );
            }
        }
    }
}


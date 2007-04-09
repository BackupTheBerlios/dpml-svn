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

import dpml.state.NullState;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;
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
public class StandardAppliance extends AbstractAppliance implements Appliance, ApplianceManager
{
    private final Component m_component;
    private final ComponentListener m_listener; 
    
    private Provider m_provider;
    private Object m_instance;
    
    public StandardAppliance( 
      final Logger logger, final Component component ) throws IOException
    {
        super( logger, null );
        
        m_component = component;
        m_listener = new InternalComponentListener( this );
        m_component.addComponentListener( m_listener );
        setShutdownHook( this );
    }
    
   /**
    * Return the current state of the instance.
    * @return the current state
    */
    public State getState() throws RemoteException
    {
        if( null != m_provider )
        {
            return m_provider.getState();
        }
        else
        {
            return new NullState();
        }
    }
    
    public String getName() throws RemoteException
    {
        return m_component.getName();
    }
    
   /**
    * Return a value assignable to the supplied remote type or null if the type
    * cannot be resolved from this strategy.
    * @param c the target class
    * @return an instance of the class or null
    * @exception IOException if an IO error occurs
    */
    public <T>T getContentForClass( Class<T> c ) throws IOException
    {
        if( null == m_instance )
        {
            throw new IllegalStateException( "Appliance has not been commissioned." );
        }
        else
        {
            if( c.isAssignableFrom( m_instance.getClass() ) )
            {
                return c.cast( m_instance );
            }
            else
            {
                return null;
            }
        }
    }


    public void commission() throws IOException
    {
        synchronized( m_component )
        {
            super.commission();
            if( getLogger().isInfoEnabled() )
            {
                getLogger().info( "commissioning " + m_component.getName() );
            }
            m_provider = m_component.getProvider();
            m_instance = m_provider.getInstance( Object.class );
        }
    }
    
    protected void decommission( long timeout, TimeUnit units ) throws RemoteException
    {
        synchronized( m_component )
        {
            if( !isCommissioned() )
            {
                return;
            }
            if( getLogger().isInfoEnabled() )
            {
                getLogger().info( "decommissioning " + m_component.getName() );
            }
            if( null != m_provider )
            {
                m_component.release( m_provider );
            }
            m_provider = null;
            m_instance = null;
            m_component.terminate();
            m_component.removeComponentListener( m_listener );
            super.decommission( timeout, units );
        }
    }
    
    /*
    public void stateChanged( final StateEvent event ) throws RemoteException
    {
        State from = event.getFromState();
        State to = event.getToState();
        if( getLogger().isDebugEnabled() )
        {
            String name = m_component.getName();
            getLogger().debug( 
              "transitioning [" 
              + name 
              + "] from [" 
              + from.getName() 
              + "] to [" 
              + to.getName() 
              + "]" );
        }
    }
    */

    private class InternalComponentListener implements ComponentListener
    {
        private Appliance m_appliance;
        
        InternalComponentListener( Appliance appliance )
        {
            m_appliance = appliance;
        }
        
       /**
        * Event within the component.
        *
        * @param event the component event
        */
        public void componentChanged( final ComponentEvent event )
        {
            if( event instanceof ProviderEvent )
            {
                ProviderEvent e = (ProviderEvent) event;
                Status status = e.getStatus();
                ApplianceEvent ae = new ApplianceEvent( m_appliance, status );
                processEvent( ae );
            }
        }
    }
}


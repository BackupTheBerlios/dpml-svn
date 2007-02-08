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

package net.dpml.station;

import dpml.station.info.PlanDescriptor;
import dpml.station.info.EntryDescriptor;

import dpml.appliance.ApplianceHelper;
import dpml.appliance.AbstractAppliance;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

import net.dpml.appliance.Appliance;
import net.dpml.appliance.ApplianceException;
import net.dpml.appliance.ApplianceListener;
import net.dpml.appliance.ApplianceEvent;
import net.dpml.appliance.ApplianceManager;

import net.dpml.runtime.Status;

import net.dpml.transit.Artifact;
import net.dpml.util.Logger;

/**
 * Appliance implementation that aggregates a collection of ordered appliance instances.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class CompositeAppliance extends AbstractAppliance implements Appliance, ApplianceManager
{
    private PlanDescriptor m_descriptor;
    private ArrayList<Appliance> m_list = new ArrayList<Appliance>();
    
    CompositeAppliance( final Logger logger, final PlanDescriptor descriptor ) throws IOException
    {
        this( logger, null, descriptor );
    }
    
    CompositeAppliance( final Logger logger, final String partition, final PlanDescriptor descriptor ) throws IOException
    {
        super( logger, descriptor.getCodebaseURI() );
        
        m_descriptor = descriptor;
        
        for( EntryDescriptor entry : m_descriptor.getEntryDescriptors() )
        {
            URI codebase = descriptor.getCodebaseURI();
            String name = getQualifiedName( partition, entry.getKey() );
            logger.info( "loading " + name );
            Appliance appliance = getApplianceForType( partition, entry );
            if( null == appliance )
            {
                final String error =
                  "Failed to resolve an appliance for the uri [ " 
                  + codebase 
                  + "]."
                throw new ApplianceException( error );
            }
            else
            {
                m_list.add( appliance );
                ApplianceContentHandler.register( name, appliance );
            }
        }
        
        ApplianceEvent ae = new ApplianceEvent( this, Status.CREATION );
        processEvent( ae );
    }
    
   /**
    * Returns the plan URI as a string.
    * @return the codebase uri
    */
    public String getCodebaseURI()
    {
        return m_descriptor.getCodebaseURI().toASCIIString();
    }
    
    private String getQualifiedName( String partition, String key )
    {
        if( null == partition )
        {
            return key;
        }
        else
        {
            return partition + "." + key;
        }
    }
    
    private Appliance getApplianceForType( String partition, EntryDescriptor entry ) throws IOException
    {
        String key = entry.getKey();
        String address = getQualifiedName( partition, key );
        URI uri = entry.getURI();
        return ApplianceHelper.newAppliance( address, uri );
    }
    
    public void commission() throws IOException
    {
        getLogger().info( "commissioning plan" );
        synchronized( this )
        {
            ApplianceEvent ae = new ApplianceEvent( this, Status.INCARNATION );
            processEvent( ae );
            super.commission();
            for( Appliance appliance : m_list )
            {
                appliance.commission();
            }
        }
        getLogger().info( "plan commissioning complete" );
    }
    
    protected void decommission( long timeout, TimeUnit units ) throws RemoteException
    {
        getLogger().info( "decommissioning plan" );
        synchronized( this )
        {
            ApplianceEvent ee = new ApplianceEvent( this, Status.ETHERIALIZATION );
            processEvent( ee );
            Appliance[] appliances = m_list.toArray( new Appliance[0] );
            for( int i = appliances.length; i>0; i-- )
            {
                Appliance appliance = appliances[i-1];
                appliance.decommission();
            }
            ApplianceEvent te = new ApplianceEvent( this, Status.TERMINATION );
            processEvent( te );
            super.decommission( timeout, units );
        }
        getLogger().info( "plan decommissioning complete" );
    }
    
   /**
    * Return a value assignable to the supplied remote type or null if the type
    * cannot be resolved from the underlying component.
    * @param type the service type
    * @return an instance of the type or null
    * @exception IOException if an IO error occurs
    */
    public <T>T getContentForClass( Class<T> type ) throws IOException
    {
        return null;
    }
}


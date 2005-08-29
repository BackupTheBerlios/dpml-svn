/*
 * Copyright 2005 Stephen McConnell
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

package net.dpml.profile.model;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Date;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.transit.model.Logger;
import net.dpml.transit.store.CodeBaseStorage;
import net.dpml.transit.model.CodeBaseModel;
import net.dpml.transit.model.DefaultContentModel;
import net.dpml.transit.model.DisposalListener;
import net.dpml.transit.model.DisposalEvent;
import net.dpml.transit.model.CodeBaseListener;
import net.dpml.transit.model.CodeBaseEvent;
import net.dpml.transit.model.Parameter;

import net.dpml.profile.ApplicationProfile;

/**
 * A DefaultApplicationProfile maintains information about the configuration
 * of an application profile.
 */
public class ApplicationModel extends DefaultContentModel implements ApplicationProfile
{
    private final ApplicationStorage m_store;

    private Properties m_properties;
    private boolean m_enabled;
    private boolean m_server;

    public ApplicationModel( 
      Logger logger, String id, String title, 
      Properties properties, boolean server, URI uri, 
      boolean enabled, Parameter[] params ) 
      throws RemoteException
    {
        super( logger, uri, params, id, title );

        m_store = null;
        m_properties = properties;
        m_enabled = enabled;
        m_server = server;
    }

    public ApplicationModel( Logger logger, ApplicationStorage store )
      throws RemoteException
    {
        super( logger, store );

        m_store = store;
        m_enabled = store.getEnabled();
        m_server = store.isaServer();
        m_properties = store.getSystemProperties();
    }

    //----------------------------------------------------------------------
    // impl
    //----------------------------------------------------------------------

    public String getID() throws RemoteException
    {
        return super.getContentType();
    }

    public Properties getSystemProperties() throws RemoteException
    {
        return m_properties;
    }

    public void setSystemProperties( Properties properties ) throws RemoteException
    {
        m_properties = properties;
    }

    public boolean isEnabled() throws RemoteException
    {
        return m_enabled;
    }

    public void setEnabled( boolean value ) throws RemoteException
    {
        m_enabled = value;
    }

    public boolean isaServer() throws RemoteException
    {
        return m_server;
    }

    public void setServerMode( boolean policy ) throws RemoteException
    {
        m_server = policy;
    }
}



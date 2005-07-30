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

package net.dpml.profile.impl;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Date;

import net.dpml.transit.model.Logger;
import net.dpml.transit.model.Connection;

import net.dpml.profile.ApplicationProfile;
import net.dpml.profile.store.ApplicationStorage;

/**
 * A DefaultApplicationProfile maintains information about the configuration
 * of an application profile.
 */
public class DefaultApplicationProfile extends DefaultProfile implements ApplicationProfile
{
    private final ApplicationStorage m_store;

    private boolean m_enabled;
    private Connection m_connection;
    private boolean m_command;
    private Properties m_params;

    public DefaultApplicationProfile( 
      Logger logger, String id, String title, 
      Properties properties, boolean command, Connection connection, URI uri, 
      boolean enabled, Properties params ) 
      throws RemoteException
    {
        super( logger, id, title, properties, uri );

        m_store = null;
        m_enabled = enabled;
        m_connection = connection;
        m_command = command;

        if( null == params )
        {
            m_params = new Properties();
        }
        else
        {
            m_params = params;
        }
    }

    public DefaultApplicationProfile( Logger logger, ApplicationStorage store )
      throws RemoteException
    {
        super( logger, store );

        m_store = store;
        m_enabled = store.getEnabled();
        m_connection = store.getConnection();
        m_command = store.getCommandPolicy();
        m_params = store.getProperties();
    }

    //----------------------------------------------------------------------
    // impl
    //----------------------------------------------------------------------

    public boolean isEnabled() throws RemoteException
    {
        return m_enabled;
    }

    public void setEnabled( boolean value ) throws RemoteException
    {
        m_enabled = value;
    }

    public boolean getCommandPolicy() throws RemoteException
    {
        return m_command;
    }

    public void setCommandPolicy( boolean policy ) throws RemoteException
    {
        m_command = policy;
    }

    public Connection getConnection() throws RemoteException
    {
        return m_connection;
    }

    public void setConnection( Connection connection )
    {
        m_connection = connection;
    }

    public Properties getProperties() throws RemoteException
    {
        return m_params;
    }

    public void setProperties( Properties args ) throws RemoteException
    {
        m_params = args;
    }
}



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
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.transit.model.Logger;
import net.dpml.transit.model.Disposable;

import net.dpml.profile.Profile;
import net.dpml.profile.store.ProfileStorage;

/**
 * A ProfileModel maintains information about the configuration
 * of an application profile.
 */
public class DefaultProfile extends DisposableCodeBaseModel implements Profile
{
    private final ProfileStorage m_home;
    private final String m_id;

    private String m_title;
    private Properties m_properties;

    public DefaultProfile( 
      Logger logger, String id, String title, Properties properties, URI uri ) throws RemoteException
    {
        super( logger, uri );

        m_id = id;
        m_title = title;
        m_properties = properties;
        m_home = null;
    }

    public DefaultProfile( Logger logger, ProfileStorage store ) throws RemoteException
    {
        super( logger, store );

        m_home = store;
        m_id = store.getID();
        m_title = store.getTitle();
        m_properties = store.getSystemProperties();
    }

    public String getID() throws RemoteException
    {
        return m_id;
    }

    public String getTitle() throws RemoteException
    {
        return m_title;
    }

    public void setTitle( String title ) throws RemoteException
    {
        m_title = title;
    }

    public Properties getSystemProperties() throws RemoteException
    {
        return m_properties;
    }

    public void setSystemProperties( Properties properties ) throws RemoteException
    {
        m_properties = properties;
    }

    //----------------------------------------------------------------------
    // Disposable
    //----------------------------------------------------------------------

    public void dispose() throws RemoteException
    {
        super.dispose();
        if( null != m_home )
        {
            m_home.remove();
        }
    }
}

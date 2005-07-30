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
import java.util.Date;
import java.util.Properties;
import java.rmi.activation.ActivationDesc;
import java.rmi.activation.ActivationGroupID;
import java.rmi.activation.ActivationException;

import net.dpml.transit.model.Logger;

import net.dpml.profile.ActivationProfile;
import net.dpml.profile.store.ActivationStorage;

/**
 * The DefaultActivationProfile profile class is the default implementation 
 * of the activation profile model.
 */
public class DefaultActivationProfile extends DefaultProfile implements ActivationProfile
{
    private final ActivationStorage m_home;

    private boolean m_restart;
    private String m_classname;

    public DefaultActivationProfile( Logger logger, ActivationStorage home )
      throws RemoteException
    {
        super( logger, home );

        m_home = home;
        m_restart = home.getRestartPolicy();
        m_classname = home.getClassname();
    }

    public String getClassname() throws RemoteException
    {
        return m_classname;
    }

    public void setClassname( String classname ) throws RemoteException
    {
        m_classname = classname;
    }

    public boolean getRestartPolicy() throws RemoteException
    {
        return m_restart;
    }

    public void setResartPolicy( boolean policy ) throws RemoteException
    {
        m_restart = policy;
    }
}

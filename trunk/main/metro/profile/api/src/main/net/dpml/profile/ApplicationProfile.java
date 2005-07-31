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

package net.dpml.profile;

import java.net.URI;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Date;

import net.dpml.transit.model.Connection;

/**
 * The ApplicationProfile interface describes a model of an application deployment
 * plan.
 */
public interface ApplicationProfile extends Profile
{
    boolean isEnabled() throws RemoteException;

    void setEnabled( boolean value ) throws RemoteException;

    boolean getCommandPolicy() throws RemoteException;

    void setCommandPolicy( boolean policy ) throws RemoteException;

    Connection getConnection() throws RemoteException;

    void setConnection( Connection connection ) throws RemoteException;

    Properties getProperties() throws RemoteException;

    void setProperties( Properties args ) throws RemoteException;

}



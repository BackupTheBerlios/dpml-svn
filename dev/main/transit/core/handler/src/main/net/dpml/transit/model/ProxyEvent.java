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

package net.dpml.transit.model;

import java.net.URL;
import java.util.EventObject;
import java.net.PasswordAuthentication; 

import net.dpml.transit.network.RequestIdentifier;

/**
 * An event pertaining to the Transit proxy configuration.
 */
public class ProxyEvent extends EventObject 
{
    private final RequestIdentifier m_identifier;
    private final PasswordAuthentication m_authentication;
    private final String[] m_excludes;

    public ProxyEvent( 
      ProxyModel model, RequestIdentifier identifier, 
      PasswordAuthentication auth, String[] excludes )
    {
        super( model );

        m_identifier = identifier;
        m_authentication = auth;
        m_excludes = excludes;
    }

    public ProxyModel getProxyModel()
    {
        return (ProxyModel) super.getSource();
    }

    public RequestIdentifier getRequestIdentifier()
    {
        return m_identifier;
    }

    public PasswordAuthentication getPasswordAuthentication()
    {
        return m_authentication;
    }

    public String[] getExcludes()
    {
        return m_excludes;
    }
}

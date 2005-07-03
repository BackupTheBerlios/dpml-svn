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
 * An event pertaining to a modification to a host model base url, 
 * index, request identifier or connection credentials.
 */
public class HostChangeEvent extends HostEvent 
{
    private final URL m_base;
    private final URL m_index;
    private final RequestIdentifier m_identifier;
    private final PasswordAuthentication m_authentication;
    private final boolean m_enabled;
    private final boolean m_trusted;

    public HostChangeEvent( 
      HostModel host, URL base, URL index, 
      RequestIdentifier identifier, PasswordAuthentication auth, 
      boolean enabled, boolean trusted )
    {
        super( host );
        m_base = base;
        m_index = index;
        m_identifier = identifier;
        m_authentication = auth;
        m_enabled = enabled;
        m_trusted = trusted;
    }
    
    public URL getBaseURL()
    {
        return m_base;
    }

    public URL getIndexURL()
    {
        return m_index;
    }

    public RequestIdentifier getRequestIdentifier()
    {
        return m_identifier;
    }

    public PasswordAuthentication getPasswordAuthentication()
    {
        return m_authentication;
    }

    public boolean getEnabled()
    {
        return m_enabled;
    }

    public boolean getTrusted()
    {
        return m_trusted;
    }
}

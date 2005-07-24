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

   /**
    * Creation of a new host change event.
    * @param host the host model that was changed
    * @param base the host base url
    * @param index the host index url
    * @param identifier the host request identifier
    * @param auth the host authentication credentials
    * @param enabled the host enabled state
    * @param trusted the host trusted status
    */
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
    
   /**
    * Return the host base URL.
    * @return the base url
    */
    public URL getBaseURL()
    {
        return m_base;
    }

   /**
    * Return the host index URL.
    * @return the index url
    */
    public URL getIndexURL()
    {
        return m_index;
    }

   /**
    * Return the host request identifier.
    * @return the request identifier
    */
    public RequestIdentifier getRequestIdentifier()
    {
        return m_identifier;
    }

   /**
    * Return the host password authentication credentials.
    * @return the authentication credentials
    */
    public PasswordAuthentication getPasswordAuthentication()
    {
        return m_authentication;
    }

   /**
    * Return the host enabled status.
    * @return the enabled status
    */
    public boolean getEnabled()
    {
        return m_enabled;
    }

   /**
    * Return the host trusted status.
    * @return the trusted status
    */
    public boolean getTrusted()
    {
        return m_trusted;
    }
}

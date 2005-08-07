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

import java.util.EventObject;
import java.net.PasswordAuthentication; 

/**
 * An event pertaining to the Transit proxy configuration.
 */
public class ProxyEvent extends EventObject 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final RequestIdentifier m_identifier;
    private final PasswordAuthentication m_authentication;
    private final String[] m_excludes;

   /**
    * Creation of a new ProxyEvent.
    * @param model the proxy model
    * @param identifier the request identifier
    * @param auth the password authentifcation credentials
    * @param excludes the set of proxy host excludes
    */
    public ProxyEvent( 
      ProxyModel model, RequestIdentifier identifier, 
      PasswordAuthentication auth, String[] excludes )
    {
        super( model );

        m_identifier = identifier;
        m_authentication = auth;
        m_excludes = excludes;
    }

   /**
    * Return the proxy model that is the subject of change.
    * @return the proxy model
    */
    public ProxyModel getProxyModel()
    {
        return (ProxyModel) super.getSource();
    }

   /**
    * Return the proxy model request identifier.
    * @return the proxy model request identifier
    */
    public RequestIdentifier getRequestIdentifier()
    {
        return m_identifier;
    }

   /**
    * Return the proxy model password authentication.
    * @return the proxy model password authentication
    */
    public PasswordAuthentication getPasswordAuthentication()
    {
        return m_authentication;
    }

   /**
    * Return the proxy model excludes.
    * @return the proxy model excludes
    */
    public String[] getExcludes()
    {
        return m_excludes;
    }
}

/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.repository.main;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Default authenticator that provides support for username password
 * based authentication in conjunction with the repository proxy settings.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultAuthenticator.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class DefaultAuthenticator extends Authenticator
{
    /**
     * Proxy username.
     */
     private String m_username;

    /**
     * Proxy password.
     */
     private char[] m_password;

    /**
     * Creation of a new simple authenticator.
     * @param username the username
     * @param password a passsword
     * @exception NullPointerException if the supplied username or password is null
     */
     public DefaultAuthenticator( String username, String password ) throws NullPointerException 
     {
         if( username == null ) throw new NullPointerException( "username" );
         if( password == null ) throw new NullPointerException( "password" );
         m_username = username;
         m_password = password.toCharArray();
     }

    /**
     * Returns the password authenticator.
     * @return the password authenticator
     */
     protected PasswordAuthentication getPasswordAuthentication()
     {
          return new PasswordAuthentication( m_username, m_password );
     }
}

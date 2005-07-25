/*
 * Copyright 2004-2005 Stephen McConnell, DPML
 * Copyright 2004 Niclas Hedhman, DPML
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

package net.dpml.transit;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import net.dpml.transit.NullArgumentException;

/**
 * Default authenticator that provides support for username password
 * based authentication in conjunction with the repository proxy settings.
 *
 * @version $Id: TransitAuthenticatorImpl.java 2854 2005-06-14 05:44:50Z mcconnell@dpml.net $
 */
final class TransitAuthenticatorImpl
    implements TransitAuthenticator
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    /**
     * Proxy username.
     */
     private final PasswordAuthentication m_authentication;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    /**
     * Creation of a new simple authenticator.
     * @param authenticator a password authenticator
     * @exception NullArgumentException if either the authenticator argument is null
     */
    public TransitAuthenticatorImpl( PasswordAuthentication authenticator )
        throws NullArgumentException
    {
        if( authenticator == null )
        {
            m_authentication = new PasswordAuthentication( "", "".toCharArray() );
        }
        else
        {
            m_authentication = authenticator;
        }
    }

    /**
     * Creation of a new simple authenticator.
     * @param username the username
     * @param password the password
     * @exception NullArgumentException if either the username or password argument is null
     */
    public TransitAuthenticatorImpl( String username, String password )
        throws NullArgumentException
    {
        if( username == null )
        {
            throw new NullArgumentException( "username" );
        }
        if( password == null )
        {
            throw new NullArgumentException( "password" );
        }
        m_authentication = new PasswordAuthentication( username, password.toCharArray() );
    }

    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------

    /**
     * Returns the password authenticator.
     * @param authenticator an default authenticator
     * @return the password authenticator
     */
    public PasswordAuthentication resolvePasswordAuthentication( Authenticator authenticator )
    {
        return m_authentication;
    }
}

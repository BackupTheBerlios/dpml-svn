/*
 * Copyright 2004 Niclas Hedhman, DPML
 * Copyright 2005-2007 Stephen J. McConnell.
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

package dpml.transit;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Internal class used to handle delegated authentication.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
final class DelegatingAuthenticator extends Authenticator
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------

   /**
    * The singleton delegating authenticator instance.
    */
    private static DelegatingAuthenticator m_INSTANCE;

    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

   /**
    * The set of registered authenticators keyed by id.
    */
    private Map<RequestIdentifier, TransitAuthenticator> m_authenticators;

   /**
    * Return the singleton authenticator.
    * @return the authenticator
    */
    public static DelegatingAuthenticator getInstance()
    {
        synchronized( DelegatingAuthenticator.class )
        {
            if( m_INSTANCE == null )
            {
                m_INSTANCE = new DelegatingAuthenticator();
                Authenticator.setDefault( m_INSTANCE );
            }
            return m_INSTANCE;
        }
    }

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new delegating authenticator.
    */
    private DelegatingAuthenticator()
    {
        m_authenticators = new HashMap<RequestIdentifier, TransitAuthenticator>();
    }

    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------

   /**
    * Add an authenticator to the set of authenticators managed by the
    * singleton delegating authenticator.
    * @param authenticator the authenticator to add
    * @param id the request identifier
    */
    public void addTransitAuthenticator( TransitAuthenticator authenticator, RequestIdentifier id )
    {
        synchronized( m_authenticators )
        {
            m_authenticators.put( id, authenticator );
        }
    }

   /**
    * Remove an authenticator from the set of authenticators managed by the
    * singleton delegating authenticator.
    * @param authenticator the authenticator to remove
    */
    public void removeTransitAuthenticator( TransitAuthenticator authenticator )
    {
        synchronized( m_authenticators )
        {
            Iterator list = m_authenticators.values().iterator();
            while( list.hasNext() )
            {
                Authenticator item = (Authenticator) list.next();
                if( item.equals( authenticator ) )
                {
                    list.remove();
                }
            }
        }
    }

   /**
    * Returns the password authenticator.
    * @return the password authenticator
    */
    protected PasswordAuthentication getPasswordAuthentication()
    {
        String host = getRequestingHost();
        String protocol = getRequestingProtocol();
        String prompt = getRequestingPrompt();
        String scheme = getRequestingScheme();
        int port = getRequestingPort();
        if( host == null )
        {
            host = getRequestingSite().getHostName();
            if( host == null || "".equals( host ) )
            {
                host = getRequestingSite().getHostAddress();
            }
        }
        
        RequestIdentifier id  = new RequestIdentifier( host, port, protocol, scheme, prompt );
        
        //try
        //{
        //    PrintWriter log = Transit.getInstance().getLogWriter();
        //    log.println( "Authentication Required: " + id );
        //    log.println( "Authenticators Available: " + m_authenticators );
        //} 
        //catch( Exception e )
        //{
        //    boolean ignorable = true;
        //} 

        synchronized( m_authenticators )
        {
            TransitAuthenticator auth = (TransitAuthenticator) m_authenticators.get( id );
            if( auth == null )
            {
                return null;
            }
            return auth.resolvePasswordAuthentication( this );
        }
    }
}

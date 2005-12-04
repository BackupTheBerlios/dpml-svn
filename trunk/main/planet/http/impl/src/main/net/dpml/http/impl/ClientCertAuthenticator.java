/*
 * Copyright 2004 Niclas Hedman.
 * Copyright 2005 Stephen McConnell.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dpml.http.impl;

/** 
 * Wrapper for the Jetty ClientCertAuthenticator
 */
public class ClientCertAuthenticator extends org.mortbay.http.ClientCertAuthenticator
{
   /**
    * Component configuration.
    */
    public interface Context 
    {
       /**
        * Return the maximum handshake value.
        * @param value the default handleshake max value
        * @return the max handshake value
        */
        int getMaxHandshake( int value );
    }

   /**
    * Creation of a new client certigficate authenticactor.
    * @param context the component context
    */
    public ClientCertAuthenticator( Context context )
    {
        int maxHandshakeSec = context.getMaxHandshake( -1 );
        if( maxHandshakeSec > -1 )
        {
            setMaxHandShakeSeconds( maxHandshakeSec );
        }
    }
}
 

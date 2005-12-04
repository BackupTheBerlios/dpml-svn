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

import java.io.IOException;

/** 
 * Wrapper for the Jetty HashUserRealm.
 */
public class HashUserRealm extends org.mortbay.http.HashUserRealm
{
   /**
    * Deployment context.
    */
    public interface Context
    {
       /**
        * Return the realm name.
        * @return the realm name
        */
        String getRealmName();
        
       /**
        * Return the realm uri.
        * @return the realm uri path
        */
        String getRealmURI();
    }

   /**
    * Create a new user realm.
    * @param context the deplioyment context
    * @exception IOException if an I/O error occurs
    */
    public HashUserRealm( Context context ) throws IOException
    {
        String realmName = context.getRealmName();
        setName( realmName );

        String uri = context.getRealmURI();
        load( uri );
    }
}

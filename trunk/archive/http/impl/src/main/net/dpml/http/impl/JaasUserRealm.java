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
 * Wrapper for the Jetty JaasUserRealm.
 */
public class JaasUserRealm extends org.mortbay.jaas.JAASUserRealm
{
   /**
    * Component context.
    */
    public interface Context
    {
       /**
        * Get the realm name.
        * @return the name
        */
        String getRealmName();
        
       /**
        * Get the login module name.
        * @return the login module name
        */
        String setLoginModuleName();
    }

   /**
    * Creation of a new JaasUserRealm instance.
    * @param context the deployment context
    */
    public JaasUserRealm( Context context )
    {
        String realmName = context.getRealmName();
        setName( realmName );

        String filename = context.setLoginModuleName();
        setLoginModuleName( filename );
    }
}

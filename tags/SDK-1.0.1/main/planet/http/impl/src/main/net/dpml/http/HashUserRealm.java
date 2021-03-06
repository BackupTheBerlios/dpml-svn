/*
 * Copyright 2006 Stephen McConnell.
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
package net.dpml.http;

import java.net.URI;

/**
 * Hash user realm with enhanced keystore resolution semantics.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class HashUserRealm extends org.mortbay.jetty.security.HashUserRealm
{
   /**
    * HTTP Context handler context defintion.
    */
    public interface Context
    {
       /**
        * Get the user realm name.
        *
        * @return the realm name
        */
        String getName();
        
       /**
        * Return a uri of the real configuration properties file.
        *
        * @return the realm configuration uri
        */
        URI getURI();
    }

   /**
    * Creation of a new hash user realm.
    * @param context the deployment context
    * @exception Exception if an instantiation error occurs
    */
    public HashUserRealm( Context context ) throws Exception
    {
        String name = context.getName();
        super.setName( name );
        URI config = context.getURI();
        setConfig( config.toASCIIString() );
    }
}

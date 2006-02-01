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
package net.dpml.web.server;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import net.dpml.transit.util.PropertyResolver;

/**
 * Hash user realm.
 */
public class HashUserRealm extends org.mortbay.jetty.security.HashUserRealm
{
    /** Load realm users from properties file.
     * The property file maps usernames to password specs followed by
     * an optional comma separated list of role names.
     *
     * @param config Filename or url of user properties file.
     * @exception IOException is an IO error occurs
     */
    public void setConfig( String config ) throws IOException
    {
        String resolved = PropertyResolver.resolve( config );
        if( resolved.startsWith( "local:" ) )
        {
            try
            {
                URI uri = new URI( resolved );
                File file = (File) uri.toURL().getContent( new Class[]{File.class} );
                super.setConfig( file.getCanonicalPath() );
            }
            catch( Exception e )
            {
                final String error = 
                  "Invalid local resource specification: " + resolved;
                IOException ioe = new IOException( error );
                ioe.initCause( e );
                throw ioe;
            }
        }
        else
        {
            super.setConfig( resolved );
        }
    }
}

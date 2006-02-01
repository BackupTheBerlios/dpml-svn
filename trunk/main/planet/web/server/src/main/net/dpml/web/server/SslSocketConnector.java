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

import org.mortbay.log.Log;

/**
 * SSL socket connector with enhanced keystore resolution semantics.
 */
public class SslSocketConnector extends org.mortbay.jetty.security.SslSocketConnector
{
   /**
    * Set the location of the keystore.  The implementation supports
    * resolution of files using the Transit local resource protocol
    * together with system property symbolic resolution. Following argument
    * evalution the implementation delegates subsequent actions to the 
    * Jetty implementation class.
    *
    * @param keystore the keystore value
    */
    public void setKeystore( String keystore )
    {
        String resolved = PropertyResolver.resolve( keystore );
        Log.info( "# resolved keystore argument: [" + resolved + "]" );
        if( resolved.startsWith( "local:" ) )
        {
            Log.info( "# argument is a local value" );
            try
            {
                URI uri = new URI( resolved );
                File file = (File) uri.toURL().getContent( new Class[]{ File.class } );
                if( !file.exists() )
                {
                    final String error = 
                      "Keystore file ["
                      + file
                      + "] does not exist.";
                    throw new IllegalArgumentException( error );
                }
                String path = file.getCanonicalPath();
                Log.info( "# validated keystore exists\npath: " + path );
                super.setKeystore( path );
            }
            catch( Exception e )
            {
                final String error = 
                  "Invalid local keystore specification: " + resolved;
                throw new RuntimeException( error, e );
            }
        }
        else
        {
            Log.info( "# keystore location: " + resolved );
            super.setKeystore( resolved );
        }
    }
}

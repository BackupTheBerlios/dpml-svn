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

package org.apache.avalon.repository ;

import java.io.IOException;
import java.net.URL;

/**
 * Repository URL protocol handler.  
 */
public class ArtifactURLConnection extends AbstractURLConnection
{
    /**
     * Creation of a new handler. 
     * @param url the url to establish a connection with
     * @param type the default type if no type specified
     * @exception NullPointerException if the supplied repository argument is null
     */
     ArtifactURLConnection( URL url ) 
       throws NullPointerException, IOException
     {
         super( url );
     }

    /**
     * Return the Artifact specified by this URL.
     * @return the artifact instance
     */
     public Object getContent() throws IOException
     {
         return getContent( "jar" );
     }

    /**
     * Return the Artifact specified by this URL.
     * @return the artifact instance
     */
     protected Object getContent( String defaultType ) throws IOException
     {
         try
         {
             final String path = getPath( url );
             final int i = path.lastIndexOf( "/" );
             final String group = path.substring( 0, i );
             final String name = path.substring( i+1 );
             final String version = getVersion( url );
             final String type = getType( getURL(), defaultType );
             return Artifact.createArtifact( group, name, version, type );
         }
         catch( Throwable e )
         {
             final String error = 
               "Unexpected exception while resolving url [" + getURL() + "].";
             throw new CascadingIOException( error );
         }
     }

     private final String getPath( URL url )
     {
         final String path = getURL().getPath();
         int n = path.indexOf( ":" );
         if( n > -1 )
         {
             return path.substring( n+1 );
         }
         else
         {
             return path;
         }
     }

     private String getType( URL url, String type )
     {
         final String path = getURL().getPath();
         int n = path.indexOf( ":" );
         if( n > -1 )
         {
             return path.substring( 0, n );
         }
         else
         {
             return getQueryField( url, "type", type );
         }
     }



}

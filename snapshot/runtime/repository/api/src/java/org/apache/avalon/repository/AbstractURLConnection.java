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
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.util.StringTokenizer;

import org.apache.avalon.repository.Artifact;

/**
 * Abstract artifact URL protocol handler.  
 * @since 3.3
 */
public abstract class AbstractURLConnection extends URLConnection
{
    /**
     * Creation of a new handler. 
     * @param url the url to establish a connection with
     */
     AbstractURLConnection( URL url ) 
       throws IOException
     {
         super( url );
 
         String path = url.getPath();
         int i = path.lastIndexOf( "/" );
         if( i<0 )
         {
             final String error = 
               "Artifact specification does not contain a [group]/[name] seperator.";
             throw new MalformedURLException( error );
         }
     }

    /**
     * Return the Artifact specified by this URL.
     * @param classes a set of classes (ignored)
     * @return the artifact instance
     * @see org.apache.avalon.repository.Artifact
     */
     public Object getContent( Class[] classes ) throws IOException
     {
         return getContent();
     }

    /**
     * Null implementation of the conect protocol.
     */
     public void connect()
     {
         // nothing to do
     }

    /**
     * Return the Artifact specified by this URL.
     * @param type the artifact type (e.g. "jar", "block", "xml", etc.)
     * @return the artifact instance
     * @see org.apache.avalon.repository.Artifact
     */
     protected Object getContent( String type ) throws IOException
     {
         try
         {
             final String path = getURL().getPath();
             final int i = path.lastIndexOf( "/" );
             final String group = path.substring( 0, i );
             final String name = path.substring( i+1 );
             final String version = getVersion( url );
             return Artifact.createArtifact( group, name, version, type );
         }
         catch( Throwable e )
         {
             final String error = 
               "Unexpected exception while resolving url [" + super.getURL() + "].";
             throw new CascadingIOException( error );
         }
     }

    /**
     * Utility method to return the version field with the url query.
     * @param url the url containing the query
     * @return the version value if declared else null
     */
     protected String getVersion( URL url )
     {
         if( null != url.getRef() ) return url.getRef();
         return getQueryField( url, "version", null );
     } 

     /**
     * Utility method to return the value of a field within the url query.
     * @param url the url containing the query
     * @param field the query field name
     * @param fallback the default value if not query parameter available
     * @return the value of the query field
     */
     protected String getQueryField( URL url, String field, String fallback )
     {
         String query = url.getQuery();
         if( null != query ) 
         {
             StringTokenizer tokenizer = new StringTokenizer( query, "&" );
             while( tokenizer.hasMoreElements() )
             {
                 String token = tokenizer.nextToken();
                 if( token.startsWith( field + "=" ) )
                 {
                     return token.substring( (field.length() + 1) );
                 }
             }
         }
         return fallback;
     }
}

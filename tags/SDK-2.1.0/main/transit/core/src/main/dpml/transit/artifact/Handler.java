/*
 * Copyright 2004-2007 Stephen J. McConnell.
 * Copyright 2004 Niclas Hedhman
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

package dpml.transit.artifact;

import dpml.transit.TransitContext;
import dpml.util.DefaultLogger;

import java.io.IOException;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.AccessController;
import java.security.PrivilegedAction;

import net.dpml.transit.Transit;
import net.dpml.transit.TransitError;
import net.dpml.transit.TransitRuntimeException;
import net.dpml.util.Logger;

/**
 * The artifact URL protocol handler.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Handler extends URLStreamHandler
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------

   /**
    * Default buffer size.
    */
    private static final int BUFFER_SIZE = 100;

    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------

    /**
     * Opens a connection to the specified URL.
     *
     * @param url A URL to open a connection to.
     * @return The established connection.
     * @throws IOException If a connection failure occurs.
     */
    protected URLConnection openConnection( final URL url )
      throws IOException
    {
        checkTransitInitialization( url );
        TransitContext context = TransitContext.getInstance();
        return new ArtifactURLConnection( url, context );
    }
    
    private void checkTransitInitialization( URL url )
    {
        try
        {
            Transit.getInstance();
        }
        catch( TransitError e )
        {
            final String error =
              "Internal error while resolving the Transit instance while handling the url ["
              + url 
              + "].";
            throw new TransitRuntimeException( error, e );
        }
    }

   /**
    * Return the external representation of the supplied url.
    * @param u the url
    * @return a string representing of the url as an artifact uri
    */
    protected String toExternalForm( URL u )
    {
        StringBuffer buf = new StringBuffer( BUFFER_SIZE );
        buf.append( u.getProtocol() );
        buf.append( ":" );
        String path = getRealPath( u );
        if( path != null )
        {
            int lastPos = path.length() - 1;
            if( path.charAt( lastPos ) == '/' )
            {
                buf.append( path.substring( 0, lastPos ) );
            }
            else
            {
                buf.append( path );
            }
        }

        String internal = getInternalPath( u );
        if( null != internal )
        {
            buf.append( internal );
        }
        
        String query = u.getQuery();
        if( query != null )
        {
            buf.append( '?' );
            buf.append( query );
        }

        String version = u.getUserInfo();
        if( ( version != null ) && !"".equals( version ) )
        {
            buf.append( '#' );
            buf.append( version );
        }
        String result = buf.toString();
        buf.setLength( 0 );
        return result;
    }

   /**
    * Return the pure artifact path without any internal address.
    * @param url the url to evaluate
    * @return the pure path
    */
    private String getRealPath( URL url )
    {
        String path = url.getPath();
        if( null == path )
        {
            return null;
        }
        int index = path.indexOf( "!" );
        if( index < 0 )
        {
            return path;
        }
        else
        {
            return path.substring( 0, index );
        }
    }

   /**
    * Return the value of an internal address associated with a path.
    * @param url the url to evaluate
    * @return the internal address of null if the url does not contain an internal address
    */
    private String getInternalPath( URL url )
    {
        String path = url.getPath();
        if( null == path )
        {
            return null;
        }
        int index = path.indexOf( "!" );
        if( index < 0 )
        {
            return null;
        }
        else
        {
            return path.substring( index );
        }
    }


   /**
    * Parse the supplied specification.
    * @param dest the destination url to populate
    * @param spec the supplied spec
    * @param start the starting position
    * @param limit the limit
    */
    protected void parseURL( final URL dest, String spec, int start, int limit )
    {
        try
        {
            final String protocol = dest.getProtocol();
            String specPath = spec.substring( start, limit );
            String path = dest.getPath();
            
            // get any query info from the supplied spec
            
            final String query = getQueryFragment( specPath );
            if( null != query )
            {
                int n = specPath.indexOf( "?" );
                if( n > -1 )
                {
                    specPath = specPath.substring( 0, n );
                }
            }
            
            // get the version using the url ref semantics
            
            final String version = getRefFragment( specPath );
            if( null != version )
            {
                int n = specPath.indexOf( "#" );
                if( n > -1 )
                {
                    specPath = specPath.substring( 0, n );
                }
            }
            
            // setup the path 
            
            if( path == null )
            {
                path = specPath;
                if( !path.endsWith( "/" ) && ( path.indexOf( "!" ) < 0 ) )
                {
                    path = path + "/";
                }
            }
            else
            {
                int lastPos = path.length() - 1;
                if( path.charAt( lastPos ) == '/' )
                {
                    path = path.substring( 0, lastPos );
                }
                if( specPath.charAt( 0 ) == '/' )
                {
                    path = path + "!" + specPath;
                }
                else
                {
                    path = path + "!/" + specPath;
                }
            }
            
            // establish the artifact type
            
            String type = dest.getUserInfo();
            if( type == null )
            {
                if( limit < spec.length() )
                {
                    type = spec.substring( limit + 1 );
                }
            }
            
            // map features to url fields
            
            final String user = type; // final String user = version;
            final String authority = null;
            final int port = -1;
            final String host = null;
            final String ref = version; // final String ref = null;
            final String finalPath = path;
            AccessController.doPrivileged( 
              new PrivilegedAction<Object>()
              {
                public Object run()
                {
                    setURL( dest, protocol, host, port, authority, user, finalPath, query, ref );
                    return null;
                }
              }
            );
        }
        catch( Throwable e )
        {
            final String message = 
              "Internal error while attempting to parse url: "
              + dest + ", " + spec + ", " + start + ", " + limit;
            Logger logger = new DefaultLogger( "dpml.transit.artifact" );
            logger.error( message, e );
        }
    }
    
    private String getQueryFragment( String path )
    {
        if( null == path )
        {
            return null;
        }
        else
        {
            int q = path.indexOf( "?" );
            if( q > -1 )
            {
                return path.substring( q+1 );
            }
            else
            {
                return null;
            }
        }
    }
    
    private String getRefFragment( String path )
    {
        if( null == path )
        {
            return null;
        }
        else
        {
            int n = path.indexOf( "#" );
            if( n > -1 )
            {
                return path.substring( n+1 );
            }
            else
            {
                return null;
            }
        }
    }
}

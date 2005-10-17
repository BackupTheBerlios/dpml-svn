/*
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

package net.dpml.transit.artifact;

import java.io.IOException;
import java.io.PrintWriter;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.AccessController;
import java.security.PrivilegedAction;

import net.dpml.transit.Transit;
import net.dpml.transit.TransitRuntimeException;
import net.dpml.transit.SecuredTransitContext;

/**
 * The artifact URL protocol handler.
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
    // state
    // ------------------------------------------------------------------------

   /**
    * The transit context.
    */
    private SecuredTransitContext   m_context;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new transit artifact protocol handler.
    */
    public Handler()
    {
        try
        {
            Transit.getInstance();
            m_context = SecuredTransitContext.getInstance();
        }
        catch( RuntimeException e )
        {
            e.printStackTrace();
            throw e;
        }
    }

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
        return new ArtifactURLConnection( url, m_context );
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
            // Niclas: Improve the parsing algorithm and have some more checks.
            final String protocol = dest.getProtocol();

            String specPath = spec.substring( start, limit );
            String path = dest.getPath();

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

            String version = dest.getUserInfo();
            if( version == null )
            {
                if( limit < spec.length() )
                {
                    version = spec.substring( limit + 1 );
                }
            }

            final String user = version;
            final String authority = null;
            final int port = -1;
            final String host = null;
            final String query = null;
            final String ref = null;
            final String finalPath = path;
            
            AccessController.doPrivileged( 
              new PrivilegedAction()
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
            try
            {
                PrintWriter log = Transit.getInstance().getLogWriter();
                String message = "Unable to parse the URL: "
                    + dest + ", " + spec + ", " + start + ", " + limit;
                log.println( message );
                log.println( "---------------------------------------------------" );
                e.printStackTrace( log );
            }
            catch( TransitRuntimeException f )
            {
                // Panic!!!! Should not happen.
                f.printStackTrace();
                e.printStackTrace();
            }
        }
    }
}

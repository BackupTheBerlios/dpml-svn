/*
 * Copyright 2005 Niclas Hedhman
 * Copyright 2005 Stephen J. McConnell.
 *
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

package net.dpml.transit.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedAction;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import net.dpml.transit.Transit;
import net.dpml.transit.Artifact;

/**
 * The local URL protocol connection implementation.
 */
public class RegistryURLConnection extends URLConnection
{
    private boolean m_connected = false;

    private Registry m_registry;

    /**
     * Creation of a new registry handler.
     * @param url the url to establish a connection with
     * @exception NullPointerException if the url argument is null
     */
    RegistryURLConnection( URL url )
        throws NullPointerException
    {
        super( url );
    }

   /**
    * Establish a connection.
    *
    * @exception IOException is an error occurs while attempting to establish
    *  the connection.
    */
    public void connect()
        throws IOException
    {
        if( m_connected )
        {
            return;
        }

        m_connected = true;
        
        try
        {
            AccessController.doPrivileged( 
              new PrivilegedExceptionAction()
              {
                public Object run()
                    throws IOException
                {
                    String spec = url.toExternalForm();
                    String host = url.getHost();
                    int port = url.getPort();

                    try
                    {
                        m_registry = LocateRegistry.getRegistry( host, port );
                    }
                    catch( Throwable e )
                    {
                        String message = e.getMessage();
                        IOException exception = new IOException( message );
                        exception.initCause( e.getCause() );
                        throw exception;
                    }
                    return null; // nothing to return
                }
              }
            );
        }
        catch( PrivilegedActionException e )
        {
            throw (IOException) e.getException();
        } 
    }

   /**
    * Return an input stream to the resource.
    * @return the input stream
    * @exception IOException is an error occurs
    */
    public InputStream getInputStream()
        throws IOException
    {
        return null;
    }

   /**
    * Return an output stream to the resource.
    * @return the output stream
    * @exception IOException if any I/O problems occur.
    */
    public OutputStream getOutputStream()
        throws IOException
    {
        return null;
    }

   /**
    * Return the content for this registry connection.
    * @return the registry object
    * @exception IOException is an error occurs
    */
    public Object getContent() throws IOException
    {
        return getContent( new Class[0] );
    }

   /**
    * Return the content for this registry connection.
    * @param classes a sequence of classes against which the
    *   implementation will attempt to establish a known match
    * @return the registry object
    * @exception IOException is an error occurs
    */
    public Object getContent( final Class[] classes ) throws IOException
    {
        connect();

        try
        {
            return AccessController.doPrivileged( 
              new PrivilegedExceptionAction()
              {
                public Object run()
                    throws IOException
                {
                    try
                    {
                        String path = getURL().getPath();
                        if( null == path )
                        {
                            return m_registry;
                        }
                        else
                        {
                            return m_registry.lookup( path );
                        }
                    }
                    catch( Exception e )
                    {
                        String message = e.getMessage();
                        IOException exception = new IOException( message );
                        exception.initCause( e.getCause() );
                        throw exception;
                    }
                }
              }
            );
        }
        catch( PrivilegedActionException e )
        {
            throw (IOException) e.getException();
        } 
    }
}

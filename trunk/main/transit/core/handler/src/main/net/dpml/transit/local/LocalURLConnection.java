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

package net.dpml.transit.local;

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

import net.dpml.transit.Transit;
import net.dpml.transit.Artifact;

/**
 * The local URL protocol connection implementation.
 */
public class LocalURLConnection extends URLConnection
{
    private boolean m_connected;

    private File     m_target;

    /**
     * Creation of a new local handler.
     * @param url the url to establish a connection with
     * @exception NullPointerException if the url argument is null
     */
    LocalURLConnection( URL url )
        throws NullPointerException
    {
        super( url );

        m_connected = false;
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
                    try
                    {
                        Artifact artifact = Artifact.createArtifact( spec );
                        String groupSpec = artifact.getGroup();
                        String artifactName = artifact.getName();
                        String typeSpec = artifact.getType();
                        String versionSpec = artifact.getVersion();
                        File prefs = Transit.DPML_PREFS;
                        File group = new File( prefs, groupSpec );
                        File type = new File( group, typeSpec + "s" );
                        if( null == versionSpec )
                        {
                            final String filename = artifactName + "." + typeSpec;
                            m_target = new File( type, filename );
                        }
                        else
                        {
                            final String filename = artifactName + "-" + versionSpec + "." + typeSpec;
                            m_target = new File( type, filename );
                        }
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
        connect();
        if( m_target == null )
        {
            return null;
        }
        else
        {
            return new FileInputStream( m_target );
        }
    }

   /**
    * Return an output stream to the resource.
    * @return the output stream
    * @exception IOException if any I/O problems occur.
    */
    public OutputStream getOutputStream()
        throws IOException
    {
        connect();
        if( m_target == null )
        {
            return null;
        }
        else
        {
            return new FileOutputStream( m_target );
        }
    }

   /**
    * Return the content for this local resource.
    * @param classes a sequence of classes against which the
    *   implementation will attempt to establish a known match
    * @return the content object (possibly null)
    * @exception IOException is an error occurs
    */
    public Object getContent( final Class[] classes )
        throws IOException
    {
        connect();
        Object result = AccessController.doPrivileged( 
          new PrivilegedAction()
          {
            public Object run()
            {
                for( int i=0; i < classes.length; i++ )
                {
                    Class c = classes[i];
                    if( c.equals( File.class ) )
                    {
                        return m_target;
                    }
                }
                return null;
            }
          }
        );

        if( null != result )
        {
            return result;
        }
        else
        {
            if( null != m_target )
            {
                return m_target.toURL().getContent( classes );
            }
            else
            {
                return super.getContent( classes );
            }
        }
    }
}

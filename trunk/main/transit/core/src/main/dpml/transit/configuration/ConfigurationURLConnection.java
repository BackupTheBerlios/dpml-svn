/*
 * Copyright 2007 Stephen J. McConnell.
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

package dpml.transit.configuration;

import dpml.transit.TransitContext;

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
import net.dpml.transit.ArtifactException;
import net.dpml.transit.ContentHandler;

/**
 * The local URL protocol connection implementation.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ConfigurationURLConnection extends URLConnection
{
   /**
    * Transit context.
    */
    private final TransitContext m_context;

    private boolean m_connected;

    private File m_target;

    /**
     * Creation of a new local handler.
     * @param url the url to establish a connection with
     * @exception NullPointerException if the url argument is null
     */
    ConfigurationURLConnection( URL url, TransitContext context )
        throws NullPointerException
    {
        super( url );

        m_context = context;
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
              new PrivilegedExceptionAction<Object>()
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
                        File config = Transit.CONFIG;
                        File group = new File( config, groupSpec );
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
        return null;
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
          new PrivilegedAction<Object>()
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
                String spec = url.toExternalForm();
                try
                {
                    Artifact artifact = Artifact.createArtifact( spec );
                    String type = artifact.getType();
                    ContentHandler handler = m_context.getContentHandler( type );
                    if( null != handler )
                    {
                        URLConnection connection = m_target.toURI().toURL().openConnection();
                        return handler.getContent( connection, classes );
                    }
                    else
                    {
                        URLConnection connection = m_target.toURI().toURL().openConnection();
                        return connection.getContent( classes );
                    }
                }
                catch( Exception e )
                {
                    final String error = 
                      "Internal error in config url connection handler.";
                    throw new ArtifactException( error, e );
                }
            }
            else
            {
                return super.getContent( classes );
            }
        }
    }
    
   /**
    * Return the link content by delegating to the underlying artifact.
    * @return the artifact content
    * @exception IOException if an IO error occurs
    */
    public Object getContent()
        throws IOException
    {
        connect();
        if( null != m_target )
        {
            String spec = url.toExternalForm();
            try
            {
                Artifact artifact = Artifact.createArtifact( spec );
                String type = artifact.getType();
                ContentHandler handler = m_context.getContentHandler( type );
                if( null != handler )
                {
                    URLConnection connection = m_target.toURI().toURL().openConnection();
                    return handler.getContent( connection );
                }
                else
                {
                    URLConnection connection = m_target.toURI().toURL().openConnection();
                    return connection.getContent();
                }
            }
            catch( Exception e )
            {
                final String error = 
                  "Internal error in config url connection handler.";
                throw new ArtifactException( error, e );
            }
        }
        else
        {
            return super.getContent();
        }
    }
}

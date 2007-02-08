/*
 * Copyright 2006-2007 Stephen J. McConnell.
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

package dpml.transit.link;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedAction;

import net.dpml.transit.Transit;
import net.dpml.transit.Artifact;
import net.dpml.transit.UnsupportedSchemeException;
import net.dpml.transit.LinkManager;

/**
 * link: URL protocol connection processor.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class LinkURLConnection extends URLConnection
{
    private static final String LINK_MIME_TYPE = "application/x-dpml-link";

    private boolean m_connected;
    private URL m_targetURL;

    /**
     * Creation of a new handler.
     * @param url the url to establish a connection with
     * @exception NullPointerException if the url argument is null
     */
    LinkURLConnection( URL url )
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
              new PrivilegedExceptionAction<Object>()
              {
                public Object run()
                    throws IOException
                {
                    URI linkUri = URI.create( url.toExternalForm() );
                    LinkManager manager = Transit.getInstance().getLinkManager();
                    URI targetUri = manager.getTargetURI( linkUri );
                    if( targetUri != null )
                    {
                        m_targetURL = Artifact.toURL( targetUri );
                    }
                    else
                    {
                        m_targetURL = null;
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
        if( m_targetURL == null )
        {
            return null;
        }
        else
        {
            URLConnection conn = m_targetURL.openConnection();
            InputStream in = conn.getInputStream();
            return in;
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
        if( m_targetURL == null )
        {
            return null;
        }
        else
        {
            URLConnection conn = m_targetURL.openConnection();
            OutputStream out = conn.getOutputStream();
            return out;
        }
    }

   /**
    * Reutrn the mimetype of the content.
    * @return the content mimetype
    */
    public String getContentType()
    {
        return LINK_MIME_TYPE;
    }
    
   /**
    * Return the content for this Link.
    * @param classes a sequence of classes against which the
    *   implementation will attempt to establish a known match
    * @return the content object (possibly null)
    * @exception IOException is an error occurs
    */
    public Object getContent( final Class[] classes )
        throws IOException
    {
        final LinkManager manager = Transit.getInstance().getLinkManager();

        Object result = AccessController.doPrivileged( 
          new PrivilegedAction<Object>()
          {
            public Object run()
            {
                for( int i=0; i < classes.length; i++ )
                {
                    Class c = classes[i];
                    if( c.equals( URI.class ) )
                    {
                        String extUri = getURL().toString();
                        URI uri = URI.create( extUri );
                        return uri;
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
            connect();
            if( null != m_targetURL )
            {
                return m_targetURL.getContent( classes );
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
        if( null != m_targetURL )
        {
            return m_targetURL.getContent();
        }
        else
        {
            return super.getContent();
        }
    }
}

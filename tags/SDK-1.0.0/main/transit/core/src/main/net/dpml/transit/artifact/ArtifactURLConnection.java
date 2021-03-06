/*
 * Copyright 2004 Niclas Hedhman
 * Copyright 2004-2006 Stephen J. McConnell
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

package net.dpml.transit.artifact;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.net.UnknownServiceException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URISyntaxException;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;
import net.dpml.transit.SecuredTransitContext;
import net.dpml.transit.CacheHandler;

import net.dpml.lang.Part;

import net.dpml.util.MimeTypeHandler;

/**
 * The connection handler for URLs based on the "artifact" protocol family.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ArtifactURLConnection extends URLConnection
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

   /**
    * Transit context.
    */
    private final SecuredTransitContext m_context;

   /**
    * Artifact.
    */
    private final Artifact m_artifact;

   /**
    * Reference fragment.
    */
    private final String m_reference;

   /**
    * The connected state.
    */
    private boolean m_connected;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    /**
     * Creation of a new handler.
     * @param url the url to establish a connection with
     * @param context the transit context
     * @exception NullPointerException if the url argument is null
     * @exception IOException if the url argument is not a valid,
     *            i.e. the path must contain a group and an artifactId
     *            separated by a slash.
     */
    ArtifactURLConnection( URL url, SecuredTransitContext context )
        throws NullPointerException, IOException
    {
        super( url );

        Transit.getInstance(); // make sure Transit is initialized

        m_context = context;
        m_reference = getReference( url );

        String spec = getRealSpec( url, m_reference );
        try
        {
            m_artifact = Artifact.createArtifact( spec );
        }
        catch( URISyntaxException e )
        {
            throw new IOException( e.toString() );
        }
    }

    // ------------------------------------------------------------------------
    // URLConnection
    // ------------------------------------------------------------------------

   /**
    * Establish a connection.  The implementation will attempt to
    * resolve the resource relative to the cache and associated hosts.
    *
    * @exception IOException is an error occurs while attempting to establish
    *  the connection.
    */
    public void connect()
        throws IOException
    {
        m_connected = true;
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
        CacheHandler cache = m_context.getCacheHandler();
        if( null != m_reference )
        {
            return cache.getResource( m_artifact, m_reference );
        }
        else
        {
            return cache.getResource( m_artifact );
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
        CacheHandler cache = m_context.getCacheHandler();
        return cache.createOutputStream( m_artifact );
    }

   /**
    * Reutrn the mimetype of the content.
    * @return the content mimetype
    */
    public String getContentType()
    {
        String type = m_artifact.getType();
        return MimeTypeHandler.getMimeType( type );
    }

   /**
    * Return the content for this artifact.
    * @return the content object (possibly null)
    * @exception IOException is an error occurs
    */
    public Object getContent()
        throws IOException
    {
        Object content = getContent( new Class[0] );
        if( content != null )
        {
            return content;
        }
        else
        {
            return super.getContent();
        }
    }

   /**
    * Return the content for this artifact.
    * @param classes a sequence of classes against which the
    *   implementation will attempt to establish a known match
    * @return the content object (possibly null)
    * @exception IOException is an error occurs
    */
    public Object getContent( Class[] classes )
        throws IOException
    {
        String type = m_artifact.getType();

        // 
        // if the type is a plugin then handle this directly
        //

        if( "part".equals( type ) )
        { 
            URI uri = m_artifact.toURI();
            Part part = Part.load( uri );
            return part.getContent( classes );
        }
        
        //
        // otherwise fallback on the default jvm content handling
        //

        try
        {
            Object content = super.getContent( classes );
            if( content != null )
            {
                return content;
            }
        }
        catch( UnknownServiceException use )
        {
            boolean ignoreThis = true;
        }

        //
        // attempt to resolve this locally as we may be dealing
        // with Magic references to the artifact File
        //

        for( int i=0; i < classes.length; i++ )
        {
            Class c = classes[i];
            if( c.equals( File.class ) )
            {
                return m_context.getCacheHandler().getLocalFile( m_artifact );
            }
        }
        return null;
    }
    
    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------

   /**
    * Return a fragment referencing content within the resource referenced by
    * the artifact.
    * @param url the url
    * @return the fragment or null if this is not a referential url
    */
    private String getReference( URL url )
    {
        String path = url.getPath();
        int i = path.indexOf( '!' );
        if( i < 0 )
        {
            return null;
        }
        else
        {
            return path.substring( i );
        }
    }

   /**
    * Return the real specification of the supplied url.
    * @param url the url to evaluate
    * @param ref a reference fragment
    * @return the artifact url spec withough the ref fragment
    */
    private String getRealSpec( URL url, String ref )
    {
        if( null != ref )
        {
            String spec = url.toString();
            int j = spec.indexOf( ref );
            if( j > 0 )
            {
                String s = spec.substring( 0, j );
                String version = url.getUserInfo();
                if( null != version )
                {
                    s = s + "#" + version;
                }
                return s;
            }
        }
        return url.toString();
    }
}

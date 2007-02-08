/*
 * Copyright 2005-2007 Stephen J. McConnell.
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

package dpml.transit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;

import net.dpml.transit.Artifact;
import net.dpml.transit.ArtifactNotFoundException;
import net.dpml.transit.TransitRuntimeException;
import net.dpml.transit.LinkManager;
import net.dpml.transit.LinkNotFoundException;

import dpml.util.PropertyResolver;

/** 
 * A link manager that maintains persistent link information as a resource.
 * Link resource located using the [cache]/[group]/[name]/[type]s/[name]-[version].[type].link
 * resource naming convention.
 * 
 * Applications should not call the methods for the LinkManager directly,
 * and it is likely that the LinkManager remains outside the reachability of
 * applications.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class StandardLinkManager
    implements LinkManager
{
    /** 
     * Sets the URI for the provided Link.
     * The LinkManager is required to persist this information between
     * JVM restarts and should be persisted on a scope larger than a
     * single JVM, typically a host or a local area network. LinkManagers
     * are encouraged to establish other virtual scopes independent of
     * network topologies.
     *
     * @param linkUri the uri of the link resource
     * @param targetUri the uri that the link redirects to
     * @exception IOException if the mapping could not be updated.
     * @exception NullPointerException if the linkUri argument is null.
     */
    public void setTargetURI( final URI linkUri, final URI targetUri )
        throws IOException, NullPointerException
    {
        if( null == linkUri )
        {
            throw new NullPointerException( "linkUri" );
        }

        try
        {
            AccessController.doPrivileged( 
              new PrivilegedExceptionAction<Object>()
              {
                public Object run()
                    throws IOException
                {
                    String artifact = linkUri.toASCIIString();
                    URL store = new URL( null, artifact, new dpml.transit.artifact.Handler() );
                    OutputStream out = store.openConnection().getOutputStream();
                    byte[] array = getByteArray( targetUri );
                    ByteArrayInputStream in = new ByteArrayInputStream( array );
                    StreamUtils.copyStream( in, out, true );
                    return null;
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
     * Returns the URI that the provided link URI instance is pointing to.
     * @param linkUri the link uri from which the target will be resolved
     * @exception LinkNotFoundException if the supplied link uri could not be located
     * @exception IOException if the mapping could not be retrieved, due to
     *    an IOException during link retrival.
     * @return target URI that the link points to (possibly null if the link does 
     *    not declare a target)
     */
    public URI getTargetURI( final URI linkUri )
        throws IOException, LinkNotFoundException
    {
        URIHolder holder = new URIHolder( linkUri );
        final URI uri = holder.getBaseURI();
        final String query = holder.getQuery();
        
        try
        {
            URI result = (URI) AccessController.doPrivileged( 
              new PrivilegedExceptionAction<Object>()
              {
                public Object run()
                    throws IOException
                {
                    URL store = null;
                    if( Artifact.isRecognized( uri ) )
                    {
                        String artifact = uri.toASCIIString();
                        store = new URL( null, artifact, new dpml.transit.artifact.Handler() );
                    }
                    else
                    {
                        store = uri.toURL();
                    }
                    
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    InputStream in = store.openConnection().getInputStream();
                    StreamUtils.copyStream( in, out, true );
                    String target = out.toString( "ISO8859-1" );
                    Properties properties = System.getProperties();
                    String path = PropertyResolver.resolve( properties, target );
                    String expanded = expand( path, query );
                    URI value = URI.create( expanded );
                    return value;
                }
              }
            );
            return result;
        }
        catch( PrivilegedActionException e )
        {
            Exception exception = e.getException();
            if( exception instanceof ArtifactNotFoundException )
            {
                 final String error =
                   "Link not found: "
                   + uri;
                 throw new LinkNotFoundException( error, uri );
            }
            else
            {
                 throw (IOException) exception;
            }
        }
    }
    
    private String expand( String artifact, String query )
    {
        if( null == query )
        {
            return artifact;
        }
        int hash = artifact.indexOf( '#' );
        if( hash > -1 )
        {
            String pre = artifact.substring( 0, hash );
            String post = artifact.substring( hash );
            return pre + "?" + query + post;
        }
        else
        {
            return artifact + "?" + query;
        }
    }
    
    private static final class URIHolder
    {
        private final URI m_uri;
        private final URI m_raw;
        private final String m_query;
        
        URIHolder( URI uri )
        {
            m_uri = uri;
            if( !Artifact.isRecognized( uri ) )
            {
                m_raw = uri;
                m_query = null;
            }
            else
            {
                URI base = Artifact.createArtifact( uri ).toURI();
                String ssp = base.getRawSchemeSpecificPart();
                int n = ssp.indexOf( '?' );
                if( n > -1 )
                {
                    m_query = ssp.substring( n + 1 );
                    String scheme = base.getScheme();
                    String result = ssp.substring( 0, n );
                    String fragment = base.getFragment();
                    try
                    {
                        m_raw = new URI( scheme, result, fragment );
                    }
                    catch( Exception e )
                    {
                        final String error = 
                          "Transformation error in ["
                          + uri
                          + "]";
                        throw new TransitRuntimeException( error, e );
                    } 
                }
                else
                {
                    m_raw = base;
                    m_query = null;
                }
            }
        }
        
        URI getBaseURI()
        {
            return m_raw;
        }
        
        String getQuery()
        {
            return m_query;
        }
    }
    
    private byte[] getByteArray( URI uri ) throws IOException
    {
        if( null != uri )
        {
            return uri.toString().getBytes( "ISO8859-1" );
        }
        else
        {
            return new byte[0];
        }
    }
}

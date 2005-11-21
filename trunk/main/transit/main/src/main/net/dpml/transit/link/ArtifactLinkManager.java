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

package net.dpml.transit.link;

import net.dpml.transit.util.StreamUtils;

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

import net.dpml.transit.NullArgumentException;
import net.dpml.transit.artifact.ArtifactNotFoundException;
import net.dpml.transit.util.PropertyResolver;

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
public class ArtifactLinkManager
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
     */
    public void setTargetURI( final URI linkUri, final URI targetUri )
        throws IOException
    {
        if( null == linkUri )
        {
            throw new NullArgumentException( "linkUri" );
        }

        try
        {
            AccessController.doPrivileged( 
              new PrivilegedExceptionAction()
              {
                public Object run()
                    throws IOException
                {
                    String artifact = linkUri.toASCIIString();
                    URL store = new URL( null, artifact, new net.dpml.transit.artifact.Handler() );
                    OutputStream out = store.openConnection().getOutputStream();
                    byte[] array = getByteArray( targetUri );
                    ByteArrayInputStream in = new ByteArrayInputStream( array );
                    StreamUtils.copyStream( in, out, true );
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
        try
        {
            URI result = (URI) AccessController.doPrivileged( 
              new PrivilegedExceptionAction()
              {
                public Object run()
                    throws IOException
                {
                    String artifact = linkUri.toASCIIString();
                    URL store = new URL( null, artifact, new net.dpml.transit.artifact.Handler() );
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    InputStream in = store.openConnection().getInputStream();
                    StreamUtils.copyStream( in, out, true );
                    String target = out.toString( "ISO8859-1" );
                    String path = PropertyResolver.resolve( target );
                    URI value = URI.create( path );
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
                   + linkUri;
                 throw new LinkNotFoundException( error, linkUri );
            }
            else
            {
                 throw (IOException) exception;
            }
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

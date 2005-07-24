/*
 * Copyright 2005 Niclas Hedhman
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

import net.dpml.transit.Transit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedAction;

/** The LinkManager is responsible for storing the mapping between a Link
 *  instance and a URI.
 * <p>
 *  Applications should never call the methods for the LinkManager directly,
 *  and it is likely that the LinkManager remains outside the reachability of
 *  applications.
 * </p>
 * <p>
 *  This LinkManager stores all the link: to URI mappings in text files,
 *  which are stored in $DPML_PREFS/transit/links.
 * </p>
 * <p>
 *  The SchemeSpecificPart of the link: URI, i.e. the value between the colon
 *  (:) and the hash (#) in "link:something#3.1", will be used as the filename.
 *  That file is in the standard java.util.Properties format and
 *  the key is the string representation of the link URI and the value is the
 *  string representation of the target URI.
 * </p>
 */

// CLASS NOT USED

class FileLinkManager
    implements LinkManager
{
    private File m_linksDir;

    public FileLinkManager()
    {
        AccessController.doPrivileged( new PrivilegedAction()
        {
            public Object run()
            {
                m_linksDir = new File( Transit.DPML_PREFS, "transit/links" );
                m_linksDir.mkdirs();
                return null;
            }
        });
    }

    /** Sets the URI for the provided Link.
     * The LinkManager is required to persist this information between
     * JVM restarts and should be persisted on a scope larger than a
     * single JVM, typically a host or a local area network. LinkManagers
     * are encouraged to establish other virtual scopes independent of
     * network topologies.
     * @exception IOException if the mapping could not be updated.
     */
    public void setTargetURI( final URI linkUri, final URI targetUri )
        throws IOException
    {
        try
        {
            AccessController.doPrivileged( new PrivilegedExceptionAction()
            {

                public Object run()
                    throws IOException
                {
                    File linkFile = getLinkFile( linkUri );
                    lockWrite( linkFile );
                    try
                    {
                        setTargetURI( linkFile, linkUri, targetUri );
                    } finally
                    {
                        unlock( linkFile );
                    }
                    return null;
                }
            });
        } catch( PrivilegedActionException e )
        {
            throw (IOException) e.getException();
        }
    }

    /** Returns the URI that the provided link: URI instance is pointing to.
     * @exception IOException if the mapping could not be retrieved, due to
     *            a IOException.
     * @return target URI that the link points to, or null if it could not
     *         be found.
     */
    public URI getTargetURI( final URI linkUri )
        throws IOException
    {
        try
        {
            URI result = (URI) AccessController.doPrivileged( new PrivilegedExceptionAction()
            {

                public Object run()
                    throws IOException
                {
                    File linkFile = getLinkFile( linkUri );
                    lockRead( linkFile );
                    try
                    {
                        return getTargetURI( linkFile, linkUri );
                    } finally
                    {
                        unlock( linkFile );
                    }
                }
            });
            return result;
        } catch( PrivilegedActionException e )
        {
            throw (IOException) e.getException();
        }
    }

    private File getLinkFile( URI linkUri )
    {
        String filename = linkUri.getSchemeSpecificPart();
        File f = new File( m_linksDir, filename );
        return f;
    }

    private void setTargetURI( final File linkFile, final URI linkUri, final URI targetUri )
        throws IOException
    {
        Properties props = new Properties();
        if( linkFile.exists() )
        {
            FileInputStream in = new FileInputStream( linkFile );
            try
            {
                props.load( in );
            } finally
            {
                in.close();
            }
        }
        String link = linkUri.toString();
        String target = targetUri.toString();
        props.put( link, target );
        File parent = linkFile.getParentFile();
        parent.mkdirs();
        FileOutputStream out = new FileOutputStream( linkFile );
        try
        {
            props.store( out, "Links for " + linkUri.getSchemeSpecificPart() );
        } finally
        {
            out.close();
        }
    }

    private URI getTargetURI( final File linkFile, final URI linkUri )
        throws IOException
    {
        if( linkFile.exists() == false )
            return null;

        FileInputStream in = new FileInputStream( linkFile );
        Properties props = new Properties();
        try
        {
            props.load( in );
        } finally
        {
            in.close();
        }
        in.close();
        String link = linkUri.toString();
        String target = props.getProperty( link );
        if( target == null )
            return null;
        return URI.create( target );
    }

    private void lockWrite( File file )
    {
        // TODO
    }

    private void lockRead( File file )
    {
        // TODO
    }

    private void unlock( File file )
    {
        // TODO
    }
}

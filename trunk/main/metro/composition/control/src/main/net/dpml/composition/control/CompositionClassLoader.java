/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.composition.control;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException ;

import net.dpml.logging.Logger;

import net.dpml.part.control.ControllerRuntimeException;

import net.dpml.transit.artifact.Artifact;
import net.dpml.transit.artifact.UnsupportedSchemeException;
import net.dpml.transit.StandardClassLoader;

/**
 * A named classloader.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
class CompositionClassLoader extends StandardClassLoader
{
    //--------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------

    private final Logger m_logger;
    private final URI m_partition;
    private final int m_index;

    //--------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------

    public CompositionClassLoader( Logger Logger, URI partition, ClassLoader parent )
    {
        this( Logger, partition, 0, new URI[0], parent );
    }

    public CompositionClassLoader( Logger logger, URI partition, int index, URI[] uris, ClassLoader parent )
    {
        super( partition, urisToURLs( uris ), parent );
        m_partition = partition;
        m_index = index;
        m_logger = logger;
    }

    //--------------------------------------------------------------------
    // CompositionClassLoader
    //--------------------------------------------------------------------

    public URI getPartition()
    {
        return m_partition;
    }

    public int getIndex()
    {
        return m_index;
    }

    //--------------------------------------------------------------------
    // Object
    //--------------------------------------------------------------------

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        listClasspath( buffer );
        return buffer.toString();
    }

    protected void listClasspath( StringBuffer buffer )
    {
        listClasspath( buffer, this );
        buffer.append( "\n" );
    }

    protected void listClasspath( StringBuffer buffer, ClassLoader classloader )
    {
        if( classloader instanceof CompositionClassLoader )
        {
            CompositionClassLoader cl = (CompositionClassLoader) classloader;
            ClassLoader parent = cl.getParent();
            if( null != parent )
            {
                listClasspath( buffer, parent );
            }
            int index = cl.getIndex();
            URI partition = cl.getPartition();
            buffer.append( "\n  " + partition + " (" + index + ")" );
            appendEntries( buffer, cl );
        }
        else if( classloader instanceof StandardClassLoader )
        {
            StandardClassLoader cl = (StandardClassLoader) classloader;
            ClassLoader parent = cl.getParent();
            if( null != parent )
            {
                listClasspath( buffer, parent );
            }
            URI partition = cl.getPartition();
            buffer.append( "\n  transit:" + partition );
            appendEntries( buffer, cl );
        }
        else if( classloader instanceof URLClassLoader )
        {
            URLClassLoader cl = (URLClassLoader) classloader;
            ClassLoader parent = cl.getParent();
            if( null != parent )
            {
                listClasspath( buffer, parent );
            }
            buffer.append( "\n  url classloader"  );
            appendEntries( buffer, cl );
        }
        else
        {
            buffer.append( "\n  classloader (no details)"  );
            buffer.append( "\n" );
        }
    }

    private void appendEntries( StringBuffer buffer, URLClassLoader classloader )
    {
        URL[] urls = classloader.getURLs();
        for( int i=0; i<urls.length; i++ )
        {
            buffer.append( "\n    " );
            URL url = urls[i];
            String spec = url.toString();
            buffer.append( spec );
        }
        buffer.append( "\n" );
    }

    protected void finalize()
    {
        if( m_logger.isDebugEnabled() )
        {
            final String message = 
              "classloader finalization: " + m_partition;
            m_logger.debug( message );
        }
    }

    //--------------------------------------------------------------------
    // static utilities
    //--------------------------------------------------------------------

    private static URL[] urisToURLs( final URI[] uris )
    {
        URL[] urls = new URL[ uris.length ];
        for( int i=0; i<uris.length; i++ )
        {
            URI uri = uris[i];
            urls[i] = uriToURL( uri );
        }
        return urls;
    }

    private static URL uriToURL( URI uri )
    {
        try
        {
            return doUriToURL( uri );
        }
        catch( MalformedURLException e )
        {
            final String error =
              "Internal error occured while attempting to transform the uri ["
              + uri 
              + "] to a url."; 
            throw new ControllerRuntimeException( CompositionController.CONTROLLER_URI, error, e );
        }
    }

    private static URL doUriToURL( URI uri ) throws MalformedURLException
    {
        try
        {
            Artifact artifact = Artifact.createArtifact( uri );
            return artifact.toURL();
        }
        catch( UnsupportedSchemeException e )
        {
            return uri.toURL();
        }
    }
}

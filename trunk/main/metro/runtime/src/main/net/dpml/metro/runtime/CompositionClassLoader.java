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

package net.dpml.metro.runtime;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.ArrayList;

import net.dpml.logging.Logger;

import net.dpml.transit.Artifact;
import net.dpml.transit.UnsupportedSchemeException;
import net.dpml.transit.StandardClassLoader;
import net.dpml.transit.Category;

/**
 * A named classloader.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class CompositionClassLoader extends StandardClassLoader
{
    //--------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------

    private final ClassLoader m_base;

    //--------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------

    public CompositionClassLoader( 
      URI uri, Category category, ClassLoader base, URI[] uris, ClassLoader parent )
    {
        super( uri, category, urisToURLs( uris, parent ), parent );

        m_base = base;
    }

    //--------------------------------------------------------------------
    // ClassLoader
    //--------------------------------------------------------------------

    protected Class loadClass( String name, boolean resolve ) throws ClassNotFoundException 
    {
        try
        {
            return m_base.loadClass( name );
        }
        catch( ClassNotFoundException e )
        {
            return super.loadClass( name, resolve );
        }
    }

    protected Class findClass( String name ) throws ClassNotFoundException 
    {
        try
        {
            return m_base.loadClass( name );
        }
        catch( ClassNotFoundException e )
        {
            return super.findClass( name );
        }
    }

    //--------------------------------------------------------------------
    // static utilities
    //--------------------------------------------------------------------

    private static URL[] urisToURLs( final URI[] uris, ClassLoader parent )
    {
        ArrayList list = new ArrayList();
        for( int i=0; i<uris.length; i++ )
        {
            URI uri = uris[i];
            URL url = uriToURL( uri );
            if( isaCandidate( parent, url ) )
            {
                list.add( url );
            }
        }
        return (URL[]) list.toArray( new URL[0] );
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
            throw new ControllerRuntimeException( error, e );
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

   /**
    * Test if the supplied url is already present within the supplied classloader.
    * @param classloader the classloader to validate against
    * @param url to url to check for
    * @return true if the url is not included in the classloader
    */
    private static boolean isaCandidate( ClassLoader classloader, URL url )
    {
        if( classloader instanceof URLClassLoader )
        {
            URL[] urls = ( (URLClassLoader) classloader ).getURLs();
            for( int i=0; i < urls.length; i++ )
            {
                if( urls[i].equals( url ) )
                {
                    return false;
                }
            }
            ClassLoader parent = classloader.getParent();
            if( parent == null )
            {
                return true;
            }
            else
            {
                return isaCandidate( parent, url );
            }
        }
        else
        {
            return true;
        }
    }
}

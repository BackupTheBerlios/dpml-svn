/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.part;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.io.IOException;

import net.dpml.lang.Category;
import net.dpml.lang.Classpath;

import net.dpml.transit.Artifact;
import net.dpml.transit.StandardClassLoader;

/**
 * Construct a part.
 */
public class StandardPartHandler implements PartHandler
{
    public static final URI PART_HANDLER_URI = createHandlerURI();
    
   /**
    * Build a classloader stack.
    * @param anchor the anchor classloader to server as the classloader chain root
    * @param classpath the part classpath definition
    */
    public ClassLoader getClassLoader( ClassLoader anchor, Classpath classpath ) throws IOException
    {
        if( null == anchor )
        {
            throw new NullPointerException( "anchor" );
        }
        if( null == classpath )
        {
            throw new NullPointerException( "classpath" );
        }
        
        URL[] sysUrls = getURLs( classpath, Category.SYSTEM );
        URL[] apis = getURLs( classpath, Category.PUBLIC );
        URL[] spis = getURLs( classpath, Category.PROTECTED );
        URL[] imps = getURLs( classpath, Category.PRIVATE );
        //if( sysUrls.length > 0 )
        //{
        //    updateSystemClassLoader( plugin, sysUrls );
        //}
        
        ClassLoader parent = buildClassLoader( Category.PUBLIC, anchor, apis );
        parent = buildClassLoader( Category.PROTECTED, parent, spis );
        parent = buildClassLoader( Category.PRIVATE, parent, imps );
        return parent;
    }
    
   /**
    * Instantiate a value.
    * @param classloader the implementation classloader established for the part
    * @param data the part deployment data
    * @param args supplimentary arguments
    * @exception Exception if a deployment error occurs
    */
    public Object getInstance( ClassLoader classloader, Object data, Object[] args ) throws Exception
    {
        throw new UnsupportedOperationException( "getInstance" );
    }
    
    private URL[] getURLs( Classpath classpath, Category category ) throws IOException
    {
        URI[] uris = classpath.getDependencies( category );
        return getURLs( uris );
    }

   /**
    * Convert a sequncence of URIs to URLs.
    * @param uris the uris to convert
    * @return the corresponding urls
    * @exception IOException of a transformation error occurs
    */
    private URL[] getURLs( URI[] uris ) throws IOException
    {
        URL[] urls = new URL[ uris.length ];
        for( int i=0; i < urls.length; i++ )
        {
            URI uri = uris[i];
            urls[i] = Artifact.toURL( uri );
        }
        return urls;
    }
    
   /**
    * Internal utility class to build a classloader.  If the supplied url
    * sequence is zero length the parent classloader is returned directly.
    *
    * @param category the type of classloader (api, spi, impl)
    * @param parent the parent classloader
    * @param urls the urls to assign as classloader content
    * @return the classloader
    */
    private ClassLoader buildClassLoader( Category category, ClassLoader parent, URL[] urls )
    {
        if( 0 == urls.length )
        {
            return parent;
        }
        ArrayList list = new ArrayList();
        for( int i=0; i < urls.length; i++ )
        {
            if( isaCandidate( parent, urls[i] ) )
            {
                list.add( urls[i] );
            }
        }
        URL[] qualified = (URL[]) list.toArray( new URL[0] );
        if( qualified.length == 0 )
        {
            return parent;
        }
        else
        {
            return new StandardClassLoader( null, category, qualified, parent );
        }
    }

   /**
    * Test if the supplied url is already present within the supplied classloader.
    * @param classloader the classloader to validate against
    * @param url to url to check for
    * @return true if the url is not included in the classloader
    */
    private boolean isaCandidate( ClassLoader classloader, URL url )
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
    
    private static URI createHandlerURI()
    {
        try
        {
            return new URI( "internal:transit" );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            return null;
        }
    }
    
}

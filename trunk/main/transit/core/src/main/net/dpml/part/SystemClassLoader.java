/*
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

package net.dpml.part;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Arrays;

import net.dpml.lang.Category;

/**
 * The SystemClassLoader is a URLClassLoader that supports late binding of 
 * URLs.  This class may be configured as the system classloader when loading plugins
 * that declare system category plugin entries.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class SystemClassLoader extends StandardClassLoader 
{
    private final ClassLoader m_parent;
    
   /**
    * Creation of a new Depot classloader.
    *
    * @param parent the parent classloader
    */
    public SystemClassLoader( ClassLoader parent )
    {
        super( SYSTEM_URI, Category.SYSTEM, new URL[0], parent );
        m_parent = parent;
        
        if( "true".equals( System.getProperty( "dpml.transit.include.tools" ) ) )
        {
            String jrePath = System.getProperty( "java.home" );
            try
            {
                File jre = new File( jrePath );
                File jdk = jre.getParentFile();
                File lib = new File( jdk, "lib" );
                File jar = new File( lib, "tools.jar" );
                URL url = jar.toURL();
                addURL( url );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Internal error while attempting to establish tools.jar in the system classloader.";
                throw new PartError( error, e );
            }
        }
    }
    
    synchronized void addDelegates( final URI[] uris ) throws IOException
    {
        URL[] urls = toURLs( uris );
        URL[] local = super.getURLs();
        List list = Arrays.asList( local );
        for( int i=0; i<urls.length; i++ )
        {
            URL url = urls[i];
            if( !list.contains( url ) )
            {
                try
                {
                    url.getContent();
                    addURL( url );
                }
                catch( Exception e )
                {
                    final String error = 
                      "Failed to resolve url " + url;
                    System.err.println( error );
                }
            }
        }
    }
    
    private static final URI createSystemLabel()
    {
        try
        {
           return new URI( "transit:system" );
        }
        catch( Exception e )
        {
            return null;
        }
    }
    
    private static final URI SYSTEM_URI = createSystemLabel();
}


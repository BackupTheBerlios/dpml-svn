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

package net.dpml.lang;

import java.io.IOException;
import java.util.ArrayList;
import java.net.URL;
import java.net.URI;
import java.net.URLClassLoader;

import net.dpml.lang.Category;

import net.dpml.transit.Artifact;

/**
 * A named classloader.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StandardClassLoader extends URLClassLoader
{
    //--------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------
    
   /**
    * Internal utility class to build a classloader.  If the supplied url
    * sequence is zero length the parent classloader is returned directly.
    *
    * @param uri the uri identifying the classloader source part
    * @param category the category that this classloader is handling
    * @param parent the parent classloader
    * @param uris the uris to assign as classloader content
    * @return the classloader
    * @exception IOException if an I/O error occurs
    */
    public static ClassLoader buildClassLoader( URI uri, Category category, ClassLoader parent, URI[] uris )
      throws IOException
    {
        URL[] urls = toURLs( uris  );
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
            return new StandardClassLoader( uri, category, qualified, parent );
        }
    }

   /**
    * Convert a sequncence of URIs to URLs.
    * @param uris the uris to convert
    * @return the corresponding urls
    * @exception IOException of a transformation error occurs
    */
    public static URL[] toURLs( URI[] uris ) throws IOException
    {
        URL[] urls = new URL[ uris.length ];
        for( int i=0; i < urls.length; i++ )
        {
            URI uri = uris[i];
            if( Artifact.isRecognized( uri ) )
            {
                urls[i] = Artifact.toURL( uri );
            }
            else
            {
                urls[i] = uri.toURL();
            }
        }
        return urls;
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

    //--------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------
    
    private final Category m_category;
    private final URI m_plugin;

    //--------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------

   /**
    * Creation of a new classloader.
    * @param plugin uri identifying the plugin
    * @param category the classloader category identifier
    * @param urls an array of urls to add to the classloader
    * @param parent the parent classloader
    */
    public StandardClassLoader( URI plugin, Category category, URL[] urls, ClassLoader parent )
    {
        super( urls, parent );
        m_category = category;
        m_plugin = plugin;
    }

    //--------------------------------------------------------------------
    // StandardClassLoader
    //--------------------------------------------------------------------

   /**
    * Return the classloader category
    * @return the classloader category
    */
    public Category getCategory()
    {
        return m_category;
    }

   /**
    * Return the plugin uri identifier
    * @return the plugin uri
    */
    public URI getPluginURI()
    {
        return m_plugin;
    }

   /**
    * Return a string representation of the classloader.
    * @return the string value
    */
    public String getAnnotations()
    {
        StringBuffer buffer = new StringBuffer();
        ClassLoader parent = getParent();
        if( parent instanceof URLClassLoader )
        {
            URLClassLoader urlClassLoader = (URLClassLoader) parent;
            buffer.append( getURLClassLoaderAnnotations( urlClassLoader ) ); 
        }
        buffer.append( " " );
        URL[] urls = getURLs();
        for( int i=0; i<urls.length; i++ )
        {
            String path = urls[i].toString();
            if( !path.startsWith( "file:" ) )
            {
                buffer.append( path );
                buffer.append( " " );
            }
        }
        return buffer.toString().trim();
    }
    
    private String getURLClassLoaderAnnotations( URLClassLoader classloader )
    {
        StringBuffer buffer = new StringBuffer();
        ClassLoader parent = classloader.getParent();
        if( ( null != parent ) && ( parent instanceof URLClassLoader ) )
        {
            URLClassLoader urlClassLoader = (URLClassLoader) parent;
            buffer.append( getURLClassLoaderAnnotations( urlClassLoader ) );
        }
        if( ClassLoader.getSystemClassLoader() == classloader )
        {
            return "";
        }
        buffer.append( " " );
        URL[] urls = classloader.getURLs();
        for( int i=0; i<urls.length; i++ )
        {
            String path = urls[i].toString();
            if( !path.startsWith( "file:" ) )
            {
                buffer.append( path );
                buffer.append( " " );
            }
        }
        return buffer.toString().trim();
    }

   /**
    * Return a string representing of the classloader.
    * @param expanded if true return an expanded representation of the classloader
    * @return the string representation 
    */
    public String toString( boolean expanded )
    {
        StringBuffer buffer = new StringBuffer();
        listClasspath( buffer );
        return buffer.toString();
    }

    //--------------------------------------------------------------------
    // Object
    //--------------------------------------------------------------------

   /**
    * Return a string representing of the classloader.
    * @return the string representation 
    */
    public String toString()
    {
        final String label = 
          getClass().getName() 
          + "#" 
          + System.identityHashCode( this );
        return label;
    }

   /**
    * Internal operation to list the classloader classpath.
    * @param buffer the buffer to list to
    */
    protected void listClasspath( StringBuffer buffer )
    {
        listClasspath( buffer, this );
        buffer.append( "\n" );
    }

   /**
    * Internal operation to list a classloader classpath.
    * @param buffer the buffer to list to
    * @param classloader the classloader to list
    */
    protected void listClasspath( StringBuffer buffer, ClassLoader classloader )
    {
        String label = 
          "\nClassLoader: " 
          + classloader.getClass().getName() 
          + " (" + System.identityHashCode( classloader ) + ")";

        if( classloader instanceof StandardClassLoader )
        {
            StandardClassLoader cl = (StandardClassLoader) classloader;
            ClassLoader parent = cl.getParent();
            if( null != parent )
            {
                listClasspath( buffer, parent );
            }

            if( null != m_plugin )
            {
                label = label.concat( "\nGroup: " + m_plugin + " " + cl.getCategory() );
            }
            else
            {
                label = label.concat( "\nCategory: " + cl.getCategory() );
            }
            buffer.append( label );
            buffer.append( "\n" );
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
            buffer.append( label );
            appendEntries( buffer, cl );
        }
        else
        {
            buffer.append( label );
            buffer.append( "]\n" );
        }
    }

    private static void appendEntries( StringBuffer buffer, URLClassLoader classloader )
    {
        URL[] urls = classloader.getURLs();
        for( int i=0; i < urls.length; i++ )
        {
            buffer.append( "\n    " );
            URL url = urls[i];
            String spec = url.toString();
            buffer.append( spec );
        }
        buffer.append( "\n" );
    }

   /**
    * Return a string representing a report fo the common classloader chain
    * following by the primary annd seciondarty classloaders.
    * @param primary the primary classloader
    * @param secondary the secondary classloader
    * @return the report
    */
    public static String toString( ClassLoader primary, ClassLoader secondary )
    {
        StringBuffer buffer = new StringBuffer();
        ClassLoader anchor = getCommonParent( primary, secondary );
        if( null != anchor )
        {
            buffer.append( "\n----------------------------------------------------------------" );
            buffer.append( "\nCommon Classloader" );
            buffer.append( "\n----------------------------------------------------------------" );
            list( buffer, anchor );
        }
        buffer.append( "\n----------------------------------------------------------------" );
        buffer.append( "\nPrimary Classloader" );
        buffer.append( "\n----------------------------------------------------------------" );
        list( buffer, primary, anchor );
        buffer.append( "\n----------------------------------------------------------------" );
        buffer.append( "\nSecondary Classloader" );
        buffer.append( "\n----------------------------------------------------------------" );
        list( buffer, secondary, anchor );
        buffer.append( "\n----------------------------------------------------------------" );
        return buffer.toString();
    }
    
    private static ClassLoader getCommonParent( ClassLoader primary, ClassLoader secondary )
    {
        ClassLoader[] primaryChain = getClassLoaderChain( primary );
        ClassLoader[] secondaryChain = getClassLoaderChain( secondary );
        return getCommonClassLoader( primaryChain, secondaryChain );
    }
    
    private static ClassLoader[] getClassLoaderChain( ClassLoader classloader )
    {
        ArrayList list = new ArrayList();
        list.add( classloader );
        ClassLoader parent = classloader.getParent();
        while( null != parent )
        {
            list.add( parent );
            parent = parent.getParent();
        }
        ArrayList result = new ArrayList();
        int n = list.size() - 1;
        for( int i=n; i>-1; i-- )
        {
            result.add( list.get( i ) );
        }
        
        return (ClassLoader[]) result.toArray( new ClassLoader[0] );
    }

    private static ClassLoader getCommonClassLoader( ClassLoader[] primary, ClassLoader[] secondary )
    {
        ClassLoader anchor = null;
        for( int i=0; i<primary.length; i++ )
        {
            ClassLoader classloader = primary[i];
            if( secondary.length > i )
            {
                ClassLoader cl = secondary[i];
                if( classloader == cl )
                {
                    anchor = cl;
                }
                else
                {
                    return anchor;
                }
            }
            else
            {
                return anchor;
            }
        }
        return anchor;
    }
    
    private static void list( StringBuffer buffer, ClassLoader classloader )
    {
        list( buffer, classloader, null );
    }
    
    private static void list( StringBuffer buffer, ClassLoader classloader, ClassLoader anchor )
    {
        if( classloader == anchor )
        {
            return;
        }
        ClassLoader parent = classloader.getParent();
        if( null != parent  )
        {
            list( buffer, parent, anchor );
        }
        String label = 
          "\nClassLoader: " 
          + classloader.getClass().getName() 
          + " (" + System.identityHashCode( classloader ) + ")";
        buffer.append( label );
        if( classloader instanceof StandardClassLoader )
        {
            StandardClassLoader loader = (StandardClassLoader) classloader;
            buffer.append( " " + loader.m_category );
            URI uri = loader.getPluginURI();
            if( null != uri )
            {
                buffer.append( "\nURI: " + uri );
            }
        }
        if( classloader instanceof URLClassLoader )
        {
            URLClassLoader urlcl = (URLClassLoader) classloader;
            buffer.append( "\n" );
            appendEntries( buffer, urlcl );
        }
    }
}

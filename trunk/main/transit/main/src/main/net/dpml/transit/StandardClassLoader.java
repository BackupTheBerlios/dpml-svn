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

package net.dpml.transit;

import java.net.URL;
import java.net.URI;
import java.net.URLClassLoader;

/**
 * A named classloader.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StandardClassLoader extends URLClassLoader
{
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

    //--------------------------------------------------------------------
    // Object
    //--------------------------------------------------------------------

   /**
    * Return a string representing of the classloader.
    * @return the string representation 
    */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        listClasspath( buffer );
        return buffer.toString();
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

            label = label.concat( "\nGroup: " + m_plugin + " " + cl.getCategory() );
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

    private void appendEntries( StringBuffer buffer, URLClassLoader classloader )
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

}

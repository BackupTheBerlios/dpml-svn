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

import java.util.ArrayList;
import java.util.Stack;
import java.net.URL;
import java.net.URI;
import java.net.URLClassLoader;

import net.dpml.lang.Category;

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
        }
        if( classloader instanceof URLClassLoader )
        {
            URLClassLoader urlcl = (URLClassLoader) classloader;
            buffer.append( "\n" );
            appendEntries( buffer, urlcl );
        }
    }
}

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

package net.dpml.depot;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import net.dpml.transit.util.PropertyResolver;

/**
 * The DepotClassLoader is a URLClassLoader that supports late binding of 
 * URLs.  This class is used by the Depot CLI scripts as the system
 * classloader.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: Main.java 2480 2005-05-10 04:44:32Z mcconnell@dpml.net $
 */
public final class DepotClassLoader extends URLClassLoader
{
    private boolean m_sealed = false;

   /**
    * Creation of a new Depot classloader.
    *
    * @param parent the parent classloader
    */
    public DepotClassLoader( ClassLoader parent )
    {
        super( new URL[0], parent );
    }

   /**
    * Set the classpath of the classloader.
    * @param classpath an array of Transit uris
    * @exception IOException if a classloader element could not be converted to a vaid URL
    * @exception IllegalStateException if the classpath has already been set
    */
    public void setClasspath( String[] classpath ) throws IOException, IllegalStateException
    {
        if( m_sealed )
        {
            final String error = 
              "Illegal attempt to modify the bootstrap classpath.";
            throw new IllegalStateException( error );
        }
        Properties properties = System.getProperties();
        for( int i=0; i < classpath.length; i++ )
        {
            String entry = classpath[i];
            URL url = resolveURL( properties, entry );
            addURL( url );
        }
        m_sealed = true;
    }

    private URL resolveURL( Properties properties, String value ) throws IOException
    {
        String path = PropertyResolver.resolve( properties, value );
        if( path.startsWith( "file:" ) || path.startsWith( "http:" ) 
          || path.startsWith( "https:" ) || path.startsWith( "ftp:" ) )
        {
            return new URL( path );
        }
        else
        {
            File file = new File( path );
            return file.toURL();
        }
    }

   /**
    * Return a string representation of the classloader.  
    * @return the classloader presented as a stack
    */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        listClasspath( buffer );
        return buffer.toString();
    }

   /**
    * List the contents of the classloader.
    * @param buffer a string buffer to write to
    */
    protected void listClasspath( StringBuffer buffer )
    {
        listClasspath( buffer, this, 0 );
        buffer.append( "\n" );
    }

   /**
    * List the contents of the classloader.
    * @param buffer a string buffer to write to
    * @param classloader the classloader to list
    * @param n the classloader index
    */
    protected void listClasspath( StringBuffer buffer, ClassLoader classloader, int n )
    {
        if( classloader instanceof URLClassLoader )
        {
            URLClassLoader cl = (URLClassLoader) classloader;
            ClassLoader parent = cl.getParent();
            if( null != parent )
            {

                listClasspath( buffer, parent, n + 1 );
            }
            if( n == 0 )
            {
                buffer.append( "\n  Depot System ClassLoader"  );
            }
            else if( n == 1 )
            {
                buffer.append( "\n  Bootstrap ClassLoader"  );
            }
            else if( n == 2 )
            {
                buffer.append( "\n  JRE Extensions ClassLoader"  );
            }
            else
            {
                buffer.append( "\n  url classloader"  );
            }
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


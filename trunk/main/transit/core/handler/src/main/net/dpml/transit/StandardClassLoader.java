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

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * A named classloader.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class StandardClassLoader extends URLClassLoader
{
    //--------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------

    private final String m_partition;

    //--------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------

   /**
    * Creation of a new classloader.
    * @param partition the classloader identifier
    * @param parent the parent classloader
    */
    public StandardClassLoader( String partition, ClassLoader parent )
    {
        this( partition, new URL[0], parent );
    }

   /**
    * Creation of a new classloader.
    * @param partition the classloader identifier
    * @param urls an array of urls to add to the classloader
    * @param parent the parent classloader
    */
    public StandardClassLoader( String partition, URL[] urls, ClassLoader parent )
    {
        super( urls, parent );
        m_partition = partition;
    }

    //--------------------------------------------------------------------
    // CompositionClassLoader
    //--------------------------------------------------------------------

   /**
    * Return the partition name.
    * @return the partition
    */
    public String getPartition()
    {
        return m_partition;
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
        if( classloader instanceof StandardClassLoader )
        {
            StandardClassLoader cl = (StandardClassLoader) classloader;
            ClassLoader parent = cl.getParent();
            if( null != parent )
            {
                listClasspath( buffer, parent );
            }
            String partition = cl.getPartition();
            buffer.append( "\n  transit classloader: " + partition + " " 
              + System.identityHashCode( classloader ) );
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

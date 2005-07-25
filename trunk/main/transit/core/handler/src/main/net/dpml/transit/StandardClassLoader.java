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

    private final URI m_partition;

    //--------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------

    public StandardClassLoader( URI partition, ClassLoader parent )
    {
        this( partition, new URL[0], parent );
    }

    public StandardClassLoader( URI partition, URL[] urls, ClassLoader parent )
    {
        super( urls, parent );
        m_partition = partition;
    }

    //--------------------------------------------------------------------
    // CompositionClassLoader
    //--------------------------------------------------------------------

    public URI getPartition()
    {
        return m_partition;
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
        if( classloader instanceof StandardClassLoader )
        {
            StandardClassLoader cl = (StandardClassLoader) classloader;
            ClassLoader parent = cl.getParent();
            if( null != parent )
            {
                listClasspath( buffer, parent );
            }
            URI partition = cl.getPartition();
            buffer.append( "\n  transit: " + partition );
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

}

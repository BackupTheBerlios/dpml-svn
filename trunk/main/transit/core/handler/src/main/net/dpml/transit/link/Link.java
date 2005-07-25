/*
 * Copyright 2005 Niclas Hedhman
 * Copyright 2005 Stephen McConnell
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

package net.dpml.transit.link;

import java.io.IOException;

import java.net.URI;

import net.dpml.transit.NullArgumentException;

/** 
 *  The Link class is a data structure that holds the target uri of a link.
 * 
 *  It is not intended that the applications instantiates this class directly,
 *  but obtains it as a object from the <code>URL.getContent()</code> method.
 *  Example;
 *
 *  <pre><code>
 *      URL url = new URL( "link:jar:some/opague/pointer" );
 *      Class[] type = new Class[] { Link.class };
 *
 *      // Get the Link object from the URL
 *      Link link = (Link) url.getContent( type );
 *
 *      // Get the URI that this link is pointing to at the moment
 *      URI uri = link.getTargetURI();
 *
 *      // Change the link to point somewhere else.
 *      URI newUri = new URI( "artifact:jar:abc/def/hoopla#3.1.2" );
 *      link.setTargetURI( newUri );
 *  </code></pre>
 */
public class Link
{
    private final LinkManager m_manager;
    private final URI m_uri;

   /** 
    * Constructor for the Link.
    * @param uri the link physical uri
    * @param manager the link manager
    * @exception NullArgumentException if the link uri or manager is null
    */
    public Link( URI uri, LinkManager manager )
    {
        if( null == manager )
        {
            throw new NullArgumentException( "manager" );
        }
        if( null == uri )
        {
            throw new NullArgumentException( "uri" );
        }

        m_manager = manager;
        m_uri = uri;
    }

    public URI getLinkURI()
    {
        return m_uri;
    }

   /** 
    * Return the URI that is currently bound to the Link.
    * @param defaultUri the default value
    * @return the current URI the link: is pointing to.
    */
    public URI getTargetURI( URI defaultUri )
        throws IOException
    {
        try
        {
            return m_manager.getTargetURI( m_uri );
        }
        catch( LinkNotFoundException e )
        {
            return defaultUri;
        }
    }

   /** 
    * Sets (and permanently remembers) the Link to point to a new URI.
    * @param uri the URI that this link: should be pointing to.
    */
    public void setTargetURI( URI uri )
        throws IOException
    {
        m_manager.setTargetURI( m_uri, uri );
    }
}


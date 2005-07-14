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

import java.io.IOException;
import java.net.URLConnection;
import java.net.URI;
import java.net.ContentHandler;
import java.util.Properties;

import net.dpml.transit.Transit;
import net.dpml.transit.adapter.LoggingAdapter;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.model.DefaultContentModel;
import net.dpml.transit.repository.Repository;

/**
 */
class PartContentHandler extends ContentHandler
{
    private final ContentHandler m_handler;

    public PartContentHandler()
    {
        try
        {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            URI uri = new URI( "@METRO-CENTRAL-URI@" );
            ContentModel content = createContentModel();
            Repository repository = Transit.getInstance().getRepository();
            m_handler = (ContentHandler) repository.getPlugin( classloader, uri, new Object[]{ content } );
        }
        catch( Throwable e )
        {
            final String error =
              "Internal error while attempting to establish the composition part content handler.";
            throw new RuntimeException( error, e );
        }
    }

    public Object getContent( URLConnection connection ) throws IOException
    {
        return m_handler.getContent( connection );
    }

    public Object getContent( URLConnection connection, Class[] classes ) throws IOException
    {
        return m_handler.getContent( connection, classes );
    }

    private ContentModel createContentModel() throws Exception
    {
        String title = "Metro Part Handler.";
        String type = "part";
        Properties properties = new Properties();
        String dir = System.getProperty( "project.test.dir" );
        if( null != dir )
        {
            properties.put( "work.dir", dir );
        }
        Logger logger = new LoggingAdapter( "part" );
        return new DefaultContentModel( logger, null, type, title, properties );
    }
}

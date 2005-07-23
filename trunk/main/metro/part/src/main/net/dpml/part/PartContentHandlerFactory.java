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
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.util.Properties;

import net.dpml.transit.adapter.LoggingAdapter;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.model.DefaultContentModel;


/**
 * A utility class supporting the establishment of a part content handler factory.
 * This class is typically used in testcase as a convinience mechanism when dealing
 * with the establishment of component instances from parts.
 */
public class PartContentHandlerFactory implements ContentHandlerFactory
{
    public ContentHandler createContentHandler( String mimetype )
    {
        try
        {
            ContentModel content = createContentModel();
            return new PartContentHandler( content );
        }
        catch( Exception e )
        {
            System.err.println( "Exception while attempting to establish the default content handler." );
            e.printStackTrace();
            return null;
        }
    }

    private ContentModel createContentModel() throws Exception
    {
        String title = "Default Part Handler.";
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

    static
    {
        System.setProperty( 
          "java.util.logging.config.class", 
          System.getProperty( 
            "java.util.logging.config.class", 
            "net.dpml.transit.util.ConfigurationHandler" ) );
    }
}

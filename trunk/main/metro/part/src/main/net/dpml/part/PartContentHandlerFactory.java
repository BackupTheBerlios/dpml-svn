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

import net.dpml.transit.model.Logger;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.monitor.LoggingAdapter;


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
            Logger logger = resolveLogger( null );
            ContentModel content = newContentModel( logger );
            return new PartContentHandler( logger, content );
        }
        catch( Exception e )
        {
            System.err.println( "Exception while attempting to establish the default content handler." );
            e.printStackTrace();
            return null;
        }
    }

    public static ContentModel newContentModel() throws Exception
    {
        Logger logger = resolveLogger( null );
        return newContentModel( logger );
    }

    public static ContentModel newContentModel( Logger logger ) throws Exception
    {
        return newContentModel( logger, null );
    }

    public static ContentModel newContentModel( Logger logger, Properties properties ) throws Exception
    {
        String title = "Default Part Handler.";
        String type = "part";
        Logger log = resolveLogger( logger );
        Properties props = resolveProperties( properties );
        return new DefaultContentModel( log, null, type, title, props );
    }

    private static Properties resolveProperties( Properties properties )
    {
        if( null != properties )
        {
            return properties;
        }
        else
        {
            Properties props = new Properties();
            String dir = System.getProperty( "project.test.dir" );
            if( null != dir )
            {
                props.put( "work.dir", dir );
            }
            return props;
       }
    }

    private static Logger resolveLogger( Logger logger )
    {
        if( null != logger )
        {
            return logger;
        }
        else
        {
            return new LoggingAdapter( "part" );
        }
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

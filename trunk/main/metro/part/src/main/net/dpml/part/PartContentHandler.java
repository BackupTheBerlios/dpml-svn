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

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.net.URI;
import java.net.URL;
import java.net.ContentHandler;
import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.Date;

import net.dpml.transit.Transit;
import net.dpml.transit.Logger;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.monitor.LoggingAdapter;
import net.dpml.transit.Repository;

/**
 */
public class PartContentHandler extends ContentHandler
{
    public static final PartHandler STANDARD = newPartHandler( new LoggingAdapter( "" ) );
    
    public static PartHandler newPartHandler( final Logger logger )
    {
        if( null == logger )
        {
            throw new NullPointerException( "logger" );
        }
        try
        {
            ClassLoader classloader = Part.class.getClassLoader();
            URI uri = new URI( "@PART-HANDLER-URI@" );
            Repository repository = Transit.getInstance().getRepository();
            Class c = repository.getPluginClass( classloader, uri );
            Constructor constructor = c.getConstructor( new Class[]{Logger.class} );
            return (PartHandler) constructor.newInstance( new Object[]{logger} );
        }
        catch( Throwable e )
        {
            final String error =
              "Internal error while attempting to establish the default part handler.";
            throw new PartRuntimeException( error, e );
        }
    }
    
    private final Logger m_logger;
    private final PartHandler m_handler;

    public PartContentHandler( Logger logger )
    {
        m_logger = logger;
        m_handler = newPartHandler( logger );
    }

    public Object getContent( URLConnection connection ) throws IOException
    {
        return getContent( connection, new Class[0] );
    }

    public Object getContent( URLConnection connection, Class[] classes ) throws IOException
    {
        URL url = connection.getURL();   
        if( classes.length == 0 )
        {
            return getPart( url );
        }
        else
        {
            return m_handler.getContent( connection, classes );
        }
    }

    private Part getPart( URL url )
    {
        if( null == url )
        {
            throw new NullPointerException( "url" );
        }
        try
        {
            String path = url.toExternalForm();
            URI uri = new URI( path );
        
            return (Part) getPartHandler().loadPart( uri );
        }
        catch( PartRuntimeException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = 
              "Error occured while attempting to load part: " + url;
            throw new PartRuntimeException( error, e );
        }
    }
    
    public PartEditor getPartEditor( Part part ) throws Exception
    {
        return getPartHandler().loadPartEditor( part );
    }

   /**
    * Return the working part handler.
    * @return the part handler
    */
    public PartHandler getPartHandler()
    {
        return m_handler;
    }
}

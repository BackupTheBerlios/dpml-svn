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

package net.dpml.metro.part;

import java.net.URI;
import java.lang.reflect.Constructor;
import java.util.Date;

import net.dpml.transit.Transit;
import net.dpml.transit.Logger;
import net.dpml.transit.monitor.LoggingAdapter;
import net.dpml.transit.Repository;

/**
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartContentHandler // extends ContentHandler
{
   /**
    * Static reference to the default controller.
    */
    public static final Controller CONTROLLER = newController( new LoggingAdapter( "" ) );
    
   /**
    * Construct a controller.
    * @param logger the assigned logging channel
    * @return the controller
    */
    public static Controller newController( final Logger logger )
    {
        if( null == logger )
        {
            throw new NullPointerException( "logger" );
        }
        try
        {
            long now = new Date().getTime();
            ClassLoader classloader = Part.class.getClassLoader();
            URI uri = new URI( "@PART-HANDLER-URI@" );
            //System.out.println( "# LOADING: " + uri );
            Repository repository = Transit.getInstance().getRepository();
            Class c = repository.getPluginClass( classloader, uri );
            Constructor constructor = c.getConstructor( new Class[]{Logger.class} );
            Controller handler = (Controller) constructor.newInstance( new Object[]{logger} );
            //System.out.println( "# LOADED: " + ( new Date().getTime() - now ) ); 
            return handler;
        }
        catch( Throwable e )
        {
            final String error =
              "Internal error while attempting to establish the default part handler.";
            throw new RuntimeException( error, e );
        }
    }
    
    private final Logger m_logger;
    private final Controller m_handler;

   /**
    * Creation of a new <tt>PartContentHandler</tt>.
    * @param logger the assigned logging channel
    */
    public PartContentHandler( Logger logger )
    {
        m_logger = logger;
        m_handler = newController( logger );
    }

   /*
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
    */
    
   /**
    * Return the default part controller.
    * @return the part controller
    */
    public Controller getController()
    {
        return m_handler;
    }
}

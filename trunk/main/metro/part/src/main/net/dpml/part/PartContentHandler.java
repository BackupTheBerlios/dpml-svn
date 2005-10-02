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
    private final Logger m_logger;
    private final PartHandler m_handler;

    public PartContentHandler( Logger logger )
    {
        m_logger = logger;

        try
        {
            ClassLoader classloader = Part.class.getClassLoader();
            URI uri = new URI( "@COMPOSITION-CONTROLLER-URI@" );
            Repository repository = Transit.getInstance().getRepository();
            Class c = repository.getPluginClass( classloader, uri );
            Constructor constructor = c.getConstructor( new Class[]{Logger.class} );
            m_handler = (PartHandler) constructor.newInstance( new Object[]{logger} );
        }
        catch( Throwable e )
        {
            final String error =
              "Internal error while attempting to establish the composition controller.";
            throw new RuntimeException( error, e );
        }
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
        try
        {
            String path = url.toExternalForm();
            URI uri = new URI( path );
            return (Part) getPartHandler().loadPart( uri );
        }
        catch( Throwable e )
        {
            final String error = 
              "Error occured while attempting to load part: " + url;
            throw new PartHandlerRuntimeException( error, e );
        }
    }
    
    //public PartEditor getPartEditor( URI uri ) throws Exception
    //{
    //    Part part = getPartHandler().loadPart( uri );
    //    return getPartEditor( part );
    //}

    public PartEditor getPartEditor( Part part ) throws Exception
    {
        return getPartHandler().loadPartEditor( part );
    }

    /*
    public PartEditorFactory getPartEditorFactory( Class clazz ) throws Exception
    {
        String classname = clazz.getName();
        String factoryClassname = classname + "EditorFactory";
        try
        {
            Class factoryClass = clazz.getClassLoader().loadClass( factoryClassname );
            PartEditorFactory factory = 
              (PartEditorFactory) Transit.getInstance().getRepository().instantiate( 
                factoryClass, new Object[]{ m_logger } );
            return factory;
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
        catch( Throwable e )
        {
            final String error = 
              "Error occured while attempting to load a part editor factory for the class: " 
              + clazz.getName();
            throw new PartHandlerRuntimeException( error, e );
        }
    }
    */

    public PartHandler getPartHandler()
    {
        return m_handler;
    }

    public static PartHandler newPartHandler( Logger logger )
    {
        PartContentHandler handler = new PartContentHandler( logger );
        return handler.getPartHandler();
    }
}

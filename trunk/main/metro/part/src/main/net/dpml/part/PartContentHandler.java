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
import java.net.URL;
import java.net.ContentHandler;
import java.util.Properties;

import net.dpml.transit.Transit;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.model.DefaultContentModel;
import net.dpml.transit.monitor.LoggingAdapter;
import net.dpml.transit.repository.Repository;

import net.dpml.part.Part;
import net.dpml.part.component.Component;
import net.dpml.part.control.Controller;

/**
 */
public class PartContentHandler extends ContentHandler
{
    private final Controller m_controller;

    public PartContentHandler( ContentModel content )
    {
        try
        {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            URI uri = new URI( "@COMPOSITION-CONTROLLER-URI@" );
            Repository repository = Transit.getInstance().getRepository();
            m_controller = (Controller) repository.getPlugin( classloader, uri, new Object[]{ content } );
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
        return getContent( connection, new Class[0] );
    }

    public Object getContent( URLConnection connection, Class[] classes )
    {
        URL url = connection.getURL();   
        if( classes.length == 0 )
        {
            return getPart( url );
        }
        else
        {
            for( int i=0; i<classes.length; i++ )
            {
                Class c = classes[i];
                if( Part.class.isAssignableFrom( c ) )
                {
                    return getPart( url );
                }
                else if( Component.class.isAssignableFrom( c ) )
                {
                    try
                    {
                        return getComponent( url );
                    }
                    catch( Throwable e )
                    {
                        final String error = 
                          "Unable to load provider: " + url;
                        throw new RuntimeException( error, e );
                    }
                }
                else if( Object.class.equals( c ) )
                {
                    Component component = getComponent( url );
                    try
                    {
                        return component.resolve( false );
                    }
                    catch( Throwable e )
                    {
                        final String error = 
                          "Unable to load component: " + url;
                        throw new RuntimeException( error, e );
                    }
                }
                else
                {
                    final String error = 
                      "Unsupported class argument: " + c.getName();
                    throw new RuntimeException( error );
                }
            }
            return null;
        }
    }

    private Part getPart( URL url )
    {
        try
        {
            String path = url.toExternalForm();
            URI uri = new URI( path );
            return (Part) m_controller.loadPart( uri );
        }
        catch( Throwable e )
        {
            final String error = 
              "Error occured while attempting to load part: " + url;
            throw new RuntimeException( error, e );
        }
    }

    private Component getComponent( URL url )
    {
        try
        {
            String path = url.toExternalForm();
            URI uri = new URI( path );
            return m_controller.newComponent( uri );
        }
        catch( Throwable e )
        {
            final String error = 
              "Error occured while attempting to load component: " + url;
            throw new RuntimeException( error, e );
        }
    }
}

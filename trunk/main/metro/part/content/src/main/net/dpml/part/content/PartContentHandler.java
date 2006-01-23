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

package net.dpml.part.content;

import java.net.URI;
import java.net.ContentHandler;
import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;

import net.dpml.transit.Logger;

import net.dpml.part.Directive;
import net.dpml.part.local.Controller;
import net.dpml.part.local.InitialContext;
import net.dpml.part.remote.Model;
import net.dpml.part.remote.Component;
import net.dpml.part.remote.Provider;

/**
 * URL content handler support the part datatype.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartContentHandler extends ContentHandler
{
    private static final Controller CONTROLLER = Controller.STANDARD;

   /**
    * Return the content form the url connection.
    * @param connection the url connection
    * @return the content
    * @exception IOException if an IO error occurs
    */
    public Object getContent( URLConnection connection ) throws IOException
    {
        return getContent( connection, new Class[0] );
    }

   /**
    * Return the content form the url connection.
    * @param connection the url connection
    * @param classes the content type selection classes
    * @return the content
    * @exception IOException if an IO error occurs
    */
    public Object getContent( URLConnection connection, Class[] classes ) throws IOException
    {
        return getPartContent( connection, classes );
    }
    
    private Object getPartContent( URLConnection connection, Class[] classes ) throws IOException
    {
        URL url = connection.getURL();
        try
        {
            String path = url.toExternalForm();
            URI uri = new URI( path );
            for( int i=0; i<classes.length; i++ )
            {
                Class c = classes[i];
                if( Directive.class.isAssignableFrom( c ) )
                {
                    return CONTROLLER.loadDirective( uri );
                }
                else if( Model.class.isAssignableFrom( c ) )
                {
                    return CONTROLLER.createModel( uri );
                }
                else if( Component.class.isAssignableFrom( c ) )
                {
                    return CONTROLLER.createComponent( uri );
                }
                else if( Provider.class.isAssignableFrom( c ) )
                {
                    Component component = CONTROLLER.createComponent( uri );
                    return component.getProvider();
                }
            }
            Component component = CONTROLLER.createComponent( uri );
            Provider provider = component.getProvider();
            return provider.getValue( false );
        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to establish content for: " + url;
            IOException cause = new IOException( error );
            cause.initCause( e );
            throw cause;
        }
    }
}

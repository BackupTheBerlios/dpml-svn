/* 
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.composition.runtime;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.logging.Logger;

import net.dpml.composition.control.CompositionController;

import net.dpml.configuration.Configuration;

import net.dpml.parameters.Parameters;

import net.dpml.part.component.Component;


/**
 * A lifestyle handler provides support for the aquisition and release
 * of component instances.  An implementation is responsible for the  
 * handling of new instance creation based on lifestyle policy declared
 * in a component model.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: LifestyleManager.java 259 2004-10-30 07:24:40Z mcconnell $
 */
class LoggingHandler
{
   /**
    * Return a log channel for a supplied model.
    * @param model the composition model
    * @return the logging channel
    */
    java.util.logging.Logger getJavaLogger( ComponentHandler component )
    {
        return getJavaLogger( component, null );
    }

   /**
    * Return a named log channel for a supplied model.
    * @param model the composition model
    * @param category the logging category name
    * @return the logging channel
    */
    java.util.logging.Logger getJavaLogger( ComponentHandler component, String category )
    {
        URI uri = component.getLocalURI();
        String path = uri.getSchemeSpecificPart();
        String channel = path.replace( '/', '.' );
        if( category != null )
        {
            channel = channel + "." + category.replace( '/', '.' );
        }
        if( ".".equals( channel ) )
        {
            return Logger.getLogger( "" );
        }
        else
        {
            return Logger.getLogger( channel );
        }
    }

    net.dpml.logging.Logger getLoggingChannel( ComponentHandler component )
    {
        URI uri = component.getLocalURI();
        String path = uri.getSchemeSpecificPart();
        if( path.endsWith( "/" ) )
        {
            path = path.substring( 0, path.length() - 1 );
        }
        path = path.replace( '/', '.' );
        path = trim( path );
        return new DefaultLogger( path );
    }

    private String trim( String path )
    {
        if( path.startsWith( "." ) )
        {
            return trim( path.substring( 1 ) );
        }
        else if( ".".equals( path ) )
        {
            return "";
        }
        else
        {
            return path;
        }
    }
}

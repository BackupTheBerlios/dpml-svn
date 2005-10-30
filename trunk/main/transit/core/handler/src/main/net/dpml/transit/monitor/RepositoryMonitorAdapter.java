/*
 * Copyright 2004 Niclas Hedhman.
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

package net.dpml.transit.monitor;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Adapts repository service monitor events to a logging channel.
 */
public class RepositoryMonitorAdapter extends AbstractAdapter
    implements RepositoryMonitor
{ 
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new repository monito to logging adapter.
    * @param adapter the logging adapter
    */
    public RepositoryMonitorAdapter( Adapter adapter )
    {
        super( adapter );
    }

    // ------------------------------------------------------------------------
    // RepositoryMonitor
    // ------------------------------------------------------------------------

   /**
    * Handle notification of an information message.
    * @param info the message
    */
    public void sequenceInfo( String info )
    {
        if( getAdapter().isDebugEnabled() )
        {
            getAdapter().debug( info );
        }
    }

   /**
    * Handle notification of a request for the establishment of a plugin.
    * @param parent the parent classloader
    * @param uri the requested plugin uri
    * @param args the supplied constructor arguments
    */
    public void getPluginRequested( ClassLoader parent, URI uri, Object[] args )
    {
        if( getAdapter().isDebugEnabled() )
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append( "loading plugin " );
            if( args.length == 1 )
            {
                buffer.append( " with a single argument"  );
            }
            else if( args.length > 1 )
            {
                buffer.append( " with " + args.length );             
                buffer.append( " arguments" );
            }
            buffer.append( "\nuri: " + uri.toString() );
            getAdapter().debug( buffer.toString() );
        }
    }

   /**
    * Handle notification of the establishment of a plugin class.
    * @param clazz the plugin class
    */
    public void establishedPluginClass( Class clazz )
    {
        if( getAdapter().isDebugEnabled() )
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append( "loaded plugin class: " );
            buffer.append( clazz.getName() );
            getAdapter().debug( buffer.toString() );
        }
    }

   /**
    * Handle notification of an exception related to plugin establishment.
    * @param method the method raising the exception
    * @param e the causal exception
    */
    public void exceptionOccurred( String method, Exception e )
    {
        if( getAdapter().isErrorEnabled() )
        {
            String error = "Respostory error at : " + method;
            getAdapter().error( error, e );
        }
    }

   /**
    * Handle notification of the discovery of a plugin constructor.
    * @param constructor the constructor
    * @param args the constructor args
    */
    public void pluginConstructorFound( Constructor constructor, Object[] args )
    {
        if( getAdapter().isDebugEnabled() )
        {
            Class[] classes = constructor.getParameterTypes();
            if( classes.length == 0 )
            {
                getAdapter().debug( "plugin class contains a null constructor" );
            }
            else if( classes.length == 1 )
            {
                getAdapter().debug( 
                  "plugin class contains a single constructor parameter" );
            }
            else
            {
                getAdapter().debug( 
                  "plugin class contains " 
                  + classes.length 
                  + " constructor parameters" );
            }
        }
    }

   /**
    * Handle notification of the instantiation of a plugin.
    * @param plugin the plugin instance
    */
    public void pluginInstantiated( Object plugin )
    {
        if( getAdapter().isDebugEnabled() )
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append( "plugin: " );
            buffer.append( plugin.getClass().getName() );
            buffer.append( "#" );
            buffer.append( System.identityHashCode( plugin ) );
            getAdapter().debug( buffer.toString() );
        }
    }

   /**
    * Handle notification of the creation of a new classloader.
    * @param type the type of classloader (api, spi or impl)
    * @param classloader the new classloader 
    */
    public void classloaderConstructed( String type, ClassLoader classloader )
    {
        if( getAdapter().isDebugEnabled() )
        {
            int id = System.identityHashCode( classloader );
            StringBuffer buffer = new StringBuffer();
            buffer.append( "created " );
            buffer.append( type );
            buffer.append( " classloader: " + id );
            if( classloader instanceof URLClassLoader )
            {
                URLClassLoader loader = (URLClassLoader) classloader;
                URL[] urls = loader.getURLs();
                for( int i=0; i < urls.length; i++ )
                {
                    URL url = urls[i];
                    buffer.append( "\n(" + i + ") " + url.toString() );
                }
            }
            getAdapter().debug( buffer.toString() );
        }
    }
}

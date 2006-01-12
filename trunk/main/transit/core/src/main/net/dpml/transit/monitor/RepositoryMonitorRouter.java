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

import java.net.URI;
import java.net.URL;
import java.lang.reflect.Constructor;
 
/**
 * A repository monitor router handles mutlicast distribution of monitor events to 
 * a set of subscribed monitors.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class RepositoryMonitorRouter extends AbstractMonitorRouter
    implements RepositoryMonitor, Router
{   
    //--------------------------------------------------------------------
    // RepositoryMonitor
    //--------------------------------------------------------------------

   /**
    * Notify all subscribed monitors of a info message event.
    * @param info the information message
    */ 
    public void sequenceInfo( String info )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            RepositoryMonitor monitor = (RepositoryMonitor) monitors[i];
            monitor.sequenceInfo( info );
        }
    }
    
   /**
    * Notify all monitors of a request for the establishment of a plugin.
    * @param parent the parent classloader
    * @param uri the requested plugin uri
    * @param args the supplied constructor arguments
    */
    public void getPluginRequested( ClassLoader parent, URI uri, Object[] args )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            RepositoryMonitor monitor = (RepositoryMonitor) monitors[i];
            monitor.getPluginRequested( parent, uri, args );
        }
    }
    
   /**
    * Notify all monitorrs of the establishment of a plugin class.
    * @param pluginClass the plugin class
    */
    public void establishedPluginClass( Class pluginClass )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            RepositoryMonitor monitor = (RepositoryMonitor) monitors[i];
            monitor.establishedPluginClass( pluginClass );
        }
    }
    
   /**
    * Notify all monitors of an exception related to plugin establishment.
    * @param methodname the method raising the exception
    * @param e the causal exception
    */
    public void exceptionOccurred( String methodname, Exception e )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            RepositoryMonitor monitor = (RepositoryMonitor) monitors[i];
            monitor.exceptionOccurred( methodname, e );
        }
    }
    
   /**
    * Notify all monitors of the discovery of a plugin constructor.
    * @param constructor the constructor
    * @param args the constructor args
    */
    public void pluginConstructorFound( Constructor constructor, Object[] args )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            RepositoryMonitor monitor = (RepositoryMonitor) monitors[i];
            monitor.pluginConstructorFound( constructor, args );
        }
    }
    
   /**
    * Notify all monitors of the instantiation of a plugin.
    * @param pluginInstance the plugin instance
    */
    public void pluginInstantiated( Object pluginInstance )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            RepositoryMonitor monitor = (RepositoryMonitor) monitors[i];
            monitor.pluginInstantiated( pluginInstance );
        }
    }

   /**
    * Notify all monitors of the creation of a new classloader.
    * @param type the type of classloader (api, spi or impl)
    * @param classloader the new classloader 
    */    
    public void classloaderConstructed( String type, ClassLoader classloader )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            RepositoryMonitor monitor = (RepositoryMonitor) monitors[i];
            monitor.classloaderConstructed( type, classloader );
        }
    }

   /**
    * Handle notification of system classloader expansion.
    * @param plugin the uri of the plugin requesting system classloader expansion
    * @param urls the array of urls added to the system classloader
    */
    public void systemExpanded( URI plugin, URL[] urls )
    {
        Monitor[] monitors = getMonitors();
        for( int i=0; i < monitors.length; i++ )
        {
            RepositoryMonitor monitor = (RepositoryMonitor) monitors[i];
            monitor.systemExpanded( plugin, urls );
        }
    }

   /**
    * Add a monitor to the set of monitors managed by this router.
    * @param monitor the monitor to add
    * @exception IllegalArgumentException if the supplied monitor does not
    *   implement the RepositoryMonitor interface
    */
    public void addMonitor( Monitor monitor ) throws IllegalArgumentException
    {
        if( !( monitor instanceof RepositoryMonitor ) )
        {
            throw new IllegalArgumentException( "monitor must be RepositoryMonitor type." );
        }
        else
        {
            super.addMonitor( monitor );
        }
    }
}
 

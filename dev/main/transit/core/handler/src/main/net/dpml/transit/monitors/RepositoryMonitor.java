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

package net.dpml.transit.monitors;

import java.net.URI;

import java.lang.reflect.Constructor;
 
/**
 * Defintion of a repository monitor.
 */
public interface RepositoryMonitor extends Monitor
{
   /**
    * Handle notification of an information message.
    * @param info the message
    */
    void sequenceInfo( String info );
    
   /**
    * Handle notification of a request for the establishment of a plugin.
    * @param parent the parent classloader
    * @param uri the requested plugin uri
    * @param args the supplied constructor arguments
    */
    void getPluginRequested( ClassLoader parent, URI uri, Object[] args );
    
   /**
    * Handle notification of the establishment of a plugin class.
    * @param pluginClass the plugin class
    */
    void establishedPluginClass( Class pluginClass );
    
   /**
    * Handle notification of an exception related to plugin establishment.
    * @param methodname the method raising the exception
    * @param e the causal exception
    */
    void exceptionOccurred( String methodname, Exception e );
    
   /**
    * Handle notification of the discovery of a plugin constructor.
    * @param constructor the constructor
    * @param args the constructor args
    */
    void pluginConstructorFound( Constructor constructor, Object[] args );
    
   /**
    * Handle notification of the instantiation of a plugin.
    * @param pluginInstance the plugin instance
    */
    void pluginInstantiated( Object pluginInstance );
    
   /**
    * Handle notification of the creation of a new classloader.
    * @param type the type of classloader (api, spi or impl)
    * @param classloader the new classloader 
    */
    void classloaderConstructed( String type, ClassLoader classloader );
}
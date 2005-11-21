/*
 * Copyright 2004-2005 Stephen J. McConnell.
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
package net.dpml.transit;

import java.io.IOException;
import java.lang.reflect.Constructor;

import java.net.URI;

/**
 * A service that provides support for the establishment of classloaders and plugin
 * instances based on plugin descriptor describing a classloader chain.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Repository
{
   /**
    * Creates a plugin descriptor from an artifact.
    *
    * @param uri the artifact reference to the plugin descriptor
    * @return the plugin descriptor
    * @exception IOException if a factory creation error occurs
    */
    Plugin getPluginDescriptor( URI uri )
      throws IOException;

   /**
    * Get a plugin classloader relative to a supplied uri.
    *
    * @param parent the parent classloader
    * @param uri the plugin uri
    * @return the plugin classloader
    * @exception IOException if plugin loading exception occurs
    */
    ClassLoader getPluginClassLoader( ClassLoader parent, URI uri )
        throws IOException;

   /**
    * Get a plugin class relative to a supplied uri.
    *
    * @param parent the parent classloader
    * @param uri the plugin uri
    * @return the plugin class
    * @exception IOException if plugin loading exception occurs
    */
    Class getPluginClass( ClassLoader parent, URI uri )
        throws IOException;

   /**
    * Creates a plugin from a uri reference.
    *
    * @param parent the parent classloader
    * @param uri the reference to the application
    * @param args instantiation arguments
    * @return the plugin instance
    * @exception IOException if plugin loading exception occurs
    */
    Object getPlugin( ClassLoader parent, URI uri, Object[] args  )
        throws IOException;

   /**
    * Instantiate an instance of a class using the supplied array of constructor 
    * parameter arguments.  Arguments will be evaluated in the order supplied 
    * for each of the parameters declared by a single public constructor of the 
    * supplied class.  If a parameter cannot be resolved from supplied arguments
    * and the parameter is a class with a single public constructor and implementation
    * shall attempt to instantiate an instance of that class via a recursive 
    * invocation of this method.
    *
    * @param clazz the class of the object to instantiate
    * @param params a priority ordered array of instances values to be used in
    *    constructor parameter value assignment
    * @return the instanciated object
    * @exception Exception if an instantiation error occurs
    */
    Object instantiate( Class clazz, Object[] params ) throws Exception;

   /**
    * Instantiate an instance using the supplied constructor and array of constructor 
    * parameter arguments.  
    *
    * @param constructor the class constructor
    * @param params a priority ordered array of instances values to be used in
    *    constructor parameter value assignment
    * @return the instanciated object
    * @exception Exception if an instantiation error occurs
    */
    Object instantiate( Constructor constructor, Object[] params ) throws Exception;

}

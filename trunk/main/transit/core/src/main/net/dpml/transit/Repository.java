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
import java.lang.reflect.InvocationTargetException;

import java.net.URI;

//import net.dpml.lang.Plugin;
import net.dpml.lang.Classpath;

import net.dpml.part.Part;

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
    Part getPart( URI uri ) throws IOException;

   /**
    * Get a part classloader relative to a supplied uri.
    *
    * @param uri the part uri
    * @return the plugin classloader
    * @exception IOException if plugin loading exception occurs
    */
    ClassLoader getPluginClassLoader( URI uri ) throws IOException;

   /**
    * Get a plugin class relative to a supplied uri.
    *
    * @param uri the part uri
    * @return the part class
    * @exception IOException if plugin loading exception occurs
    */
    Class getPluginClass( URI uri ) throws IOException;

   /**
    * Creates an object from a uri reference.
    *
    * @param uri the reference to the application
    * @param args instantiation arguments
    * @return the plugin instance
    * @exception IOException if plugin loading exception occurs
    * @exception InvocationTargetException if the plugin constructor invocation error occurs
    */
    Object getPlugin( URI uri, Object[] args  ) throws IOException, InvocationTargetException;
}

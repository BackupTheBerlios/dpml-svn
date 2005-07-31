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

package net.dpml.depot;

import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;
import java.net.MalformedURLException;
import java.net.URL;

import net.dpml.transit.model.Logger;
import net.dpml.transit.monitor.LoggingAdapter;

/**
 * The DepotRMIClassLoaderSpi handles the loading of classes that are based on 
 * plugin artifact types.
 */
public class DepotRMIClassLoaderSpi extends RMIClassLoaderSpi
{
     private RMIClassLoaderSpi m_delegate = 
       RMIClassLoader.getDefaultProviderInstance();

     private Logger m_logger;

     public DepotRMIClassLoaderSpi()
     {
         super();
         m_logger = setupLogger();
         m_logger.debug( "classloader spi established" );
     }

    /**
     * Provides the implementation for
     * {@link RMIClassLoader#loadClass(URL,String)},
     * {@link RMIClassLoader#loadClass(String,String)}, and
     * {@link RMIClassLoader#loadClass(String,String,ClassLoader)}.
     *
     * Loads a class from a codebase URL path, optionally using the
     * supplied loader.
     *
     * @param codebase the list of URLs (separated by spaces) to load
     *     the class from, or <code>null</code>
     * @param name the name of the class to load
     * @param defaultLoader additional contextual class loader
     *     to use, or <code>null</code>
     * @return	the <code>Class</code> object representing the loaded class
     * @throws MalformedURLException if <code>codebase</code> is
     *     non-<code>null</code> and contains an invalid URL, or
     *     if <code>codebase</code> is <code>null</code> and the system
     *     property <code>java.rmi.server.codebase</code> contains an
     *     invalid URL
     * @throws ClassNotFoundException if a definition for the class
     *     could not be found at the specified location
     */
    public Class loadClass(String codebase, String name,
				    ClassLoader defaultLoader)
	throws MalformedURLException, ClassNotFoundException
    {
        if( null != codebase )
        {
            final String message = 
              "Loading class: " + name 
              + "\nCodebase: " + codebase;
            getLogger().debug( message );
        }
        return m_delegate.loadClass( codebase, name, defaultLoader );
    }
    
    /**
     * Provides the implementation for
     * {@link RMIClassLoader#loadProxyClass(String,String[],ClassLoader)}.
     *
     * Loads a dynamic proxy class (see {@link java.lang.reflect.Proxy}
     * that implements a set of interfaces with the given names
     * from a codebase URL path, optionally using the supplied loader.
     * 
     * <p>An implementation of this method must either return a proxy
     * class that implements the named interfaces or throw an exception.
     *
     * @param	codebase the list of URLs (space-separated) to load
     * classes from, or <code>null</code>
     *
     * @param	interfaces the names of the interfaces for the proxy class
     * to implement
     *
     * @return	a dynamic proxy class that implements the named interfaces
     *
     * @param	defaultLoader additional contextual class loader
     * to use, or <code>null</code>
     *
     * @throws	MalformedURLException if <code>codebase</code> is
     * non-<code>null</code> and contains an invalid URL, or
     * if <code>codebase</code> is <code>null</code> and the system
     * property <code>java.rmi.server.codebase</code> contains an
     * invalid URL
     *
     * @throws	ClassNotFoundException if a definition for one of
     * the named interfaces could not be found at the specified location,
     * or if creation of the dynamic proxy class failed (such as if
     * {@link java.lang.reflect.Proxy#getProxyClass(ClassLoader,Class[])}
     * would throw an <code>IllegalArgumentException</code> for the given
     * interface list)
     */
    public Class loadProxyClass(String codebase, String[] interfaces,
					 ClassLoader defaultLoader)
	throws MalformedURLException, ClassNotFoundException
    {
        if( null != codebase )
        {
            getLogger().debug( "Loading proxy: " + codebase );
        }
        return m_delegate.loadProxyClass( codebase, interfaces, defaultLoader );
    }

    /**
     * Provides the implementation for
     * {@link RMIClassLoader#getClassLoader(String)}.
     *
     * Returns a class loader that loads classes from the given codebase
     * URL path.
     *
     * <p>If there is a security manger, its <code>checkPermission</code>
     * method will be invoked with a
     * <code>RuntimePermission("getClassLoader")</code> permission;
     * this could result in a <code>SecurityException</code>.
     * The implementation of this method may also perform further security
     * checks to verify that the calling context has permission to connect
     * to all of the URLs in the codebase URL path.
     *
     * @param	codebase the list of URLs (space-separated) from which
     * the returned class loader will load classes from, or <code>null</code>
     *
     * @return a class loader that loads classes from the given codebase URL
     * path
     *
     * @throws	MalformedURLException if <code>codebase</code> is
     * non-<code>null</code> and contains an invalid URL, or
     * if <code>codebase</code> is <code>null</code> and the system
     * property <code>java.rmi.server.codebase</code> contains an
     * invalid URL
     *
     * @throws	SecurityException if there is a security manager and the
     * invocation of its <code>checkPermission</code> method fails, or
     * if the caller does not have permission to connect to all of the
     * URLs in the codebase URL path
     */
    public ClassLoader getClassLoader(String codebase)
	throws MalformedURLException
    {
        if( null != codebase )
        {
            getLogger().debug( "getClassLoader: " + codebase );
        }
        return m_delegate.getClassLoader( codebase );
    }

    /**
     * Provides the implementation for
     * {@link RMIClassLoader#getClassAnnotation(Class)}.
     *
     * Returns the annotation string (representing a location for
     * the class definition) that RMI will use to annotate the class
     * descriptor when marshalling objects of the given class.
     *
     * @param	cl the class to obtain the annotation for
     *
     * @return	a string to be used to annotate the given class when
     * it gets marshalled, or <code>null</code>
     *
     * @throws	NullPointerException if <code>cl</code> is <code>null</code>
     */
    public String getClassAnnotation(Class cl)
    {
        return m_delegate.getClassAnnotation( cl );
    }

    private Logger getLogger()
    {
        return m_logger;
    }

    private static Logger setupLogger()
    {
        return new LoggingAdapter( java.util.logging.Logger.getLogger( "depot" ) );
    }
}

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
import java.beans.Expression;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;

import net.dpml.transit.monitor.RepositoryMonitorRouter;

import net.dpml.lang.Classpath;
import net.dpml.lang.Category;
import net.dpml.lang.Logger;

import net.dpml.part.Part;
import net.dpml.part.Plugin;

/**
 * Utility class supporting downloading of resources based on
 * artifact references.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class StandardLoader implements Repository
{
    private final Logger m_logger;
    
   /**
    * Creation of a new repository handler.
    * @param logger the assigned logging channel
    */
    StandardLoader( Logger logger )
    {
        m_logger = logger;
    }

    // ------------------------------------------------------------------------
    // Repository
    // ------------------------------------------------------------------------
    
   /**
    * Creates a plugin descriptor from an artifact.
    *
    * @param uri the artifact reference to the plugin descriptor
    * @return the plugin descriptor
    * @exception IOException if a factory creation error occurs
    */
    public Part getPart( URI uri ) throws IOException
    {
        try
        {
            return Part.load( uri );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error during part resolve."
              + "\nURI: " + uri;
            throw new RepositoryException( error, e );
        }
    }
    
   /**
    * Get a plugin classloader relative to a supplied uri.
    *
    * @param uri the plugin uri
    * @return the plugin classloader.
    * @exception IOException if plugin loading exception occurs.
    * @exception NullArgumentException if the supplied uri or parent classloader
    *            is null.
    */
    public ClassLoader getPluginClassLoader( URI uri )
        throws IOException, NullArgumentException
    {
        Part part = getPart( uri );
        return part.getClassLoader();
    }
    
   /**
    * Get a plugin class relative to a supplied artifact.  The artifact uri
    * must refer to a plugin descriptor (i.e. the artifact type is "plugin").
    * The class returned will be the class named in the plugin descriptor.
    *
    * @param uri the plugin artifact
    * @return the plugin class
    * @exception IOException if a class resolution error occurs
    */
    public Class getPluginClass( URI uri )
       throws IOException
    {
        Part part = Part.load( uri );
        if( part instanceof Plugin )
        {
            try
            {
                Plugin plugin = (Plugin) part;
                return plugin.getPluginClass();
            }
            catch( Exception e )
            {
                final String error =
                  "Internal error while attempting to load plugin class.";
                throw new RepositoryException( error, e );
            }
        }
        else
        {
            final String error = 
              "Part uri is not a plugin."
              + "\nPart: " + part.getClass().getName()
              + "\nURI: " + uri;
            throw new RepositoryException( error );
        }
    }
    
   /**
    * Instantiate an object using a plugin uri, parent classloader and 
    * a supplied argument array. The plugin uri is used to resolve a plugin
    * descriptor.  A classloader stack will be constructed using the supplied
    * classloader as the anchor for stack construction. A classname declared
    * in the plugin descriptor must has a single public constructor.  The 
    * implementation will resolve constructor parameters relative to the 
    * supplied argument array and return an instance of the class.
    *
    * @param uri the reference to the application
    * @param args commandline arguments
    * @return the plugin instance
    * @exception IOException if a plugin creation error occurs
    * @exception InvocationTargetException if the plugin constructor invocation error occurs
    */
    public Object getPlugin( URI uri, Object[] args  )
      throws IOException, InvocationTargetException
    {
        Part part = Part.load( uri );
        try
        {
            return part.instantiate( args );
        }
        catch( Exception e )
        {
            final String error =
              "Internal error while attempting to load plugin instance.";
            throw new RepositoryException( error, e );
        }
    }
    
    //---------------------------------------------------------------------
    // implementation
    //---------------------------------------------------------------------

    private RepositoryMonitorRouter getMonitor()
    {
        return Transit.getInstance().getRepositoryMonitorRouter();
    }

    private Logger getLogger()
    {
        return m_logger;
    }
}

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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import net.dpml.transit.StandardClassLoader;
import net.dpml.transit.util.PropertyResolver;
import net.dpml.transit.Plugin;

/**
 * The DepotClassLoader is a URLClassLoader that supports late binding of 
 * URLs.  This class is used by the Depot CLI scripts as the system
 * classloader.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: Main.java 2480 2005-05-10 04:44:32Z mcconnell@dpml.net $
 */
public final class DepotClassLoader extends StandardClassLoader 
{
    private boolean m_sealed = false;

   /**
    * Creation of a new Depot classloader.
    *
    * @param parent the parent classloader
    */
    public DepotClassLoader( ClassLoader parent )
    {
        super( BOOTSTRAP, Plugin.SYSTEM, new URL[0], parent );
    }

   /**
    * Set the classpath of the classloader.
    * @param classpath an array of Transit uris
    * @exception IOException if a classloader element could not be converted to a vaid URL
    * @exception IllegalStateException if the classpath has already been set
    */
    public void setClasspath( URI[] classpath ) throws IOException, IllegalStateException
    {
        if( m_sealed )
        {
            final String error = 
              "Illegal attempt to modify the bootstrap classpath.";
            throw new IllegalStateException( error );
        }
        for( int i=0; i < classpath.length; i++ )
        {
            URI uri = classpath[i];
            URL url = uri.toURL();
            addURL( url );
        }
        m_sealed = true;
    }

    private static final URI createBootstrapLabel()
    {
        try
        {
           return new URI( "system:depot" );
        }
        catch( Exception e )
        {
            return null;
        }
    }

    private static URI BOOTSTRAP = createBootstrapLabel();
}


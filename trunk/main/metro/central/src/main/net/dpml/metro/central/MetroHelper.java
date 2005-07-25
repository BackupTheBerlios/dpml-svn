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

package net.dpml.metro.central;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.Date;

import net.dpml.composition.control.CompositionController;
import net.dpml.composition.control.CompositionControllerContext;

import net.dpml.part.control.Controller;
import net.dpml.part.control.ControllerContext;

import net.dpml.transit.model.Logger;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.model.DefaultContentModel;
import net.dpml.transit.monitor.LoggingAdapter;
import net.dpml.transit.Repository;

/**
 * A utility class used for the deployment of components in embedded scenarios
 * includuing but not limited to test-cases.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class MetroHelper
{
    //------------------------------------------------------------------
    // static
    //------------------------------------------------------------------

    static
    {
        System.setProperty( 
          "java.util.logging.config.class", 
          System.getProperty( 
            "java.util.logging.config.class", 
            "net.dpml.transit.util.ConfigurationHandler" ) );
    }

    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

    private CompositionController m_controller;

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

    public MetroHelper() throws Exception
    {
        this( new LoggingAdapter( "metro" ) );
    }

    public MetroHelper( Logger logger ) throws Exception
    {
        String title = "Metro Part Handler.";
        String type = "part";
        Properties properties = new Properties();
        String dir = System.getProperty( "project.test.dir" );
        if( null != dir )
        {
            properties.put( "work.dir", dir );
        }
        DefaultContentModel model = new DefaultContentModel( logger, null, type, title, properties );
        m_controller = new CompositionController( model );
    }

    public MetroHelper( ContentModel model ) throws Exception
    {
        if( null == model )
        {
            throw new NullPointerException( "model" );
        }
        m_controller = new CompositionController( model );
    }

    //------------------------------------------------------------------
    // internal
    //------------------------------------------------------------------

    public Controller getController()
    {
        return m_controller;
    }

    public URI toURI( String path )
    {
        try
        {
            File base = m_controller.getControllerContext().getWorkingDirectory();
            File target = new File( base, path );
            return target.toURI();
        }
        catch( Throwable e )
        {
            return null;
        }
    }

    public void dispose()
    {
        System.gc();
    }

    private static URI createStaticURI( String path )
    {
        try
        {
            return new URI( path );
        }
        catch( Throwable e )
        {
            return null;
        }
    }

    //------------------------------------------------------------------
    // static
    //------------------------------------------------------------------

    public static File getTestWorkingDir()
    {
        String dir = System.getProperty( "project.test.dir" );
        if( null == dir )
        {
            final String error = 
              "System property 'project.test.dir' is undefined.";
            throw new IllegalStateException( error );
        }

        try
        {
            return new File( dir ).getCanonicalFile();
        } 
        catch( IOException e )
        {
            return new File( dir );
        }
    }
}

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

package net.dpml.magic;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileFilter;
import java.net.URI;
import java.net.URL;

import net.dpml.transit.Artifact;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.TransitRegistryModel;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.util.StreamUtils;

/**
 * Installer for the Magic build system.  Handles registration of a transit development
 * profile and updating of the user's ${user.home}/.ant/lib environment.
 */
public class MagicInstaller
{
    private Logger m_logger;

   /**
    * Creation of a new installer instance.
    * @param home the transit registry
    * @param install evalues to TRUE if this is an install othwise it's a deinstall
    * @param logger the assigned logging channel
    * @exception Exception if an error occurs
    */
    public MagicInstaller( TransitRegistryModel home, boolean install, Logger logger ) throws Exception
    {
        m_logger = logger;

        if( install )
        {
            getLogger().info( "checking ${user.home}/.ant/lib" );
            File user = new File( System.getProperty( "user.home" ) );
            File ant = new File( user, ".ant" );
            File lib = new File( ant, "lib" );
            checkAntLib( lib );
            purgeAntLib( lib );
            updateAntLib( lib );
        }
    }

   /**
    * Return the assigned logging channel.
    * @return the logging channel
    */
    private Logger getLogger()
    {
        return m_logger;
    }

   /**
    * Check that supplied ant lib dir exist creating if if necessary.
    * @param file the ${user.home}/.ant/lib directory
    */
    private void checkAntLib( File file )
    {
        if( !file.exists() )
        {
            getLogger().debug( "  creating " + file );
            file.mkdirs();
        }
    }

   /**
    * Remove any old version of DPML files from the antlib directory.
    * @param file the ${user.home}/.ant/lib directory
    */
    private void purgeAntLib( File file )
    {
        File[] files = file.listFiles( new DpmlFileFilter() );
        for( int i=0; i < files.length; i++ )
        {
            File f = files[i];
            getLogger().debug( "removing old file ${user.home}/.ant/lib/" + f.getName() );
            f.delete();
        }
    }

   /**
    * Update the antlib directory with JUnit, Transit main and Transit tools.
    * @param file the ${user.home}/.ant/lib directory
    */
    private void updateAntLib( File lib )
    {
        updateJUnit( lib );
        updateTransitMain( lib );
        updateTransitTools( lib );
    }

   /**
    * Update the antlib directory with JUnit.
    * @param file the ${user.home}/.ant/lib directory
    */
    private void updateJUnit( File lib )
    {
        try
        {
            URI unit = new URI( JUNIT_PATH );
            copyInto( lib, unit );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while attempting to update the Junit jar file."
              + "\nSource URI: " + JUNIT_PATH;
            throw new RuntimeException( error, e );
        }
    }

   /**
    * Update the antlib directory with Transit main.
    * @param file the ${user.home}/.ant/lib directory
    */
    private void updateTransitMain( File lib )
    {
        try
        {
            URI uri = new URI( TRANSIT_MAIN_PATH );
            copyInto( lib, uri );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while attempting to update the Transit Main jar file."
              + "\nSource URI: " + TRANSIT_MAIN_PATH;
            throw new RuntimeException( error, e );
        }
    }

   /**
    * Update the antlib directory with Transit tools.
    * @param file the ${user.home}/.ant/lib directory
    */
    private void updateTransitTools( File lib )
    {
        try
        {
            URI uri = new URI( TRANSIT_TOOLS_PATH );
            copyInto( lib, uri );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while attempting to update the Transit Tools jar file."
              + "\nSource URI: " + TRANSIT_TOOLS_PATH;
            throw new RuntimeException( error, e );
        }
    }

   /**
    * Internal class used for DPML content filtering.
    */
    private static class DpmlFileFilter implements FileFilter
    {
       /**
        * Return true if the supplied file matchines the dpml-transit filter criteria.
        * @return TRUE if the filter matches
        */
        public boolean accept( File file )
        {
            return file.getName().startsWith( "dpml-transit-" );
        }
    }

   /**
    * Utility to copy a artifact to a directory.
    * @param lib the target directory
    * @param source the source uri
    */
    private void copyInto( File lib, URI source ) throws IOException
    {
        Artifact artifact = Artifact.createArtifact( source );
        String name = artifact.getName();
        String type = artifact.getType();
        URL url = artifact.toURL();
        InputStream input = url.openStream();
        String filename = name + "." + type;
        getLogger().debug( "adding file ${user.home}/.ant/lib/" + filename );
        File destination = new File( lib, filename );
        FileOutputStream output = new FileOutputStream( destination );
        StreamUtils.copyStream( input, output, true );
    }

    private static final String JUNIT_PATH = "@JUNIT-URI@";
    private static final String TRANSIT_MAIN_PATH = "@TRANSIT-MAIN-URI@";
    private static final String TRANSIT_TOOLS_PATH = "@TRANSIT-TOOLS-URI@";

}

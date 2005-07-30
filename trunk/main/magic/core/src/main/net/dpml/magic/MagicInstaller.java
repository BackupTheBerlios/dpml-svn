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

import java.io.*;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import net.dpml.transit.Artifact;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.TransitRegistryModel;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.util.StreamUtils;

/**
 */
public class MagicInstaller
{
    private Logger m_logger;

    public MagicInstaller( TransitRegistryModel home, Boolean install, Logger logger ) throws Exception
    {
        m_logger = logger;

        String profile = "development";
        if( install.booleanValue() )
        {
            try
            {
                home.getTransitModel( profile );
                getLogger().info( "transit development profile exists (no action required)" );
            }
            catch( UnknownKeyException e )
            {
                getLogger().info( "adding transit development profile" );
                home.addTransitModel( profile );
                getLogger().info( "profile created" );
            }

            getLogger().info( "checking ${user.home}/.ant/lib" );
            File user = new File( System.getProperty( "user.home" ) );
            File ant = new File( user, ".ant" );
            File lib = new File( ant, "lib" );
            checkAntLib( lib );
            purgeAntLib( lib );
            updateAntLib( lib );
        }
        else
        {
            getLogger().info( "removing transit development profile" );
        }
    }

    private Logger getLogger()
    {
        return m_logger;
    }

    private void checkAntLib( File file )
    {
        if( false == file.exists() )
        {
            getLogger().debug( "  creating " + file );
            file.mkdirs();
        }
    }

    private void purgeAntLib( File file )
    {
        File[] files = file.listFiles( new DpmlFileFilter() );
        for( int i=0; i<files.length; i++ )
        {
            File f = files[i];
            getLogger().debug( "removing old file ${user.home}/.ant/lib/" + f.getName() );
            f.delete();
        }
    }

    private void updateAntLib( File lib )
    {
        updateJUnit( lib );
        updateTransitMain( lib );
        updateTransitTools( lib );
    }

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

    private static class DpmlFileFilter implements FileFilter
    {
        public boolean accept( File file )
        {
            return file.getName().startsWith( "dpml-transit-" );
        }
    }

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

    private static String JUNIT_PATH = "@JUNIT-URI@";
    private static String TRANSIT_MAIN_PATH = "@TRANSIT-MAIN-URI@";
    private static String TRANSIT_TOOLS_PATH = "@TRANSIT-TOOLS-URI@";

}

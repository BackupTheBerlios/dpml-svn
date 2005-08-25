/*
 * Copyright 2005 Stephen McConnell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.depot.install;

import net.dpml.transit.model.Logger;

import net.dpml.depot.Main;

import java.util.prefs.Preferences;

/**
 * Handles the setup of Depot preferences.
 */
public class DepotInstaller
{
    private Logger m_logger;

   /**
    * Depot package installer constructor.
    * @param logger the assigned logging channel
    */
    public DepotInstaller( Logger logger ) throws Exception
    {
        m_logger = logger;
    }

    public void install() throws Exception
    {
        m_logger.info( "executing bootstrap setup" );
        m_logger.info( "adding http profile" );
        setupHttpProfile();
        if( getProfilesPreferences().nodeExists( "test" ) )
        {
            m_logger.info( "removing test profile" );
            getProfilePreferences( "test" ).removeNode();
        }
    }

    private void setupHttpProfile()
    {
        String id = "http";
        Preferences prefs = getProfilePreferences( id );
        prefs.put( "uri", "link:part:dpml/planet/http/dpml-http-demo" );
        prefs.put( "title", "DPML HTTP Demo" );
    }

    private Preferences getPreferences()
    {
        return Preferences.userNodeForPackage( Main.class );
    }

    private Preferences getProfilesPreferences()
    {
        return getPreferences().node( "profiles" );
    }

    private Preferences getProfilePreferences( String id )
    {
        return getProfilesPreferences().node( id );
    }
}

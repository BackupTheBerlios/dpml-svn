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

import net.dpml.station.Station;
import net.dpml.profile.ApplicationRegistry;
import net.dpml.profile.ApplicationProfile;

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
        setupAlternateProfile();
    }

    private void setupHttpProfile()
    {
        String id = "http";
        Preferences prefs = getProfilePreferences( id );
        prefs.put( "uri", "link:part:dpml/planet/http/dpml-http-demo" );
        prefs.put( "title", "DPML HTTP Demo" );
        prefs.put( "startup", ApplicationProfile.MANUAL.key() );
    }

    private void setupAlternateProfile()
    {
        String id = "demo";
        Preferences prefs = getProfilePreferences( id );
        prefs.put( "uri", "link:part:dpml/planet/http/dpml-http-demo" );
        prefs.put( "title", "DPML Alternate Demo" );
        prefs.putBoolean( "enabled", false );
        Preferences system = prefs.node( "system" );
        system.put( "abc", "def" );
        system.put( "xyz", "qwerty" );
        Preferences params = prefs.node( "parameters" );
        params .put( "classname", "net.dpml.composition.impl.CompositionContext" );
        Preferences basedir = params.node( "base" );
        basedir.put( "value", "${java.temp.dir}" );
        basedir.put( "classname", "java.io.File" );
        Preferences tempdir = params.node( "temp" );
        tempdir.put( "value", "${user.dir}" );
        tempdir.put( "classname", "java.io.File" );
        prefs.put( "startup", ApplicationProfile.DISABLED.key() );
    }

    private Preferences getPreferences()
    {
        return Preferences.userNodeForPackage( Station.class );
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

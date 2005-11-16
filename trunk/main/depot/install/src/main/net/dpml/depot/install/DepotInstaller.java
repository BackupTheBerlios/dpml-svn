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

import java.net.URI;

import net.dpml.transit.Logger;

import net.dpml.station.Station;

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
    public DepotInstaller( Logger logger )
    {
        m_logger = logger;
    }

   /**
    * Install Depot resources.
    * @exception Exception if an install etrror occurs
    */
    public void install() throws Exception
    {
        m_logger.info( "executing bootstrap setup" );
        m_logger.info( "adding http profile" );
    }

    /*
    private void setupHttpProfile()
    {
        String id = "/test/http";
        getProfilesPreferences().put( "http", id );
        Preferences prefs = getProfilePreferences( id );
        prefs.put( "uri", "link:part:dpml/planet/http/dpml-http-demo" );
        prefs.put( "title", "DPML HTTP Demo" );
        prefs.put( "startup", ApplicationProfile.MANUAL.getName() );
    }
    */

    /*
    private void setupAlternateProfile()
    {
        String id = "/test/demo";
        getProfilesPreferences().put( "alternate", id );
        Preferences prefs = getProfilePreferences( id );
        prefs.put( "uri", "link:part:dpml/planet/http/dpml-http-demo" );
        prefs.put( "title", "DPML Alternate Demo" );
        prefs.putBoolean( "enabled", false );
        Preferences system = prefs.node( "system" );
        system.put( "abc", "def" );
        system.put( "xyz", "qwerty" );
        Preferences params = prefs.node( "parameters" );
        params.put( "classname", "net.dpml.metro.runtime.impl.CompositionContext" );
        Preferences basedir = params.node( "base" );
        basedir.put( "value", "${java.temp.dir}" );
        basedir.put( "classname", "java.io.File" );
        Preferences tempdir = params.node( "temp" );
        tempdir.put( "value", "${user.dir}" );
        tempdir.put( "classname", "java.io.File" );
        prefs.put( "startup", ApplicationProfile.DISABLED.getName() );
    }
    */

    /*
    private void setupTestProfile() throws Exception
    {
        String id = "/dpml/planet/http/demo";
        getProfilesPreferences().put( "demo", id );
        Preferences prefs = getProfilePreferences( id );

        ApplicationStorageUnit store = new ApplicationStorageUnit( prefs );
        store.setCodeBaseURI( new URI( "link:part:dpml/planet/http/dpml-http-demo" ) );
        store.setTitle( "DPML Test Profile" );
        store.setSystemProperty( "abc", "xyz" );
        store.setSystemProperty( "xyz", "qwerty" );
        store.setSystemProperty( "test", "${xyz}" );
        store.setStartupPolicy( ApplicationProfile.MANUAL );
    }
    */

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

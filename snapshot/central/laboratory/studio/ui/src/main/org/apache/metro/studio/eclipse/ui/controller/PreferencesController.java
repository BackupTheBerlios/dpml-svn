/*

   Copyright 2004. The Apache Software Foundation.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 

*/
package org.apache.metro.studio.eclipse.ui.controller;

import org.apache.metro.facility.presentationservice.api.ChannelListener;
import org.apache.metro.facility.presentationservice.api.ChannelEvent;
import org.apache.metro.facility.presentationservice.api.ChannelException;

import org.apache.metro.facility.presentationservice.impl.ViewChannel;

import org.apache.metro.studio.eclipse.core.environment.MetroEnvironment;
import org.apache.metro.studio.eclipse.core.environment.ServerEnvironment;

import org.apache.metro.studio.eclipse.ui.panels.ServerSettingsPanel;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Metro Development Team</a>
 */
public class PreferencesController
{
    public PreferencesController()
    {
        super();
    }
    
    private void serverApplyClicked(ChannelEvent event)
    {
        event.getValue( ServerSettingsPanel.SERVER_DEPLOY );
        event.getValue( ServerSettingsPanel.SERVER_LANG );
        event.getValue( ServerSettingsPanel.SERVER_ROOT_BLOCK );
        event.getValue( ServerSettingsPanel.SERVER_ROOT_FACILITY );
        event.getValue( ServerSettingsPanel.SERVER_STARTUP_JAR );
    }
    
    private void serverWindowCreated(ChannelEvent event)
    {
        MetroEnvironment env = new MetroEnvironment();
        
        event.putValue( ServerSettingsPanel.SERVER_DEPLOY, "" );
        event.putValue( ServerSettingsPanel.SERVER_LANG, ServerEnvironment.DEFAULT_LANG );
        String merlinHome = env.getMerlinHome();
        event.putValue( ServerSettingsPanel.SERVER_ROOT_BLOCK, merlinHome );
        event.putValue( ServerSettingsPanel.SERVER_ROOT_FACILITY, "" );
        event.putValue( ServerSettingsPanel.SERVER_STARTUP_JAR, "" );
    }

    /**
     * register all event listeners
     *
     */
    public void initialize()
    {
        try
        {
            ViewChannel channel = new ViewChannel( "server" );
            
            channel.addControlClickedListener( ServerSettingsPanel.SERVER_APPLY, new ChannelListener()
            {
                public ChannelEvent notify( ChannelEvent event ) 
                    throws ChannelException
                {
                    serverApplyClicked( event );
                    return event;
                }          
            });
            
            channel.addWindowCreatedListener( ServerSettingsPanel.SERVER_PANEL, new ChannelListener()
            {
                public ChannelEvent notify( ChannelEvent event ) 
                    throws ChannelException
                {
                    serverWindowCreated( event );
                    return event;
                }          
            });
        } catch( ChannelException e )
        {
            e.printStackTrace();
        }
    }
}

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

import org.apache.metro.studio.eclipse.core.templateengine.BlockProjectManager;
import org.apache.metro.studio.eclipse.core.templateengine.ResourceTemplateManager;

import org.apache.metro.studio.eclipse.ui.wizards.NewMetroProjectSelectionPage;
import org.apache.metro.studio.eclipse.ui.wizards.NewMetroProjectWizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.CoreException;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Metro Development Team</a>
 * 22.08.2004
 * last change:
 * 
 */
public class NewMetroProjectWizardController
{
    ResourceTemplateManager rm;
    /**
     * 
     */
    public NewMetroProjectWizardController()
    {
        super();
    }
    
    private ChannelEvent selectionListClicked( ChannelEvent event )
    {
        String helpKey = event.getValue( NewMetroProjectSelectionPage.SELECTION_CONTROL_SELECTED );
        String help = BlockProjectManager.getResourceHelp( helpKey );
        event.putValue( "resource.help", help );
        return event;
    }
    
    private ChannelEvent selectionApplyClicked( ChannelEvent event )
    {
        try
        {
            String projectName = event.getValue( NewMetroProjectWizard.NEW_PROJECT_NAME );
            String templateName = event.getValue( NewMetroProjectSelectionPage.SELECTION_CONTROL_SELECTED );

            IProject project = BlockProjectManager.create( projectName, templateName );
            project.refreshLocal( IResource.DEPTH_INFINITE, null );
        } catch( CoreException e )
        {
            e.printStackTrace();
        }


        return event;
    }
    
    private ChannelEvent selectionWindowCreated( ChannelEvent event )
    {
        rm = ResourceTemplateManager.load( ResourceTemplateManager.getDefaultConfigPath() );
        String[] names = BlockProjectManager.listTemplateNames();
        event.putValueArray( NewMetroProjectSelectionPage.SELECTION_CONTROL_LIST, names );
        return event;
    }

    /**
     * register all event listeners
     *
     */
    public void initialize()
    {
        try
        { 
            ViewChannel channel = new ViewChannel( "newBlockWizard" );
            
            channel.addControlClickedListener( NewMetroProjectSelectionPage.SELECTION_CONTROL, new ChannelListener ()
            {
                public ChannelEvent notify( ChannelEvent event ) 
                    throws ChannelException
                {
                    selectionListClicked(event);                    
                    return event;
            
                }          
            });

            channel.addControlClickedListener( NewMetroProjectWizard.NEW_PROJECT_FINISH, new ChannelListener()
            {
                public ChannelEvent notify( ChannelEvent event ) 
                    throws ChannelException
                {
                    selectionApplyClicked( event );
                    return event;
                }          
            });
            
            channel.addWindowCreatedListener( NewMetroProjectSelectionPage.SELECTION_PANEL, new ChannelListener()
            {
                public ChannelEvent notify( ChannelEvent event ) 
                    throws ChannelException
                {
                    event = selectionWindowCreated( event );
                    return event;
            
                }          
            });

        } catch (ChannelException e)
        {
            e.printStackTrace();
        }
    }
}

/*
 *     Copyright 2004. The Apache Software Foundation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *  
 */
package org.apache.metro.studio.eclipse.ui.controller;

import java.lang.reflect.InvocationTargetException;

import org.apache.metro.facility.presentationservice.api.IModelChannel;
import org.apache.metro.facility.presentationservice.api.ChannelException;

import org.apache.metro.studio.eclipse.ui.wizards.NewMetroProjectWizard;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 *
 */
public class CreateBlockProjectOperation 
    implements IRunnableWithProgress
{

    private IModelChannel channel;
    
    public CreateBlockProjectOperation()
    {
    }

    /**
     * @param pParam
     * @param pModel
     */
    public CreateBlockProjectOperation( IModelChannel channel )
    {
        this.channel = channel;
    }

    public void run( IProgressMonitor monitor )
        throws InvocationTargetException, InterruptedException
    {
        if( monitor == null )
        {
            monitor = new NullProgressMonitor();
        }
        try
        {
            // monitor.beginTask( MerlinDeveloperUI.getResourceString("NewWebAppProjectOperation.createWebAppTask.description"), 1); //$NON-NLS-1$

            channel.controlClicked( NewMetroProjectWizard.NEW_PROJECT_FINISH );
        } catch (ChannelException e)
        {
            e.printStackTrace();
        } finally
        {
            monitor.done();
        }
    }

}

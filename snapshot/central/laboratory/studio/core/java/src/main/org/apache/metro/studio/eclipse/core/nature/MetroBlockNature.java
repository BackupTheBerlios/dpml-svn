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
package org.apache.metro.studio.eclipse.core.nature;

import org.apache.metro.studio.eclipse.core.MetroStudioCore;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.IJavaProject;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 *
 */
public class MetroBlockNature implements IProjectNature
{
    private IProject project;
    private IJavaProject javaProject;
    
    /**
     * Normaly, the whole initialization stuff (creating directory structure,
     * initial files etc. ) is done here. But because we need greater
     * flexibility, this is done in the wizards operastions classes.
     * 
     * @see org.eclipse.core.resources.IProjectNature#configure()
     */
    public void configure() throws CoreException
    {
        addBuilder( "org.apache.metro.studio.launch.merlinBuilder" );
    }
    /**
     * @param pString
     */
    public void addBuilder( String builderID )
    {
        try
        {
            IProjectDescription desc = getProject().getDescription();
            ICommand[] commands = desc.getBuildSpec();
            boolean found = false;

            for (int i = 0 ; i < commands.length ; ++i) 
            {
                String cmdBuilderName = commands[i].getBuilderName();
                if( cmdBuilderName.equals( builderID ) ) 
                {
                    found = true;
                    break;
                }
            }
            if( !found ) 
            { 
                //add builder to project
                ICommand command = desc.newCommand();
                command.setBuilderName( builderID );
                ICommand[] newCommands = new ICommand[ commands.length + 1 ];

                // Add it before other builders.
                System.arraycopy( commands, 0, newCommands, 1, commands.length );
                newCommands[0] = command;
                desc.setBuildSpec( newCommands );
                getProject().setDescription( desc, null );
            }
        } catch( CoreException e )
        {
            MetroStudioCore.log( e, "Error while setting the builder commands" );
        }

    }

    public void deconfigure() 
        throws CoreException
    {
        // Remove the nature-specific information here.
    }

    public String getDocumentBase()
    {
        return getProject().getLocation().toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IProjectNature#getProject()
     */
    public IProject getProject()
    {
        return project;
    }

    /**
     * This method is automatically called by eclipse during project creation
     * before the configure() method is called
     * 
     * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
     */
    public void setProject( IProject project )
    {
        this.project = project;
    }

    /**
     * @param pJavaProject
     */
    private void setJavaProject( IJavaProject pJavaProject )
    {
        javaProject = pJavaProject;
    }
    
    /**
     * @return Returns the javaProject.
     */
    public IJavaProject getJavaProject()
    {
        return javaProject;
    }
}

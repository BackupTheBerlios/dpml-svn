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
package org.apache.metro.studio.eclipse.core.templateengine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.metro.studio.eclipse.core.tools.DynProjectParam;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.launching.JavaRuntime;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Metro Development Team</a>
 */
public class ResourceTemplate
{
    private String directoryType;
    private String templateId;
    private String description;
    private List wizardPages;
    private List resources;
    private Vector libraries;
    
    public ResourceTemplate()
    {
        super();
        wizardPages = new ArrayList();
        resources = new ArrayList();
        libraries = new Vector();
    }
    
    /**
     * Creates the directorystructure and all Resources of
     * the template project
     * 
     * @param project
     * 
     */
    public void create( IProject project, DirectoryTemplateManager manager, DynProjectParam param )
    {
        // load directoryType and create the directory structure
        DirectoryTemplate template = manager.create( directoryType, project );
        
        // add all needed resources to created directory structure
        Iterator it = getResources().iterator();
       
        while( it.hasNext() )
        {
            Resource resource = (Resource)it.next();
            resource.create( template, param );
        }
 
        // add all needed libraries
        addLibraries(project);
    }

    /**
     * @param project
     */
    private void addLibraries( IProject project )
    {
        try
        {
            Vector libs = new Vector();
            
            IJavaProject javaProject = JavaCore.create( project );
            
            // first retain already created libs
            IClasspathEntry[] entries = javaProject.getResolvedClasspath( true );
            for(int i=0 ; i < entries.length ; i++ )
            {
                libs.add( entries[i] );
            }
            
            // allways add the java library
            libs.add( JavaRuntime.getJREVariableEntry() );

            // now add custom libraries
            libs.addAll( getLibraryEntries() );
            
            IClasspathEntry[] cpEntries = new IClasspathEntry[libraries.size()];
            libs.toArray( cpEntries );
            IPath location = javaProject.getOutputLocation();
            javaProject.setRawClasspath( cpEntries, location, null );
        } catch( JavaModelException e )
        {
            e.printStackTrace();
        }
    }
    
    public void addLibrary( Library library ) 
    {
        libraries.add(library);
    }
    
    private Vector getLibraryEntries()
    {
        Vector libs = new Vector();
        Iterator it = libraries.iterator();
        while(it.hasNext())
        {
            Library library = (Library)it.next();
            IPath path = library.getPath();
            IClasspathEntry libEntry = JavaCore.newLibraryEntry( path, null, null );
            libs.add( libEntry );
        }
        return libs;
    }
    
    /**
     * @return Returns the directoryType.
     */
    public String getDirectoryType()
    {
        return directoryType;
    }
    
    /**
     * @param directoryType The directoryType to set.
     */
    public void setDirectoryType( String directoryType )
    {
        this.directoryType = directoryType;
    }
    
    /**
     * @return Returns the resources.
     */
    public List getResources()
    {
        return resources;
    }
    
    /**
     * @param resources The resources to set.
     */
    public void setResources( List resources )
    {
        this.resources = resources;
    }
    
    /**
     * @return Returns the templateId.
     */
    public String getTemplateId()
    {
        return templateId;
    }
    
    /**
     * @param templateId The templateId to set.
     */
    public void setTemplateId( String templateId )
    {
        this.templateId = templateId;
    }

    public void addResource( Resource resource )
    {
        resources.add( resource );
    }
    
    /**
     * @return Returns the description.
     */
    public String getDescription()
    {
        return description;
    }
    
    /**
     * @param description The description to set.
     */
    public void setDescription( String description )
    {
        this.description = description;
    }
}

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

import org.apache.metro.studio.eclipse.core.MetroStudioCore;

import org.eclipse.core.resources.IProject;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Metro Development Team</a>
 */
public class DirectoryTemplate
{
    private String id;
    private List directories = new ArrayList(); 
    
    /**
     * Create all directories in Project
     * @param project
     */
    public void create( IProject project )
    {
        Iterator it = getDirectories().iterator();
        while(it.hasNext())
        {
            Directory dir = (Directory) it.next();
            dir.create(project);
        }
    }
    
    public void addDirectory( Directory directory )
    {
        directories.add(directory);
    }

    public List getDirectories()
    {
        return directories;
    }
    
    /**
     * @return Returns the id.
     */
    public String getId()
    {
        return id;
    }
    
    /**
     * @param id The id to set.
     */
    public void setId( String id )
    {
        this.id = id;
    }

    public Directory getDirectory( String directoryName )
    {
        Iterator it = getDirectories().iterator();
        while(it.hasNext())
        {
            Directory dir = (Directory) it.next();
            if( directoryName.equals( dir.getName() ) )
            {
                return dir;
            }
        }
        MetroStudioCore.log( null, "can't find rootsegment resource " + directoryName );
        return null;
    }
    
    /**
     * @return
     */
    public Vector getSourceFolderNames()
    {
        Vector libraries = new Vector();
        Iterator it = directories.iterator();
        while(it.hasNext())
        {
            Directory dir = (Directory) it.next();
            if( dir.isSource() )
            {
                libraries.add( dir.getName() );
            }
        }
        return libraries;
    }
}

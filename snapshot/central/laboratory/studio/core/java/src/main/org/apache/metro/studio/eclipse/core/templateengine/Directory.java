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

import java.util.Iterator;
import java.util.Vector;

import org.apache.metro.studio.eclipse.core.MetroStudioCore;

import org.apache.metro.studio.eclipse.core.tools.ClassNameAnalyzer;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Metro Development Team </a>
 *         11.08.2004 last change:
 *  
 */
public class Directory
{
    private String name;
    private boolean isSource = false;
    private transient IFolder eclipseFolder;

    /**
     * @return Returns the isSource.
     */
    public boolean isSource()
    {
        return isSource;
    }

    /**
     * @param isSource
     *            The isSource to set.
     */
    public void setSource( boolean isSource )
    {
        this.isSource = isSource;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * Create a directory under the given project.
     * 
     * @param project
     */
    public void create( IProject project )
    {
        IFolder folder = null;
        try
        {
            // if we like to create folder / subfolders, each folder has to be
            // created by its own. The ClassNameAnalyzer splits a filePath into
            // its segments
            ClassNameAnalyzer cna = new ClassNameAnalyzer();
            cna.setPath( name );
            Iterator it = cna.getSegments().iterator();

            while( it.hasNext() )
            {
                String segment = (String) it.next();
                
                // Now get the folder
                if( folder == null )
                {
                    // first segment
                    folder = project.getFolder( segment );
                } 
                else
                {
                    folder = folder.getFolder( segment );
                }

                // ... and create it
                if( ! folder.exists() )
                {
                    folder.create( false, true, null );
                    eclipseFolder = folder;
                }
                
                if( isSource() )
                {
                    String foldername = folder.getName();
                    IPath projPath = project.getFullPath();
                    IPath srcEntry = projPath.append( foldername );
                    IClasspathEntry entry = JavaCore.newSourceEntry( srcEntry );
                    addClasspath( project, entry );
                }
            }
        } catch (CoreException e)
        {
            e.printStackTrace();
        }
    }
    

    private void addClasspath( IProject project, IClasspathEntry entry )
    {
        try
        {
            Vector libraries = new Vector();
            
            IJavaProject javaProject = JavaCore.create( project );
            IClasspathEntry[] current = javaProject.getResolvedClasspath( true );

            for( int i=0 ; i < current.length ; i++ )
            {
                // don't add the project to the classpath!
                IPath curPath = current[i].getPath();
                IPath projPath = project.getFullPath();
                
                // TODO: Shouldn't IPath.equals() work??
                if( ! curPath.toString().equals( projPath.toString() ) )
                {
                    libraries.add( current[ i ] );  
                }
            }
            libraries.add( entry );
            
            int size = libraries.size();
            IClasspathEntry[] entries = new IClasspathEntry[ size ];
            libraries.toArray( entries );
            IPath location = javaProject.getOutputLocation();
            javaProject.setRawClasspath( entries, location, null );
        } catch( JavaModelException e )
        {
            MetroStudioCore.log( e, "could not add libraries to project" );
        }
    }

    /**
     * append a directory.
     * 
     * @param project
     */
    public Directory appendPackage( String packageName )
    {
        IFolder folder = null;
        try
        {
            // if we like to create folder / subfolders, each folder has to be
            // created by its own. The ClassNameAnalyzer splits a filePath into
            // its segments
            ClassNameAnalyzer cna = new ClassNameAnalyzer();
            cna.setPackageName( packageName );
            Iterator it = cna.getSegments().iterator();

            while( it.hasNext() )
            {
                String foldername = (String) it.next();
                if( folder == null )
                {
                    folder = getEclipseFolder().getFolder( foldername );
                } 
                else
                {
                    folder = folder.getFolder( foldername );
                }
                
                // ... and create it
                if( ! folder.exists() )
                {
                    folder.create( false, true, null );
                }
            }
        } catch( CoreException e )
        {
            e.printStackTrace();
        }
        Directory dir = new Directory();
        dir.setEclipseFolder( folder );
        IPath location = folder.getLocation();
        dir.setName( location.toString() );
        return dir;
    }

    /**
     * @return Returns the eclipseFolder.
     */
    public IFolder getEclipseFolder()
    {
        return eclipseFolder;
    }
    /**
     * @param eclipseFolder The eclipseFolder to set.
     */
    public void setEclipseFolder( IFolder eclipseFolder )
    {
        this.eclipseFolder = eclipseFolder;
    }
}

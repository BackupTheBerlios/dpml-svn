/*
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.metro.studio.eclipse.launch.builder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>*
 */
public class MerlinBuilderFactory extends IncrementalProjectBuilder
{
    private static List builderList;

    /**
     * 
     */
    public MerlinBuilderFactory()
    {
        super();
        builderList = new ArrayList();
        builderList.add( new MerlinTypeBuilder() );
    }

    /*
     * Add builders to the build process. This method is also called
     * by other plug-in (e.g. EnterpriseDeveloper).
     * Registration of builders is done in the main plug-in class (eg. EnterpriseDeveloperCore.java)
     *   
     */
    public static void addBuilder(IMerlinBuilder builder)
    {
        builderList.add(builder);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
     */
    protected IProject[] build( int pKind, Map pArgs, IProgressMonitor pMonitor )
        throws CoreException
    {
        IProject project = getProject();
        IResourceDelta delta = getDelta( project );
        if( delta == null ) 
            return null;
        
        IResourceDelta affected[] = delta.getAffectedChildren();
        List files = getChangedResource( affected );
        
        Iterator it = builderList.iterator();
        while( it.hasNext() )
        {
            IMerlinBuilder builder = (IMerlinBuilder) it.next();
            builder.build( pKind, getProject(), files, pMonitor );
        }
        IJavaProject proj = JavaCore.create( project );
        proj.getProject().refreshLocal( IProject.DEPTH_INFINITE, null );
        return null;
    }

    /*
     * retrieves the changed resource.
     * only returns changed java resources (java source files) 
     */
    private List getChangedResource( IResourceDelta delta[] )
    {
        List res = new ArrayList();

        for( int i = 0; i < delta.length ; i++ )
        {
            IResourceDelta affected[] = delta[i].getAffectedChildren();
            if( affected.length > 0)
            {
                res.addAll(getChangedResource(delta[i].getAffectedChildren()));
            } else
            {
                res.add(delta[i].getResource());
            }
        }

        return res;
    }

}

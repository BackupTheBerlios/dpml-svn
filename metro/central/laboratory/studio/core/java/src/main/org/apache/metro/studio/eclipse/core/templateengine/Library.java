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

import org.apache.metro.studio.eclipse.core.MetroStudioCore;

import org.eclipse.core.runtime.IPath;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Metro Development Team</a>
 */
public class Library
{
    private String name;
    private String version;
    private String repositoryPath;

    public Library()
    {
        super();
    }
    
    /**
     * @return Returns the repositoryPath.
     */
    public String getRepositoryPath()
    {
        return "lib/avalon-framework";
    }
    
    /**
     * @param repositoryPath The repositoryPath to set.
     */
    public void setRepositoryPath( String repositoryPath )
    {
        this.repositoryPath = repositoryPath;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName( String name )
    {
        this.name = name;
    }
    
    /**
     * @return Returns the version.
     */
    public String getVersion()
    {
        return version;
    }
    
    /**
     * @param version The version to set.
     */
    public void setVersion( String version )
    {
        this.version = version;
    }
    
    public IPath getPath()
    {
        // TODO: has to be changed to metro repository
        IPath pluginPath = MetroStudioCore.getDefault().getPluginLocation();
        
        pluginPath = pluginPath.append( getRepositoryPath() );
        return pluginPath.append( getFullName() );
    }
    
    private String getFullName()
    {

        StringBuffer buf = new StringBuffer( name );
        buf.append( "-" );
        buf.append( version );
        buf.append( ".jar" );
        return buf.toString();
    }
}

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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.apache.metro.studio.eclipse.core.MetroStudioCore;

import org.apache.metro.studio.eclipse.core.tools.DynProjectParam;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.IPath;

import com.thoughtworks.xstream.XStream;

import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Metro Development Team</a>
 */
public class ResourceTemplateManager
{
    private static XStream xstream;
    
    private Hashtable resourceTemplates;
    private DirectoryTemplateManager directoryManager;
    
    static public String getDefaultConfigPath()
    {
        MetroStudioCore core = MetroStudioCore.getDefault();
        IPath pluginLocation = core.getPluginLocation();
        String baseDir = pluginLocation.toString();
        return baseDir + "config/resources.test_cfg";
    }
    
    
    /**
     * 
     */
    public ResourceTemplateManager()
    {
        super();
        resourceTemplates = new Hashtable();
    }

    public static ResourceTemplateManager load( String resourceFilePathName )
    {
        if( resourceFilePathName == null )
        {
            resourceFilePathName = getDefaultConfigPath();
        }
        
        initXStream();

        DirectoryTemplateManager.addXStreamAliases( xstream );
        FileReader reader = null;
        try
        {
            reader = new FileReader( resourceFilePathName );

        } catch( FileNotFoundException e )
        {
            MetroStudioCore.log( e, "can't open Resource Template configuration file" );
            return null;
        }
        
        ResourceTemplateManager rm = (ResourceTemplateManager) xstream.fromXML(reader);
        return rm;
    }

    /**
     * @param xstream
     */
    public static void initXStream()
    {
        xstream = new XStream( new DomDriver() );
        xstream.alias( "ResourceTemplates", ResourceTemplateManager.class );
        xstream.alias( "ResourceTemplate", ResourceTemplate.class );
        xstream.alias( "Resource", Resource.class );
        xstream.alias( "Library", Library.class );
    }

    public void addResourceTemplate( ResourceTemplate resource )
    {
        String templateID = resource.getTemplateId();
        resourceTemplates.put( templateID, resource );
    }

    /**
     * @return String[]. An array of all keys
     */
    public String[] listTemplateNames()
    {
        Set set = resourceTemplates.keySet();
        String[] keyList = new String[ set.size() ];
        set.toArray( keyList );
        return keyList;
    }

    /**
     * @param string
     * @return
     */
    public ResourceTemplate getTemplate( String key )
    {
        return (ResourceTemplate) resourceTemplates.get( key );
    }

    /**
     * @param dm
     */
    public void importDirectoryTemplates( DirectoryTemplateManager dm )
    {
        DirectoryTemplateManager.addXStreamAliases( xstream );
        directoryManager = dm;
    }
    
    /**
     * Create a directorystrucure and all required resources in a project
     */
    public void create( IProject project, String resourceName, DynProjectParam param )
    {
        ResourceTemplate template = getTemplate( resourceName );
        if( template == null )
        {
            MetroStudioCore.log( null, "cant find resource template <" + resourceName +">");
            return;
        }
        template.create( project, directoryManager, param );
    }

    /**
     * 
     */
    public Hashtable getResourceTemplates()
    {
        return resourceTemplates;
    }

    /**
     * @param resourcesLocation
     */
    public void store( String resourcesLocation )
    {
        try
        {
            Writer out = new FileWriter( resourcesLocation );
            xstream.toXML( this, out );
        } catch( IOException e )
        {
            MetroStudioCore.log( e, "cant write xstream" );
        }
    }
}

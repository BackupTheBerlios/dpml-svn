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
package org.apache.metro.studio.eclipse.docs;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class DocsPlugin extends AbstractUIPlugin 
{
    //The shared instance.
    private static DocsPlugin plugin;
    
    //Resource bundle.
    private ResourceBundle resourceBundle;
    
    /**
     * The constructor.
     */
    public DocsPlugin() 
    {
        super();
        plugin = this;
        
        try 
        {
            resourceBundle = ResourceBundle.getBundle("org.apache.metro.studio.eclipse.docs.DocsPluginResources");
        } catch( MissingResourceException x ) 
        {
            resourceBundle = null;
        }
    }

    /**
     * Returns the shared instance.
     */
    public static DocsPlugin getDefault() 
    {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     */
    public static String getResourceString(String key) 
    {
        ResourceBundle bundle = DocsPlugin.getDefault().getResourceBundle();
        try 
        {
            if( bundle == null )
                return key;
                
            return bundle.getString( key );
        } catch( MissingResourceException e ) 
        {
            return key;
        }
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() 
    {
        return resourceBundle;
    }
}

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
package org.apache.metro.studio.eclipse.ui;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a> 
 * The main plugin class to be used in the desktop.
 */
public class MetroStudioUI extends AbstractUIPlugin
{
    //The shared instance.
    private static MetroStudioUI plugin;
    
    //Resource bundle.
    private ResourceBundle resourceBundle;
    
    public static final String PLUGIN_ID = "org.apache.metro.MetroStudioUI";
    
    /**
     * The constructor.
     */
    public MetroStudioUI()
    {
        super();
        plugin = this;
        try
        {
            String bundlename = "org.apache.metro.studio.eclipse.ui.MetroStudioUIResources";
            resourceBundle = ResourceBundle.getBundle( bundlename );
        } catch( MissingResourceException x )
        {
            resourceBundle = null;
        }
    }

    /**
     * Returns the shared instance.
     */
    public static MetroStudioUI getDefault()
    {
        return plugin;
    }

    /**
     * Returns the workspace instance.
     */
    public static IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     */
    public static String getResourceString( String key )
    {
        ResourceBundle bundle = getDefault().getResourceBundle();
        try
        {
            String value = bundle.getString(key);
            if( value != null )
                return value;
            else
                return key;
        } catch (MissingResourceException e)
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

    public static void log( IStatus status )
    {
        getDefault().getLog().log(status);
    }

    public static void log( Throwable e, String message )
    {
        boolean isDebugging = true; // change to false for production
        Bundle bundle = ResourcesPlugin.getPlugin().getBundle();
        String symName = bundle.getSymbolicName();
        IStatus status = new Status( IStatus.ERROR, symName, 
                                     IStatus.ERROR, message, e
        );

        log( status );
        
        if (isDebugging)
        {
            System.out.println( message + ": " + e.getMessage() ); //$NON-NLS-1$
        }
    }

    public static ImageDescriptor getImageDescriptor(String path)
    {
        try
        {
            URL prefix = getDefault().getBundle().getEntry( "/" );
            URL url = new URL( prefix, path );
            return ImageDescriptor.createFromURL( url );
        } catch (MalformedURLException e)
        {
            log( e, "getResourceBundle() handling MalformedURLException"); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start( BundleContext context ) 
        throws Exception 
    {
        super.start( context );
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop( BundleContext context ) 
        throws Exception 
    {
        super.stop( context );
    }
}

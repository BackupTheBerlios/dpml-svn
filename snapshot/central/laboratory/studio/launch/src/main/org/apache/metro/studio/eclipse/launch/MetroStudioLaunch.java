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
package org.apache.metro.studio.eclipse.launch;

import java.net.URL;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>*
 *         The main plugin class to be used in the desktop.
 */
public class MetroStudioLaunch extends AbstractUIPlugin
{
    //The shared instance.
    private static MetroStudioLaunch plugin;
    //Resource bundle.
    private ResourceBundle resourceBundle;

    public static final String PLUGIN_ID;
    public static final String METRO_CONFIG_TYPE;
    
    public static final String MERLIN_PROJECT_NATURE_ID;
    public static final String MERLIN_PROJECT_CONFIG_NATURE_ID;
    public static final String MERLIN_BUILDER_ID;
    public static final String ATTR_MERLIN_CONTAINER_ID;
    
    static
    {
        PLUGIN_ID = "org.apache.metro.studio.launch"; //$NON-NLS-1$
        METRO_CONFIG_TYPE = PLUGIN_ID + ".metroLaunchConfigurationDelegate"; //$NON-NLS-1$
        MERLIN_PROJECT_NATURE_ID = PLUGIN_ID + ".merlinProjectNature"; //$NON-NLS-1$
        MERLIN_PROJECT_CONFIG_NATURE_ID = PLUGIN_ID + ".merlinConfigNature"; //$NON-NLS-1$
        MERLIN_BUILDER_ID = PLUGIN_ID + ".merlinBuilder"; //$NON-NLS-1$
        ATTR_MERLIN_CONTAINER_ID = "merlinContainerID"; //$NON-NLS-1$
    }
    
    /**
     * The constructor.
     */
    public MetroStudioLaunch()
    {
        super();
        plugin = this;
        try
        {
            // activate EnterpriseDeveloper if present
            // Platform.getPlugin("biz.softwarefabrik.j4ee.core");
            // MerlinBuilderFactory.addBuilder(new MerlinTypeBuilder());
            
            String bundleName = "org.apache.avalon.ide.eclipse.merlin.launch.MerlinDeveloperLaunchResources";
            resourceBundle = ResourceBundle.getBundle( bundleName );
        } catch (MissingResourceException x)
        {
            resourceBundle = null;
        }
    }

    /**
     * Returns the shared instance.
     */
    public static MetroStudioLaunch getDefault()
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
        MetroStudioLaunch launch = getDefault();
        ResourceBundle bundle = launch.getResourceBundle();
        try
        {
            if( bundle == null )
                return key;
                
            return bundle.getString( key );
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
        getDefault().getLog().log( status );
    }

    public static void log( Throwable e, String message )
    {
        boolean isDebugging = true; // change to false for production
        ResourcesPlugin plugin = ResourcesPlugin.getPlugin();
        Bundle bundle = plugin.getBundle();
        String symName = bundle.getSymbolicName();
        IStatus status = 
            new Status( IStatus.ERROR, symName, IStatus.ERROR, message, e);

        log( status );
        if( isDebugging )
        {
            System.out.println( message + ": " + e.getMessage() ); //$NON-NLS-1$
        }
    }

    public IPath getPluginLocation()
    {
        return getPluginLocation( PLUGIN_ID );
    }

    public IPath getPluginLocation( String pluginId )
    {
        try
        {
            Bundle bundle = getDefault().getBundle();
            URL installURL = bundle.getEntry("/");
            URL resource = Platform.resolve( installURL );
            String file = resource.getFile();
            Path path = new Path( file );
            return path;
        } catch( Exception e )
        {
            log( e, "getPluginLocation() handling Exception" ); //$NON-NLS-1$
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

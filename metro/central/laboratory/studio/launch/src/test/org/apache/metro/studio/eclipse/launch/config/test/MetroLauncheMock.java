/*

   Copyright 2004. The Apache Software Foundation.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 

*/
package org.apache.metro.studio.eclipse.launch.config.test;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Metro Development Team</a>
 * 15.08.2004
 * last change:
 * 
 */
public class MetroLauncheMock extends TestCase
{

    public static void main(String[] args)
    {
    }

    public final void testLaunchMetroExecute()
    {
        try
        {
        	ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        	ILaunchConfigurationType type = manager.getLaunchConfigurationType("org.apache.metro.studio.launch.metroLaunchConfigurationDelegate");

            ILaunchConfigurationWorkingCopy wc = type.newInstance(null, "Hello Component");
            // set attributes here
            
            ILaunchConfiguration config = wc.doSave();
            config.launch(ILaunchManager.RUN_MODE, null);
        } catch (CoreException e)
        {
            fail("error while launching Metro");
        }

    }
}

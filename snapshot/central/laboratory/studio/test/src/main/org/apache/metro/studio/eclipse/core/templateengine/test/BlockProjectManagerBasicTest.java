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
package org.apache.metro.studio.eclipse.core.templateengine.test;

import java.io.File;

import junit.framework.TestCase;

import org.apache.metro.studio.eclipse.core.templateengine.BlockProjectManager;
import org.eclipse.core.resources.IProject;

/**
 * This test class only tests to create Metro projects with different
 * natures.
 * Those methods are than needed by other testcases:
 *   - DirectoryTemplateMangerTest
 *   - ResourceTemplateManagerTest
 *   - ProjectManagerTest
 * The ProjectMangerTest class is testing higher level methods than.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Metro Development Team</a>
 * 11.08.2004
 * last change: 
 * 
 */
public class BlockProjectManagerBasicTest extends TestCase
{

    private static IProject project;
    
    public static void main(String[] args)
    {
    }

    public final void testCreateBlockProject()
    {
        project = BlockProjectManager.testCreateProject("EmptyBlock");
        assertEquals("Project was not created", true, (new File(project.getLocation().toString()).exists()));
        
    }

    public final void testDelete()
    {
        BlockProjectManager.delete(project);
    }

}

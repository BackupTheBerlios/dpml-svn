
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
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.metro.studio.eclipse.core.MetroStudioCore;
import org.apache.metro.studio.eclipse.core.templateengine.BlockProjectManager;
import org.apache.metro.studio.eclipse.core.templateengine.Directory;
import org.apache.metro.studio.eclipse.core.templateengine.DirectoryTemplate;
import org.apache.metro.studio.eclipse.core.templateengine.DirectoryTemplateManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Metro Development Team</a>
 * 11.08.2004
 * last change:
 * 
 */
public class DirectoryTemplateManagerTest extends TestCase
{

    static IProject project;
    final static String configFileLocation = MetroStudioCore.getDefault().getPluginLocation().toString() + "config/directories.test_cfg";
    
    public static void main(String[] args)
    {
    }
    /**
     * Create DirectoryTemplates and store/reload them to/from file.
     *
     */
    public final void testCreateLoadConfigFile()
    {
        DirectoryTemplateManager.initXStream();
        DirectoryTemplateManager m = new DirectoryTemplateManager();
        
        m.addDirectoryTemplate(createStandardBlock());        
        m.addDirectoryTemplate(createImplApiBlock()); 
        
        m.store(configFileLocation);
        m = DirectoryTemplateManager.load(configFileLocation);
        
        assertNotNull("Could not reload xml file", m);

    }

    /**
     * Create the directory structure in project.
     * Add standard libraries 
     * @throws CoreException
     *
     */
    public final void testCreateStandardBlockProject() throws CoreException
    {

        project = BlockProjectManager.testCreateProject("StandardBlockTest");
        DirectoryTemplateManager m = DirectoryTemplateManager.load(configFileLocation);
        m.create("StandardBlock", project.getProject());
        
        assertEquals("Project was not created", true, (new File(project.getLocation().toString()).exists()));
        assertEquals("Sourcefolder not created", true, (new File(project.getLocation().toString()+"/src").exists()));
        assertEquals("Subfolder was not created", true, (new File(project.getLocation().toString()+"/src/BLOCK-INF").exists()));
        assertEquals("Test folder was not created", true, (new File(project.getLocation().toString()+"/test").exists()));
        assertEquals("Docs folder was not created", true, (new File(project.getLocation().toString()+"/docs").exists()));
    }

    /**
     * Test, whether libraries are added
     *
     */
    public final void testStandardBlockClasspath()
    {
        int libCount=0;
        int rootCount=0;
        int sourceCount=0;
        
        DirectoryTemplateManager m = DirectoryTemplateManager.load(configFileLocation);
        DirectoryTemplate template = m.getTemplate("StandardBlock");

        Vector sourceFolders = template.getSourceFolderNames();
        
        try
        {
            IJavaProject javaProject = JavaCore.create(project);
            IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();

            for(int i=0; i<roots.length; i++)
            {
                rootCount++;
                if(roots[i].isArchive())
                {
                    libCount++;
                } else {
                    sourceCount++;
                }
            }
        } catch (JavaModelException e)
        {
            fail("can't retrieve project libraries");
        }
        
        assertEquals("not all required sourcedirectories are created", sourceFolders.size(), sourceCount);
        
        BlockProjectManager.delete(project);
    }

    /**
     * Create the directory structure in project.
     * Add standard libraries 
     *
     */
    public final void testCreateImplApiBlockProject()
    {
        project = BlockProjectManager.create("ImplApiTest", null);
        DirectoryTemplateManager m = DirectoryTemplateManager.load(configFileLocation);
        m.create("ImplApiBlock", project.getProject());
        
        assertEquals("Project was not created", true, (new File(project.getLocation().toString()).exists()));
        assertEquals("Implfolder not created", true, (new File(project.getLocation().toString()+"/impl").exists()));
        assertEquals("Apifolder not created", true, (new File(project.getLocation().toString()+"/api").exists()));
        assertEquals("Subfolder was not created", true, (new File(project.getLocation().toString()+"/impl/BLOCK-INF").exists()));
        assertEquals("Test folder was not created", true, (new File(project.getLocation().toString()+"/test").exists()));
        assertEquals("Docs folder was not created", true, (new File(project.getLocation().toString()+"/docs").exists()));
    }

    /**
     * Test, whether libraries are added
     *
     */
    public final void testImplApiBlockClasspath()
    {
        int libCount=0;
        int rootCount=0;
        int sourceCount=0;
        
        DirectoryTemplateManager m = DirectoryTemplateManager.load(configFileLocation);
        DirectoryTemplate template = m.getTemplate("ImplApiBlock");
        
        Vector sourceFolders = template.getSourceFolderNames();

        try
        {
            IJavaProject javaProject = JavaCore.create(project);
            IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();

            for(int i=0; i<roots.length; i++)
            {
                rootCount++;
                if(roots[i].isArchive())
                {
                    libCount++;
                } else {
                    sourceCount++;
                }
            }
        } catch (JavaModelException e)
        {
            fail("can't retrieve project libraries");
        }
        
        assertEquals("not all required sourcedirectories are created", sourceFolders.size(), sourceCount);
    }

    /**
     * Delete the project
     *
     */
    public final void testDeleteProject()
    {
        String path = project.getLocation().toString();
        try
        {
            
            project.delete(true, true, null);
        } catch (CoreException e)
        {
            fail("can't delete project");
        }
        assertEquals("project not deleted", false, new File(path).exists());
    }
    
    /**
     * 
     */
    private DirectoryTemplate createStandardBlock()
    {
        DirectoryTemplate dt = new DirectoryTemplate();
        dt.setId("StandardBlock");

        
        Directory d = new Directory();
        d.setName("src");
        d.setSource(true);
        dt.addDirectory(d);
        
        d = new Directory();
        d.setName("src/BLOCK-INF");
        d.setSource(false);
        dt.addDirectory(d);
        
        d = new Directory();
        d.setName("test");
        d.setSource(true);
        dt.addDirectory(d);
        
        d = new Directory();
        d.setName("docs");
        d.setSource(false);
        dt.addDirectory(d);
        
        return dt;
    }
    
    /**
     * 
     */
    private DirectoryTemplate createImplApiBlock()
    {
        DirectoryTemplate dt = new DirectoryTemplate();
        dt.setId("ImplApiBlock");

        Directory d = new Directory();
        d.setName("impl");
        d.setSource(true);
        dt.addDirectory(d);

        d = new Directory();
        d.setName("api");
        d.setSource(true);
        dt.addDirectory(d);
        
        d = new Directory();
        d.setName("impl/BLOCK-INF");
        d.setSource(false);
        dt.addDirectory(d);
        
        d = new Directory();
        d.setName("test");
        d.setSource(true);
        dt.addDirectory(d);
        
        d = new Directory();
        d.setName("docs");
        d.setSource(false);
        dt.addDirectory(d);
        
        return dt;
    }

}

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

import org.apache.metro.studio.eclipse.core.MetroStudioCore;
import org.apache.metro.studio.eclipse.core.templateengine.BlockProjectManager;
import org.apache.metro.studio.eclipse.core.templateengine.DirectoryTemplateManager;
import org.apache.metro.studio.eclipse.core.templateengine.Library;
import org.apache.metro.studio.eclipse.core.templateengine.Resource;
import org.apache.metro.studio.eclipse.core.templateengine.ResourceTemplate;
import org.apache.metro.studio.eclipse.core.templateengine.ResourceTemplateManager;
import org.eclipse.core.resources.IProject;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Metro Development Team </a>
 *         12.08.2004 last change:
 *  
 */
public class ResourceTemplateManagerTest extends TestCase
{
    final static String directoryLocation = DirectoryTemplateManagerTest.configFileLocation;

    final static String baseDir = MetroStudioCore.getDefault()
            .getPluginLocation().toString();

    final static String resourcesLocation = baseDir
            + "config/resources.test_cfg";

    static IProject project;  

    public static void main(String[] args)
    {
    }

    public final void testCreateResourceTemplate()
    {
        ResourceTemplateManager.initXStream();
        ResourceTemplateManager rm = new ResourceTemplateManager();

        // import DirectoryTemplates, which are created in
        // DirectoryTemplatemanagerTest
        DirectoryTemplateManager dm = DirectoryTemplateManager
                .load(directoryLocation);
        rm.importDirectoryTemplates(dm);

        rm.addResourceTemplate(createHelloWorldResources());
        //rm.addResourceTemplate(createCompositionApplicationResources());
        //rm.addResourceTemplate(createConfigurationResources());
        rm.addResourceTemplate(createStandardContextResources());
        /*
        rm.addResourceTemplate(createCustomContextResources());
        rm.addResourceTemplate(createCastingContextResources());
        rm.addResourceTemplate(createAliasContextResources());
        rm.addResourceTemplate(createPlusContextResources());
        rm.addResourceTemplate(createStrategyContextResources());
        */

        rm.store(resourcesLocation);
        
        rm = ResourceTemplateManager.load(resourcesLocation);

        assertNotNull("Could not reload xml file", rm);

    }

    public final void testListTemplateNames()
    {
        String[] names = BlockProjectManager.listTemplateNames();
        
        ResourceTemplateManager rtm = ResourceTemplateManager.load(null);
        int number = rtm.getResourceTemplates().size();
        
        assertEquals("didnt get all template names ", number, names.length);
        
    }

    public final void testCreateHelloWorldProject()
    {
        project = BlockProjectManager.create("HelloWorld Tutorial", "HelloWorld Tutorial");
        assertNotNull("project was not created", project);
        
        String testpath;
        assertEquals("Project was not created", true, (new File(project.getLocation().toString()).exists()));
        testpath = "/src/tutorial/HelloComponent.java";
        assertEquals("HelloComponent not created", true, (new File(project.getLocation().toString()+testpath).exists()));
        testpath = "/src/BLOCK-INF/block.xml";
        assertEquals("block.xml not created", true, (new File(project.getLocation().toString()+testpath).exists()));

        BlockProjectManager.delete(project);
    }

    public final void testCreateStandardContextProject()
    {
        project = BlockProjectManager.create("Context Tutorial (Standard)", "Context Tutorial (Standard)");
        assertNotNull("project was not created", project);
        
        String testpath;
        assertEquals("Project was not created", true, (new File(project.getLocation().toString()).exists()));
        testpath = "/src/tutorial/HelloComponent.java";
        assertEquals("Standard Context Tutorial", true, (new File(project.getLocation().toString()+testpath).exists()));
        testpath = "/src/BLOCK-INF/block.xml";
        assertEquals("block.xml not created", true, (new File(project.getLocation().toString()+testpath).exists()));

        BlockProjectManager.delete(project);
    }

    /**
     * @return
     */
    private ResourceTemplate createHelloWorldResources()
    {
        ResourceTemplate rt = new ResourceTemplate();
        rt.setTemplateId("HelloWorld Tutorial");
        rt.setDescription("This tutorial takes you through the creation of a very simple component, the declaration of a component type descriptor, and the declaration of a block containing the component.");
        rt.setDirectoryType("StandardBlock");
        Library library = new Library();
        library.setName("avalon-framework-api");
        library.setVersion("4.2.0");
        rt.addLibrary(library);

        Resource r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/hello/HelloComponent.java");
        r.setPackageName("tutorial");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("src/BLOCK-INF");
        r.setSourceFilePathName(baseDir+"templates/hello/block.xml");
        r.setPackageName("");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/hello/HelloComponent.xinfo");
        r.setPackageName("tutorial");
        rt.addResource(r);

        return rt;
    }
    /**
     * @return
     */
    private ResourceTemplate createConfigurationResources()
    {
        ResourceTemplate rt = new ResourceTemplate();
        rt.setTemplateId("Configuration Tutorial");
        rt.setDescription("This example is the HelloComponent extended to include a configuration constructor argument and updates to log the source of the configuration based on runtime information.");
        rt.setDirectoryType("StandardBlock");
        Library library = new Library();
        library.setName("avalon-framework-api");
        library.setVersion("4.2.0");
        rt.addLibrary(library);

        Resource r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/configuration/HelloComponent.java");
        r.setPackageName("tutorial");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("src/BLOCK-INF");
        r.setSourceFilePathName(baseDir+"templates/configuration/block.xml");
        r.setPackageName("");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("src/BLOCK-INF");
        r.setSourceFilePathName(baseDir+"templates/configuration/categories.xml");
        r.setPackageName("");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("src/BLOCK-INF");
        r.setSourceFilePathName(baseDir+"templates/configuration/config.xml");
        r.setPackageName("");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/configuration/HelloComponent.xconfig");
        r.setPackageName("tutorial");
        rt.addResource(r);
        
        return rt;
    }
    /**
     * @return
     */
    private ResourceTemplate createCustomContextResources()
    {
        ResourceTemplate rt = new ResourceTemplate();
        rt.setTemplateId("Context Tutorial(Custom)");
        rt.setDescription("This tutorial presents information about the management of the runtime context supplied to your component.");
        rt.setDirectoryType("StandardBlock");
        Library library = new Library();
        library.setName("avalon-framework-api");
        library.setVersion("4.2.0");
        rt.addLibrary(library);

        Resource r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/custom/HelloComponent.java");
        r.setPackageName("tutorial");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/custom/NumberCruncher.java");
        r.setPackageName("tutorial");
        rt.addResource(r);
        
        r = new Resource();
        r.setRootSegment("src/BLOCK-INF");
        r.setSourceFilePathName(baseDir+"templates/context/custom/block.xml");
        r.setPackageName("");
        rt.addResource(r);
        
        return rt;
    }
    /**
     * @return
     */
    private ResourceTemplate createCastingContextResources()
    {
        ResourceTemplate rt = new ResourceTemplate();
        rt.setTemplateId("Context Tutorial (Casting)");
        rt.setDescription("This tutorial covers the declaration of context casting criteria and the creation of a typed context.");
        rt.setDirectoryType("StandardBlock");
        Library library = new Library();
        library.setName("avalon-framework-api");
        library.setVersion("4.2.0");
        rt.addLibrary(library);

        Resource r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/casting/HelloComponent.java");
        r.setPackageName("tutorial");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/casting/DemoContext.java");
        r.setPackageName("tutorial");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/casting/DemoContextProvider.java");
        r.setPackageName("tutorial");
        rt.addResource(r);
        
        r = new Resource();
        r.setRootSegment("src/BLOCK-INF");
        r.setSourceFilePathName(baseDir+"templates/context/casting/block.xml");
        r.setPackageName("");
        rt.addResource(r);
        
        return rt;
    }
    /**
     * @return
     */
    private ResourceTemplate createStandardContextResources()
    {
        ResourceTemplate rt = new ResourceTemplate();
        rt.setTemplateId("Context Tutorial (Standard)");
        rt.setDescription("This tutorial presents information about the management of the runtime context supplied to your component.");
        rt.setDirectoryType("StandardBlock");
        Library library = new Library();
        library.setName("avalon-framework-api");
        library.setVersion("4.2.0");
        rt.addLibrary(library);

        Resource r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/standard/HelloComponent.java");
        r.setPackageName("tutorial");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/standard/HelloComponent.xinfo");
        r.setPackageName("tutorial");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("src/BLOCK-INF");
        r.setSourceFilePathName(baseDir+"templates/context/standard/block.xml");
        r.setPackageName("");
        rt.addResource(r);
        
        return rt;
    }
    /**
     * @return
     */
    private ResourceTemplate createCompositionApplicationResources()
    {
        ResourceTemplate rt = new ResourceTemplate();
        rt.setTemplateId("Composition (Application) Tutorial");
        rt.setDirectoryType("ImplApiBlock");

        Resource r = new Resource();
        r.setRootSegment("impl");
        r.setSourceFilePathName(baseDir+"templates/composition/application/Application.java");
        r.setPackageName("tutorial.application");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("impl/BLOCK-INF");
        r.setSourceFilePathName(baseDir+"templates/composition/application/block.xml");
        r.setPackageName("");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("impl/BLOCK-INF");
        r.setSourceFilePathName(baseDir+"templates/composition/application/debug.xml");
        r.setPackageName("");
        rt.addResource(r);

        return rt;
    }
    /**
     * @return
     */
    private ResourceTemplate createAliasContextResources()
    {
        ResourceTemplate rt = new ResourceTemplate();
        rt.setTemplateId("Context Tutorial  (Alias)");
        rt.setDescription("This tutorial covers usage of standard context entries using a constructor supplied context (as opposed to the classic Contextualization delivery mecahanism).");
        rt.setDirectoryType("StandardBlock");

        Resource r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/alias/HelloComponent.java");
        r.setPackageName("tutorial.application");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("impl/BLOCK-INF");
        r.setSourceFilePathName(baseDir+"templates/context/alias/block.xml");
        r.setPackageName("");
        rt.addResource(r);

        return rt;
    }
    /**
     * @return
     */
    private ResourceTemplate createPlusContextResources()
    {
        ResourceTemplate rt = new ResourceTemplate();
        rt.setTemplateId("Context Tutorial (Plus)");
        rt.setDescription("This tutorial covers usage of context entries using a constructor supplied custom context");
        rt.setDirectoryType("StandardBlock");
        Library library = new Library();
        library.setName("avalon-framework-api");
        library.setVersion("4.2.0");
        rt.addLibrary(library);

        Resource r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/plus/HelloComponent.java");
        r.setPackageName("tutorial");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/plus/DemoContext.java");
        r.setPackageName("tutorial");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/plus/DemoContextProvider.java");
        r.setPackageName("tutorial");
        rt.addResource(r);
        
        r = new Resource();
        r.setRootSegment("src/BLOCK-INF");
        r.setSourceFilePathName(baseDir+"templates/context/plus/block.xml");
        r.setPackageName("");
        rt.addResource(r);
        
        return rt;
    }
    /**
     * @return
     */
    private ResourceTemplate createStrategyContextResources()
    {
        ResourceTemplate rt = new ResourceTemplate();
        rt.setTemplateId("Context Tutorial (Strategy)");
        rt.setDescription("This tutorial covers the declaration of custom contextualization strategy.");
        rt.setDirectoryType("StandardBlock");
        Library library = new Library();
        library.setName("avalon-framework-api");
        library.setVersion("4.2.0");
        rt.addLibrary(library);

        Resource r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/strategy/StandardComponent.java");
        r.setPackageName("tutorial");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/strategy/StandardService.java");
        r.setPackageName("tutorial");
        rt.addResource(r);
        
        r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/strategy/StandardContext.java");
        r.setPackageName("tutorial");
        rt.addResource(r);

        r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/strategy/StandardContextImp.java");
        r.setPackageName("tutorial");
        rt.addResource(r);
        
        r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/strategy/Contextualizable.java");
        r.setPackageName("tutorial");
        rt.addResource(r);
        
        r = new Resource();
        r.setRootSegment("src");
        r.setSourceFilePathName(baseDir+"templates/context/strategy/DemoContextualizationHandler.java");
        r.setPackageName("tutorial");
        rt.addResource(r);
        
        r = new Resource();
        r.setRootSegment("src/BLOCK-INF");
        r.setSourceFilePathName(baseDir+"templates/context/strategy/block.xml");
        r.setPackageName("");
        rt.addResource(r);
        
        return rt;
    }
}
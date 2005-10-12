/*
 * Copyright 2005 Stephen J. McConnell
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.tools.control;

import java.io.File;

import net.dpml.tools.model.Module;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Project;
import net.dpml.tools.info.Scope;

import net.dpml.transit.monitor.LoggingAdapter;

/**
 * Test the DefaultLibrary implementation.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultLibraryTestCase extends AbstractTestCase
{   
    private DefaultLibrary m_library;
    
    public void setUp() throws Exception
    {
        String testPath = System.getProperty( "project.test.dir" );
        File test = new File( testPath );
        File example = new File( test, "dpml.xml" );
        LoggingAdapter logger = new LoggingAdapter( "library" );
        m_library = new DefaultLibrary( logger, example );
    }
    
    public void testRootModules() throws Exception
    {
        Module[] modules = m_library.getModules();
        for( int i=0; i<modules.length; i++ )
        {
            listModule( "# ", modules[i] );
        }
    }
    
    private void listModule( String pad, Module module ) throws Exception
    {
        System.out.println( pad + "module: " + module.getName() );
        String p = pad + "  ";
        Resource[] resources = module.getResources();
        for( int i=0; i<resources.length; i++ )
        {
            listResource( p, resources[i], "resource: " );
        }
        Project[] projects = module.getProjects();
        for( int i=0; i<projects.length; i++ )
        {
            listProject( p, projects[i] );
        }
        Module[] modules = module.getModules();
        for( int i=0; i<modules.length; i++ )
        {
            listModule( p, modules[i] );
        }
    }
    
    private void listResource( String pad, Resource resource, String tag ) throws Exception
    {
        System.out.println( pad + tag + resource.getName() );
        String p = pad + "  ";
        Resource[] resources = resource.getDependencies();
        for( int i=0; i<resources.length; i++ )
        {
            listResource( p, resources[i], "dependency: " );
        }
    }

    private void listProject( String pad, Project project ) throws Exception
    {
        System.out.println( pad + project.getName() );
        String p = pad + "  ";
        Resource[] resources = project.getDependencies( Scope.TEST );
        for( int i=0; i<resources.length; i++ )
        {
            listResource( p, resources[i], "dependency: " );
        }
    }

}

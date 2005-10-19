/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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

package net.dpml.tools.ant;

import java.io.File;
import java.beans.Expression;
import java.beans.BeanDescriptor;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.beans.Encoder;

import net.dpml.tools.ant.Definition;
import net.dpml.tools.info.Scope;
import net.dpml.tools.model.Library;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

/**
 * Project context.
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: InfoDescriptor.java 2522 2005-05-12 11:23:50Z mcconnell@dpml.net $
 */
public final class Context
{
    private final Definition m_definition;
    private final Library m_library;
        
    Context( Definition definition, Library library, Project project )
    {
        m_definition = definition;
        m_library = library;
        
        String[] names = definition.getPropertyNames();
        for( int i=0; i<names.length; i++ )
        {
            String name = names[i];
            String value = definition.getProperty( name );
            project.setNewProperty( name, value );
        }
        
        final Path compilePath = definition.getPath( project, Scope.BUILD );
        compilePath.add( definition.getPath( project, Scope.RUNTIME ) );
        project.addReference( "project.compile.path", compilePath );
        
        final Path compileSrcPath = new Path( project );
        File srcMain = definition.getTargetBuildMainDirectory();
        compileSrcPath.createPathElement().setLocation( srcMain );
        project.addReference( "project.build.src.path", compileSrcPath );
        
        final File testClasses = definition.getTargetClassesTestDirectory();
        final Path testPath = definition.getPath( project, Scope.TEST );
        testPath.add( compilePath );
        testPath.createPathElement().setLocation( testClasses );
        project.addReference( "project.test.path", testPath );
        
        project.setNewProperty( "project.name", definition.getName() );
        project.setNewProperty( "project.version", definition.getVersion() );
        project.setNewProperty( "project.group", definition.getGroup() );
        project.setNewProperty( "project.nl", "\n" );
        project.setNewProperty( 
          "project.line", 
          "---------------------------------------------------------------------------\n" );
        project.setNewProperty( 
          "project.info", 
          "---------------------------------------------------------------------------\n"
          + definition.getProjectPath()
          + "\n---------------------------------------------------------------------------" );
        
        project.setNewProperty( "project.src.dir", definition.getSrcDirectory().toString() );
        project.setNewProperty( "project.src.main.dir", definition.getSrcMainDirectory().toString() );
        project.setNewProperty( "project.src.test.dir", definition.getSrcTestDirectory().toString() );
        project.setNewProperty( "project.etc.dir", definition.getEtcDirectory().toString() );
        
        project.setNewProperty( "project.target.dir", definition.getTargetDirectory().toString() );
        project.setNewProperty( "project.target.build.main.dir", definition.getTargetBuildMainDirectory().toString() );
        project.setNewProperty( "project.target.build.test.dir", definition.getTargetBuildTestDirectory().toString() );
        project.setNewProperty( "project.target.classes.main.dir", definition.getTargetClassesMainDirectory().toString() );
        project.setNewProperty( "project.target.classes.test.dir", definition.getTargetClassesTestDirectory().toString() );
        project.setNewProperty( "project.target.deliverables.dir", definition.getTargetDeliverablesDirectory().toString() );
        project.setNewProperty( "project.target.test.dir", definition.getTargetTestDirectory().toString() );
    }
    
    public Definition getDefinition()
    {
        return m_definition;
    }
    
    public Library getLibrary()
    {
        return m_library;
    }
}


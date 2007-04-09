/*
 * Copyright 2006 Stephen McConnell
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

package dpml.tools.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dpml.library.Scope;

import dpml.tools.Context;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Rmic;
import org.apache.tools.ant.types.Path;

/**
 * Compile sources located in ${project.target}/main to java class file under
 * the ${project.target}/classes directory.  Properties influencing the compilation
 * include:
 * <ul>
 *  <li>project.javac.debug : boolean true (default) or false</li>
 *  <li>project.javac.fork: boolean true or false (default) </li>
 *  <li>project.javac.deprecation: boolean true (default) or false</li>
 * </ul>
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class RMICTask extends GenericTask
{
    static
    {
        System.setProperty( 
          "com.sun.CORBA.ORBDynamicStubFactoryFactoryClass", 
          "com.sun.corba.se.impl.presentation.rmi.StubFactoryFactoryStaticImpl" );
    }
    
    private List<Include> m_includes = new ArrayList<Include>();
    private List<Include> m_excludes = new ArrayList<Include>();
    private String m_classPathRef;
    private File m_base;
    
    public RMICTask()
    {
        super();
    }
    
   /**
    * Set the id of the compilation classpath.
    * @param id the classpath reference
    */
    public void setClasspathRef( String id ) 
    {
        m_classPathRef = id;
    }
    
   /**
    * Set the base directory.
    * @param base the base directory
    */
    public void setBase( File base )
    {
        m_base = base;
    }
    
    public Include createInclude()
    {
        Include include = new Include();
        m_includes.add( include );
        return include;
    }
    
    public Include createExclude()
    {
        Include include = new Include();
        m_excludes.add( include );
        return include;
    }
    
    public void init()
    {
        setTaskName( "rmic" );
    }
    
   /**
    * Task execution.
    */
    public void execute()
    {
        Context context = getContext();
        if( null == m_base )
        {
            setBase( context.getTargetClassesMainDirectory() );
        }
        
        if( !m_base.exists() )
        {
            return;
        }
        
        final Rmic rmic = (Rmic) getProject().createTask( "rmic" );
        rmic.setTaskName( "rmic" );
        final Project project = getProject();
        rmic.setProject( project );
        rmic.setBase( m_base );
        final Path classpath = getClasspath();
        
        rmic.setClasspath( classpath );
        
        // populate includes/excludes
        
        for( Include include : m_includes )
        {
            String name = include.getName();
            rmic.createInclude().setName( name );
        }
        
        for( Include exclude : m_excludes )
        {
            String name = exclude.getName();
            rmic.createExclude().setName( name );
        }
        
        // initialize and execute
        
        rmic.init();
        rmic.execute();
    }
    
    private Path getClasspath()
    {
        if( null != m_classPathRef )
        {
            return (Path) getProject().getReference( m_classPathRef );
        }
        else
        {
            getContext().getPath( Scope.RUNTIME );
            setClasspathRef( "project.compile.path" );
            return getClasspath();
        }
    }
    
    public static class Include
    {
        private String m_name;
        
        public void setName( String name )
        {
            m_name = name;
        }
        
        public String getName()
        {
            return m_name;
        }
    }
}

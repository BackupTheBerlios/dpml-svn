/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.tools.tasks;

import org.apache.avalon.tools.model.Info;
import org.apache.avalon.tools.model.Resource;
import org.apache.avalon.tools.model.Home;
import org.apache.avalon.tools.model.XMLDefinitionBuilder;
import org.apache.avalon.tools.model.ElementHelper;
import org.apache.avalon.tools.model.Plugin.ListenerDef;
import org.apache.avalon.tools.model.Plugin.TaskDef;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.AntClassLoader;

import org.w3c.dom.Element;

import java.io.File;
import java.lang.reflect.Constructor;

/**
 * Load a plugin. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class PluginTask extends SystemTask
{
    private String m_id;
    private String m_name;

    public void setName( final String name )
    {
        m_name = name;
    }

    public void setUri( final String id )
    {
        m_id = id;
    }

    private String getArtifactSpec()
    {
        if( null != m_id )
        {
            return m_id;
        }
        else
        {
            final String error = 
              "Missing plugin 'artifact' attribute.";
            throw new BuildException( error );
        }
    }

    public void execute() throws BuildException 
    {
        try
        {
            //
            // get the xml definition of the plugin
            //

            final String id = getArtifactSpec();
            final Info info = Info.create( getHome(), id );

            final Project project = getProject();
            final Resource resource = new Resource( getHome(), info );
            final File file = resource.getArtifact( project );

            //
            // create a utility data object from the defintion
            //

            final AntLibData data = new AntLibData( getHome(), getProject(), file );

            final AntClassLoader classloader = project.createClassLoader( data.getPath() );
            final String spec = data.getInfo().getSpec();
            final String uri = getURI( spec );

            log( "Install \"" + uri + "\"" );

            //
            // install the ant task defintions
            //

            final ComponentHelper helper =
              ComponentHelper.getComponentHelper( project );
            final TaskDef[] defs = data.getTaskDefs();
            if(( defs.length == 1 ) && ( null != m_name ))
            {
                final TaskDef def = defs[0];
                final Class taskClass = classloader.loadClass( def.getClassname() );
                helper.addTaskDefinition( m_name, taskClass );
                log( "Task \"" + m_name + "\"" );
            }
            else
            {
                for( int i=0; i<defs.length; i++ )
                {
                    final TaskDef def = defs[i];
                    final Class taskClass = classloader.loadClass( def.getClassname() );
                    final String name = uri + ":" + def.getName();
                    helper.addTaskDefinition( name, taskClass );
                    log( "Task \"" + name + "\"" );
                }
            }

            //
            // register plugins that declare themselves as build listeners
            //

            final ListenerDef[] listeners = data.getListenerDefs();
            for( int i=0; i<listeners.length; i++ )
            {
                final ListenerDef def = listeners[i];
                final Class listenerClass = classloader.loadClass( def.getClassname() );
                final BuildListener listener = createBuildListener( listenerClass, data, uri );
                getProject().addBuildListener( listener );
            }
        }
        catch( Throwable e )
        {
            throw new BuildException( e );
        }
    }

    private String getURI( String spec )
    { 
        if( spec.indexOf( "#" ) > -1 )
        {
            return "plugin:" + spec.substring( 0, spec.indexOf( "#" ) );
        }
        else
        {
            return "plugin:" + spec;
        }
    }

   /**
    * Create a build listerer using a supplied class.  The implementation
    * checks the first available constructor arguments and builds a set of 
    * arguments based on the arguments supplied to this task.
    *
    * @param clazz the listener class
    * @return a instance of the class
    * @exception BuildException if the class does not expose a public 
    *    constructor, or the constructor requires arguments that the 
    *    method cannot resolve, or if a unexpected instantiation error 
    *    ooccurs
    */ 
    public BuildListener createBuildListener( final Class clazz, final AntLibData data, final String uri )
      throws BuildException
    {
        if( !BuildListener.class.isAssignableFrom( clazz ) )
        {
            final String error = 
              "Listener class [" + clazz.getName() 
              + "] declared within the plugin ["
              + uri
              + "] does not implement BuildListener.";
            throw new BuildException( error );
        }

        final Constructor[] constructors = clazz.getConstructors();
        if( constructors.length < 1 ) 
        {
            final String error = 
              "Cannot handle listeners classes with more than one public constructor.";
            throw new BuildException( error );
        }

        final Constructor constructor = constructors[0];
        final Class[] classes = constructor.getParameterTypes();
        final Object[] args = new Object[ classes.length ];
        for( int i=0; i<classes.length; i++ )
        {
            final Class c = classes[i];
            if( AntLibData.class.isAssignableFrom( c ) )
            {
                args[i] = data;
            }
            else if( String.class.isAssignableFrom( c ) )
            {
                args[i] = uri;
            }
            else
            {
                final String error = 
                  "Unrecognized constructor parameter: " + c.getName();
                throw new BuildException( error );
            }
        }

        //
        // instantiate the factory
        //

        return instantiateBuildListener( constructor, args );
    }

   /**
    * Instantiation of a listener instance using a supplied constructor 
    * and arguments.
    * 
    * @param constructor the class constructor
    * @param args the constructor arguments
    * @return the listener instance
    * @exception BuildException if an instantiation error occurs
    */
    private BuildListener instantiateBuildListener( 
      final Constructor constructor, final Object[] args )
      throws BuildException
    {
        try
        {
            return (BuildListener) constructor.newInstance( args );
        }
        catch( Throwable e )
        {
            throw new BuildException( e );
        }
    }

    private class AntLibData 
    {
        final Project project = getProject();
        private final Info m_info;
        private final Path m_path = new Path( project );
        private final TaskDef[] m_tasks;
        private final ListenerDef[] m_listeners;

        public AntLibData( final Home home, final Project project, final File file )
        {
            final Element root = ElementHelper.getRootElement( file );
            final Element infoElement = ElementHelper.getChild( root, "info" );
            m_info = XMLDefinitionBuilder.createInfo( home, infoElement );
            final Element tasksElement = ElementHelper.getChild( root, "tasks" );
            m_tasks = XMLDefinitionBuilder.getTaskDefs( tasksElement );
            final Element listenerElement = ElementHelper.getChild( root, "listeners" );
            m_listeners = XMLDefinitionBuilder.getListenerDefs( listenerElement );

            final Element classpathElement = ElementHelper.getChild( root, "classpath" );
            final Element[] children = ElementHelper.getChildren( classpathElement, "artifact" );
            for( int i=0; i<children.length; i++ )
            {
                final Element child = children[i];
                final String value = ElementHelper.getValue( child );
                final String type = getArtifactType( value );
                final String uri = getArtifactURI( value );
                final Info info = Info.create( home, type, uri );
                final Resource resource = new Resource( home, info );
                final File jar = resource.getArtifact( project );
                m_path.createPathElement().setLocation( jar );
            }
        }

        public Info getInfo()
        {
            return m_info;
        }

        public TaskDef[] getTaskDefs()
        {
            return m_tasks;
        }

        public ListenerDef[] getListenerDefs()
        {
            return m_listeners;
        }

        public Path getPath()
        {
            return m_path;
        }

        private String getArtifactType( final String value )
        {
            final int i = value.indexOf( ":" );
            if( i > 0 ) return value.substring( 0, i );
            return "jar";
        }

        private String getArtifactURI( final String value )
        {
            final int i = value.indexOf( ":" );
            if( i > 0 ) return value.substring( i+1 );
            return value;
        }
    }
}

/*
 * Copyright 2004-2005 Stephen McConnell
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

package net.dpml.transit.tools;

import java.io.InputStream;

import java.net.URI;

import java.util.List;
import java.util.ArrayList;

import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.Plugin;
import net.dpml.transit.util.ElementHelper;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ComponentHelper;

import org.w3c.dom.Element;

/**
 * The plugin task handles the establishment of ant tasks, listeners, and antlibs derived
 * from a classloader established by the transit sub-system.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PluginTask extends TransitTask
{
    private static Repository m_REPOSITORY;

   /**
    * The uri of the plugin to load.
    */
    private String m_uri;

   /**
    * A list of tasks declared by the plugin declaration.
    */
    private List m_tasks = new ArrayList();

   /**
    * List of listeners declared by the plugin task declaration.
    */
    private List m_listeners = new ArrayList();

   /**
    * List of antlibs declared by the plugin task declaration.
    */
    private List m_antlibs = new ArrayList();

   /**
    * Plugin name delalred by the plugin task declaration.
    */
    private String m_name;

   /**
    * A flag indicating that nested directives have been provided.
    */
    private boolean m_flag = false;

   /**
    * Set the project.
    * @param project the current project
    */
    public void setProject( Project project )
    {
        setTaskName( "plugin" );
        super.setProject( project );
    }

   /**
    * Create and associate a new antlib urn entry with the plugin.
    * @return the new antlib entry
    */
    public Antlib createAntlib()
    {
        m_flag = true;
        final Antlib antlib = new Antlib();
        m_antlibs.add( antlib );
        return antlib;
    }

   /**
    * Create and associate a new task entry with the plugin.
    * @return the new task entry
    */
    public Task createTask()
    {
        m_flag = true;
        final Task task = new Task();
        m_tasks.add( task );
        return task;
    }

   /**
    * Create and associate a new build listener with the plugin.
    * @return the new listener entry
    */
    public Listener createListener()
    {
        m_flag = true;
        final Listener listener = new Listener();
        m_listeners.add( listener );
        return listener;
    }

   /**
    * Set the artifact uri of the plugin from which the task is to be loaded.
    * @param uri an artifact plugin uri
    */
    public void setUri( String uri )
    {
        m_uri = uri;
    }

   /**
    * Return the artifact uri of the plugin.
    * @return the plugin uri
    */
    public URI getUri()
    {
        try
        {
            return new URI( m_uri );
        }
        catch( Throwable e )
        {
            final String error =
              "Cound not convert the supplied uri spec ["
              + m_uri
              + "] to a formal URI.";
            throw new BuildException( error, e, getLocation() );
        }
    }

   /**
    * Load the plugin and handle registration of listeners, tasks, and antlib
    * declarations based on the nested nested task directives.
    * @exception BuildException if an error occurs during plugin loading or deployment
    */
    public void execute() throws BuildException
    {
        if( null == m_uri )
        {
            final String error =
              "Missing uri attribute.";
            throw new BuildException( error );
        }

        final Project project = getProject();
        //ClassLoader classloader = this.getClass().getClassLoader();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final ComponentHelper helper =
          ComponentHelper.getComponentHelper( project );

        if( !m_flag )
        {
            createAntlib();
        }

        try
        {
            URI uri = new URI( m_uri );
            ClassLoader loader =
              getRepository().getPluginClassLoader( classloader, uri );

            Task[] tasks = (Task[]) m_tasks.toArray( new Task[0] );
            if( tasks.length > 0 )
            {
                for( int i=0; i < tasks.length; i++ )
                {
                    try
                    {
                        Task task = tasks[i];
                        String classname = task.getClassname();
                        String name = task.getName();
                        Class c = loader.loadClass( classname );
                        helper.addTaskDefinition( name, c );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "Failed to load a named task ["
                          + tasks[i].getName()
                          + "] from the plugin ["
                          + uri
                          + "].";
                        throw new BuildException( error, e, getLocation() );
                    }
                }
            }

            Listener[] listeners = (Listener[]) m_listeners.toArray( new Listener[0] );
            for( int i=0; i < listeners.length; i++ )
            {
                Listener listener = listeners[i];
                String classname = listener.getClassname();
                Class c = loader.loadClass( classname );
                Object object = c.newInstance();
                if( object instanceof BuildListener )
                {
                    BuildListener instance = (BuildListener) object;
                    getProject().addBuildListener( instance );
                    log( "registered listener: " + instance.getClass().getName() );
                }
                else
                {
                    final String error =
                      "The plugin ["
                      + uri
                      + "] establishing the class ["
                      + object.getClass().getName()
                      + "] could not be registered as a project listener because it does not implement the ["
                      + BuildListener.class.getName()
                      + "] interface.";
                    throw new BuildException( error, getLocation() );
                }
            }

            Antlib[] antlibs = (Antlib[]) m_antlibs.toArray( new Antlib[0] );
            for( int i=0; i < antlibs.length; i++ )
            {
                loadAntlib( uri, loader, helper, antlibs[i] );
            }
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = "Unable to load the plugin ["
              + m_uri
              + "] due to "
              + e.toString();
            throw new BuildException( error, e, getLocation() );
        }
    }

   /**
    * Load an antlib.
    * @param classloader the classloader from which the antlib will be loaded
    * @param helper the component helper
    * @param antlib the antlib to load
    * @exception Exception if it doesn't work out
    */
    private void loadAntlib(
      URI uri, ClassLoader classloader, ComponentHelper helper, Antlib antlib ) throws Exception
    {
        Plugin descriptor = getRepository().getPluginDescriptor( uri );
        String resource = antlib.getPath();
        if( null == resource )
        {
            resource = descriptor.getResource();
        }
        if( null == resource )
        {
            final String error =
              "Resource path for the antlib is not declared in the plugin descriptor "
              + "or antlib directive ["
              + uri
              + "]";
            throw new BuildException( error, getLocation() );
        }

        String urn = antlib.getURN();
        if( null == urn )
        {
            urn = descriptor.getURN();
        }
        if( null == urn )
        {
            final String error =
              "URN for the antlib is not declared in the plugin descriptor "
              + "or antlib directive ["
              + uri
              + "]";
            throw new BuildException( error, getLocation() );
        }

        InputStream input = classloader.getResourceAsStream( resource );
        Element root = ElementHelper.getRootElement( input );
        Element[] tasks = ElementHelper.getChildren( root, "taskdef" );
        for( int i=0; i < tasks.length; i++ )
        {
            Element task = tasks[i];
            String name = ElementHelper.getAttribute( task, "name" );
            String classname = ElementHelper.getAttribute( task, "classname" );
            loadTaskDef( classloader, helper, classname, urn + ":" + name );
        }

        Element[] types = ElementHelper.getChildren( root, "typedef" );
        for( int i=0; i < types.length; i++ )
        {
            Element type = types[i];
            String name = ElementHelper.getAttribute( type, "name" );
            String classname = ElementHelper.getAttribute( type, "classname" );
            loadTypeDef( classloader, helper, classname, urn + ":" + name );
        }
    }

   /**
    * Load a single task defintion.
    * @param loader the classloader from which the task will be loaded
    * @param helper the component helper
    * @param classname the task classname
    * @param name the task name
    * @exception BuildException if an error occurs while attempting to load the task
    */
    private void loadTaskDef( ClassLoader loader, ComponentHelper helper, String classname, String name )
      throws BuildException
    {
        if( getProject().getTaskDefinitions().get( name ) != null )
        {
            return;
        }

        try
        {
            Class c = loader.loadClass( classname );
            helper.addTaskDefinition( name, c );
            log( "installed taskdef: " + name, Project.MSG_VERBOSE );
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
             final String error =
               "Unable to load task [" 
               + name 
               + "] from class [" 
               + classname
               + "].";
             throw new BuildException( error, e, getLocation() );
        }
    }

   /**
    * Load a single type defintion.
    * @param loader the classloader from which the type will be loaded
    * @param helper the component helper
    * @param classname the type classname
    * @param name the task type
    * @exception BuildException if an error occurs while attempting to load the task
    */
    private void loadTypeDef( ClassLoader loader, ComponentHelper helper, String classname, String name )
      throws BuildException
    {
        if( getProject().getDataTypeDefinitions().get( name ) != null )
        {
            return;
        }

        try
        {
            Class c = loader.loadClass( classname );
            helper.addDataTypeDefinition( name, c );
            log( "installed typedef: " + name, Project.MSG_VERBOSE );
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
             final String error =
               "Unable to load type [" 
               + name + "] from class [" 
               + classname
               + "].";
             throw new BuildException( error, e, getLocation() );
        }
    }

   /**
    * Nested element with the &lt;plugin&gt; element declaring the name and class of
    * a task to be loaded from the classloader established by the transit plugin descriptor.
    */
    public static class Task
    {
       /**
        * The task name.
        */
        private String m_name;

       /**
        * The task classname.
        */
        private String m_classname;

       /**
        * Set the task name.
        * @param name the name of the task
        */
        public void setName( final String name )
        {
            m_name = name;
        }

       /**
        * Set the task classname.
        * @param classname the task classname
        */
        public void setClass( final String classname )
        {
            m_classname = classname;
        }

       /**
        * Return the task classname.
        * @return the classname
        * @exception BuildException if the class attribute is missing
        */
        public String getClassname() throws BuildException
        {
            if( null == m_classname )
            {
                final String error =
                  "Missing class attribute.";
                throw new BuildException( error );
            }
            return m_classname;
        }

       /**
        * Return the task name.
        * @return the name
        * @exception BuildException if the name attribute is missing
        */
        public String getName() throws BuildException
        {
            if( null == m_name )
            {
                final String error =
                  "Missing name attribute.";
                throw new BuildException( error );
            }
            return m_name;
        }
    }

   /**
    * Nested element with the &lt;plugin&gt; element declaring the name and class of
    * a project listener to be loaded from the classloader established by the transit plugin descriptor.
    */
    public static class Listener
    {
       /**
        * The listener classname.
        */
        private String m_classname;

       /**
        * Set the task classname.
        * @param classname the task classname
        */
        public void setClass( final String classname )
        {
            m_classname = classname;
        }

       /**
        * Return the task classname.
        * @return the classname
        * @exception BuildException if the class attribute is missing
        */
        public String getClassname() throws BuildException
        {
            if( null == m_classname )
            {
                final String error =
                  "Missing class attribute.";
                throw new BuildException( error );
            }
            return m_classname;
        }
    }

   /**
    * Nested element with the &lt;plugin&gt; element declaring a packaged resource and urn of
    * an antlib descriptor to be loaded from the classloader established by the transit plugin
    * descriptor.
    */
    public static class Antlib
    {
       /**
        * The antlib urn.
        */
        private String m_urn;

       /**
        * The antlib descriptor resource path.
        */
        private String m_path;

       /**
        * Set the urn for this antlib.
        * @param urn the antlib urn
        */
        public void setUrn( final String urn )
        {
            m_urn = urn;
        }

       /**
        * Return the antlib urn.
        * @return the urn (possibly null in which case the a urn must be declared
        *   within the plugin descriptor)0
        */
        public String getURN()
        {
            return m_urn;
        }

       /**
        * Set the antlib resource path
        * @param path the resource path
        */
        public void setResource( final String path )
        {
            m_path = path;
        }

       /**
        * Return the antlib resource path.
        * @return the path (possibly null in which case the resource reference
        *   must exist in the plugin descriptor)
        */
        public String getPath()
        {
            return m_path;
        }
    }

    private Repository getRepository()
    {
        if( null == m_REPOSITORY )
        {
            m_REPOSITORY = setupRepository();
        }
        return m_REPOSITORY;
    }

    private Repository setupRepository()
    {
        try
        {
            return Transit.getInstance().getRepository();
        }
        catch( Throwable e )
        {
            final String error =
              "Internal error while attemting to resolve Transit repository.";
            throw new BuildException( error, e, getLocation() );
        }
    }
}

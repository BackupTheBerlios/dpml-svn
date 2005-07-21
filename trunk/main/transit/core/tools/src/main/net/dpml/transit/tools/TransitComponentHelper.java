/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

import java.io.File;
import java.io.InputStream;
import java.util.Vector;
import java.util.Hashtable;
import java.net.URI;

import net.dpml.transit.util.ElementHelper;
import net.dpml.transit.repository.Plugin;
import net.dpml.transit.repository.StandardLoader;

import org.apache.tools.ant.Main;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.helper.ProjectHelper2;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.AntTypeDefinition;
import org.apache.tools.ant.SubBuildListener;
import org.apache.tools.ant.BuildEvent;

import org.w3c.dom.Element;

/**
 * A component helper that handles automatic loading of plugins into the
 * ant plugin based on namespace declarations in the project file.  This is similar
 * to the 'antlib:' convention except we use the 'plugin:' convention.
 *
 * @author <a href="mailto:dev@dpmlnet">Stephen J. McConnell</a>
 * @version $Id: Metro.java 916 2004-11-25 12:15:17Z niclas@apache.org $
 */
public class TransitComponentHelper extends ComponentHelper
    implements SubBuildListener
{
   //------------------------------------------------------------------------
   // static
   //------------------------------------------------------------------------

    public static final String TRANSIT_ANTLIB_URN = "antlib:net.dpml.transit";
    public static final String TRANSIT_INIT_URN = TRANSIT_ANTLIB_URN + ":init";
    public static final String TRANSIT_PLUGIN_URN = TRANSIT_ANTLIB_URN + ":plugin";
    public static final String TRANSIT_IMPORT_URN = TRANSIT_ANTLIB_URN + ":import";
    public static final String TRANSIT_GET_URN = TRANSIT_ANTLIB_URN + ":get";

    public static final String PLUGIN_ARTIFACT_HEADER = "artifact:plugin:";

   /**
    * Creation of a component helper for the supplied project.
    *
    * @param project the project
    * @return the project component for a specific project
    */
    public static void initialize( Project project )
    {
        initialize( project, false );
    }

   /**
    * Creation of a component helper for the supplied project.
    *
    * @param project the project
    * @return the project component for a specific project
    */
    public static void initialize( Project project, boolean flag )
    {
        ComponentHelper current =
          (ComponentHelper) project.getReference( "ant.ComponentHelper" );
        if( ( null != current ) && ( current instanceof TransitComponentHelper) )
        {
            return;
        }
        TransitComponentHelper helper = new TransitComponentHelper( project, current );
        helper.initDefaultDefinitions();
        if( flag )
        {
            project.log(
              "\nAssigning Transit component helper to sub-project: "
              + project.getBaseDir() );
        }
        else
        {
            project.log(
              "\nAssigning Transit component helper to project: "
              + project.getBaseDir() );
        }
        project.addReference( "ant.ComponentHelper", helper );
        project.addBuildListener( helper );

    }

   /**
    * Vector of plugin uris already loaded.
    */
    private static Vector m_uris = new Vector();

   /**
    * Table of urn to uri mappings.
    */
    private static Hashtable m_mappings = new Hashtable();

   /**
    * Register the mapping between a urn and a plugin uri.
    * @param maps a sequence of urn to uri bindings
    */
    public static void register( MapDataType[] maps )
    {
        if( null == maps )
        {
            throw new NullPointerException( "maps" );
        }

        for( int i=0; i<maps.length; i++ )
        {
            MapDataType map = maps[i];
            String urn = map.getURN();
            if( m_mappings.contains( urn ) == false )
            {
                URI uri = map.getURI();
                m_mappings.put( urn, uri );
            }
        }
    }

   //------------------------------------------------------------------------
   // state
   //------------------------------------------------------------------------

   /**
    * The current project.
    */
    private Project m_project;

   /**
    * The parent component helper.
    */
    private ComponentHelper m_parent;

   //------------------------------------------------------------------------
   // constructor
   //------------------------------------------------------------------------

   public TransitComponentHelper( Project project )
   {
       this( project, ComponentHelper.getComponentHelper( project ) );
   }

   public TransitComponentHelper( Project project, ComponentHelper parent )
   {
       setProject( project );
       m_parent = parent;
       if( null != parent )
       {
           parent.setNext( this );
       }

       Hashtable map = getTaskDefinitions();
       if( null == map.get( TRANSIT_INIT_URN ) )
       {
           addTaskDefinition( TRANSIT_INIT_URN, MainTask.class );
           addTaskDefinition( TRANSIT_PLUGIN_URN, PluginTask.class );
           addTaskDefinition( TRANSIT_IMPORT_URN, ImportArtifactTask.class );
           addTaskDefinition( TRANSIT_GET_URN, GetTask.class );
       }
   }

   //------------------------------------------------------------------------
   // implementation
   //------------------------------------------------------------------------

   /**
    * Set the current project.
    * @param project the current ant project
    */
    public void setProject( Project project )
    {
        m_project = project;
        super.setProject( project );
    }

   /**
    * Create an object for a component using a supplied name. The name
    * is the fully qualified component name which allows us to intercept
    * specific namespace qualifiers - in this case 'plugin:'.  In the event of
    * a plugin namespace we check to see if the plugin for that name is already
    * loaded and it not we proceed with classic transit-based loading of the
    * plugin and registration of plugin classes with the component helper.
    *
    * @param name the name of the component, if the component is in a namespace, the
    *   name is prefixed with the namespace uri and ":"
    * @return the class if found or null if not.
    */
    public Object createComponent( String name )
    {
        Object object = super.createComponent( name );
        if( null != object )
        {
            return object;
        }

        if( null != m_parent )
        {
            object = m_parent.createComponent( name );
            if( null != object )
            {
                return object;
            }
        }

        //
        // from here we need to validate - code has been worked over more than a
        // few time ans I would not recommend it for flight control scenarios
        // just yet
        //

        int k = name.lastIndexOf( ":" );
        if( k > 0 )
        {
            String urn = name.substring( 0, k );
            String task = name.substring( k + 1 );
            URI uri = convertUrnToURI( urn );
            if( null != uri )
            {
                installPlugin( uri, urn, task );
                object = super.createComponent( name );
                if( null != object )
                {
                    return object;
                }
                else
                {
                    final String error =
                      "Mapped urn returned a null object.";
                    throw new BuildException( error );
                }
            }
            else
            {
                return super.createComponent( name );
            }
        }
        else
        {
            return super.createComponent( name );
        }
    }

   /**
    * Convert a urn to a uri taking into account possible urn alias names.
    * TODO: turn this into a parameteizable namespace to artifact map.
    *
    * @param urn the urn to convert to a uri
    */
    private URI convertUrnToURI( String urn )
    {
        URI uri = (URI) m_mappings.get( urn );
        if( null != uri )
        {
            return uri;
        }
        if( urn.startsWith( PLUGIN_ARTIFACT_HEADER ) )
        {
            return convertToURI( urn );
        }
        else
        {
            return null;
        }
    }

   /**
    * The implementation will retrieve the plugin descriptor.  If the descriptor
    * declares a classname then the class will be loaded and assigned under
    * name.  If the classname is undefined and resource is defined, the
    * implementation will attempt to locate an antlib definition at the resource
    * location and will attempt to load all taskdef and typedef entries declared
    * in the antlib.
    *
    * @param uri the plugin uri
    * @param name the fully qualified component name
    */
    private void installPlugin( URI uri, String urn, String name )
    {
        final String label = uri + ":" + name;
        if( null != getTaskDefinitions().get( label ) )
        {
            return;
        }

        try
        {
            m_project.log( "installing plugin: " + urn + " / " + label );

            StandardLoader loader = new StandardLoader();
            Plugin descriptor = loader.getPluginDescriptor( uri );
            ClassLoader current = Thread.currentThread().getContextClassLoader();
            ClassLoader classloader = loader.getPluginClassLoader( current, uri );
            if( null != descriptor.getClassname() )
            {
                Class clazz = classloader.loadClass( descriptor.getClassname() );
                final String key = uri + ":" + name;
                getProject().log( "installing single task plugin [" + key + "]", Project.MSG_VERBOSE );
                super.addTaskDefinition( key, clazz );
            }
            else
            {
                String resource = descriptor.getResource();
                getProject().log( "installing antlib plugin [" + resource + "]", Project.MSG_VERBOSE );
                InputStream input = classloader.getResourceAsStream( resource );
                if( null == input )
                {
                    final String error = 
                      "Cannot load plugin resource [" 
                      + resource 
                      + "] because it does not exist within the cloassloader defined by the uri [" 
                      + uri 
                      + "]"
                      + "\n" + classloader.toString();
                    throw new BuildException( error );
                }

                Element root = ElementHelper.getRootElement( input );
                Element[] tasks = ElementHelper.getChildren( root, "taskdef" );

                for( int i=0; i < tasks.length; i++ )
                {
                    Element task = tasks[i];
                    String key = urn + ":" + ElementHelper.getAttribute( task, "name" );
                    getProject().log( "installing task [" + key + "]", Project.MSG_VERBOSE );
                    String classname = ElementHelper.getAttribute( task, "classname" );
                    Class clazz = classloader.loadClass( classname );
                    super.addTaskDefinition( key, clazz );
                }

                Element[] types = ElementHelper.getChildren( root, "typedef" );
                for( int i=0; i < types.length; i++ )
                {
                    Element type = types[i];
                    String key = urn + ":" + ElementHelper.getAttribute( type, "name" );
                    getProject().log( "installing type [" + key + "]", Project.MSG_VERBOSE );
                    String classname = ElementHelper.getAttribute( type, "classname" );
                    Class clazz = classloader.loadClass( classname );
                    super.addDataTypeDefinition( key, clazz );
                }
            }

            m_uris.add( uri );
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error =
              "Could not load plugin: " + uri;
            throw new BuildException( error, e );
        }
    }

   /**
    * Returns the current project.
    * @return the project
    */
    private Project getProject()
    {
        return m_project;
    }

   /**
    * Return the plugin uri by resolving the string form the beggining of the name
    * to the last occurance of ":". The content following the ":" is used by ant for
    * the actual task name. We use the string preceeding the name to hold the
    * artifact uri.
    *
    * @param name the fully qualified task name
    * @return the plugin uri
    * @exception BuildException if a convertion error occurs
    */
    private URI getURI( String name ) throws BuildException
    {
        String urn = name.substring( 0, name.lastIndexOf( ":" ) );
        if( name.startsWith( "artifact:plugin:" ) )
        {
            return convertToURI( urn );
        }
        else
        {
            String spec = "artifact:plugin:" + urn.substring( 7 );
            return convertToURI( spec );
        }
    }

   /**
    * Convert a urn to a url wrapping any errors in a build exception.
    * @param urn the urn
    * @return the uri
    * @exception BuildException if a convertion error occurs
    */
    private URI convertToURI( String urn ) throws BuildException
    {
        try
        {
            return new URI( urn );
        }
        catch( Exception e )
        {
            final String error =
              "Unable to convert the urn ["
              + urn
              + "] to a uri.";
            throw new BuildException( error, e );
        }
    }

    public void buildStarted( BuildEvent event )
        throws BuildException
    {
        initialize( event.getProject(), false );
    }

    public void subBuildStarted( BuildEvent event )
    {
        initialize( event.getProject(), true );
    }

    public void subBuildFinished( BuildEvent event )
    {
    }

    public void buildFinished( BuildEvent event )
    {
    }

    public void targetStarted( BuildEvent event )
    {
    }

    public void targetFinished( BuildEvent event )
    {
    }

    public void taskStarted( BuildEvent event )
    {
    }

    public void taskFinished( BuildEvent event )
    {
    }

    public void messageLogged( BuildEvent event )
    {
    }

}


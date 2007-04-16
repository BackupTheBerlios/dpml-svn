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

package dpml.tools.transit;

import dpml.tools.BuilderError;

import dpml.util.ElementHelper;

import java.io.InputStream;
import java.util.Vector;
import java.util.Hashtable;
import java.net.URI;

import net.dpml.lang.AntlibStrategy;
import net.dpml.lang.Strategy;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.SubBuildListener;
import org.apache.tools.ant.BuildEvent;

import org.w3c.dom.Element;

/**
 * A component helper that handles automatic loading of plugins into the
 * ant plugin based on namespace declarations in the project file.  This is similar
 * to the 'antlib:' convention except we use the 'plugin:' convention.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TransitComponentHelper extends ComponentHelper
    implements SubBuildListener
{
   //------------------------------------------------------------------------
   // static
   //------------------------------------------------------------------------

   /**
    * The constant Transit ANTLIB namespace.
    */
    public static final String TRANSIT_ANTLIB_URN = "antlib:dpml.tools.transit";

   /**
    * The constant Transit ANTLIB init task namespace.
    */
    public static final String TRANSIT_INIT_URN = TRANSIT_ANTLIB_URN + ":init";

   /**
    * The constant Transit ANTLIB plugin task namespace.
    */
    public static final String TRANSIT_PLUGIN_URN = TRANSIT_ANTLIB_URN + ":plugin";

   /**
    * The constant Transit ANTLIB import task namespace.
    */
    public static final String TRANSIT_IMPORT_URN = TRANSIT_ANTLIB_URN + ":import";

   /**
    * The constant Transit ANTLIB get task namespace.
    */
    public static final String TRANSIT_GET_URN = TRANSIT_ANTLIB_URN + ":get";

   /**
    * The constant artifact plugin header.
    */
    public static final String PLUGIN_ARTIFACT_HEADER = "artifact:part:";

   /**
    * Creation of a component helper for the supplied project.
    *
    * @param project the project
    */
    public static void initialize( Project project )
    {
        initialize( project, false );
    }

   /**
    * Creation of a component helper for the supplied project.
    *
    * @param project the project
    * @param flag subproject flag
    */
    public static void initialize( Project project, boolean flag )
    {
        ComponentHelper current =
          (ComponentHelper) project.getReference( "ant.ComponentHelper" );
        if( ( null != current ) && ( current instanceof TransitComponentHelper ) )
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
    private static Vector<URI> m_URIS = new Vector<URI>();

   /**
    * Table of urn to uri mappings.
    */
    private static Hashtable<String,URI> m_MAPPINGS = new Hashtable<String,URI>();

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

        for( int i=0; i < maps.length; i++ )
        {
            MapDataType map = maps[i];
            String urn = map.getURN();
            if( !m_MAPPINGS.contains( urn ) )
            {
                URI uri = map.getURI();
                m_MAPPINGS.put( urn, uri );
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

   /**
    * Creation of a new transit component helper.
    * @param project the current project
    */  
    public TransitComponentHelper( Project project )
    {
        this( project, ComponentHelper.getComponentHelper( project ) );
    }

   /**
    * Creation of a new transit component helper.
    * @param project the current project
    * @param parent the parent component helper
    */  
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
        else if( null != m_parent )
        {
            object = m_parent.createComponent( name );
            if( null != object )
            {
                return object;
            }
        }
        
        if( name.startsWith( "antlib:" ) )
        {
            String spec = name.substring( 7 );
            int n = spec.indexOf( ":" );
            if( n < 0 )
            {
                final String error =
                  "Invalid antlib component name: "
                  + name;
                throw new BuildException( error );
            }
            
            String resource = spec.substring( 0, n );
            if( spec.length() < ( n + 1 ) )
            {
                final String error =
                  "Invalid antlib component name: "
                  + name;
                throw new BuildException( error );
            }
            String theName = spec.substring( n + 1 );
            String path = resource.replace( '.', '/' ) + "/antlib.xml";
            ClassLoader classloader = getClass().getClassLoader();
            InputStream input = classloader.getResourceAsStream( path );
            
            if( null == input )
            {
                if( name.startsWith( "antlib:net.dpml.transit:" ) )
                {
                    // 1.X to 2.X migration issue, re-apply using 
                    // "antlib:dpml.tools:"
                    
                    String value = name.replace( 
                      "antlib:net.dpml.transit:", 
                      "antlib:dpml.tools:" );
                    return createComponent( value );
                }
                final String error = 
                  "Task ["
                  + name
                  + "] could not be loaded because the Antlib resource ["
                  + path
                  + "] does not exist.";
                throw new BuildException( error );
            }
            else
            {
                int i = name.lastIndexOf( ":" );
                if( i < 0 )
                {
                    final String error =
                      "Invalid antlib component name: "
                      + name;
                    throw new BuildException( error );
                }
                else
                {
                    String urn = name.substring( 0, name.lastIndexOf( ":" ) );
                    installAntlib( classloader, input, urn );
                    return createComponent( name );
                }
            }
        }
        else
        {
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
    }

   /**
    * Convert a urn to a uri taking into account possible urn alias names.
    *
    * @param urn the urn to convert to a uri
    * @return the converted uri
    */
    private URI convertUrnToURI( String urn )
    {
        URI uri = (URI) m_MAPPINGS.get( urn );
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
            m_project.log( "installing: " + uri + " as " + urn );
            Strategy strategy = Strategy.load( null, null, uri, null );
            ClassLoader classloader = strategy.getClassLoader();
            ClassLoader current = Thread.currentThread().getContextClassLoader();
            if( strategy instanceof AntlibStrategy )
            {
                AntlibStrategy res = (AntlibStrategy) strategy;
                String resource = res.getPath();
                getProject().log( "installing antlib plugin [" + resource + "]", Project.MSG_VERBOSE );
                InputStream input = classloader.getResourceAsStream( resource );
                if( null == input )
                {
                    final String error = 
                      "Cannot load resource [" 
                      + resource 
                      + "] because it does not exist within the cloassloader defined by the uri [" 
                      + uri 
                      + "]"
                      + "\n" + classloader.toString();
                    throw new BuildException( error );
                }
                installAntlib( classloader, input, urn );
            }
            else
            {
                final String error = 
                  "Strategy class not supported ["
                  + strategy.getClass().getName()
                  + "].";
                throw new BuildException( error );
            }
            m_URIS.add( uri );
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

    private void installAntlib( ClassLoader classloader, InputStream input, String urn )
    {
        try
        {
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
        catch( Exception e )
        {
            final String error =
              "Antlib installation error: " + urn;
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

   /**
    * Notification that the build has started.
    * @param event the build event
    * @exception BuildException if a build error occurs
    */
    public void buildStarted( BuildEvent event )
        throws BuildException
    {
        initialize( event.getProject(), false );
    }

   /**
    * Notification that a sub build has started.
    * @param event the build event
    */
    public void subBuildStarted( BuildEvent event )
    {
        initialize( event.getProject(), true );
    }

   /**
    * Notification that a sub build has finished.
    * @param event the build event
    */
    public void subBuildFinished( BuildEvent event )
    {
    }

   /**
    * Notification that the build has finished.
    * @param event the build event
    */
    public void buildFinished( BuildEvent event )
    {
    }

   /**
    * Notification that the build target has started.
    * @param event the build event
    */
    public void targetStarted( BuildEvent event )
    {
    }

   /**
    * Notification that the build target has finished.
    * @param event the build event
    */
    public void targetFinished( BuildEvent event )
    {
    }

   /**
    * Notification that the build task has started.
    * @param event the build event
    */
    public void taskStarted( BuildEvent event )
    {
    }

   /**
    * Notification that the build task has finaished.
    * @param event the build event
    */
    public void taskFinished( BuildEvent event )
    {
    }

   /**
    * Notification of a message logged.
    * @param event the build event
    */
    public void messageLogged( BuildEvent event )
    {
    }
}


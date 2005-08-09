/*
 * Copyright 2004 Stephen McConnell
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

package net.dpml.magic.project;

import net.dpml.magic.AntFileIndex;
import net.dpml.magic.UnknownResourceException;
import net.dpml.magic.builder.AntFileIndexBuilder;
import net.dpml.magic.model.Definition;

import net.dpml.transit.NullArgumentException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Property;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A project context holds information about a single project node and is used
 * by multiple tasks during a project build sequence.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class Context
{
    //-------------------------------------------------------------
    // static
    //-------------------------------------------------------------

   /**
    * Constant resource key for context.
    */
    public static final String KEY = "project.context";

   /**
    * Constant key for the GPG exe name.
    */
    public static final String GPG_EXE_KEY = "project.gpg.exe";

   /**
    * Constant name of the user properties filename.
    */
    public static final String USER_PROPERTIES = "user.properties";

   /**
    * Constant name of the build properties filename.
    */
    public static final String BUILD_PROPERTIES = "build.properties";

   /**
    * The singleton index.
    */
    private static AntFileIndex m_INDEX;    

   /**
    * Return the UTC YYMMDD.HHMMSSS signature.
    * @return the UTC date-stamp
    */
    public static String getSignature()
    {
        return getSignature( new Date() );
    }

   /**
    * Return the UTC YYMMDD.HHMMSSS signature of a date.
    * @param date the date
    * @return the UTC date-stamp signature
    */
    public static String getSignature( final Date date )
    {
        final SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMdd.HHmmss" );
        sdf.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        return sdf.format( date );
    }

    //-------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------

    private final String m_key;
    private final Definition m_definition;
    private final Project m_project;

    private final File m_src;
    private final File m_srcMain;
    private final File m_srcTest;
    private final File m_etc;
    private final File m_target;
    private final File m_deliverables;
    private final File m_classes;
    private final File m_testClasses;
    private final File m_test;
    private final File m_reports;
    private final File m_docs;
    private final File m_build;
    private final File m_temp;

    //-------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------

   /**
    * Creation of a new context instance.
    * @param project the current project
    * @exception BuildException if a build error occurs
    */
    public Context( Project project ) throws BuildException
    {
        m_project = project;
        m_key = resolveKey();

        resolveIndex( project );

        m_definition = resolveDefinition( m_key );

        //
        // set the properties relating to the current project
        //

        updateProperty( project, "project.key", m_key );

        if( null != m_definition )
        {
            updateProperty( project, "project.group", m_definition.getInfo().getGroup() );
            updateProperty( project, "project.name", m_definition.getInfo().getName() );

            final String version = m_definition.getInfo().getVersion();
            if( version != null )
            {
                updateProperty( project, "project.version", version );
            }
            else
            {
                updateProperty( project, "project.version", "" );
            }

            updateProperty( project, "project.short-filename", m_definition.getInfo().getShortFilename() );
            updateProperty( project, "project.spec", m_definition.getInfo().getSpec() );
        }

        final File user = project.resolveFile( USER_PROPERTIES );
        loadProperties( project, user );

        final File build = project.resolveFile( BUILD_PROPERTIES );
        loadProperties( project, build );

        m_src = project.resolveFile( "src" );
        m_srcMain = new File( m_src, "main" );
        m_srcTest = new File( m_src, "test" );
        m_etc = project.resolveFile( "etc" );
        m_target = project.resolveFile( "target" );
        m_deliverables = new File( m_target, "deliverables" );
        m_classes = new File( m_target, "classes" );
        m_testClasses = new File( m_target, "test-classes" );
        m_test = new File( m_target, "test" );
        m_reports = new File( m_target, "test-reports" );
        m_docs = new File( m_target, "docs" );
        m_build = new File( m_target, "build" );
        m_temp = new File( m_target, "temp" );

        updateFileProperty( project, "project.build.dir", m_build );
        updateFileProperty( project, "project.target", m_target );
        updateFileProperty( project, "project.test.dir", m_test );
        updateFileProperty( project, "project.deliverables.dir", m_deliverables );

        updateFileProperty( project, "project.target.dir", m_target );
        updateFileProperty( project, "project.deliverables", m_deliverables );
        updateFileProperty( project, "project.build", m_build );
        updateFileProperty( project, "project.test", m_test );
    }

    //-------------------------------------------------------------
    // Context
    //-------------------------------------------------------------

   /**
    * Return the project key.  The value returned corresponds to either a formally
    * declared key, or in the case of a buildable project - the project name.  In the case
    * of a directory node the project key corresp-onds to the ant build file name.
    *
    * @return the key
    */
    public String getKey()
    {
        return m_key;
    }

   /**
    * Returns an immutable defintion of the system index taking into account
    * imported indexes and local project, plugin and resource defintions.
    *
    * @return the index
    */
    public AntFileIndex getIndex()
    {
        return m_INDEX;
    }

   /**
    * Returns the project definition corresponding to the key.  If the key does not
    * refer to a know project and UnknownResourceException will be thrown.
    *
    * @return the project definition
    * @exception UnknownResourceException if the key does not refer to a project
    */
    public Definition getDefinition() throws UnknownResourceException
    {
        if( null == m_definition )
        {
            throw new UnknownResourceException( getKey() );
        }
        else
        {
            return m_definition;
        }
    }

   /**
    * Return the project src directory.
    * @return the src dir
    */
    public File getSrcDirectory()
    {
        return m_src;
    }

   /**
    * Return the project filtered target/main/src directory.
    * @return the filtered src dir
    */
    public File getSrcMainDirectory()
    {
        return m_srcMain;
    }

   /**
    * Return the project filtered target/main/test directory.
    * @return the filtered test dir
    */
    public File getSrcTestDirectory()
    {
        return m_srcTest;
    }

   /**
    * Return the project etc dir
    * @return the etc dir
    */
    public File getEtcDirectory()
    {
        return m_etc;
    }

   /**
    * Return the project target dir
    * @return the target dir
    */
    public File getTargetDirectory()
    {
        return m_target;
    }

   /**
    * Return the project target/build dir
    * @return the build dir
    */
    public File getBuildDirectory()
    {
        return m_build;
    }

   /**
    * Return the project target/classes dir
    * @return the classes dir
    */
    public File getClassesDirectory()
    {
        return m_classes;
    }

   /**
    * Return the project target/test-classes dir
    * @return the test classes dir
    */
    public File getTestClassesDirectory()
    {
        return m_testClasses;
    }

   /**
    * Return the project target/test dir
    * @return the test dir
    */
    public File getTestDirectory()
    {
        return m_test;
    }

   /**
    * Return the project target/test-reports dir
    * @return the test reports dir
    */
    public File getTestReportsDirectory()
    {
        return m_reports;
    }

   /**
    * Return the project docs dir
    * @return the docs dir
    */
    public File getDocsDirectory()
    {
        return m_docs;
    }

   /**
    * Return the project temp dir
    * @return the temp dir
    */
    public File getTempDirectory()
    {
        return m_temp;
    }

   /**
    * Return the project target/deliverables dir.
    * @return the deliverables dir
    */
    public File getDeliverablesDirectory()
    {
        return m_deliverables;
    }

   /**
    * Return the project target/deliverables/[type] dir.
    * @param type the deliverable type
    * @return the deliverables dir
    * @exception NullArgumentException if the type argument is null
    */
    public File getDeliverablesDirectory( String type )
        throws NullArgumentException
    {
        if( null == type )
        {
            throw new NullArgumentException( "type" );
        }
        else
        {
            File deliverables = getDeliverablesDirectory();
            String path = type + "s";
            return new File( deliverables, path );
        }
    }

    //-------------------------------------------------------------
    // private immplementation
    //-------------------------------------------------------------

   /**
    * Load properties into the project.
    * @param project the current project
    * @param file the property file to load
    * @exception BuildException if a build error occurs
    */
    protected void loadProperties(
      final Project project, final File file ) throws BuildException
    {
        final Property props = (Property) project.createTask( "property" );
        props.setTaskName( "project" );
        props.init();
        props.setFile( file );
        props.execute();
    }

    private Project getProject()
    {
        return m_project;
    }

    private Definition resolveDefinition( String key )
    {
        try
        {
            return getIndex().getDefinition( key );
        }
        catch( BuildException e )
        {
            return null;
        }
    }

    private String resolveKey()
    {
        final String key = getProject().getProperty( "project.key" );
        if( null != key )
        {
            return key;
        }
        else
        {
            final String name = getProject().getProperty( "project.name" );
            if( null != name )
            {
                return name;
            }
            else
            {
                return getProject().getName();
            }
        }
    }

    private void resolveIndex( final Project project )
    {
        if( null != m_INDEX )
        {
            return;
        }
        else
        {
            AntFileIndexBuilder builder = new AntFileIndexBuilder( project );
            m_INDEX = builder.getIndex();
        }
    }

    private void updateFileProperty( Project project, String key, File file )
    {
        try
        {
            String path = file.getCanonicalPath();
            updateProperty( project, key, path );
        }
        catch( IOException ioe )
        {
            final String error =
              "Internal error while attempting to resolve the canonical representation of a file ["
                + file.toString()
                + "].";
            throw new BuildException( error, ioe );
        }
    }

    private void updateProperty( Project project, String key, String value )
    {
        String v = project.getProperty( key );
        if( null == v )
        {
            project.setProperty( key, value );
        }
        else if( !v.equals( value ) )
        {
            project.setNewProperty( key, value );
        }
    }
}

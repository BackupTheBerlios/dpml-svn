/*
 * Copyright 2004 Niclas Hedhman
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

package net.dpml.tools.tasks;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.dpml.transit.Transit;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

/**
 * Site generation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DocTask extends GenericTask
{
    private static final String ORG_NAME_VALUE = "The Digital Product Meta Library";
    private static final String DOC_TEMP_VALUE = "docs";
    private static final String DOC_SRC_VALUE = "docs";
    private static final String DOC_RESOURCES_VALUE = "resources";
    private static final String DOC_THEME_VALUE = "formal";
    private static final String DOC_FORMAT_VALUE = "html";
    private static final String DOC_DATE_FORMAT_VALUE = "yyyy-MMM-dd";
    private static final String DOC_STYLE_VALUE = "standard";
    private static final String DOC_ENTRY_VALUE = "";
    private static final String DOC_LOGO_RIGHT_FILE_VALUE = "";
    private static final String DOC_LOGO_RIGHT_URL_VALUE = "";
    private static final String DOC_LOGO_LEFT_FILE_VALUE = "";
    private static final String DOC_LOGO_LEFT_URL_VALUE = "";
    private static final String DOC_LOGO_MIDDLE_FILE_VALUE = "";
    private static final String DOC_LOGO_MIDDLE_URL_VALUE = "";
    private static final String DOC_BRAND_NAME_VALUE = "DPML";

   /**
    * Constant organization key.
    */
    public static final String ORG_NAME_KEY = "project.organization.name";

   /**
    * Constant temp docs key.
    */
    public static final String DOC_TEMP_KEY = "project.target.temp.docs";

   /**
    * Constant docs src key.
    */
    public static final String DOC_SRC_KEY = "project.docs.src";

   /**
    * Constant docs resources key.
    */
    public static final String DOC_RESOURCES_KEY = "project.docs.resources";

   /**
    * Constant docs theme key.
    */
    public static final String DOC_THEME_KEY = "project.docs.theme";

   /**
    * Constant docs output format key.
    */
    public static final String DOC_FORMAT_KEY = "project.docs.output.format";

   /**
    * Constant docs date format key.
    */
    public static final String DOC_DATE_FORMAT_KEY = "project.docs.date.format";

   /**
    * Constant docs output style key.
    */
    public static final String DOC_STYLE_KEY = "project.docs.output.style";

   /**
    * Constant docs entry-point key.
    */
    public static final String DOC_ENTRY_KEY = "project.docs.entry-point";

   /**
    * Constant docs logo-right-file key.
    */
    public static final String DOC_LOGO_RIGHT_FILE_KEY = "project.docs.logo.right.file";

   /**
    * Constant docs logo-right url key.
    */
    public static final String DOC_LOGO_RIGHT_URL_KEY = "project.docs.logo.right.url";

   /**
    * Constant docs logo-left file key.
    */
    public static final String DOC_LOGO_LEFT_FILE_KEY = "project.docs.logo.left.file";

   /**
    * Constant docs logo-left url key.
    */
    public static final String DOC_LOGO_LEFT_URL_KEY = "project.docs.logo.left.url";

   /**
    * Constant docs logo-middle file key.
    */
    public static final String DOC_LOGO_MIDDLE_FILE_KEY = "project.docs.logo.middle.file";

   /**
    * Constant docs logo-middle url key.
    */
    public static final String DOC_LOGO_MIDDLE_URL_KEY = "project.docs.logo.middle.url";

   /**
    * Constant docs brand key.
    */
    public static final String DOC_BRAND_NAME_KEY = "project.docs.brand.name";

   /**
    * Constant docs anchor url key.
    */
    public static final String DOC_ANCHOR_URL_KEY = "project.docs.anchor.url";

    private String m_theme;
    private File m_baseToDir;
    private File m_baseSrcDir;

   /**
    * Return the assigned theme.
    * @return the theme
    */
    public String getTheme()
    {
        if( m_theme != null )
        {
            return m_theme;
        }
        String theme = getProject().getProperty( DOC_THEME_KEY );
        if( null != theme )
        {
            return theme;
        }
        else
        {
            return getResource().getProperty( DOC_THEME_KEY, "formal" );
        }
    }

   /**
    * Set the doc theme.
    * @param theme the theme name
    */
    public void setTheme( final String theme )
    {
        m_theme = theme;
    }

   /**
    * Initialize the task.
    * @exception BuildException if a build error occurs
    */
    public void init() throws BuildException
    {
        if( !isInitialized() )
        {
            super.init();
            final Project project = getProject();
            project.setNewProperty( ORG_NAME_KEY, ORG_NAME_VALUE );
            project.setNewProperty( DOC_SRC_KEY, DOC_SRC_VALUE );
            project.setNewProperty( DOC_RESOURCES_KEY, DOC_RESOURCES_VALUE );
            project.setNewProperty( DOC_FORMAT_KEY, DOC_FORMAT_VALUE );
            project.setNewProperty( DOC_DATE_FORMAT_KEY, DOC_DATE_FORMAT_VALUE );
            project.setNewProperty( DOC_STYLE_KEY, DOC_STYLE_VALUE );
            project.setNewProperty( DOC_ENTRY_KEY, DOC_ENTRY_VALUE );
            project.setNewProperty( DOC_TEMP_KEY, DOC_TEMP_VALUE );
            project.setNewProperty( DOC_LOGO_RIGHT_FILE_KEY, DOC_LOGO_RIGHT_FILE_VALUE );
            project.setNewProperty( DOC_LOGO_RIGHT_URL_KEY, DOC_LOGO_RIGHT_URL_VALUE );
            project.setNewProperty( DOC_LOGO_LEFT_FILE_KEY, DOC_LOGO_LEFT_FILE_VALUE );
            project.setNewProperty( DOC_LOGO_LEFT_URL_KEY, DOC_LOGO_LEFT_URL_VALUE );
            project.setNewProperty( DOC_LOGO_MIDDLE_FILE_KEY, DOC_LOGO_MIDDLE_FILE_VALUE );
            project.setNewProperty( DOC_LOGO_MIDDLE_URL_KEY, DOC_LOGO_MIDDLE_URL_VALUE );
            project.setNewProperty( DOC_BRAND_NAME_KEY, DOC_BRAND_NAME_VALUE );
        }
    }

   /**
    * Execute the task.
    */
    public void execute()
    {
        final Project project = getProject();
        final File srcDir = getContext().getTargetBuildDocsDirectory();
        if( !srcDir.exists() )
        {
            return;
        }
        log( "Filtered source: " + srcDir.getAbsolutePath() );

        //
        // create the temporary directory into which we generate the
        // navigation structure (normally target/temp/docs)
        //

        final File temp = getContext().getTargetTempDirectory();
        final File destDir = new File( temp, "docs" );
        mkDir( destDir );

        //
        // get the theme, output formats, etc.
        //

        final File docs = getContext().getTargetDocsDirectory();
        log( "Destination: " + docs.getAbsolutePath() );
        mkDir( docs );

        final String theme = getTheme();
        final String output = getOutputFormat();
        final File themeRoot = getThemesDirectory();
        final File themeDir = new File( themeRoot, theme + "/" + output );

        final File target = getContext().getTargetDirectory();
        final String resourcesPath = project.getProperty( DOC_RESOURCES_KEY );
        final File resources = new File( target, resourcesPath );

        log( "Year: " + getYear() );
        log( "Theme: " + themeDir );

        //
        // initiate the transformation starting with the generation of
        // the navigation structure based on the src directory content
        // into the temporary destingation directory, copy the content
        // sources to to the temp directory, transform the content and
        // generated navigation in the temp dir using the selected them
        // into the final docs directory, and copy over resources to
        // the final docs directory
        //

        try
        {
            transformNavigation( themeDir, srcDir, destDir );
            copySources( srcDir, destDir );
            transformDocs( themeDir, destDir, docs );
            copyThemeResources( themeDir, docs );
            copySrcResources( resources, docs );
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            log( "XSLT execution failed: " + e.getMessage() );
            throw new BuildException( e );
        }
    }

    private File getThemesDirectory()
    {
        return new File( Transit.DPML_PREFS, "tools/themes" );
    }

    private String getOutputFormat()
    {
        return getProject().getProperty( DOC_FORMAT_KEY );
    }

    private String getOutputStyle()
    {
        return getProject().getProperty( DOC_STYLE_KEY );
    }

    private String getDateFormat()
    {
        return getProject().getProperty( DOC_DATE_FORMAT_KEY );
    }

    private void transformNavigation( final File themeDir, final File source, final File dest )
    {
        final File xslFile = new File( themeDir,  "nav-aggregate.xsl" );
        if( !xslFile.exists() )
        {
            return;  // Theme may not use navigation.
        }
        log( "Transforming navigation." );
        try
        {
            transformTrax(
                source, dest, xslFile,
                "^.*/navigation.xml$", "", ".xml" );
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = 
              "Transformation failure: " + source;
            throw new BuildException( error, e );
        }
    }

    private void copySources( final File source, final File dest )
    {
        copy( source, dest, "**/*", "**/navigation.xml" );
    }

    private void transformDocs( final File themeDir, final File build, final File docs )
    {
        String style = getOutputStyle();
        File xslFile = new File( themeDir,  style + ".xsl" );
        String output = getOutputFormat();
        log( "Transforming content." );
        transformTrax(
          build, docs, xslFile,
          "^.*\\.xml$", "^.*/navigation.xml$", "." + output );
    }

    private void copySrcResources( final File resources, final File docs )
    {
        copy( resources, docs, "**/*", "" );
    }

    private void copyThemeResources( final File themeDir, final File docs )
    {
        final File fromDir = new File( themeDir, "resources" );
        copy( fromDir, docs, "**/*", "" );
    }

    private void copy( final File fromDir, final File toDir, final String includes, final String excludes )
    {
        if( !fromDir.exists() )
        {
            return;
        }

        final FileSet from = new FileSet();
        from.setDir( fromDir );
        from.setIncludes( includes );
        from.setExcludes( excludes );

        mkDir( toDir );

        final Copy copy = (Copy) getProject().createTask( "copy" );
        copy.setTodir( toDir );
        copy.addFileset( from );
        copy.setPreserveLastModified( true );
        copy.execute();
    }


    private void transformTrax(
            final File srcDir, final File toDir, final File xslFile,
            final String includes, final String excludes, final String extension )
        throws BuildException
    {
        FileInputStream fis = null;
        try
        {
            ClassLoader cl = getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader( cl );
            StreamSource source = new StreamSource( xslFile );
            final TransformerFactory tfactory = TransformerFactory.newInstance();
            final Transformer transformer = tfactory.newTransformer( source );
            final RegexpFilter filter = new RegexpFilter( includes, excludes );

            m_baseToDir = toDir;
            m_baseSrcDir = srcDir.getAbsoluteFile();
            String entrypoint = getEntryPoint();
            if( "".equals( entrypoint ) )
            {
                transform( transformer, m_baseSrcDir, toDir, filter, extension );
            }
            else
            {
                File fileToConvert = new File( m_baseSrcDir, entrypoint );
                transformFile(
                  transformer, fileToConvert, m_baseSrcDir, entrypoint, extension, m_baseToDir );
            }
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            throw new BuildException( e.getMessage(), e );
        }
        finally
        {
            if( fis != null )
            {
                try
                {
                    fis.close();
                } 
                catch( IOException f )
                {
                    log( f.toString() );
                }
            }
        }
    }

    private void transform( final Transformer transformer, final File srcDir, final File toDir,
        final FileFilter filter, final String extension )
        throws BuildException
    {
        boolean recursive = isRecursive();
        final File[] content = srcDir.listFiles( filter );
        for( int i=0; i < content.length; i++ )
        {
            String base = content[i].getName();
            if( content[i].isDirectory() && recursive )
            {
                final File newDest = new File( toDir, base );
                newDest.mkdirs();
                transform( transformer, content[i], newDest, filter, extension );
            }
            if( content[i].isFile() )
            {
                transformFile( transformer, content[i], srcDir, base, extension, toDir );
            }
        }
    }

    private void transformFile( Transformer transformer, File content, File srcDir,
                                String base, String extension, File toDir )
    {
        String userDir = System.getProperty( "user.dir" );
        System.setProperty( "user.dir", toDir.getAbsolutePath() );
        final String year = getYear();
        final String org = getOrganization();
        final String copyright =
          "Copyright " 
          + year 
          + ", " 
          + org 
          + " All rights reserved.";

        final String svnRoot = getProject().getProperty( DOC_ANCHOR_URL_KEY );
        final String svnSource = svnRoot + getRelSrcPath( srcDir ) + "/" + base;

        final int pos = base.lastIndexOf( '.' );
        if( pos > 0 )
        {
            base = base.substring( 0, pos );
        }
        base = base + extension;

        final File newDest = new File( toDir, base );
        final StreamSource xml = new StreamSource( content );
        final StreamResult out = new StreamResult( newDest );

        transformer.clearParameters();
        transformer.setParameter( "directory", getRelToPath( toDir ) );
        transformer.setParameter( "fullpath", getRelToPath( newDest ) );
        transformer.setParameter( "file", base );
        transformer.setParameter( "svn-location", svnSource );
        transformer.setParameter( "copyright", copyright );
        transformer.setParameter(
            "logoright_file",
            getProject().getProperty( DOC_LOGO_RIGHT_FILE_KEY ).trim() );
        transformer.setParameter(
            "logoright_url",
            getProject().getProperty( DOC_LOGO_RIGHT_URL_KEY ).trim() );
        transformer.setParameter(
            "logoleft_file",
            getProject().getProperty( DOC_LOGO_LEFT_FILE_KEY ).trim() );
        transformer.setParameter(
            "logoleft_url",
            getProject().getProperty( DOC_LOGO_LEFT_URL_KEY ).trim() );
        transformer.setParameter(
            "logomiddle_file",
            getProject().getProperty( DOC_LOGO_MIDDLE_FILE_KEY ).trim() );
        transformer.setParameter(
            "logomiddle_url",
            getProject().getProperty( DOC_LOGO_MIDDLE_URL_KEY ).trim() );
        transformer.setParameter(
            "brand_name",
            getProject().getProperty( DOC_BRAND_NAME_KEY ).trim() );
        transformer.setParameter( "generated_date", getNow() );
        setOtherProperties( transformer );
        try
        {
            transformer.transform( xml, out );
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = 
              "An error occured while attempting to transform document: "
              + getRelToPath( newDest );
            throw new BuildException( error, e, getLocation() );
        }
        finally
        {
            System.setProperty( "user.dir", userDir );
        }
    }

    private String getRelToPath( final File dir )
    {
        final String basedir = m_baseToDir.getAbsolutePath();
        final String curdir = dir.getAbsolutePath();
        return curdir.substring( basedir.length() );
    }

    private String getRelSrcPath( final File dir )
    {
        final String basedir = m_baseSrcDir.getAbsolutePath();
        final String curdir = dir.getAbsolutePath();
        return curdir.substring( basedir.length() );
    }

   /**
    * Utility regualar expression filter.
    */
    public class RegexpFilter implements FileFilter
    {
        private Pattern m_includes;
        private Pattern m_excludes;

       /**
        * New filter creation.
        * @param includes the includes
        * @param excludes the excludes
        */
        public RegexpFilter( final String includes, final String excludes )
        {
            m_includes = Pattern.compile( includes );
            m_excludes = Pattern.compile( excludes );
        }

       /**
        * Test supplied file for acceptance.
        * @param file the candidate
        * @return TRUE if acceptable
        */
        public boolean accept( final File file )
        {
            final String basename = file.getName();

            if( basename.equals( ".svn" ) )
            {
                return false;
            }

            if( basename.equals( "CVS" ) )
            {
                return false;
            }

            if( file.isDirectory() )
            {
                return true;
            }

            final String fullpath = file.getAbsolutePath().replace( '\\', '/' );

            Matcher m = m_includes.matcher( fullpath );
            if( !m.matches() )
            {
                return false;
            }

            m = m_excludes.matcher( fullpath );
            return !m.matches();
        }
    }

    private String getYear()
    {
        String year = getProject().getProperty( "magic.year" );
        if( year != null )
        {
            return year;
        }
        else
        {
            Calendar cal = Calendar.getInstance();
            return Integer.toString( cal.get( Calendar.YEAR ) );
        }
    }

    private String getOrganization()
    {
        return getProject().getProperty( ORG_NAME_KEY );
    }

    private boolean isRecursive()
    {
        return "".equals( getEntryPoint() );
    }

    private String getEntryPoint()
    {
        String point = getProject().getProperty( DOC_ENTRY_KEY );
        if( point == null )
        {
            return "";
        }
        return point;
    }

    private void setOtherProperties( Transformer transformer )
    {
        String prefix = "project.docs.xsl.";
        int prefixLen = prefix.length();

        Hashtable p = getProject().getProperties();
        Iterator list = p.entrySet().iterator();
        while( list.hasNext() )
        {
            Map.Entry entry = (Map.Entry) list.next();
            String key = (String) entry.getKey();
            if( key.startsWith( prefix ) )
            {
                String value = (String) entry.getValue();
                key = key.substring( prefixLen );
                transformer.setParameter( key, value );
                log( "Setting " + key + "=" + value );
            }
        }
    }

    private String getNow()
    {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat( getDateFormat() );
        String result = sdf.format( now );
        return result;
    }
}

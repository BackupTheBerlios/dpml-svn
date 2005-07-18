/*
 * Copyright 2004 Niclas Hedhman
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

package net.dpml.magic.doc;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;

import net.dpml.magic.tasks.ProjectTask;

import net.dpml.transit.Transit;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

public class DocTask extends ProjectTask
{
    public static final String ORG_NAME_KEY = "project.organization.name";
    public static final String ORG_NAME_VALUE = "The Digital Product Meta Library";

    public static final String DOC_TEMP_KEY = "project.target.temp.docs";
    public static final String DOC_TEMP_VALUE = "docs";

    public static final String DOC_SRC_KEY = "project.docs.src";
    public static final String DOC_SRC_VALUE = "docs";

    public static final String DOC_RESOURCES_KEY = "project.docs.resources";
    public static final String DOC_RESOURCES_VALUE = "resources";

    public static final String DOC_THEME_KEY = "project.docs.theme";
    public static final String DOC_THEME_VALUE = "modern";

    public static final String DOC_FORMAT_KEY = "project.docs.output.format";
    public static final String DOC_FORMAT_VALUE = "html";

    public static final String DOC_DATE_FORMAT_KEY = "project.docs.date.format";
    public static final String DOC_DATE_FORMAT_VALUE = "yyyy-MMM-dd";

    public static final String DOC_STYLE_KEY = "project.docs.output.style";
    public static final String DOC_STYLE_VALUE = "standard";

    public static final String DOC_ENTRY_KEY = "project.docs.entry-point";
    public static final String DOC_ENTRY_VALUE = "";

    public static final String DOC_LOGO_RIGHT_FILE_KEY = "project.docs.logo.right.file";
    public static final String DOC_LOGO_RIGHT_FILE_VALUE = "";

    public static final String DOC_LOGO_RIGHT_URL_KEY = "project.docs.logo.right.url";
    public static final String DOC_LOGO_RIGHT_URL_VALUE = "";

    public static final String DOC_LOGO_LEFT_FILE_KEY = "project.docs.logo.left.file";
    public static final String DOC_LOGO_LEFT_FILE_VALUE = "";

    public static final String DOC_LOGO_LEFT_URL_KEY = "project.docs.logo.left.url";
    public static final String DOC_LOGO_LEFT_URL_VALUE = "";

    public static final String DOC_LOGO_MIDDLE_FILE_KEY = "project.docs.logo.middle.file";
    public static final String DOC_LOGO_MIDDLE_FILE_VALUE = "";

    public static final String DOC_LOGO_MIDDLE_URL_KEY = "project.docs.logo.middle.url";
    public static final String DOC_LOGO_MIDDLE_URL_VALUE = "";

    public static final String DOC_BRAND_NAME_KEY = "project.docs.brand.name";
    public static final String DOC_BRAND_NAME_VALUE = "DPML";

    public static final String DOC_ANCHOR_URL_KEY = "project.docs.anchor.url";

    private String m_theme;
    private File m_BaseToDir;
    private File m_BaseSrcDir;

    public String getTheme()
    {
        if( m_theme != null )
            return m_theme;
        return getProject().getProperty( DOC_THEME_KEY );
    }

    public void setTheme( final String theme )
    {
        m_theme = theme;
    }

    public void init() throws BuildException
    {
        if( !isInitialized() )
        {
            super.init();
            final Project project = getProject();
            project.setNewProperty( ORG_NAME_KEY, ORG_NAME_VALUE );
            project.setNewProperty( DOC_SRC_KEY, DOC_SRC_VALUE );
            project.setNewProperty( DOC_RESOURCES_KEY, DOC_RESOURCES_VALUE );
            project.setNewProperty( DOC_THEME_KEY, DOC_THEME_VALUE );
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

    private File getThemesDirectory()
    {
        return new File( Transit.DPML_PREFS, "magic/themes" );
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

    public void execute()
    {
        final Project project = getProject();
        final File docs = getContext().getDocsDirectory();

        //
        // get the directory containing the filtered docs source files
        // (normally target/src/docs)
        //

        final File build = getContext().getBuildDirectory();
        final String docsPath = project.getProperty( DOC_SRC_KEY );
        if( null == docsPath )
        {
            final String message =
              "Cannot continue as doc src directory not defined.";
            log( message );
            return;
        }

        final File srcDir = new File( build, docsPath );
        if( ! srcDir.exists() )
            return;
        log( "Filtered source: " + srcDir.getAbsolutePath() );

        //
        // create the temporary directory into which we generate the
        // navigation structure (normally target/temp/docs)
        //

        final File temp = getContext().getTempDirectory();
        final String tempPath = project.getProperty( DOC_TEMP_KEY );
        final File destDir = new File( temp, tempPath );
        mkDir( destDir );

        //
        // get the theme, output formats, etc.
        //

        log( "Destination: " + docs.getAbsolutePath() );
        mkDir( docs );

        final String theme = getTheme();
        final String output = getOutputFormat();
        final File themeRoot = getThemesDirectory();
        final File themeDir = new File( themeRoot, theme + "/" + output );

        final String resourcesPath = project.getProperty( DOC_RESOURCES_KEY );
        final File resources = new File( build, resourcesPath );

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
        catch( Throwable e )
        {
            log( "XSLT execution failed: " + e.getMessage() );
            throw new BuildException( e );
        }
    }

    private void transformNavigation( final File themeDir, final File source, final File dest )
    {
        final File xslFile = new File( themeDir,  "nav-aggregate.xsl" );
        if( ! xslFile.exists() )
            return;  // Theme may not use navigation.

        log( "Transforming navigation." );
        transformTrax(
          source, dest, xslFile,
          "^.*/navigation.xml$", "", ".xml" );
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
        if( ! fromDir.exists() )
            return;

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
/* Attempt to parse the XSL file separately failed. No idea why?
            fis = new FileInputStream( xslFile );
            BufferedInputStream stream = new BufferedInputStream( fis );
            InputSource in = new InputSource( stream );
            final SAXParserFactory pfactory = SAXParserFactory.newInstance();
            final SAXParser parser = pfactory.newSAXParser();
            XMLReader xmlReader = parser.getXMLReader();
            SAXSource source = new SAXSource( xmlReader, in );
*/
            ClassLoader cl = getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader( cl );
            StreamSource source = new StreamSource( xslFile );
            final TransformerFactory tfactory = TransformerFactory.newInstance();
            final Transformer transformer = tfactory.newTransformer( source );

            final RegexpFilter filter = new RegexpFilter( includes, excludes );

            m_BaseToDir = toDir;
            m_BaseSrcDir = srcDir.getAbsoluteFile();
            String entrypoint = getEntryPoint();
            if( "".equals( entrypoint ) )
            {
                transform( transformer, m_BaseSrcDir, toDir, filter, extension );
            }
            else
            {
                File fileToConvert = new File( m_BaseSrcDir, entrypoint );
                transformFile( transformer, fileToConvert, m_BaseSrcDir,
                               entrypoint, extension, m_BaseToDir );
            }
        } catch( Exception e )
        {
            throw new BuildException( e.getMessage(), e );
        } finally
        {
            if( fis != null )
            {
                try
                {
                    fis.close();
                } catch( IOException f )
                {}
            }
        }
    }

    private void transform( final Transformer transformer, final File srcDir, final File toDir,
        final FileFilter filter, final String extension )
        throws BuildException
    {
        boolean recursive = isRecursive();

        final File[] content = srcDir.listFiles( filter );
        for( int i = 0 ; i < content.length ; i++ )
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
        System.setProperty( "user.dir", toDir.getAbsolutePath() );
        final String year = getYear();
        final String org = getOrganization();
        final String copyright =
          "Copyright " + year + ", " + org + " All rights reserved.";

        final String svnRoot = getProject().getProperty( DOC_ANCHOR_URL_KEY );
        final String svnSource = svnRoot + getRelSrcPath( srcDir ) + "/" + base;

        final int pos = base.lastIndexOf( '.' );
        if( pos > 0 )
            base = base.substring( 0, pos );
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
            getProject().getProperty( DOC_LOGO_RIGHT_URL_KEY).trim() );
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
        catch( Exception e )
        {
            log( "ERROR: " + getRelToPath( newDest ) + " : " + e.getMessage() );
            throw new BuildException(
                "Unable to transform document." );
        }
    }

    private String getRelToPath( final File dir )
    {
        final String basedir = m_BaseToDir.getAbsolutePath();
        final String curdir = dir.getAbsolutePath();
        return curdir.substring( basedir.length() );
    }

    private String getRelSrcPath( final File dir )
    {
        final String basedir = m_BaseSrcDir.getAbsolutePath();
        final String curdir = dir.getAbsolutePath();
        return curdir.substring( basedir.length() );
    }


    public class RegexpFilter
        implements FileFilter
    {
        private Pattern m_Includes;
        private Pattern m_Excludes;

        public RegexpFilter( final String includes, final String excludes )
        {
            m_Includes = Pattern.compile( includes );
            m_Excludes = Pattern.compile( excludes );
        }

        public boolean accept( final File file )
        {
            final String basename = file.getName();

            if( basename.equals( ".svn" ) )
                return false;

            if( basename.equals( "CVS" ) )
                return false;

            if( file.isDirectory() )
                return true;

            final String fullpath = file.getAbsolutePath().replace( '\\', '/' );

            Matcher m = m_Includes.matcher( fullpath );
            if( ! m.matches() )
                return false;

            m = m_Excludes.matcher( fullpath );
            return ! m.matches() ;
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
            return "";
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

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

package org.apache.metro.magic.spells.xdoc;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

import org.apache.avalon.tools.tasks.SystemTask;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.util.Calendar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XdocTask extends SystemTask
{
    public static final String ORG_NAME_KEY = "project.organization.name";
    public static final String ORG_NAME_VALUE = "The Apache Software Foundation";

    public static final String XDOC_TEMP_KEY = "project.target.temp.xdocs";
    public static final String XDOC_TEMP_VALUE = "xdocs";

    public static final String XDOC_SRC_KEY = "project.xdocs.src";
    public static final String XDOC_SRC_VALUE = "xdocs";

    public static final String XDOC_RESOURCES_KEY = "project.xdocs.resources";
    public static final String XDOC_RESOURCES_VALUE = "resources";

    public static final String XDOC_THEME_KEY = "project.xdoc.theme";
    public static final String XDOC_THEME_VALUE = "modern";

    public static final String XDOC_FORMAT_KEY = "project.xdoc.output.format";
    public static final String XDOC_FORMAT_VALUE = "html";

    public static final String XDOC_LOGO_RIGHT_FILE_KEY = "project.xdoc.logo.right.file";
    public static final String XDOC_LOGO_RIGHT_FILE_VALUE = "";

    public static final String XDOC_LOGO_RIGHT_URL_KEY = "project.xdoc.logo.right.url";
    public static final String XDOC_LOGO_RIGHT_URL_VALUE = "";

    public static final String XDOC_LOGO_LEFT_FILE_KEY = "project.xdoc.logo.left.file";
    public static final String XDOC_LOGO_LEFT_FILE_VALUE = "";

    public static final String XDOC_LOGO_LEFT_URL_KEY = "project.xdoc.logo.left.url";
    public static final String XDOC_LOGO_LEFT_URL_VALUE = "";

    public static final String XDOC_LOGO_MIDDLE_FILE_KEY = "project.xdoc.logo.middle.file";
    public static final String XDOC_LOGO_MIDDLE_FILE_VALUE = "";

    public static final String XDOC_LOGO_MIDDLE_URL_KEY = "project.xdoc.logo.middle.url";
    public static final String XDOC_LOGO_MIDDLE_URL_VALUE = "";

    public static final String XDOC_BRAND_NAME_KEY = "project.xdoc.brand.name";
    public static final String XDOC_BRAND_NAME_VALUE = "Avalon";

    public static final String XDOC_ANCHOR_URL_KEY = "project.xdoc.anchor.url";

    private String m_theme;
    private File m_BaseToDir;    
    private File m_BaseSrcDir; 

    public String getTheme()
    {
        if( m_theme != null ) 
            return m_theme;
        return getProject().getProperty( XDOC_THEME_KEY );
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
            project.setNewProperty( XDOC_SRC_KEY, XDOC_SRC_VALUE );
            project.setNewProperty( XDOC_RESOURCES_KEY, XDOC_RESOURCES_VALUE );
            project.setNewProperty( XDOC_THEME_KEY, XDOC_THEME_VALUE );
            project.setNewProperty( XDOC_FORMAT_KEY, XDOC_FORMAT_VALUE );
            project.setNewProperty( XDOC_TEMP_KEY, XDOC_TEMP_VALUE );
            project.setNewProperty( XDOC_LOGO_RIGHT_FILE_KEY, XDOC_LOGO_RIGHT_FILE_VALUE );
            project.setNewProperty( XDOC_LOGO_RIGHT_URL_KEY, XDOC_LOGO_RIGHT_URL_VALUE );
            project.setNewProperty( XDOC_LOGO_LEFT_FILE_KEY, XDOC_LOGO_LEFT_FILE_VALUE );
            project.setNewProperty( XDOC_LOGO_LEFT_URL_KEY, XDOC_LOGO_LEFT_URL_VALUE );
            project.setNewProperty( XDOC_LOGO_MIDDLE_FILE_KEY, XDOC_LOGO_MIDDLE_FILE_VALUE );
            project.setNewProperty( XDOC_LOGO_MIDDLE_URL_KEY, XDOC_LOGO_MIDDLE_URL_VALUE );
            project.setNewProperty( XDOC_BRAND_NAME_KEY, XDOC_BRAND_NAME_VALUE );
        }
    }

    private File getThemesDirectory()
    {
        final File cache = getHome().getRepository().getCacheDirectory();
        return new File( cache, "avalon/tools/themes" );
    }
    
    private String getOutputFormat()
    {
        return getProject().getProperty( XDOC_FORMAT_KEY );
    }

    public void execute()
    {
        final Project project = getProject();
        final File docs = getContext().getDocsDirectory();

        //
        // get the directory containing the filtered xdocs source files 
        // (normally target/src/xdocs)
        //

        final File build = getContext().getBuildDirectory();
        final String xdocsPath = project.getProperty( XDOC_SRC_KEY );
        if( null == xdocsPath )
        {
            final String message =
              "Cannot continue as xdoc src directory not defined.";
            log( message );
            return;
        }

        final File srcDir = new File( build, xdocsPath );
        if( !srcDir.exists() ) return;
        log( "Filtered source: " + srcDir.getAbsolutePath() );

        //
        // create the temporary directory into which we generate the 
        // navigation structure (normally target/temp/xdocs)
        //

        final File temp = getContext().getTempDirectory();
        final String tempPath = project.getProperty( XDOC_TEMP_KEY );
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
        
        final String resourcesPath = project.getProperty( XDOC_RESOURCES_KEY );
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
            transformXdocs( themeDir, destDir, docs );
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
        log( "Transforming navigation." );
        transformTrax( 
          source, dest, xslFile, 
          "^.*/navigation.xml$", "", ".xml" );
    }
    
    private void copySources( final File source, final File dest )
    {
        copy( source, dest, "**/*", "**/navigation.xml" );
    }
    
    private void transformXdocs( final File themeDir, final File build, final File docs )
    {
        final File xslFile = new File( themeDir,  "transform.xsl" );
        final String output = getOutputFormat();
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
        try
        {
            final TransformerFactory factory = TransformerFactory.newInstance();
            final StreamSource xsl = new StreamSource( xslFile );
            final Transformer transformer = factory.newTransformer( xsl );

            final RegexpFilter filter = new RegexpFilter( includes, excludes );

            m_BaseToDir = toDir;
            m_BaseSrcDir = srcDir.getAbsoluteFile();
            transform( transformer, m_BaseSrcDir, toDir, filter, extension );
        } 
        catch( TransformerException e )
        {
            throw new BuildException( e.getMessage(), e );
        }
    }

    private void transform( final Transformer transformer, final File srcDir, final File toDir,
        final FileFilter filter, final String extension )
        throws BuildException
    {
        final String year = getYear();
        final String org = getOrganization();
        final String copyright =
          "Copyright " + year + ", " + org + " All rights reserved.";


        final File[] content = srcDir.listFiles( filter );
        for( int i = 0 ; i < content.length ; i++ )
        {
            String base = content[i].getName();
            if( content[i].isDirectory() )
            {
                final File newDest = new File( toDir, base );
                newDest.mkdirs();
                transform( transformer, content[i], newDest, filter, extension );
            }
            if( content[i].isFile() )
            {
                final String svnRoot = getProject().getProperty( XDOC_ANCHOR_URL_KEY );
                final String svnSource = svnRoot + getRelSrcPath( srcDir ) + "/" + base;

                final int pos = base.lastIndexOf( '.' );
                if( pos > 0 )
                    base = base.substring( 0, pos );
                base = base + extension;

                final File newDest = new File( toDir, base );
                final StreamSource xml = new StreamSource( content[i] );
                final StreamResult out = new StreamResult( newDest );
                
                transformer.clearParameters();

                transformer.setParameter( "directory", getRelToPath( toDir ) );
                transformer.setParameter( "fullpath", getRelToPath( newDest ) );
                transformer.setParameter( "file", base );
                transformer.setParameter( "svn-location", svnSource );

                transformer.setParameter( "copyright", copyright );
                transformer.setParameter( 
                    "logoright_file", 
                    getProject().getProperty( XDOC_LOGO_RIGHT_FILE_KEY ).trim() );
                transformer.setParameter( 
                    "logoright_url", 
                    getProject().getProperty( XDOC_LOGO_RIGHT_URL_KEY).trim() );
                transformer.setParameter( 
                    "logoleft_file", 
                    getProject().getProperty( XDOC_LOGO_LEFT_FILE_KEY ).trim() );
                transformer.setParameter( 
                    "logoleft_url", 
                    getProject().getProperty( XDOC_LOGO_LEFT_URL_KEY ).trim() );
                transformer.setParameter( 
                    "logomiddle_file", 
                    getProject().getProperty( XDOC_LOGO_MIDDLE_FILE_KEY ).trim() );
                transformer.setParameter( 
                    "logomiddle_url", 
                    getProject().getProperty( XDOC_LOGO_MIDDLE_URL_KEY ).trim() );
                transformer.setParameter( 
                    "brand_name", 
                    getProject().getProperty( XDOC_BRAND_NAME_KEY ).trim() );
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
}

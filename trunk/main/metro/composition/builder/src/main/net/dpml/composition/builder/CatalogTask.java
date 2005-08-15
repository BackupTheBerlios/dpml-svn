/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.composition.builder;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Map;

import net.dpml.composition.info.Type;

import net.dpml.magic.tasks.ProjectTask;

import net.dpml.transit.Transit;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.XSLTProcess;
import org.apache.tools.ant.taskdefs.XSLTProcess.Param;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Create a set of html reports about the component types based on serialized
 * types under the user's ${basedir}/target/classes directory.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class CatalogTask extends ProjectTask
{
    private File m_work;
    private File m_destination;
    private String m_style;
    private List m_filesets = new LinkedList();
    private Map m_packages = new Hashtable();

   /**
    * Set the final destination directory for generated HTML content.
    * @param destination the destination directory
    */
    public void setDest( File destination )
    {
        m_destination = destination;
    }

    public void setStyle( String style )
    {
        m_style = style;
    }

    private String getStyle()
    {
        if( null == m_style )
        {
            return new File( Transit.DPML_PREFS, "metro/style/type-formatter.xsl" ).toString();
        }
        else
        {
            return m_style;
        }
    }

    public void addFileset( FileSet set )
    {
        m_filesets.add( set );
    }

    public void init()
    {
        File target = getContext().getTargetDirectory();
        File reports = new File( target, "reports" );
        File types = new File( reports, "types" );
        m_work = types;
    }

    public void execute()
    {
        Project proj = getProject();
        File[] types = getTypes();
        log( "Catalog size: " + types.length );
        if( types.length == 0 )
        {
            return;
        }
        else
        {
            File reports = getWorkingDirectory();
            File html = new File( reports, "html" );
            html.mkdirs();

            File css = new File( Transit.DPML_PREFS, "metro/style/type-stylesheet.css" );
            File stylesheet = new File( html, "stylesheet.css" );
            final Copy copy = (Copy) getProject().createTask( "copy" );
            copy.setFile( css );
            copy.setTofile( stylesheet );
            copy.execute();

            for( int i=0; i < types.length; i++ )
            {
                File type = types[i];
                processType( reports, type );
            }

            createIndexFile( html );
        }
    }

   /**
    * A side effect of type processing is the population of the m_packages map.  The 
    * map keys are package names and the map values are list of types within the 
    * respective package.  The keys constitute the package list and for each package 
    * we construct an index of types within that package.
    *
    * @param root the root html directory
    */
    private void createIndexFile( File root )
    {
         createIndex( root );
         createPackagesIndex( root );
         createTypesIndex( root );
         createOverviewPage( root );
    }

   /**
    * Create the list of all types.
    * @param root the root html directory
    */
    private void createTypesIndex( File root )
    {
        File index = new File( root, "types.html" );
        try
        {
            FileWriter writer = new FileWriter( index );
            writer.write( "<html>" );
            writer.write( "\n  <head>" );
            writer.write( "\n    <META http-equiv=\"Content-Type\" content=\"text/html; charset=US-ASCII\">" );
            writer.write( "\n    <title>Package Index</title>" );
            writer.write( "\n    <link href=\"stylesheet.css\" type=\"text/css\" rel=\"stylesheet\">" );
            writer.write( "\n  </head>" );
            writer.write( "\n  <body>" );
            writer.write( "\n    <p><b>Overview</b></p>" );
            writer.write( "\n    <p><a href=\"packages-overview.html\" target=\"typeFrame\">All Packages</a></p>" );
            writer.write( "\n    <p><b>All Types</b></p>" );
            writer.write( "\n    <table>" );

            List[] lists = (List[]) m_packages.values().toArray( new List[0] );
            for( int i=0; i < lists.length; i++ )
            {
                List list = lists[i];
                Type[] types = (Type[]) list.toArray( new Type[0] );
                for( int j=0; j < types.length; j++ )
                {
                    Type type = types[j];
                    String path = type.getInfo().getClassname().replace( '.', '/' ); 
                    String classname = getClassName( type );
                    String filename = path + ".html"; 
                    writer.write( "\n      <tr><td><a href=\"" );
                    writer.write( filename );
                    writer.write( "\" target=\"typeFrame\">" );
                    writer.write( classname );
                    writer.write( "</a></td></tr>" );
                }
            }
            writer.write( "\n    </table>" );
            writer.write( "\n  </body>" );
            writer.write( "\n</html>" );
            writer.close();
        }
        catch( Exception e )
        {
            final String error = 
              "Internal error while attempting to create packages list.";
            throw new BuildException( error, e, getLocation() );
        }
    }

    private void createOverviewPage( File root )
    {
        File overview = new File( root, "packages-overview.html" );
        try
        {
            FileWriter writer = new FileWriter( overview );
            writer.write( "<html>" );
            writer.write( "\n  <head>" );
            writer.write( "\n    <META http-equiv=\"Content-Type\" content=\"text/html; charset=US-ASCII\">" );
            writer.write( "\n    <title>Type Packages List</title>" );
            writer.write( "\n    <link href=\"stylesheet.css\" type=\"text/css\" rel=\"stylesheet\">" );
            writer.write( "\n  </head>" );
            writer.write( "\n  <body>" );
            writer.write( "\n  <h3>Overview</h3>" );
            writer.write( "\n    <table>" );
            writer.write( "\n    <tr><th>Package</th><th>Components</th></tr>" );

            Object[] keys = m_packages.keySet().toArray();
            for( int i=0; i < keys.length; i++ )
            {
                String name = (String) keys[i];
                createPackageOverview( root, name );
                List list = getPackageList( name );
                writer.write( "\n      <tr>" );
                writer.write( "<td><a href=\"" );
                String path = name.replace( '.', '/' );
                writer.write( path + "/overview.html\" target=\"typeFrame\">" );
                writer.write( name );
                writer.write( "</a></td>"  );
                writer.write( "<td>" );
                Type[] types = (Type[]) list.toArray( new Type[0] );
                for( int j=0; j < types.length; j++ )
                {
                    Type type = types[j];
                    String classname = getClassName( type );
                    String filename = type.getInfo().getClassname().replace( '.', '/' ) + ".html";
                    if( j > 0 )
                    {
                        writer.write( ", " );
                    }
                    writer.write( "<a href=\"" );
                    writer.write( filename );
                    writer.write( "\" target=\"typeFrame\">" );
                    writer.write( classname );
                    writer.write( "</a>" );
                }
                writer.write( "</td>" );
                writer.write( "</tr>" );
            }

            writer.write( "\n    </table>" );
            writer.write( "\n  </body>" );
            writer.write( "\n</html>" );
            writer.close();
        }
        catch( Exception e )
        {
            final String error = 
              "Internal error while attempting to create packages overview.";
            throw new BuildException( error, e, getLocation() );
        }
    }

    private void createPackagesIndex( File root )
    {
        File packages = new File( root, "packages.html" );
        try
        {
            FileWriter writer = new FileWriter( packages );
            writer.write( "<html>" );
            writer.write( "\n  <head>" );
            writer.write( "\n    <META http-equiv=\"Content-Type\" content=\"text/html; charset=US-ASCII\">" );
            writer.write( "\n    <title>Type Packages List</title>" );
            writer.write( "\n    <link href=\"stylesheet.css\" type=\"text/css\" rel=\"stylesheet\">" );
            writer.write( "\n  </head>" );
            writer.write( "\n  <body>" );
            writer.write( "\n  <h3>Metro</h3>" );
            writer.write( "\n  <p><a href=\"types.html\" target=\"typeListFrame\">All Types</a></p>" );
            writer.write( "\n  <p><b>Packages</b></p>" );
            writer.write( "\n    <table>" );

            Object[] keys = m_packages.keySet().toArray();
            for( int i=0; i < keys.length; i++ )
            {
                String name = (String) keys[i];
                writer.write( "\n      <tr><td><a href=\"" );
                String path = name.replace( '.', '/' );
                writer.write( path + "/package-index.html\" target=\"typeListFrame\">" );
                writer.write( name  );
                writer.write( "</a></td></tr>"  );
                createPackageIndex( root, name );
            }
  
            writer.write( "\n    </table>" );
            writer.write( "\n  </body>" );
            writer.write( "\n</html>" );
            writer.close();
        }
        catch( Exception e )
        {
            final String error = 
              "Internal error while attempting to create packages list.";
            throw new BuildException( error, e, getLocation() );
        }
    }

    private void createPackageIndex( File root, String name )
    {
        String path = name.replace( '.', '/' );
        File dir = new File( root, path );
        dir.mkdirs();
        File index = new File( dir, "package-index.html" );
        String offset = getOffset( name );
        try
        {
            FileWriter writer = new FileWriter( index );
            writer.write( "<html>" );
            writer.write( "\n  <head>" );
            writer.write( "\n    <META http-equiv=\"Content-Type\" content=\"text/html; charset=US-ASCII\">" );
            writer.write( "\n    <title>Package Index</title>" );
            writer.write( "\n    <link href=\"" + offset + "/stylesheet.css\" type=\"text/css\" rel=\"stylesheet\">" );
            writer.write( "\n  </head>" );
            writer.write( "\n  <body>" );
            writer.write( "\n    <p><b>Overview</b></p>" );
            writer.write( "\n    <p><a href=\"overview.html\" target=\"typeFrame\">" + name + "</a></p>" );
            writer.write( "\n    <p><b>Types</b></p>" );
            writer.write( "\n    <table>" );

            List list = getPackageList( name );
            Type[] types = (Type[]) list.toArray( new Type[0] );
            for( int i=0; i < types.length; i++ )
            {
                Type type = types[i];
                String classname = getClassName( type );
                String filename = classname + ".html"; 
                writer.write( "\n      <tr><td><a href=\"" );
                writer.write( filename );
                writer.write( "\" target=\"typeFrame\">" );
                writer.write( classname );
                writer.write( "</a></td></tr>" );
            }
            writer.write( "\n    </table>" );
            writer.write( "\n  </body>" );
            writer.write( "\n</html>" );
            writer.close();
        }
        catch( Exception e )
        {
            final String error = 
              "Internal error while attempting to create packages list.";
            throw new BuildException( error, e, getLocation() );
        }
    }

    private void createPackageOverview( File root, String name )
    {
        String path = name.replace( '.', '/' );
        File dir = new File( root, path );
        dir.mkdirs();
        File index = new File( dir, "overview.html" );
        String offset = getOffset( name );
        try
        {
            FileWriter writer = new FileWriter( index );
            writer.write( "<html>" );
            writer.write( "\n  <head>" );
            writer.write( "\n    <META http-equiv=\"Content-Type\" content=\"text/html; charset=US-ASCII\">" );
            writer.write( "\n    <title>Package Overview: " + name + "</title>" );
            writer.write( "\n    <link href=\"" + offset + "/stylesheet.css\" type=\"text/css\" rel=\"stylesheet\">" );
            writer.write( "\n  </head>" );
            writer.write( "\n  <body>" );
            writer.write( "\n    <h3>Package " + name + "</h3>" );
            writer.write( "\n    <p><b>Types</b></p>" );
            writer.write( "\n    <table>" );
            writer.write( "\n      <tr><th>Type</th><th>Services</th></tr>" );

            List list = getPackageList( name );
            Type[] types = (Type[]) list.toArray( new Type[0] );
            for( int i=0; i < types.length; i++ )
            {
                Type type = types[i];
                String classname = getClassName( type );
                String filename = classname + ".html"; 
                writer.write( "\n      <tr>" );
                writer.write( "\n        <td><a href=\"" );
                writer.write( filename );
                writer.write( "\" target=\"typeFrame\">" );
                writer.write( classname );
                writer.write( "</a>" );
                writer.write( "\n        </td>" );
                writer.write( "\n        <td></td>" );
                writer.write( "\n      </tr>" );
            }
            writer.write( "\n    </table>" );
            writer.write( "\n  </body>" );
            writer.write( "\n</html>" );
            writer.close();
        }
        catch( Exception e )
        {
            final String error = 
              "Internal error while attempting to create packages list.";
            throw new BuildException( error, e, getLocation() );
        }
    }


    private List getPackageList( String name )
    {
        List list = (List) m_packages.get( name );
        if( null == list )
        {
            list = new ArrayList();
            m_packages.put( name, list );
        }
        return list;
    }

    private void processType( File reports, File source )
    {
        File xmls = new File( reports, "xml" );
        File htmls = new File( reports, "html" );

        int offset = reports.toString().length() + 1;
        try
        {
            InputStream input = new FileInputStream( source );
            Type type = Type.loadType( input );
            String packagename = getPackageName( type );
            List packageList = getPackageList( packagename );
            packageList.add( type );

            File xml = getReportDestination( xmls, type, "xml" );
            xml.getParentFile().mkdirs();
            XStream XStream = new XStream( new DomDriver() );
            XStream.alias( "type", Type.class );
            XStream.toXML( type, new FileWriter( xml ) );

            File html = getReportDestination( htmls, type, "html" );
            html.getParentFile().mkdirs();
            String basepath = getBasePath( type );
            String classname = getClassName( type );
            final XSLTProcess xslt = (XSLTProcess) getProject().createTask( "xslt" );
            xslt.setProject( getProject() );
            xslt.init();

            Param param = xslt.createParam();
            param.setName( "basepath" );
            param.setExpression( basepath );

            Param packageParam = xslt.createParam();
            packageParam.setName( "package" );
            packageParam.setExpression( packagename );

            Param classnameParam = xslt.createParam();
            classnameParam.setName( "classname" );
            classnameParam.setExpression( classname );

            xslt.setIn( xml );
            xslt.setStyle( getStyle() );
            xslt.setOut( html );
            xslt.execute();
        }
        catch( Exception e )
        {
            throw new BuildException( e.getMessage(), e, getLocation() );
        }
    }

    private File getReportDestination( File dir, Type type, String suffix )
    {
        final String classname = type.getInfo().getClassname();
        String path = classname.replace( '.', '/' );
        String filename = path + "." + suffix;
        return new File( dir, filename );
    }

    private String getClassName( Type type )
    {
        String path = type.getInfo().getClassname();
        int n = path.lastIndexOf( "." );
        if( n > - 1 )
        {
            return path.substring( n + 1 );
        }
        else
        {
            return path;
        }
    }

    private String getPackageName( Type type )
    {
        String path = type.getInfo().getClassname();
        int n = path.lastIndexOf( "." );
        if( n > - 1 )
        {
            return path.substring( 0, n );
        }
        else
        {
            return "";
        }
    }

    private String getBasePath( Type type )
    {
        String base = "";
        String path = type.getInfo().getClassname();
        while( path.indexOf( "." ) > 0 )
        {
            path = path.substring( path.indexOf( "." ) + 1 );
            if( base.equals( "" ) )
            {
                base = "..";
            }
            else
            {
                base = base + "/..";
            }
        }
        return base;
    }

    private String getOffset( String packagename )
    {
        String base = "..";
        String path = packagename;
        while( path.indexOf( "." ) > 0 )
        {
            path = path.substring( path.indexOf( "." ) + 1 );
            base = base + "/..";
        }
        return base;
    }



    private File getWorkingDirectory()
    {
        return m_work;
    }

    private File[] getTypes()
    {
        if( m_filesets.size() == 0 )
        {         
            File classes = getContext().getClassesDirectory();
            return getTypes( classes );
        }
        else
        {
            ArrayList list = new ArrayList();
            Project project = getProject();
            FileSet[] filesets = (FileSet[]) m_filesets.toArray( new FileSet[0] );
            for( int i=0; i < filesets.length; i++ )
            {
                FileSet fileset = filesets[i];
                DirectoryScanner ds = fileset.getDirectoryScanner( project );
                String[] files = ds.getIncludedFiles();
                for( int j=0; j < files.length; j++ )
                {
                    list.add( new File( files[j] ) );
                }
            }
            return (File[]) list.toArray( new File[0] );
        }
    }

    private File[] getTypes( File dir )
    {
        ArrayList list = new ArrayList();
        getTypes( list, dir );
        return (File[]) list.toArray( new File[0] );
    }

    private void getTypes( List list, File dir )
    {
        File[] types = dir.listFiles( new TypeFileFilter() );
        for( int i=0; i<types.length; i++ )
        {
            list.add( types[i] );
        }
        File[] dirs = dir.listFiles( new DirectoryFilter() );
        for( int i=0; i<dirs.length; i++ )
        {
            getTypes( list, dirs[i] );
        }
    }

    private static class TypeFileFilter implements FileFilter
    {
        public boolean accept( File file )
        {
            return file.getName().endsWith( ".type" );
        }
    }

    private static class DirectoryFilter implements FileFilter
    {
        public boolean accept( File file )
        {
            return file.isDirectory();
        }
    }

    private void createIndex( File root )
    {
        File index = new File( root, "index.html" );
        try
        {
            FileWriter writer = new FileWriter( index );
            writer.write( "<html>" );
            writer.write( "\n  <head>" );
            writer.write( "\n    <META http-equiv=\"Content-Type\" content=\"text/html; charset=US-ASCII\">" );
            writer.write( "\n    <title>Type Packages List</title>" );
            writer.write( "\n    <link href=\"stylesheet.css\" type=\"text/css\" rel=\"stylesheet\">" );
            writer.write( "\n  </head>" );
            writer.write( "\n  <frameset cols=\"20%,80%\">" );
            writer.write( "\n    <frameset rows=\"30%,70%\">" );
            writer.write( "\n      <frame name=\"packageListFrame\" src=\"packages.html\">" );
            writer.write( "\n      <frame name=\"typeListFrame\" src=\"types.html\">" );
            writer.write( "\n    </frameset>" );
            writer.write( "\n    <frame name=\"typeFrame\" src=\"packages-overview.html\">" );
            writer.write( "\n  </frameset>" );
            writer.write( "\n</html>" );
            writer.close();
        }
        catch( Exception e )
        {
            final String error = 
              "Internal error while attempting to create frameset.";
            throw new BuildException( error, e, getLocation() );
        }
    }
}

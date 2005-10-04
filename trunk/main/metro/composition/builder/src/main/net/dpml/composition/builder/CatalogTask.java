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

import net.dpml.part.Part;
import net.dpml.component.info;.PartReference;
import net.dpml.component.info;.EntryDescriptor;

import net.dpml.component.info;.Type;
import net.dpml.component.info;.InfoDescriptor;
import net.dpml.component.info;.LifestylePolicy;
import net.dpml.component.info;.ContextDescriptor;
import net.dpml.component.info;.ServiceDescriptor;
import net.dpml.component.data.ComponentDirective;

import net.dpml.configuration.Configuration;
import net.dpml.configuration.impl.ConfigurationUtil;

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
    private String m_title;
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

   /**
    * Optional override of the XSL stylesheet used to transform type descriptors.
    * @param style the XSL transformation to use
    */
    public void setStyle( String style )
    {
        m_style = style;
    }

   /**
    * Optional override of the index page title.
    * @param title the index page title
    */
    public void setTitle( String title )
    {
        m_title = title;
    }

   /**
    * Add a fileset to the set of filesets definted within the catalog generation. If no
    * filesets are added the default behaviour will be resolve against component types 
    * declared under the current projects ${basedir}/target/classes directory.
    *
    * @param set the fileset to add
    */
    public void addFileset( FileSet set )
    {
        m_filesets.add( set );
    }

   /**
    * Task initialization.
    */
    public void init()
    {
        File target = getContext().getTargetDirectory();
        File reports = new File( target, "reports" );
        File types = new File( reports, "types" );
        m_work = types;
    }

   /**
    * Task execution during which all component types declared in the current project or referenced
    * by nested filesets will be used as the source defintions fro the generation of a Type catalog.
    */
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

            createCatalog( html );
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
    private void createCatalog( File root )
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
            writer.write( "\n    <link href=\"stylesheet.css\" type=\"text/css\" rel=\"stylesheet\"/>" );
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
            writer.write( "\n    <link href=\"stylesheet.css\" type=\"text/css\" rel=\"stylesheet\"/>" );
            writer.write( "\n  </head>" );
            writer.write( "\n  <body>" );

            //
            // add menu containing packages overview links
            //

            writer.write( "\n    <table class=\"menubar\">" );
            writer.write( "\n      <tr>" );
            writer.write( "<td class=\"package\"><a class=\"package\" href=\"packages-overview.html\">" );
            writer.write( "overview" );
            writer.write( "</a></td>" );
            writer.write( "\n      </tr>" );
            writer.write( "\n    </table>" );

            writer.write( "\n  <p class=\"title\">Overview</p>" );

            writer.write( "\n    <table>" );
            writer.write( "\n    <tr><th>Package</th><th>Components</th></tr>" );

            boolean flag = true;
            Object[] keys = m_packages.keySet().toArray();
            for( int i=0; i < keys.length; i++ )
            {
                String name = (String) keys[i];
                createPackageOverview( root, name );
                List list = getPackageList( name );
                if( flag )
                {
                    writer.write( "\n      <tr class=\"p-even\">" );
                }
                else
                {
                    writer.write( "\n      <tr class=\"p-odd\">" );
                }
                flag = !flag;

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

            String title = getTitle();

            writer.write( "\n    <table>" );
            writer.write( "\n      <tr><td class=\"title\">" + title + "</td></tr>" );
            writer.write( "\n    </table>" );

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
            writer.write( "\n    <link href=\"" + offset + "/stylesheet.css\" type=\"text/css\" rel=\"stylesheet\"/>" );
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
            writer.write( "\n    <link href=\"" + offset + "/stylesheet.css\" type=\"text/css\" rel=\"stylesheet\"/>" );
            writer.write( "\n  </head>" );
            writer.write( "\n  <body>" );

            //
            // add the menubar containing the link to the all packages overview
            //

            writer.write( "\n    <table class=\"menubar\">" );
            writer.write( "\n      <tr>" );
            writer.write( "<td class=\"package\"><a class=\"package\" href=\"" + offset + "/packages-overview.html\">" );
            writer.write( "overview" );
            writer.write( "</a></td>" );
            writer.write( "<td class=\"package-selected\"><a class=\"package-selected\" href=\"overview.html\">" );
            writer.write( name );
            writer.write( "</a></td>" );
            writer.write( "\n      </tr>" );
            writer.write( "\n    </table>" );

            //
            // add the package name
            //

            writer.write( "\n    <table>" );
            writer.write( "\n      <tr><td class=\"title\">" + name + "</td></tr>" );
            writer.write( "\n    </table>" );

            writer.write( "\n    <table width=\"100%\">" );
            writer.write( "\n      <tr><th>Type</th><th>Name</th><th>Version</th><th>Lifestyle</th></tr>" );

            //
            // list all component types in the package
            //

            boolean flag = true;
            List list = getPackageList( name );
            Type[] types = (Type[]) list.toArray( new Type[0] );
            for( int i=0; i < types.length; i++ )
            {
                Type type = types[i];
                if( flag )
                {
                    writer.write( "\n      <tr class=\"p-even\">" );
                }
                else
                {
                    writer.write( "\n      <tr class=\"p-odd\">" );
                }
                flag = !flag;

                String classname = getClassName( type );
                String filename = classname + ".html"; 
                writer.write( "\n        <td><a href=\"" );
                writer.write( filename );
                writer.write( "\" target=\"typeFrame\">" );
                writer.write( classname );
                writer.write( "</a>" );
                writer.write( "\n        </td>" );

                writer.write( "\n        <td>" );
                writer.write( type.getInfo().getName() );
                writer.write( "\n        </td>" );

                writer.write( "\n        <td>" );
                writer.write( type.getInfo().getVersion().toString() );
                writer.write( "\n        </td>" );

                writer.write( "\n        <td>" );
                writer.write( type.getInfo().getLifestyle().getName() );
                writer.write( "\n        </td>" );

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

    private void createTypePage( File root, Type type )
    {
        File html = getReportDestination( root, type, "html" );
        html.getParentFile().mkdirs();
        String classname = type.getInfo().getClassname();
        String cname = getClassName( type );
        String packagename = getPackageName( type );
        String offset = getOffset( packagename );
        List packageList = getPackageList( packagename );
        packageList.add( type );
        try
        {
            FileWriter writer = new FileWriter( html );
            writer.write( "<html>" );
            writer.write( "\n  <head>" );
            writer.write( "\n    <META http-equiv=\"Content-Type\" content=\"text/html; charset=US-ASCII\">" );
            writer.write( "\n    <title>Component Type: " + classname + "</title>" );
            writer.write( "\n    <link href=\"" + offset + "/stylesheet.css\" type=\"text/css\" rel=\"stylesheet\"/>" );
            writer.write( "\n  </head>" );
            writer.write( "\n  <body>" );

            //
            // add menu containing packages and package overview links
            //

            writer.write( "\n    <table class=\"menubar\">" );
            writer.write( "\n      <tr>" );
            writer.write( "<td class=\"package\"><a class=\"package\" href=\"" + offset + "/packages-overview.html\">" );
            writer.write( "overview" );
            writer.write( "</a></td>" );
            writer.write( "<td class=\"package\"><a class=\"package\" href=\"overview.html\">" );
            writer.write( packagename );
            writer.write( "</a></td>" );
            writer.write( "<td class=\"package-selected\">" + cname + "</td>" );
            writer.write( "\n      </tr>" );
            writer.write( "\n    </table>" );

            //
            // add component type classname as title
            //

            writer.write( "\n    <table>" );
            writer.write( "\n      <tr><td class=\"type\">" + cname + "</td></tr>" );
            writer.write( "\n    </table>" );

            //
            // write out the InfoDescriptor freatures
            //

            writer.write( "\n    <table>" );
            writer.write( "\n      <tr><td class=\"feature\">Version:</td><td>" + type.getInfo().getVersion() + "</td></tr>" );
            writer.write( "\n      <tr><td class=\"feature\">Name:</td><td>" + type.getInfo().getName() + "</td></tr>" );
            writer.write( "\n      <tr><td class=\"feature\">Lifestyle:</td><td>" + type.getInfo().getLifestyle().getName() + "</td></tr>" );
            writer.write( "\n      <tr><td class=\"feature\">Thread-Safe:</td><td>" + type.getInfo().isThreadsafe() + "</td></tr>" );
            writer.write( "\n      <tr><td class=\"feature\">Collection:</td><td>" 
              + type.getInfo().getCollectionPolicy().getName() + "</td></tr>" );
            writer.write( "\n    </table>" );

            //
            // write out the exported services that the component provides
            //

            boolean flag = true;
            ServiceDescriptor[] services = type.getServiceDescriptors();
            if( services.length > 0 )
            {
                writer.write( "\n    <p class=\"category\">Services</p>" );
                writer.write( "\n    <table width=\"100%\">" );
                for( int j=0; j < services.length; j++ )
                {
                     String service = services[j].getClassname();
                     if( flag )
                     {
                         writer.write( "<tr class=\"even\"><td>" + service + "</td></tr>" );
                     }
                     else
                     {
                         writer.write( "<tr class=\"odd\"><td>" + service + "</td></tr>" );
                     }
                     flag = !flag;
                }
                writer.write( "\n    </table>" );
            }

            //
            // write out the context model
            //

            flag = true;
            ContextDescriptor context = type.getContextDescriptor();
            EntryDescriptor[] entries = context.getEntryDescriptors();
            if( entries.length > 0  )
            {
                writer.write( "\n    <p class=\"category\">Context</p>" );
                writer.write( "\n    <table width=\"100%\">" );
                for( int j=0; j < entries.length; j++ )
                {
                     EntryDescriptor entry = entries[j];
                     String key = entry.getKey();
                     String entryClassname = entry.getClassname();
                     boolean optional = entry.isOptional();

                     if( flag )
                     {
                         writer.write( "<tr class=\"c-even\">" );
                     }
                     else
                     {
                         writer.write( "<tr class=\"c-odd\">" );
                     }
                     writer.write( "<td>" + key + "</td>" );
                     if( optional )
                     {
                         writer.write( "<td>optional</td>" );
                     }
                     else
                     {
                         writer.write( "<td>required</td>" );
                     }
                     writer.write( "<td>" + entryClassname + "</td>" );
                     writer.write( "</tr>" );
                     flag = !flag;
                }
                writer.write( "\n    </table>" );
            }

            //
            // write out the default configuration
            //

            Configuration config = type.getConfiguration();
            if( null != config )
            {
                writer.write( "\n    <p class=\"category\">Default Configuration</p>" );
                writer.write( "\n    <pre>" );
                String str = ConfigurationUtil.list( config );
                str = str.replaceAll( "<", "&lt;" );
                str = str.replaceAll( ">", "&gt;" );
                writer.write( str );
                writer.write( "\n   </pre>" );
            }

            //
            // write out links to embedded types
            //

            flag = true;
            PartReference[] parts = type.getPartReferences();
            if( parts.length > 0 )
            {
                writer.write( "\n    <p class=\"category\">Embedded Components</p>" );
                writer.write( "\n    <table width=\"100%\">" );
                for( int j=0; j < parts.length; j++ )
                {
                     PartReference ref = parts[j];
                     String key = ref.getKey();
                     Part part = type.getPart( key );

                     if( flag )
                     {
                         writer.write( "<tr class=\"p-even\">" );
                     }
                     else
                     {
                         writer.write( "<tr class=\"p-odd\">" );
                     }
                     writer.write( "<td>" + key + "</td>" );
                     String pname = part.getClass().getName();
                     if( pname.equals( ComponentDirective.class.getName() ) )
                     {
                         ComponentDirective directive = (ComponentDirective) part;
                         String tname = directive.getClassname();
                         String tpath = tname.replace( '.', '/' ).concat( ".html" );
                         writer.write( "<td><a href=\"" + offset + "/" + tpath + "\">" + tname + "</a></td>" );
                     }
                     else
                     {
                         writer.write( "<td>" + part.getClass().getName() + "</td>" );
                     }
                     writer.write( "</tr>" );
                     flag = !flag;
                }
                writer.write( "\n    </table>" );
            }

            writer.write( "\n  </body>" );
            writer.write( "\n</html>" );
            writer.close();
        }
        catch( Exception e )
        {
            final String error = 
              "Internal error while attempting to create type page.";
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
        File htmls = new File( reports, "html" );
        try
        {
            InputStream input = new FileInputStream( source );
            Type type = Type.decode( input );
            createTypePage( htmls, type );
        }
        catch( Exception e )
        {
            throw new BuildException( e.getMessage(), e, getLocation() );
        }

        /*
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
        */
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

    private String getTitle()
    {
        if( null == m_title )
        {
            return "DPML Metro Catalog";
        }
        else
        {
            return m_title;
        }
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
            File basedir = project.getBaseDir();
            FileSet[] filesets = (FileSet[]) m_filesets.toArray( new FileSet[0] );
            for( int i=0; i < filesets.length; i++ )
            {
                FileSet fileset = filesets[i];
                DirectoryScanner ds = fileset.getDirectoryScanner( project );
                String[] files = ds.getIncludedFiles();
                for( int j=0; j < files.length; j++ )
                {
                    String path = files[j];
                    File file = new File( basedir, path );
                    list.add( file );
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
        String title = getTitle();
        File index = new File( root, "index.html" );
        try
        {
            FileWriter writer = new FileWriter( index );
            writer.write( "<html>" );
            writer.write( "\n  <head>" );
            writer.write( "\n    <META http-equiv=\"Content-Type\" content=\"text/html; charset=US-ASCII\">" );
            writer.write( "\n    <title>" + title + "</title>" );
            writer.write( "\n    <link href=\"stylesheet.css\" type=\"text/css\" rel=\"stylesheet\"/>" );
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

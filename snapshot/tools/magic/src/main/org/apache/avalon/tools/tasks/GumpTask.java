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

import org.apache.avalon.tools.model.Definition;
import org.apache.avalon.tools.model.ResourceRef;
import org.apache.avalon.tools.model.Info;
import org.apache.avalon.tools.model.Resource;
import org.apache.avalon.tools.model.Plugin.ListenerDef;
import org.apache.avalon.tools.model.Plugin.TaskDef;
import org.apache.avalon.tools.model.Plugin;
import org.apache.avalon.tools.model.Policy;
import org.apache.avalon.tools.model.Home;
import org.apache.avalon.tools.model.Context;
import org.apache.avalon.tools.model.UnknownResourceException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.ArrayList;

/**
 * Create meta-data for a block.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class GumpTask extends SystemTask
{
    private static final String MAGIC_INTEGRATION =
      "Magic Integration &lt;dev@avalon.apache.org&gt;";
 
    public static class Nag
    {
        private String m_from;
        private String m_to;

        public void setFrom( String from )
        {
            m_from = from;
        }
   
        public String getFrom()
        {
            if( null != m_from )
            {
                return m_from;
            }
            else
            {
                return MAGIC_INTEGRATION;
            }
        }

        public void setTo( String to )
        {
            m_to = to;
        }
   
        public String getTo()
        {
            return m_to;
        }
    }

    public static class Href
    {
        private String m_href;

        public void setHref( String href )
        {
            m_href = href;
        }
   
        public String getHref()
        {
            return m_href;
        }
    }

    public static class FileHolder
    {
        private String m_file;

        public void setFile( String file )
        {
            m_file = file;
        }
   
        public String getFile()
        {
            return m_file;
        }
    }

    public static class License extends FileHolder
    {
    }

    public static class Template extends FileHolder
    {
        private String m_target;

        public void setTarget( String target )
        {
            m_target = target;
        }
   
        public String getTarget()
        {
            return m_target;
        }
    }

    public static class Url extends Href
    {
    }

    public static class Repository extends Href
    {
        private String m_repository;
        private String m_dir;

        public void setRepository( String repository )
        {
            m_repository = repository;
        }

        public void setDir( String dir )
        {
            if( dir.endsWith( "/" ) )
            {
                m_dir = dir;
            }
            else
            {
                m_dir = dir + "/";
            }
        }

        public String getRepository()
        {
            return m_repository;
        }

        public String getDir()
        {
            return m_dir;
        }
    }

    public static class Cvs extends Repository
    {
    }

    public static class Svn extends Repository
    {
    }

    private String m_name;
    private String m_filename;
    private String m_description;
    private Url m_url;
    private Cvs m_cvs;
    private Svn m_svn;
    private License m_license;
    private Template m_template = new Template();
    private boolean m_public = false;
    private Nag m_nag;

    public void setName( String name )
    {
        m_name = name;
    }

    public void setFilename( String filename )
    {
        m_filename = filename;
    }

    public void setDescription( String description )
    {
        m_description = description;
    }
        
    public void setPublic( boolean flag )
    {
        m_public = flag;
    }

    public Url createUrl()
    {
        if( null == m_url )
        {
            m_url = new Url();
            return m_url;
        }
        else
        {
            throw new BuildException( "Multiple url entries not allowed." );
        }
    }

    public Svn createSvn()
    {
        if( null == m_svn )
        {
            if( null == m_cvs )
            {
                m_svn = new Svn();
                return m_svn;
            }
            else
            {
                throw new BuildException( "Cannot declare both cvs and svn." );
            }
        }
        else
        {
            throw new BuildException( "Multiple svn entries not allowed." );
        }
    }

    public Cvs createCvs()
    {
        if( null == m_cvs )
        {
            if( null == m_cvs )
            {
                m_cvs = new Cvs();
                return m_cvs;
            }
            else
            {
                throw new BuildException( "Cannot declare both cvs and svn." );
            }
        }
        else
        {
            throw new BuildException( "Multiple cvs entries not allowed." );
        }
    }

    public License createLicense()
    {
        if( null == m_license )
        {
            m_license = new License();
            return m_license;
        }
        else
        {
            throw new BuildException( "Multiple license entries not allowed." );
        }
    }

    public Nag createNag()
    {
        if( null == m_nag )
        {
            m_nag = new Nag();
            return m_nag;
        }
        else
        {
            throw new BuildException( "Multiple nag entries not allowed." );
        }
    }

    public Template createTemplate()
    {
        return m_template;
    }


    private File getFile()
    {
        if( null != m_filename )
        {
            final Project project = getProject();
            return Context.getFile( project.getBaseDir(), m_filename );
        }
        else
        {
            return new File( getContext().getDeliverablesDirectory(), "gump.xml" );
        }
    }

    public void execute()
    {
        try
        {
            final File file = getFile();
            file.getParentFile().mkdirs();
            file.createNewFile();
            log( "output: " + file );
            final OutputStream output = new FileOutputStream( file );
            final Writer writer = new OutputStreamWriter( output );

            try
            {
                writeHeader( writer );
                int count = writeModule( writer );
                log( "project: " + count );
                writer.flush();
            }
            finally
            {
                closeStream( output );
            }
        }
        catch( Throwable e )
        {
            throw new BuildException( e );
        }
    }

    private String getName()
    {
        if( null != m_name )
        {
            return m_name;
        }
        else
        {
            return getProject().getName();
        }
    }

    protected int writeModule( final Writer writer )
        throws IOException
    {
        Home home = getHome();
        Project project = getProject();
        String name = getName();

        log( "module: " + name );
        writer.write( "\n\n<module name=\"" + name + "\">" );
        writer.write( "\n\n  <url href=\"" + m_url.getHref() + "\"/>" );

        if( null != m_description )
        {
            writer.write( "\n  <description>" );
            writer.write( m_description );
            writer.write( "</description>" );
        }

        if( null != m_svn )
        {
            if( null != m_svn.getHref() )
            {
                writer.write( "\n  <svn href=\"" + m_svn.getHref() + "\"/>" );
            }
            else
            {
                writer.write( "\n  <svn repository=\"" + m_svn.getRepository() 
                  + "\" dir=\"" + m_svn.getDir() + "\"/>" );
            }
        }
        else if( null != m_cvs )
        {
            if( null != m_cvs.getHref() )
            {
                writer.write( "\n  <cvs href=\"" + m_cvs.getHref() + "\"/>" );
            }
            else
            {
                writer.write( "\n  <cvs repository=\"" + m_cvs.getRepository() 
                  + "\" dir=\"" + m_cvs.getDir() + "\"/>" );
            }
        }

        Definition[] definitions = home.getDefinitions();
        for( int i=0; i<definitions.length; i++ )
        {
            Definition def = definitions[i];
            try
            {
                writeProject( writer, def );
            }
            catch( UnknownResourceException ure )
            {
                final String error = 
                  "Project defintion [" + def + "] contains a unknown resource reference ["
                  + ure.getKey() + "].";
                throw new BuildException( error );
            }
        }

        if( m_public )
        {
             writer.write( "\n\n  <redistributable/>" );
        }

        writer.write( "\n\n</module>\n" );
        return definitions.length;
    }

   /**
    * Write the XML header.
    * @param writer the writer
    * @throws IOException if unable to write xml
    */
    private void writeHeader( final Writer writer )
        throws IOException
    {
        writer.write( "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" );
        writer.write( "\n" );
        writer.write( "\n<!--" );
        writer.write( "\n#" );
        writer.write( "\n# Copyright 2004 Apache Software Foundation " );
        writer.write( "\n# Licensed  under the  Apache License,  Version 2.0  (the \"License\"); " );
        writer.write( "\n# you may not use  this file  except in  compliance with the License. " );
        writer.write( "\n# You may obtain a copy of the License at  " );
        writer.write( "\n#  " );
        writer.write( "\n# http://www.apache.org/licenses/LICENSE-2.0 " );
        writer.write( "\n#  " );
        writer.write( "\n# Unless required by applicable law or agreed to in writing, software " );
        writer.write( "\n# distributed  under the  License is distributed on an \"AS IS\" BASIS, " );
        writer.write( "\n#  WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or " );
        writer.write( "\n#  implied. " );
        writer.write( "\n#  " );
        writer.write( "\n# See the License for the specific language governing permissions and " );
        writer.write( "\n# limitations under the License. " );
        writer.write( "\n# " );
        writer.write( "\n# NOTE: The content of this file is automatically generated by Magic." );
        writer.write( "\n# http://avalon.apache.org/" );
        writer.write( "\n#" );
        writer.write( "\n-->" );
        writer.write( "\n" );
    }

   /**
    * Write the XML project definition.
    * @param writer the writer
    * @param definition the project definition
    * @throws IOException if unable to write xml
    */
    private void writeProject( final Writer writer, Definition definition )
        throws IOException, UnknownResourceException
    {
        String path = definition.getBaseDir().getCanonicalPath();

        Project project = getProject();
        String basedir = resolveBaseDir( project, path );

        writer.write( 
          "\n\n  <project name=\"" + definition.getKey() + "\">" );

        if( null != m_license )
        {
            String license = m_license.getFile();
            writer.write( 
               "\n    <license name=\"" + license + "\"/>" );
        }

        String template = m_template.getFile();
        String target = m_template.getTarget();

        writer.write( 
          "\n    <ant basedir=\"" + basedir + "\"" );
        if( null != template )
        {
            writer.write( 
              "\n      buildfile=\"" + template + "\"" );
        }
        if( null != target )
        {
            writer.write( 
              " target=\"" + target + "\"" );
        }
        writer.write( ">" );

        writer.write( 
           "\n      <!-- for magic -->" );
        writer.write( 
           "\n      <property name=\"build.sysclasspath\" value=\"last\"/> " );
        writer.write( 
           "\n      <property name=\"magic.home\" reference=\"home\" project=\"magic\"/>" );
        writer.write( 
           "\n      <property name=\"gump.signature\" value=\"@@DATE@@\"/>" );


        final Resource[] resources = getContributingResources( definition );

        boolean flag = false;
        for( int i=0; i<resources.length; i++ )
        {
            Resource resource = resources[i];
            if( !( resource instanceof Definition ) )
            {
                if( !flag )
                {
                    flag = true;
                    writer.write( 
                      "\n      <!-- external references -->" );
                }
                String key = resource.getKey();
                String alias = Resource.getKeyForResource( resource );
                String id = resource.getGump().getId();

                writer.write( 
                   "\n      <depend property=\"gump.resource." + key 
                   + "\" project=\"" + alias + "\"" );
                if( null != id )
                {
                    writer.write( " id=\"" + id + "\"" );
                }

                if( resource.getGump().isClasspathEntry() )
                {
                    writer.write( "/>" );
                }
                else
                {
                    writer.write( ">" );
                    writer.write( "\n        <noclasspath/>" );
                    writer.write( "\n      </depend>" );
                }
            }
        }

        writer.write( "\n      <!-- end for -->" );
        writer.write( "\n    </ant>" );       
        writer.write( "\n    <depend project=\"magic\" runtime=\"true\" inherit=\"runtime\"/> ");

        //
        // add dependencies for gump to do its sequencing correctly
        //

        flag = false;
        for( int i=0; i<resources.length; i++ )
        {
            Resource resource = resources[i];
            if( resource instanceof Definition )
            {
                if( !flag )
                {
                    flag = true;
                    writer.write( "\n    <!-- for gump -->" );
                }
                String key = resource.getKey();
    
                writer.write( 
                  "\n    <depend project=\"" + key + "\"" );
    
                if( resource.getGump().isClasspathEntry() )
                {
                    writer.write( "/>" );
                }
                else
                {
                    writer.write( "><noclasspath/></depend>" );
                }
            }
        }
    
        if( flag )
        {
            writer.write( "\n    <!-- end for -->" );
        }

        writer.write( 
          "\n    <home nested=\"" 
          + basedir + "/target/deliverables\"/>" );

        String name = definition.getInfo().getName();
        String type = definition.getInfo().getType();
        String filename = definition.getInfo().getFilename();

        if( "jar".equals( type ) || "bar".equals( type ) )
        {
            writer.write( 
              "\n    <jar name=\"" + type + "s/" 
              + name + "-@@DATE@@." + type  + "\"/>" );
        }
        if( "plugin".equals( type ) )
        {
            writer.write( 
              "\n    <jar name=\"plugins/" 
              + name + "-@@DATE@@." + type  + "\"/>" );
        }
        else if( "doc".equals( type ) )
        {
            writer.write( 
              "\n    <!-- doc output is relative merlin home docs cache -->" );
        }

        if( null != m_nag )
        {
            writer.write( "\n    <nag to=\"" + m_nag.getTo() + "\"" );
            writer.write( "\n       from=\"" + m_nag.getFrom() + "\"/>" );
        }
        writer.write( "\n  </project>" );
    }

    private boolean isIgnorableDependency( Resource resource )
    {
        if( resource.getGump().isIgnorable() ) 
        {
            return true;
        }
        else
        {
            String type = resource.getInfo().getType();
            if ( "null".equals( type ) )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    private Resource[] getContributingResources( Definition def )
    {
        ResourceRef[] refs = 
          def.getResourceRefs( getProject(), Policy.ANY, ResourceRef.ANY, true );
        ArrayList list = new ArrayList();
        for( int i=0; i<refs.length; i++ )
        {
            Resource resource = getHome().getResource( refs[i] );
            boolean ignorable = isIgnorableDependency( resource );
            if( !ignorable && !list.contains( resource ) )
            {
                list.add( resource );
            }
        }
        ResourceRef[] plugins = def.getPluginRefs();
        for( int i=0; i<plugins.length; i++ )
        {
            Resource resource = getHome().getResource( plugins[i] );
            if( !list.contains( resource ) )
            {
                list.add( resource );
            }
        }
        return (Resource[]) list.toArray( new Resource[0] );
    }

    private String resolveBaseDir( Project project, String path )
    {
        int j = getProject().getBaseDir().toString().length();
        if( path.length() > j )
        {
            return path.substring( j+1 ).replace( '\\', '/' );
        }
        else
        {
            return ".";
        }
    }

    private String getKeyForResource( Resource resource )
    {
        final String alias = resource.getGump().getAlias();
        if( null != alias ) 
        {
            return alias;
        }
        else
        {
            return resource.getKey();
        }
    }

    private void closeStream( final OutputStream output )
    {
        if( null != output )
        {
            try
            {
                output.close();
            }
            catch( IOException e )
            {
                // ignore
            }
        }
    }
}

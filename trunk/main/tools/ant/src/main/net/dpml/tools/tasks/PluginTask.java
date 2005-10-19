/*
 * Copyright 2005 Stephen J. McConnell
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
import java.io.OutputStream;
import java.io.IOException;
import java.io.Writer;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import net.dpml.transit.Category;
import net.dpml.transit.Plugin;

import net.dpml.tools.ant.Definition;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;

/**
 * Execute all plugins relative to the current build phase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class PluginTask extends GenericTask
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------
    
   /**
    * The constant namespace key.
    */
    public static final String NAMESPACE = "net.dpml";

   /**
    * The constant version key.
    */
    public static final String VERSION = "1.0";

   /**
    * The constant plugin class key.
    */
    public static final String CLASS_KEY = "dpml.plugin.class";

   /**
    * The constant plugin export key.
    */
    public static final String EXPORT_KEY = "dpml.plugin.export";

   /**
    * The constant resource key.
    */
    public static final String RESOURCE_KEY = "dpml.plugin.resource";

   /**
    * The constant urn key.
    */
    public static final String URN_KEY = "dpml.plugin.urn";

   /**
    * The constant group key.
    */
    public static final String ARTIFACT_GROUP = "dpml.artifact.group";

   /**
    * The constant name key.
    */
    public static final String ARTIFACT_NAME = "dpml.artifact.name";

   /**
    * The constant artifact version key.
    */
    public static final String ARTIFACT_VERSION = "dpml.artifact.version";

   /**
    * The constant artifact signature key.
    */
    public static final String ARTIFACT_SIGNATURE = "dpml.artifact.signature";

   /**
    * The constant artifact api dependency key.
    */
    public static final String ARTIFACT_SYSTEM = "dpml.artifact.dependency.sys";

   /**
    * The constant artifact api dependency key.
    */
    public static final String ARTIFACT_PUBLIC = "dpml.artifact.dependency.api";

   /**
    * The constant artifact spi dependency key.
    */
    public static final String ARTIFACT_PROTECTED = "dpml.artifact.dependency.spi";

   /**
    * The constant artifact impl dependency key.
    */
    public static final String ARTIFACT_PRIVATE = "dpml.artifact.dependency";


    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------
    
    private String m_class;
    private Antlib m_antlib;

    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------

   /**
    * Set the plugin classname.
    * @param type the classname
    */
    public void setClass( String type )
    {
        m_class = type;
    }

   /**
    * Construct a new antlib defintion.
    * @return the antlib definition
    */
    public Antlib createAntlib()
    {
        if( null == m_antlib )
        {
            m_antlib = new Antlib();
            return m_antlib;
        }
        else
        {
            final String error =
              "Multiple antlib elements not allowed.";
            throw new BuildException( error );
        }
    }
    
    public void execute()
    {
        Project project = getProject();
        if( ( null == getClassname() ) && ( null == m_antlib ) )
        {
            final String error =
              "Either the class attribute or nested antlib element must be declared.";
            throw new BuildException( error );
        }
            
        final Definition definition = getDefinition();
        final String path = definition.getLayoutPath( "plugin" );
        final File deliverables = getDefinition().getTargetDeliverablesDirectory();
        final File plugins = new File( deliverables, "plugins" );
        final File plugin = new File( plugins, path );
        writePluginFile( plugin );
    }

    private String getClassname()
    {
        if( null != m_class )
        {
            return m_class;
        }
        else
        {
            return getDefinition().getProductionProperty( "plugin", "project.plugin.class", null );
        }
    }

    private void writePluginFile( final File file )
    {
        if( file.exists() && ( getDefinition().getLastModified() < file.lastModified() ) )
        {
            log( "Plugin directive is up-to-date.", Project.MSG_VERBOSE );
            return;
        }
        else if( !file.exists() )
        {
            log( "Creating plugin directive" );
        }
        else
        {
            log( "Updating plugin directive" );
        }
        
        try
        {
            mkDir( file.getParentFile() );
            file.createNewFile();
            final OutputStream output = new FileOutputStream( file );
            try
            {
                writePluginDescriptor( output, file );
            }
            finally
            {
                closeStream( output );
            }
            checksum( file );
            asc( file );
        }
        catch( Throwable e )
        {
            throw new BuildException( e );
        }
    }

    private void writePluginDescriptor( final OutputStream output, final File file )
        throws IOException
    {
        final Writer writer = new OutputStreamWriter( output );
        writeHeader( writer );
        writeDescriptor( writer, file );
        writeProperties( writer );
        writeClasspath( writer );
        writeTail( writer );
        writer.flush();
    }

   /**
    * Write the properties header.
    * @param writer the writer
    * @throws IOException if unable to write xml
    */
    private void writeHeader( final Writer writer )
        throws IOException
    {
        writer.write( "\n#" );
        writer.write( "\n# Specification classifier." );
        writer.write( "\n#" );
        writer.write( "\ndpml.plugin.meta.namespace = " + NAMESPACE );
        writer.write( "\ndpml.plugin.meta.version = " + VERSION );
    }

   /**
    * Write the artifact descriptor.
    * @param writer the writer
    * @throws IOException if unable to write xml
    */
    private void writeDescriptor(
       final Writer writer, final File file )
       throws IOException
    {
        writer.write( "\n" );
        writer.write( "\n#" );
        writer.write( "\n# Artifact descriptor." );
        writer.write( "\n#" );
        writer.write( "\n" + ARTIFACT_GROUP + " = " + getDefinition().getGroup() );
        writer.write( "\n" + ARTIFACT_NAME + " = " +  getDefinition().getName() );
        writer.write( "\n" + ARTIFACT_VERSION + " = " + getDefinition().getVersion() );
        writer.write( "\n" + ARTIFACT_SIGNATURE + " = " + getSignature( file ) );
    }


    private String getSignature( final File file )
    {
        if( !file.exists() )
        {
            final String error =
              "Cannot create artifact descriptor due to missing resource: "
              + file;
            throw new BuildException( error );
        }
        final Date created = new Date( file.lastModified() );
        return getSignature( created );
    }

    private void writeClasspath( final Writer writer )
        throws IOException
    {
        /*
        final ArrayList visited = new ArrayList();
        final ResourceRef[] sys = def.getQualifiedRefs( getProject(), visited, Category.SYSTEM );
        if( sys.length > 0 )
        {
            writer.write( "\n" );
            writer.write( "\n#" );
            writer.write( "\n# System dependencies." );
            writer.write( "\n#" );
            final String lead = ARTIFACT_SYSTEM;
            writeRefs( writer, sys, lead );
        }
        final ResourceRef[] apis = def.getQualifiedRefs( getProject(), visited, Category.API );
        if( apis.length > 0 )
        {
            writer.write( "\n" );
            writer.write( "\n#" );
            writer.write( "\n# API dependencies." );
            writer.write( "\n#" );
            final String lead = ARTIFACT_PUBLIC;
            writeRefs( writer, apis, lead );
        }
        final ResourceRef[] spis = def.getQualifiedRefs( getProject(), visited, Category.SPI );
        if( spis.length > 0 )
        {
            writer.write( "\n" );
            writer.write( "\n#" );
            writer.write( "\n# SPI dependencies." );
            writer.write( "\n#" );
            final String lead = ARTIFACT_PROTECTED;
            writeRefs( writer, spis, lead );
        }
        final ResourceRef[] impl = def.getQualifiedRefs( getProject(), visited, Category.IMPL );
        boolean isaJar = def.getInfo().isa( "jar" );
        if( ( impl.length > 0 ) || isaJar )
        {
            final String lead = ARTIFACT_PRIVATE;
            writer.write( "\n" );
            writer.write( "\n#" );
            writer.write( "\n# Implementation dependencies." );
            writer.write( "\n#" );
            int n = writeRefs( writer, impl, lead );
            if( isaJar )
            {
                writeReference( writer, def, lead, n );
            }
        }
        */
    }

    /*
    private int writeRefs(
      final Writer writer, final ResourceRef[] refs, final String lead )
      throws IOException
    {
        for( int i=0; i < refs.length; i++ )
        {
            final ResourceRef ref = refs[i];
            final Resource resource = getIndex().getResource( ref );
            writeReference( writer, resource, lead, i );
        }
        return refs.length;
    }

    private void writeReference(
      final Writer writer, final Resource resource, final String lead, int i )
      throws IOException
    {
        writer.write( "\n" );
        writer.write( lead );
        writer.write( "." + i );
        writer.write( " = " );
        writer.write( resource.getInfo().getURI( "jar" ) );
    }
    */

   /**
    * Write the factory class.
    * @param writer the writer
    * @throws IOException if unable to write xml
    */
    private void writeProperties( final Writer writer )
        throws IOException
    {
        String classname = getClassname();
        if( classname != null )
        {
            writer.write( "\n" );
            writer.write( "\n#" );
            writer.write( "\n# Type." );
            writer.write( "\n#" );
            writer.write( "\n" + CLASS_KEY + " = " + classname );
        }

        if( m_antlib != null )
        {
            writer.write( "\n" );
            writer.write( "\n#" );
            writer.write( "\n# Antlib." );
            writer.write( "\n#" );
            writer.write( "\n" + RESOURCE_KEY + " = " + m_antlib.getPath() );
            writer.write( "\n" + URN_KEY + " = " + m_antlib.getURN() );
        }

        final String export = getProject().getProperty( EXPORT_KEY );
        if( null != export )
        {
            writer.write( "\n" );
            writer.write( "\n#" );
            writer.write( "\n# Export." );
            writer.write( "\n#" );
            writer.write( "\n" + EXPORT_KEY + " = " + export );
        }
    }

   /**
    * Write the tail.
    * @param writer the writer
    * @throws IOException if unable to write xml
    */
    private void writeTail( final Writer writer )
        throws IOException
    {
        writer.write( "\n" );
        writer.write( "\n#" );
        writer.write( "\n# EOF." );
        writer.write( "\n#" );
        writer.write( "\n" );
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
                log( e.toString() );
            }
        }
    }
    
    // ------------------------------------------------------------------------
    // static utils
    // ------------------------------------------------------------------------
    
    private static final String TYPE_ID = "plugin";

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
    
   /**
    * Creation of a new AntLib instance.
    */
    public static class Antlib extends net.dpml.transit.tools.PluginTask.Antlib
    {
    }

}

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

package net.dpml.magic.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;

import net.dpml.magic.model.Definition;
import net.dpml.magic.model.Info;
import net.dpml.magic.model.Resource;
import net.dpml.magic.model.ResourceRef;
import net.dpml.magic.project.Context;
import net.dpml.magic.project.DeliverableHelper;

import net.dpml.transit.tools.PluginTask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Create a repository plugin descriptor in the form of a properties file.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ExportTask extends ProjectTask
{
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
    public static final String ARTIFACT_PUBLIC = "dpml.artifact.dependency.api";

   /**
    * The constant artifact spi dependency key.
    */
    public static final String ARTIFACT_PROTECTED = "dpml.artifact.dependency.spi";

   /**
    * The constant artifact impl dependency key.
    */
    public static final String ARTIFACT_PRIVATE = "dpml.artifact.dependency";

    private static final String TYPE_ID = "plugin";

    private String m_class;
    private Antlib m_antlib;

   /**
    * Creation of a new AntLib instance.
    */
    public static class Antlib extends PluginTask.Antlib
    {
    }

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

   /**
    * task execution during which the prugin descriptior is created.
    * @exception BuildException if a build error occurs
    */
    public void execute() throws BuildException
    {
        if( ( null == getClassname() ) && ( null == m_antlib ) )
        {
            final String error =
              "Either the class attribute or nested antlib element must be declared.";
            throw new BuildException( error );
        }
        final File types = getContext().getDeliverablesDirectory( TYPE_ID );
        final File artifact = getFile( types, getDefinition() );
        writePluginFile( types, artifact );
    }

    private File getFile( File types, Definition def )
    {
        final String filename = def.getFilename( TYPE_ID );
        return new File( types, filename );
    }

    private String getClassname()
    {
        if( null != m_class )
        {
            return m_class;
        }
        else
        {
            return getProject().getProperty( CLASS_KEY );
        }
    }

    private void writePluginFile( final File dir, final File file )
    {
        if( file.exists() )
        {
            if( getIndex().getIndexLastModified() < file.lastModified() )
            {
                File build = getProject().resolveFile( "build.xml" );
                if( build.exists() && build.lastModified() < file.lastModified() )
                {
                    log( "Plugin directive is up-to-date.", Project.MSG_VERBOSE );
                    return;
                }
            }
        }

        final Definition def = getDefinition();

        try
        {
            log( "Creating plugin directive" );
            dir.mkdirs();
            file.createNewFile();
            final OutputStream output = new FileOutputStream( file );
            try
            {
                writePluginDescriptor( output, def, file );
            }
            finally
            {
                closeStream( output );
            }
            DeliverableHelper.checksum( this, file );
            String gpg = getIndex().getProperty( Context.GPG_EXE_KEY );
            DeliverableHelper.asc( this, file, gpg );
        }
        catch( Throwable e )
        {
            throw new BuildException( e );
        }
    }

    private void writePluginDescriptor( final OutputStream output, final Definition def, final File file )
        throws IOException
    {
        final Writer writer = new OutputStreamWriter( output );
        writeHeader( writer );
        writeDescriptor( writer, def, file );
        writeProperties( writer );
        writeClasspath( writer, def );
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
       final Writer writer, final Definition def, final File file )
       throws IOException
    {
        final Info info = def.getInfo();

        writer.write( "\n" );
        writer.write( "\n#" );
        writer.write( "\n# Artifact descriptor." );
        writer.write( "\n#" );
        writer.write( "\n" + ARTIFACT_GROUP + " = " + info.getGroup() );
        writer.write( "\n" + ARTIFACT_NAME + " = " +  info.getName() );
        writer.write( "\n" + ARTIFACT_VERSION + " = " + info.getVersion() );
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
        return Context.getSignature( created );
    }

    private void writeClasspath( final Writer writer, final Definition def )
        throws IOException
    {
        final ArrayList visited = new ArrayList();
        final ResourceRef[] apis = def.getQualifiedRefs( getProject(), visited, ResourceRef.API );
        if( apis.length > 0 )
        {
            writer.write( "\n" );
            writer.write( "\n#" );
            writer.write( "\n# API dependencies." );
            writer.write( "\n#" );
            final String lead = ARTIFACT_PUBLIC;
            writeRefs( writer, apis, lead );
        }
        final ResourceRef[] spis = def.getQualifiedRefs( getProject(), visited, ResourceRef.SPI );
        if( spis.length > 0 )
        {
            writer.write( "\n" );
            writer.write( "\n#" );
            writer.write( "\n# SPI dependencies." );
            writer.write( "\n#" );
            final String lead = ARTIFACT_PROTECTED;
            writeRefs( writer, spis, lead );
        }
        final ResourceRef[] impl = def.getQualifiedRefs( getProject(), visited, ResourceRef.IMPL );
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
    }

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
}

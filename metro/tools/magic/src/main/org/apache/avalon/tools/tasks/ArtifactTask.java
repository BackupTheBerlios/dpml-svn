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
import org.apache.avalon.tools.model.Context;
import org.apache.avalon.tools.model.Resource;
import org.apache.tools.ant.BuildException;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Date;

/**
 * Create a repository plugin meta data descriptor in the form of a 
 * properties file.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ArtifactTask extends SystemTask
{
    public static final String SUFFIX = "meta";
    public static final String VERSION = "1.1";
    public static final String FACTORY_KEY = "avalon.artifact.factory";
    public static final String EXPORT_KEY = "avalon.artifact.export";

    private String m_factory;

    public void setFactory( String factory )
    {
        m_factory = factory;
    }

    private String getFactory()
    {
        if( null != m_factory )
        {
            return m_factory;
        }
        else
        {
            return getProject().getProperty( FACTORY_KEY );
        }
    }

    public void execute() throws BuildException 
    {
        final String key = getContext().getKey();
        final ResourceRef ref = new ResourceRef( key );
        final Definition def = getHome().getDefinition( ref );
        final File artifact = getArtifactFile( def );

        m_factory = getFactory();
        if( null == m_factory ) 
        {
            final String error = 
              "Required artifact property '" + FACTORY_KEY + "' is undefined.";
            throw new BuildException( error );
        }
        writeMetaFile( def, artifact );
    }

    private void writeMetaFile( final Definition def, final File artifact )
    {
        final String path = artifact.toString();
        final File file = new File( path + "." + SUFFIX );

        if( file.exists() )
        {
            //
            // TODO : check if the index.xml was modified
            //

            if( file.lastModified() > artifact.lastModified() )
            {
                if( getHome().getIndexLastModified() < file.lastModified() )
                {
                    return;
                }
            }
        }

        try
        {
            log( "Creating meta directive" );
            file.createNewFile();
            final OutputStream output = new FileOutputStream( file );
            try
            {
                writeMetaDescriptor( output, def, artifact );
            }
            finally
            {
                closeStream( output );
            }
            DeliverableHelper.checksum( this, file );
            DeliverableHelper.asc( getHome(), this, file );
        }
        catch( Throwable e )
        {
            throw new BuildException( e );
        }
    }

    private File getArtifactFile( final Definition def )
    {
        final Info info = def.getInfo();
        final String type = info.getType();

        final File dir = getContext().getDeliverablesDirectory();
        final File types = new File( dir, type + "s" );

        final String filename = getFilename( info );
        return new File( types, filename );
    }

    private String getFilename( final Info info )
    {
        final String version = info.getVersion();
        if( null == version )
        {
            return info.getName() + "." + info.getType() ;
        }
        else
        {
            return info.getName() + "-" + version + "." + info.getType();
        }
    }

    public void writeMetaDescriptor( final OutputStream output, final Definition def, final File artifact )
        throws IOException
    {
        final Writer writer = new OutputStreamWriter( output );
        writeHeader( writer );
        writeDescriptor( writer, def, artifact );
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
        writer.write( "\n# Meta classifier." );
        writer.write( "\n#" );
        writer.write( "\nmeta.domain = avalon" );
        writer.write( "\nmeta.version = " + VERSION );
    }

   /**
    * Write the artifact descriptor.
    * @param writer the writer
    * @throws IOException if unable to write xml
    */
    private void writeDescriptor( 
       final Writer writer, final Definition def, final File artifact )
       throws IOException
    {
        final Info info = def.getInfo();

        writer.write( "\n" );
        writer.write( "\n#" );
        writer.write( "\n# Artifact descriptor." );
        writer.write( "\n#" );
        writer.write( "\navalon.artifact.group = " + info.getGroup() );
        writer.write( "\navalon.artifact.name = " + info.getName() );
        writer.write( "\navalon.artifact.version = " + info.getVersion() );
        writer.write( "\navalon.artifact.signature = " + getSignature( artifact ) );
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
            final String lead = "avalon.artifact.dependency.api";
            writeRefs( writer, apis, lead );
        }
        final ResourceRef[] spis = def.getQualifiedRefs( getProject(), visited, ResourceRef.SPI );
        if( spis.length > 0 )
        {
            writer.write( "\n" );
            writer.write( "\n#" );
            writer.write( "\n# SPI dependencies." );
            writer.write( "\n#" );
            final String lead = "avalon.artifact.dependency.spi";
            writeRefs( writer, spis, lead );
        }

        final ResourceRef[] impl = def.getQualifiedRefs( getProject(), visited, ResourceRef.IMPL );
        if( impl.length > 0 )
        {
            writer.write( "\n" );
            writer.write( "\n#" );
            writer.write( "\n# Implementation dependencies." );
            writer.write( "\n#" );
            final String lead = "avalon.artifact.dependency";
            writeRefs( writer, impl, lead );
        }
    }

    private void writeRefs( 
      final Writer writer, final ResourceRef[] refs, final String lead )
      throws IOException
    {
        for( int i=0; i<refs.length; i++ )
        {
            final ResourceRef ref = refs[i];
            final Resource resource = getHome().getResource( ref );
            writer.write( "\n" );
            writer.write( lead );
            writer.write( "." + i );
            writer.write( " = " );
            writer.write( resource.getInfo().getURI() );
        }
    }

   /**
    * Write the factory class.
    * @param writer the writer
    * @throws IOException if unable to write xml
    */
    private void writeProperties( final Writer writer )
        throws IOException
    {
        writer.write( "\n" );
        writer.write( "\n#" );
        writer.write( "\n# Factory classname." );
        writer.write( "\n#" );
        writer.write( "\n" + FACTORY_KEY + " = " + m_factory);

        final String export = getProject().getProperty( EXPORT_KEY );
        if( null == export ) 
        {
            return;
        }
        writer.write( "\n" );
        writer.write( "\n#" );
        writer.write( "\n# Service export." );
        writer.write( "\n#" );
        writer.write( "\n" + EXPORT_KEY + " = " + export );


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
                // ignore
            }
        }
    }
}

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import net.dpml.transit.Artifact;
import net.dpml.transit.Category;

import net.dpml.tools.ant.Context;
import net.dpml.tools.model.Resource;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;

/**
 * Generates an module as an exportable artifact.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PluginExportTask extends GenericTask
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
    * Task initialization.
    */
    public void init()
    {
        if( !isInitialized() )
        {
            super.init();
            getContext().init();
        }
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
   * Task execution.
   */
    public void execute()
    {
        Project project = getProject();
        if( ( null == getClassname() ) && ( null == getAntlib() ) )
        {
            final String error =
              "Either the class attribute or nested antlib element must be declared.";
            log( "Plugin creation failure. " );
            throw new BuildException( error );
        }
            
        final Context context = getContext();
        final String path = context.getLayoutPath( "plugin" );
        final File deliverables = context.getTargetDeliverablesDirectory();
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
            return getResource().getType( "plugin" ).getProperty( "project.plugin.class", null );
        }
    }
    
    private Antlib getAntlib()
    {
        if( null != m_antlib )
        {
            return m_antlib;
        }
        else
        {
            String res = getResource().getType( "plugin" ).getProperty( "project.plugin.resource", null );
            String urn = getResource().getType( "plugin" ).getProperty( "project.plugin.urn", null );
            if( ( null != res ) && ( null != urn ) )
            {
                Antlib antlib = new Antlib();
                antlib.setResource( res );
                antlib.setUrn( urn );
                m_antlib = antlib;
                return antlib;
            }
            else
            {
                return null;
            }
        }
    }

    private void writePluginFile( final File file )
    {
        //if( file.exists() && ( getDefinition().getLastModified() < file.lastModified() ) )
        //{
        //    log( "Plugin directive is up-to-date.", Project.MSG_VERBOSE );
        //    return;
        //}
        //else
        if( !file.exists() )
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
        throws Exception
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
        writer.write( "\n" + ARTIFACT_GROUP + " = " + getResource().getParent().getResourcePath() );
        writer.write( "\n" + ARTIFACT_NAME + " = " +  getResource().getName() );
        writer.write( "\n" + ARTIFACT_VERSION + " = " + getResource().getVersion() );
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
        throws Exception
    {
        Resource[] systemResources = getResource().getClasspathProviders( Category.SYSTEM );
        if( systemResources.length > 0 )
        {
            final String label = "System dependencies.";
            final String lead = ARTIFACT_SYSTEM;
            writeRefs( writer, systemResources, lead, label );
        }
        
        Resource[] apiResources = getResource().getClasspathProviders( Category.PUBLIC );
        if( apiResources.length > 0 )
        {
            final String label = "Public.";
            final String lead = ARTIFACT_PUBLIC;
            writeRefs( writer, apiResources, lead, label );
        }
        
        Resource[] spiResources = getResource().getClasspathProviders( Category.PROTECTED );
        if( spiResources.length > 0 )
        {
            final String label = "Protected.";
            final String lead = ARTIFACT_PROTECTED;
            writeRefs( writer, spiResources, lead, label );
        }
        
        Resource[] implResources = getResource().getClasspathProviders( Category.PRIVATE );
        Resource[] resources = new Resource[ implResources.length + 1 ];
        for( int i=0; i<implResources.length; i++ )
        {
            resources[i] = implResources[i];
        }
        resources[ implResources.length ] = getResource();
        final String label = "Private.";
        final String lead = ARTIFACT_PRIVATE;
        writeRefs( writer, resources, lead, label );
    }
    
    private int writeRefs(
      final Writer writer, final Resource[] refs, final String lead, String label )
      throws IOException
    {
        writer.write( "\n" );
        writer.write( "\n#" );
        writer.write( "\n# " + label );
        writer.write( "\n#" );
        for( int i=0; i < refs.length; i++ )
        {
            final Resource resource = refs[i];
            writeResource( writer, resource, lead, i );
        }
        return refs.length;
    }

    private void writeResource(
      final Writer writer, final Resource resource, final String lead, int i )
      throws IOException
    {
        writer.write( "\n" );
        writer.write( lead );
        writer.write( "." + i );
        writer.write( " = " );
        try
        {
            Artifact artifact = resource.getArtifact( "jar" );
            String uri = artifact.toURI().toString();
            writer.write( uri );
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while attempting to write resource.";
            IOException ioe = new IOException( error );
            ioe.initCause( e );
            throw ioe;
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

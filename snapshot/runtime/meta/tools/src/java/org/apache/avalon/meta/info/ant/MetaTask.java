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

package org.apache.avalon.meta.info.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.avalon.meta.info.Service;
import org.apache.avalon.meta.info.Type;
import org.apache.avalon.meta.info.writer.SerializedServiceWriter;
import org.apache.avalon.meta.info.writer.SerializedTypeWriter;
import org.apache.avalon.meta.info.writer.ServiceWriter;
import org.apache.avalon.meta.info.writer.TypeWriter;
import org.apache.avalon.meta.info.writer.XMLServiceWriter;
import org.apache.avalon.meta.info.writer.XMLTypeWriter;
import org.apache.avalon.meta.info.builder.tags.TypeTag;
import org.apache.avalon.meta.info.builder.tags.ServiceTag;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.thoughtworks.qdox.ant.AbstractQdoxTask;
import com.thoughtworks.qdox.model.JavaClass;

/**
 * Generate a meta info model from javadoc tags.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: MetaTask.java 45970 2004-09-13 12:09:43Z niclas $
 */
public class MetaTask
    extends AbstractQdoxTask
{
   /**
    * XML output type code.
    */
    public static final int XML_TYPE = 0;

   /**
    * Serial output type code.
    */
    public static final int SER_TYPE = 1;

    /**
     * A utility object that writes out info as xml files.
     */
    private static final TypeWriter XML_WRITER = new XMLTypeWriter();

    /**
     * A utility object that writes out info as serialized object files.
     */
    private static final TypeWriter SERIAL_WRITER = new SerializedTypeWriter();

    /**
     * A utility object that writes out a service as xml files.
     */
    private static final ServiceWriter XML_SERVICE_WRITER = new XMLServiceWriter();

    /**
     * A utility object that writes out a service as serialized object files.
     */
    private static final ServiceWriter SERIAL_SERVICE_WRITER = new SerializedServiceWriter();

    /**
     * The destination directory for metadata files.
     */
    private File m_destDir;

    /**
     * Variable that indicates the output type.
     */
    private int m_format;

    /**
     * The preferred postfix value.
     */
    private String m_postfix = "xinfo";

    /**
     * Variable that indicates whether the output
     * will be generated only when the source file is
     * newer than the destination file.
     */
    private boolean m_force = false;

    /**
     * Set the desitation directory to generate output files to.
     *
     * @param destDir The destination directory
     */
    public void setDestDir( final File destDir )
    {
        m_destDir = destDir;
    }

    /**
     * Specify the output format. Must be one of xml or serialized.
     *
     * @param format the output format
     */
    public void setFormat( final FormatEnum format )
    {
        m_format = format.getTypeCode();
    }

    /**
     * Set force to be true indicating that destination
     * files should always be regenerated.
     *
     * @param force the flag for forcing output
     */
    public void setForce( boolean force )
    {
        m_force = force;
    }

    /**
     * Set the file type to be used for meta info type
     * documents.  May be one of "xinfo" or "xtype".
     *
     * @param postfix the postfix value
     */
    public void setPostfix( String postfix )
    {
        m_postfix = validatePostfix( postfix );
    }

    private String getPostfix()
    {
        String postfix = getProject().getProperty( "avalon.meta.postfix" );
        if( null != postfix )
        {
            return validatePostfix( postfix );
        }
        return m_postfix;
    }

    private String validatePostfix( String postfix )
    {
        if( postfix.equalsIgnoreCase( "xtype" )
          || postfix.equalsIgnoreCase( "xinfo" ) )
        {
            return postfix.toLowerCase();
        }
        else
        {
            final String error = 
              "Illegal postfix value: " + postfix + ". "
              + "Recognized values include 'xinfo' and 'xtype'.";
            throw new BuildException( error );
        }
    }

    public void addConfigured( FileSet fileset )
    {
        super.addFileset( fileset );
    }

    /**
     * Execute generator task.
     * @exception BuildException if a build error occurs
     */
    public void execute()
        throws BuildException
    {
        validate();
        final String message =
          "Writing descriptors using '"
          + getOutputDescription()
          + "' format.";
        log( message );
        super.execute();
        addInnerClasses();
        try
        {
            Counter counter = writeMetaData();
            final String update =
              "Processed " + counter.getTypes() + " Types and "
              + counter.getServices() + " Services from a total of "
              + counter.getCount() + " classes.";
            log( update );
        }
        catch( final Exception e )
        {
            throw new BuildException( e.toString(), e );
        }
    }

    /**
     * allClasses contains only non-inner classes, so here we (recursively) extract out all inner
     * classes and explictly add them.
     */
    private void addInnerClasses()
    {
        ArrayList expList = new ArrayList();
        for( int i = 0; i < allClasses.size(); i++ )
        {
            addWithInnerClasses( expList, (JavaClass)allClasses.get( i ) );
        }
        allClasses = expList;
    }

    private void addWithInnerClasses( ArrayList list, JavaClass javaClass )
    {
        list.add( javaClass );

        final JavaClass[] innerClasses = javaClass.getInnerClasses();
        for( int i = 0; i < innerClasses.length; i++ )
        {
            addWithInnerClasses( list, innerClasses[i] );
        }
    }



    /**
     * Validate that the parameters are valid.
     */
    private void validate()
    {
        if( null == m_destDir )
        {
            final String message =
                "DestDir (" + m_destDir + ") not specified";
            throw new BuildException( message );
        }
        if( !m_destDir.isDirectory() )
        {
            final String message =
                "DestDir (" + m_destDir + ") is not a directory.";
            throw new BuildException( message );
        }

        if( !m_destDir.exists() && !m_destDir.mkdirs() )
        {
            final String message =
                "DestDir (" + m_destDir + ") could not be created.";
            throw new BuildException( message );
        }
    }

    /**
     * Return a description of output format to print as debug message.
     *
     * @return the output formats descriptive name
     */
    private String getOutputDescription()
    {
        if( SER_TYPE == m_format )
        {
            return "serial";
        }
        else
        {
            return "xml";
        }
    }

    /**
     * Output the metadata files.
     * @return the count holder
     * @throws IOException If a problem writing output
     */
    private Counter writeMetaData() throws IOException
    {
        int services = 0;
        int types = 0;
        final int size = allClasses.size();
        for( int i = 0; i < size; i++ )
        {
            final JavaClass javaClass = (JavaClass)allClasses.get( i );
            if( javaClass.isInterface() )
            {
                Service service = new ServiceTag( javaClass ).getService();
                if( service == null )
                {
                    continue;
                }

                services++;

                //
                // it is a service so we can fo ahead and build a
                // a service descriptor
                //

                final String classname = javaClass.getFullyQualifiedName();
                final File source = javaClass.getParentSource().getFile();
                final File dest = getOutputFileForService( classname );

                if( !m_force )
                {
                    if( dest.exists()
                      && dest.lastModified() >= source.lastModified() )
                    {
                        continue;
                    }
                }
                final File parent = dest.getParentFile();
                if( null != parent )
                {
                    if( !parent.exists() && !parent.mkdirs() )
                    {
                        final String message =
                            "Failed to create output directory: " + parent;
                        throw new BuildException( message );
                    }
                }
                writeService( service );
            }
            else
            {

                Type type = new TypeTag( javaClass ).getType();
                if( type == null )
                {
                    continue;
                }

                types++;
 
                //
                // it is a type implementation so we can fo ahead and build a
                // a type descriptor
                //

                final String classname = javaClass.getFullyQualifiedName();
                final File source = javaClass.getParentSource().getFile();
                final File dest = getOutputFileForClass( classname );

                if( !m_force )
                {
                    if( dest.exists()
                      && dest.lastModified() >= source.lastModified() )
                    {
                        continue;
                    }
                }
                final File parent = dest.getParentFile();
                if( null != parent )
                {
                    if( !parent.exists() && !parent.mkdirs() )
                    {
                        final String message =
                            "Failed to create output directory: " + parent;
                        throw new BuildException( message );
                    }
                }
                writeType( type );
            }
        }
        return new Counter( size, services, types );
    }

    /**
     * Write Service to a file.
     *
     * @param service the Service descriptor
     * @throws IOException if an error occurs while writing to file
     */
    private void writeService( final Service service )
        throws IOException
    {
        final String fqn = service.getReference().getClassname();
        final File file = getOutputFileForService( fqn );
        final OutputStream outputStream = new FileOutputStream( file );
        try
        {
            getServiceWriter().writeService( service, outputStream );
        }
        catch( final Exception e )
        {
            log( "Error writing service to " + file + ". Cause: " + e );
        }
        finally
        {
            shutdownStream( outputStream );
        }
    }

    /**
     * Write Type to a file.
     *
     * @param type the Type descriptor
     * @throws IOException if unable to write info out
     */
    private void writeType( final Type type )
        throws IOException
    {
        final String fqn = type.getInfo().getClassname();
        final File file = getOutputFileForClass( fqn );
        final OutputStream outputStream = new FileOutputStream( file );
        try
        {
            getTypeWriter().writeType( type, outputStream );
        }
        catch( final Exception e )
        {
            log( "Error writing " + file + ". Cause: " + e );
        }
        finally
        {
            shutdownStream( outputStream );
        }
    }

    /**
     * Return the correct info writer depending on
     * what format the info will be output as.  The
     * implementation will return either a servialized
     * wtiter or an xml writer based on the format
     * established by the client.
     *
     * @return the TypeWriter to output info with
     */
    private TypeWriter getTypeWriter()
    {
        if( SER_TYPE == m_format  )
        {
            return SERIAL_WRITER;
        }
        else
        {
            return XML_WRITER;
        }
    }


    /**
     * Return the correct service writer depending on
     * what format the service will be output as.  The
     * implementation will return either a serial
     * wtiter or an xml writer based on the format
     * established by the client.
     *
     * @return the ServiceWriter to output info with
     */
    private ServiceWriter getServiceWriter()
    {
        if( SER_TYPE == m_format  )
        {
            return SERIAL_SERVICE_WRITER;
        }
        else
        {
            return XML_SERVICE_WRITER;
        }
    }

    /**
     * Determine the file for the {@link Task}.
     *
     * @param classname the fully qualified name of file to generate
     * @return the file for info
     * @throws IOException if unable to determine base file
     */
    private File getOutputFileForClass( final String classname )
        throws IOException
    {
        String filename =
            classname.replace( '.', File.separatorChar );

        if( SER_TYPE == m_format )
        {
            filename += ".stype";
        }
        else
        {
            filename += "." + getPostfix();
        }
        return new File( m_destDir, filename ).getCanonicalFile();
    }

    /**
     * Determine the file for specified {@link Service}.
     *
     * @param classname the fully qualified name of file to generate
     * @return the file for the service descriptor
     * @throws IOException if unable to determine base file
     */
    private File getOutputFileForService( final String classname )
        throws IOException
    {
        String filename =
            classname.replace( '.', File.separatorChar );

        if( SER_TYPE == m_format )
        {
            filename += ".sservice";
        }
        else
        {
            filename += ".xservice";
        }
        return new File( m_destDir, filename ).getCanonicalFile();
    }


    /**
     * Close the specified output stream.
     *
     * @param outputStream the output stream
     */
    private void shutdownStream( final OutputStream outputStream )
    {
        if( null != outputStream )
        {
            try
            {
                outputStream.close();
            }
            catch( IOException e )
            {
                // ignore
            }
        }
    }

    /**
     * Return the destination directory in which files are generated.
     *
     * @return the destination directory in which files are generated.
     */
    protected final File getDestDir()
    {
        return m_destDir;
    }

   /**
    * Internal utility class that aggregates the number of services, the
    * number of types, and the total component count.
    */
    private class Counter
    {
        private int m_services;
        private int m_types;
        private int m_count;
        Counter( int count, int services, int types )
        {
            m_count = count;
            m_services = services;
            m_types = types;
        }
        protected int getServices()
        {
            return m_services;
        }
        protected int getTypes()
        {
            return m_types;
        }
        protected int getCount()
        {
            return m_count;
        }
    }
}

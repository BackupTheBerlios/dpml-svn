/*
 * Created on 06.05.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.metro.studio.eclipse.launch.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.meta.info.Type;
import org.apache.avalon.meta.info.Service;
import org.apache.avalon.meta.info.builder.BuildException;
import org.apache.avalon.meta.info.builder.tags.ServiceTag;
import org.apache.avalon.meta.info.builder.tags.TypeTag;
import org.apache.avalon.meta.info.writer.SerializedServiceWriter;
import org.apache.avalon.meta.info.writer.SerializedTypeWriter;
import org.apache.avalon.meta.info.writer.ServiceWriter;
import org.apache.avalon.meta.info.writer.TypeWriter;
import org.apache.avalon.meta.info.writer.XMLServiceWriter;
import org.apache.avalon.meta.info.writer.XMLTypeWriter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaSource;

/**
 * @author Andreas Develop
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MerlinTypeBuilder implements IMerlinBuilder
{
    protected JavaClass[] allClasses;
    private IResource m_resource;
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
    private String m_postfix = ".xinfo";

    /**
     * Variable that indicates whether the output
     * will be generated only when the source file is
     * newer than the destination file.
     */
    private boolean m_force = true;

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
    public void setFormat( final String format )
    {
        m_format = MerlinTypeBuilder.XML_TYPE;
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
        if (postfix.equalsIgnoreCase("xtype") || postfix.equalsIgnoreCase("xinfo"))
        {
            m_postfix = "." + postfix;
        } else
        {
            final String error =
                "Illegal postfix value: "
                    + postfix
                    + ". "
                    + "Recognized values include 'xinfo' and 'xtype'.";
            log(error);
        }
    }

    /**
     * Execute generator task.
     * @exception BuildException if a build error occurs
     */
    public void execute() 
        throws Exception
    {
        validate();
        final String message = "Writing descriptors using '" + getOutputDescription() + "' format.";
        log(message);

        addInnerClasses();

        try
        {
            Counter counter = writeMetaData();
            final String update =
                "Processed "
                    + counter.getTypes()
                    + " Types and "
                    + counter.getServices()
                    + " Services from a total of "
                    + counter.getCount()
                    + " classes.";
            log(update);
        } catch (final IllegalArgumentException e)
        {
            //MetroStudioLaunch.log(update,  e);
        }
    }

    /**
     * allClasses contains only non-inner classes, so here we (recursively) extract out all inner
     * classes and explictly add them.
     */
    private void addInnerClasses()
    {
        ArrayList expList = new ArrayList();
        for (int i = 0; i < allClasses.length; i++)
        {
            addWithInnerClasses(expList, (JavaClass) allClasses[i]);
        }
        allClasses = (JavaClass[])expList.toArray(new JavaClass[expList.size()]);
    }

    private void addWithInnerClasses( ArrayList list, JavaClass javaClass )
    {
        list.add(javaClass);

        final JavaClass[] innerClasses = javaClass.getInnerClasses();
        for (int i = 0; i < innerClasses.length; i++)
        {
            addWithInnerClasses(list, innerClasses[i]);
        }
    }

    /**
     * Validate that the parameters are valid.
     */
    private void validate()
    {
        if (null == m_destDir)
        {
            final String message = "DestDir (" + m_destDir + ") not specified";
            log(message);
        }
        if (!m_destDir.isDirectory())
        {
            final String message = "DestDir (" + m_destDir + ") is not a directory.";
            log(message);
        }

        if (!m_destDir.exists() && !m_destDir.mkdirs())
        {
            final String message = "DestDir (" + m_destDir + ") could not be created.";
            log(message);
        }
    }
 
    /**
     * Return a description of output format to print as debug message.
     *
     * @return the output formats descriptive name
     */
    private String getOutputDescription()
    {
        if (SER_TYPE == m_format)
        {
            return "serial";
        } else
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
        final int size = allClasses.length;
        for (int i = 0; i < size; i++)
        {
            final JavaClass javaClass = (JavaClass) allClasses[i];
            if (javaClass.isInterface())
            {
                Service service = new ServiceTag(javaClass).getService();
                if (service == null)
                {
                    continue;
                }

                services++;

                //
                // it is a service so we can fo ahead and build a
                // a service descriptor
                //

                final String classname = javaClass.getFullyQualifiedName();
                final File source = new File(javaClass.getParentSource().getURL().getFile());
                final File dest = getOutputFileForService(classname);

                if (!m_force)
                {
                    if (dest.exists() && dest.lastModified() >= source.lastModified())
                    {
                        continue;
                    }
                }
                final File parent = dest.getParentFile();
                if (null != parent)
                {
                    if (!parent.exists() && !parent.mkdirs())
                    {
                        final String message = "Failed to create output directory: " + parent;
                        log(message);
                    }
                }
                writeService(service);
            } else
            {
                Type type = null;
                TypeTag tag = new TypeTag(javaClass);
                type = tag.getType();
                if (type == null)
                {
                    continue;
                }

                types++;

                //
                // it is a type implementation so we can fo ahead and build a
                // a type descriptor
                //

                final String classname = javaClass.getFullyQualifiedName();
                final JavaSource src = javaClass.getParentSource();
                final File source = new File(src.getURL().getFile());
                final File dest = getOutputFileForClass(classname);

                if (!m_force)
                {
                    if (dest.exists() && dest.lastModified() >= source.lastModified())
                    {
                        continue;
                    }
                }
               
                final File parent = dest.getParentFile();
                if (null != parent)
                {
                    if (!parent.exists() && !parent.mkdirs())
                    {
                        final String message = "Failed to create output directory: " + parent;
                        log(message);
                    }
                }
                writeType(type);
            }
        }
        return new Counter(size, services, types);
    }

    /**
     * Write Service to a file.
     *
     * @param service the Service descriptor
     * @throws IOException if an error occurs while writing to file
     */
    private void writeService( final Service service ) throws IOException
    {
        final String fqn = service.getReference().getClassname();
        final File file = getOutputFileForService(fqn);
        final OutputStream outputStream = new FileOutputStream(file);
        try
        {
            getServiceWriter().writeService(service, outputStream);
        } catch (final Exception e)
        {
            log("Error writing service to " + file + ". Cause: " + e);
        } finally
        {
            shutdownStream(outputStream);
        }
    }

    /**
     * Write Type to a file.
     *
     * @param type the Type descriptor
     * @throws IOException if unable to write info out
     */
    private void writeType( final Type type ) throws IOException
    {
        final String fqn = type.getInfo().getClassname();
        final File file = getOutputFileForClass(fqn);
        final OutputStream outputStream = new FileOutputStream(file);
        try
        {
            getTypeWriter().writeType(type, outputStream);
        } catch (final Exception e)
        {
            log("Error writing " + file + ". Cause: " + e);
        } finally
        {
            shutdownStream(outputStream);
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
        if (SER_TYPE == m_format)
        {
            return SERIAL_WRITER;
        } else
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
        if (SER_TYPE == m_format)
        {
            return SERIAL_SERVICE_WRITER;
        } else
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
    private File getOutputFileForClass( final String classname ) throws IOException
    {
        String filename = classname.replace('.', File.separatorChar);

        if (SER_TYPE == m_format)
        {
            filename += ".stype";
        } else
        {
            filename += m_postfix;
        }
        return new File(m_destDir, filename).getCanonicalFile();
    }

    /**
     * Determine the file for specified {@link Service}.
     *
     * @param classname the fully qualified name of file to generate
     * @return the file for the service descriptor
     * @throws IOException if unable to determine base file
     */
    private File getOutputFileForService( final String classname ) throws IOException
    {
        String filename = classname.replace('.', File.separatorChar);

        if (SER_TYPE == m_format)
        {
            filename += ".sservice";
        } else
        {
            filename += ".xservice";
        }
        return new File(m_destDir, filename).getCanonicalFile();
    }

    /**
     * Close the specified output stream.
     *
     * @param outputStream the output stream
     */
    private void shutdownStream( final OutputStream outputStream )
    {
        if (null != outputStream)
        {
            try
            {
                outputStream.close();
            } catch (IOException e)
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
        Counter(int count, int services, int types)
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

    /**
     * 
     */
    public MerlinTypeBuilder()
    {
        super();
    }

    /**
     * Main build method. Only work on changed java files, which are marked to
     * be persistent.
     * 
     * @see org.apache.avalon.ide.eclipse.merlin.builder.IMerlinBuilder#build(int,
     *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void build( int pKind, IProject project, List pResources, IProgressMonitor pMonitor )
    {
        if (!isBuildingAllowed())
        {
            return;
        }
        pMonitor.subTask("Source");
        for (int i = 0; pResources.size() > i; i++)
        {
            m_resource = (IResource) pResources.get(i);
            
            if (m_resource.getFileExtension().toLowerCase().equals("java"))
            {
                try
                {
                    // MetroStudioLaunch.clearMarkers(m_resource);
                    String path = m_resource.getLocation().toString();
                    JavaDocBuilder builder = new JavaDocBuilder();
                    builder.addSource(new File(path));
                    allClasses = builder.getClasses();
                    if(allClasses.length>0)
                    {
                        JavaClass clazz = allClasses[0];
                        int clazzNameLen = clazz.getFullyQualifiedName().length() + 5;
                        
                        String str = m_resource.getLocation().toString();
                        str = str.substring(0, str.length()-clazzNameLen);
                        File file = new File(str);

                        setDestDir(file);
                        setForce(true);
                        setPostfix("xinfo");
                        execute();
                        
                    }
                } catch (FileNotFoundException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    private void log(String mes)
    {
        System.out.println(mes);
    }
    /**
     * @return
     */
    private boolean isBuildingAllowed()
    {
        boolean value = true;
        /*
        try
        {
            value = (EnterpriseDeveloperCore.getDefault().getPreferenceStore()
                    .getBoolean(BUILD_SOURCE_PROPERTY));
        } catch (Exception e)
        {
            EnterpriseDeveloperCore.log(e);
        }
        */
        return value;
    }

    

}

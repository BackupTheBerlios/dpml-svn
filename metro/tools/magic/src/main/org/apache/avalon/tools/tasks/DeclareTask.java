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
import org.apache.tools.ant.BuildException;
import org.apache.avalon.tools.model.Plugin.ListenerDef;
import org.apache.avalon.tools.model.Plugin.TaskDef;
import org.apache.avalon.tools.model.Plugin;
import org.apache.avalon.tools.model.Policy;

import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Create meta-data for a plugin.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class DeclareTask extends SystemTask
{
    private static final String PLUGIN = "plugin";

    private String m_type = PLUGIN;

    protected void setType( String type )
    {
        m_type = type;
    }

    public void execute() throws BuildException 
    {
        log( "creating plugin declaration" );
        final Definition def = getHome().getDefinition( getKey() );

        try
        {
            final File file = getPluginFile();
            file.getParentFile().mkdirs();
            file.createNewFile();
            final OutputStream output = new FileOutputStream( file );

            try
            {
                writePluginDef( output, def );
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

    protected File getPluginFile()
    {
        final File dir = getContext().getDeliverablesDirectory();
        final File ants = new File( dir, m_type + "s" );
        final Definition def = getHome().getDefinition( getKey() );
        final Info info = def.getInfo();
        final String filename = info.getShortFilename() + "." + m_type;
        return new File( ants, filename );
    }

    private void writePluginDef( final OutputStream output, final Definition def )
        throws IOException
    {
        final Writer writer = new OutputStreamWriter( output );
        writeHeader( writer );
        writePlugin( writer, def );
        writer.flush();
    }

    protected void writePlugin( final Writer writer, final Definition def )
        throws IOException
    {
        final Info info = def.getInfo();

        writer.write( "\n\n<plugin>" );
        writeInfo( writer, info );
        if( def instanceof Plugin )
        {
            final Plugin plugin = (Plugin) def;
            writeTaskDefs( writer, plugin );
            writeListenerDefs( writer, plugin );
        }
        writeClasspath( writer, def, "  ", true );
        writer.write( "\n</plugin>\n" );
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
    }

    private void writeTaskDefs( final Writer writer, final Plugin plugin )
        throws IOException
    {
        writer.write( "\n  " );
        writer.write( "<tasks>" );
        final TaskDef[] defs = plugin.getTaskDefs();
        for( int i=0; i<defs.length; i++ )
        {
            final TaskDef def = defs[i];
            writer.write( 
              "\n    <taskdef name=\"" 
              + def.getName() 
              + "\" class=\""
              + def.getClassname() 
              + "\"/>" );
        }
        writer.write( "\n  </tasks>" );
    }

    private void writeListenerDefs( final Writer writer, final Plugin plugin )
        throws IOException
    {
        writer.write( "\n  " );
        writer.write( "<listeners>" );
        final ListenerDef[] defs = plugin.getListenerDefs();
        for( int i=0; i<defs.length; i++ )
        {
            final ListenerDef def = defs[i];
            writer.write( 
              "\n    <listener class=\""
              + def.getClassname() 
              + "\"/>" );
        }
        writer.write( "\n  </listeners>" );
    }

    private void writeInfo( final Writer writer, final Info info )
        throws IOException
    {
        final String name = info.getName();
        final String group = info.getGroup();
        final String version = info.getVersion();

        writer.write( "\n  <info>" );
        writer.write( "\n    <name>" + name + "</name>" );
        writer.write( "\n    <group>" + group + "</group>" );
        if( null != version )
        {
            writer.write( "\n    <version>" + version + "</version>" );
        }
        writer.write( "\n    <type>" + m_type + "</type>" );
        writer.write( "\n  </info>" );
    }

    protected void writeClasspath( 
      final Writer writer, final Definition def, String padding, boolean flag )
        throws IOException
    {
        writer.write( "\n" + padding + "<classpath>" );
        final String pad = padding + "  ";
        final ResourceRef[] resources =
          def.getResourceRefs( getProject(), Policy.RUNTIME, ResourceRef.ANY, true );
        writeResourceRefs( writer, pad, resources, "jar" );
        if( flag )
        {
            writeResource( writer, pad, def, "jar" );
        }
        writer.write( "\n" + padding + "</classpath>" );
    }

    private void writeResourceRefs( 
      final Writer writer, final String pad, final ResourceRef[] resources, String type )
      throws IOException
    {
        for( int i=0; i<resources.length; i++ )
        {
            final ResourceRef ref = resources[i];
            final Policy policy = ref.getPolicy();
            if( policy.isRuntimeEnabled() )
            {
                final Resource resource = getHome().getResource( ref );
                writeResource( writer, pad, resource, type );
            }
        }
    }

    private void writeResource( 
      final Writer writer, final String pad, final Resource resource, String type )
      throws IOException
    {
        final Info info = resource.getInfo();
        if( !info.getType().equals( type ) )
          return;

        final String name = info.getName();
        final String group = info.getGroup();
        final String version = info.getVersion();

        writer.write( "\n" );
        writer.write( pad );
        writer.write( "<artifact>" + type + ":" );
        writer.write( group );
        writer.write( "/" );
        writer.write( name );

        if( null != version )
        {
            writer.write( "#" );
            writer.write( version );
        }
        writer.write( "</artifact>" );
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

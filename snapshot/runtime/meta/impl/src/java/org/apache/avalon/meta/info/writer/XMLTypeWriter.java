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

package org.apache.avalon.meta.info.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.avalon.meta.info.ContextDescriptor;
import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.Descriptor;
import org.apache.avalon.meta.info.EntryDescriptor;
import org.apache.avalon.meta.info.InfoDescriptor;
import org.apache.avalon.meta.info.CategoryDescriptor;
import org.apache.avalon.meta.info.ServiceDescriptor;
import org.apache.avalon.meta.info.StageDescriptor;
import org.apache.avalon.meta.info.ExtensionDescriptor;
import org.apache.avalon.meta.info.Type;
import org.apache.avalon.meta.info.SecurityDescriptor;
import org.apache.avalon.meta.info.PermissionDescriptor;

/**
 * Write {@link Type} objects to a stream as xml documents.
 *
 * TODO: Address configuration schema support
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: XMLTypeWriter.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class XMLTypeWriter
    implements TypeWriter
{
    private static final String CONTEXT_CLASS =
      "org.apache.avalon.framework.context.Context";

    /**
     * Write out type representation to xml.
     *
     * @param type the type object
     * @param outputStream the stream to write to
     * @throws Exception if unable to write xml
     */
    public void writeType( final Type type,
                                    final OutputStream outputStream )
        throws Exception
    {
        final Writer writer = new OutputStreamWriter( outputStream );
        writeHeader( writer );
        writeDoctype( writer, "type" );
        writer.write( "\n\n<type>" );
        writeInfo( writer, type.getInfo() );
        writeSecurity( writer, type.getSecurity() );
        writeLoggers( writer, type.getCategories() );
        writeContext( writer, type.getContext() );
        writeServices( writer, type.getServices() );
        writeDependencies( writer, type.getDependencies() );
        writeStages( writer, type.getStages() );
        writeExtensions( writer, type.getExtensions() );
        writer.write( "\n</type>" );
        writer.flush();
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

    /**
     * Write out DOCType delcaration.
     *
     * @param writer the writer
     * @param root the root name of document
     * @throws IOException if unable to write xml
     */
    private void writeDoctype( final Writer writer,
                               final String root )
        throws IOException
    {
        final String doctype =
            "\n<!DOCTYPE " + root
            + " PUBLIC \"-//AVALON/Type DTD Version 1.0//EN\" "
            + "\"http://avalon.apache.org/dtds/meta/type_1_1.dtd\" >";
        writer.write( doctype );
    }

    /**
     * Write out xml representation of the info descriptor from a Type.
     *
     * @param writer the writer
     * @param info the type info descriptor
     * @throws IOException if unable to write xml
     */
    private void writeInfo( final Writer writer, final InfoDescriptor info )
        throws IOException
    {
        writer.write( "\n  <info>" );

        writer.write( "\n    <name>");
        writer.write( info.getName() );
        writer.write( "</name>" );

        writer.write( "\n    <version>");
        writer.write( info.getVersion().toString() );
        writer.write( "</version>");

        if( info.getConfigurationSchema() != null )
        {
            writer.write( "\n    <schema>");
            writer.write( info.getConfigurationSchema() );
            writer.write( "</schema>");
        }

        writer.write( "\n    <lifestyle>");
        writer.write( info.getLifestyle() );
        writer.write( "</lifestyle>");

        writer.write( "\n    <collection>");
        writer.write( info.getCollectionPolicyKey( info.getCollectionPolicy() ) );
        writer.write( "</collection>");

        if( 0 == info.getAttributeNames().length )
        {
            writer.write( "\n  </info>" );
        }
        else
        {
            writeAttributes( writer, info );
            writer.write( "\n  </info>" );
        }
    }

    /**
     * Write out xml representation of the security criteria.
     *
     * @param writer the writer
     * @param security the security descriptor
     * @throws IOException if unable to write xml
     */
    private void writeSecurity( final Writer writer,
                               final SecurityDescriptor descriptor )
        throws IOException
    {
        PermissionDescriptor[] permissions = descriptor.getPermissions();
        if(
          ( 0 == descriptor.getAttributeNames().length )
          && ( 0 == permissions.length ) )
        {
            return;
        }

        writer.write( "\n  <security>" );
        for( int i = 0; i < permissions.length; i++ )
        {
            writePermission( writer, permissions[ i ] );
        }

        writer.write( "\n  </security>" );
    }


    /**
     * Write out xml representation of a logger.
     *
     * @param writer the writer
     * @param logger the logger
     * @throws IOException if unable to write xml
     */
    private void writePermission( final Writer writer,
                              final PermissionDescriptor permission )
        throws IOException
    {
        writer.write( "\n    <permission class=\"" );
        writer.write( permission.getClassname() );
        writer.write( "\" " );
        if( null != permission.getName() )
        {
            writer.write( "name=\"" );
            writer.write( permission.getName() );
            writer.write( "\"" );
        }
        if( 0 == permission.getActions().length )
        {
            writer.write( "/>" );
        }
        else
        {
            writer.write( ">" );
            String[] actions = permission.getActions();
            for( int i=0; i<actions.length; i++ )
            {
                String action = actions[i];
                writer.write( "\n      <action>" );
                writer.write( action );
                writer.write( "</action>" );
            }
            writer.write( "\n    </permission>" );
        }
    }

    /**
     * Write out xml representation of a set of loggers.
     *
     * @param writer the writer
     * @param loggers the loggers
     * @throws IOException if unable to write xml
     */
    private void writeLoggers( final Writer writer,
                               final CategoryDescriptor[] loggers )
        throws IOException
    {
        if( 0 == loggers.length )
        {
            return;
        }
        writer.write( "\n  <loggers>" );
        for( int i = 0; i < loggers.length; i++ )
        {
            writeLogger( writer, loggers[ i ] );
        }

        writer.write( "\n  </loggers>" );
    }

    /**
     * Write out xml representation of a logger.
     *
     * @param writer the writer
     * @param logger the logger
     * @throws IOException if unable to write xml
     */
    private void writeLogger( final Writer writer,
                              final CategoryDescriptor logger )
        throws IOException
    {
        writer.write( "\n    <logger name=\"" );
        writer.write( logger.getName() );
        if( 0 == logger.getAttributeNames().length )
        {
            writer.write( "\"/>" );
        }
        else
        {
            writer.write( "\">" );
            writeAttributes( writer, logger );
            writer.write( "\n    </logger>" );
        }
    }

    /**
     * Write out xml representation of a context.
     *
     * @param writer the writer
     * @param context the context
     * @throws IOException if unable to write xml
     */
    private void writeContext( final Writer writer,
                               final ContextDescriptor context )
        throws IOException
    {
        final String type = context.getContextInterfaceClassname();
        final EntryDescriptor[] entrys = context.getEntries();
        final int count = context.getAttributeNames().length;

        if( CONTEXT_CLASS.equals( type )
            && 0 == count
            && 0 == entrys.length )
        {
            return;
        }

        writer.write( "\n  <context" );
        if( !CONTEXT_CLASS.equals( type ) )
        {
            writer.write( " type=\"" );
            writer.write( type );
            writer.write( "\"" );
        }

        if( 0 == count && 0 == entrys.length )
        {
            writer.write( "/>" );
        }
        else
        {
            writer.write( ">" );
            for( int i = 0; i < entrys.length; i++ )
            {
                writeEntry( writer, entrys[ i ] );
            }
            writeAttributes( writer, context );
            writer.write( "\n  </context>" );
        }
    }

    /**
     * Write out xml representation of an entry.
     *
     * @param writer the writer
     * @param entry the entry
     * @throws IOException if unable to write xml
     */
    private void writeEntry( final Writer writer,
                             final EntryDescriptor entry )
        throws IOException
    {
        writer.write( "\n    <entry key=\"" );
        writer.write( entry.getKey() );
        writer.write( "\" " );

        if( !entry.getClassname().equals( "java.lang.String" ) )
        {
            writer.write( "type=\"" );
            writer.write( entry.getClassname() );
            writer.write( "\" " );
        }
        if(( entry.getAlias() != null ) && !entry.getAlias().equals( entry.getKey() ) )
        {
            writer.write( "alias=\"" );
            writer.write( entry.getAlias() );
            writer.write( "\" " );
        }
        if( entry.isOptional() )
        {
            writer.write( "optional=\"true\" " );
        }
        if( entry.isVolatile() )
        {
            writer.write( "volatile=\"true\" " );
        }

        writer.write( "/>" );
    }

    /**
     * Write out xml representation of a set of services.
     *
     * @param writer the writer
     * @param services the services
     * @throws IOException if unable to write xml
     */
    private void writeServices( final Writer writer,
                                final ServiceDescriptor[] services )
        throws IOException
    {
        if( 0 == services.length )
        {
            return;
        }

        writer.write( "\n  <services>" );
        for( int i = 0; i < services.length; i++ )
        {
            final ServiceDescriptor service = services[ i ];
            writer.write( "\n    <service type=\"" );
            writer.write( service.getReference().getClassname() );
            if( service.getReference().getVersion().getMajor() > -1 )
            {
                writer.write( "\" version=\"" );
                writer.write( service.getReference().getVersion().toString() );
            }
            final int count = service.getAttributeNames().length;
            if( 0 == count )
            {
                writer.write( "\"/>" );
            }
            else
            {
                writer.write( "\">" );
                writeAttributes( writer, service );
                writer.write( "\n    </service>" );
            }
        }
        writer.write( "\n  </services>" );
    }

    /**
     * Write out xml representation of a set of dependencies.
     *
     * @param writer the writer
     * @param dependencies the dependencies
     * @throws IOException if unable to write xml
     */
    private void writeDependencies( final Writer writer,
                                    final DependencyDescriptor[] dependencies )
        throws IOException
    {
        if( 0 == dependencies.length )
        {
            return;
        }

        writer.write( "\n  <dependencies>" );
        for( int i = 0; i < dependencies.length; i++ )
        {
            final DependencyDescriptor dependency = dependencies[ i ];
            writer.write( "\n    <dependency " );

            if( !dependency.getKey().equals( dependency.getReference().getClassname() ) )
            {
                writer.write( "key=\"" );
                writer.write( dependency.getKey() );
                writer.write( "\" " );
            }

            writer.write( "type=\"" );
            writer.write( dependency.getReference().getClassname() );
            if( dependency.getReference().getVersion().getMajor() > -1 )
            {
                writer.write( "\" version=\"" );
                writer.write( dependency.getReference().getVersion().toString() );
            }

            if( dependency.isOptional() )
            {
                writer.write( "\" optional=\"true" );
            }

            if( dependency.getPosition() > -1 )
            {
                writer.write( "\" index=\"" + dependency.getPosition() );
            }

            final int count = dependency.getAttributeNames().length;
            if( 0 == count )
            {
                writer.write( "\"/>" );
            }
            else
            {
                writer.write( "\">" );
                writeAttributes( writer, dependency );
                writer.write( "\n    </dependency>" );
            }
        }
        writer.write( "\n  </dependencies>" );
    }

    /**
     * Write out xml representation of a set of attributes.
     *
     * @param writer the writer
     * @param descriptor a meta info descriptor holding attributes
     * @throws IOException if unable to write xml
     */
    private void writeAttributes( final Writer writer, final Descriptor descriptor )
        throws IOException
    {
        String[] names = descriptor.getAttributeNames();
        if( names.length > 0 )
        {
            writer.write( "\n    <attributes>" );
            for( int i = 0; i < names.length; i++ )
            {
                writeAttribute( writer, names[i], descriptor.getAttribute( names[i] ) );
            }
            writer.write( "\n    </attributes>" );
        }
    }

    /**
     * Write out xml representation of a single attribute entry.
     *
     * @param writer the writer
     * @param key the attribute key
     * @param value the attribute value
     * @throws IOException if unable to write xml
     */
    private void writeAttribute( final Writer writer,
                                 final String key, final String value )
        throws IOException
    {
        writer.write( "\n      <attribute key=\"" );
        writer.write( key );
        writer.write( "\" value=\"" );
        writer.write( value );
        writer.write( "\"/>" );
    }

    /**
     * Write out xml representation of a set of stages.
     *
     * @param writer the writer
     * @param stages the stages
     * @throws IOException if unable to write xml
     */
    private void writeStages( final Writer writer,
                              final StageDescriptor[] stages )
        throws IOException
    {
        if( 0 == stages.length )
        {
            return;
        }

        writer.write( "\n  <stages>" );
        for( int i = 0; i < stages.length; i++ )
        {
            final StageDescriptor stage = stages[ i ];
            writer.write( "\n    <stage " );
            writer.write( "id=\"" );
            writer.write( stage.getKey() );

            final int count = stage.getAttributeNames().length;
            if( 0 == count )
            {
                writer.write( "\"/>" );
            }
            else
            {
                writer.write( "\">" );
                writeAttributes( writer, stage );
                writer.write( "\n    </stage>" );
            }
        }
        writer.write( "\n  </stages>" );
    }

    /**
     * Write out xml representation of a set of extensions.
     *
     * @param writer the writer
     * @param extensions the extensions
     * @throws IOException if unable to write xml
     */
    private void writeExtensions( final Writer writer,
                                  final ExtensionDescriptor[] extensions )
        throws IOException
    {
        if( 0 == extensions.length )
        {
            return;
        }

        writer.write( "\n  <extensions>" );
        for( int i = 0; i < extensions.length; i++ )
        {
            final ExtensionDescriptor extension = extensions[ i ];

            writer.write( "\n    <extension " );
            writer.write( "id=\"" );
            writer.write( extension.getKey() );

            final int count = extension.getAttributeNames().length;
            if( 0 == count )
            {
                writer.write( "\"/>" );
            }
            else
            {
                writer.write( "\">" );
                writeAttributes( writer, extension );
                writer.write( "\n    </extension>" );
            }
        }
        writer.write( "\n  </extensions>" );
    }
}

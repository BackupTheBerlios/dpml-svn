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

package org.apache.avalon.composition.data.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.avalon.composition.data.BlockIncludeDirective;
import org.apache.avalon.composition.data.BlockCompositionDirective;
import org.apache.avalon.composition.data.DeploymentProfile;
import org.apache.avalon.composition.data.ContainmentProfile;
import org.apache.avalon.composition.data.ComponentProfile;
import org.apache.avalon.composition.data.ClassLoaderDirective;
import org.apache.avalon.composition.data.ClasspathDirective;
import org.apache.avalon.composition.data.LibraryDirective;
import org.apache.avalon.composition.data.ServiceDirective;
import org.apache.avalon.composition.data.FilesetDirective;
import org.apache.avalon.composition.data.IncludeDirective;
import org.apache.avalon.composition.data.ExcludeDirective;

import org.apache.avalon.repository.Artifact;

/**
 * Write {@link ContainmentProfile} objects to a stream as xml documents.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: XMLContainmentProfileWriter.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class XMLContainmentProfileWriter extends XMLComponentProfileWriter
{
    /**
     * Write out a containment profile to xml.
     *
     * @param profile the type object
     * @param output the stream to write to
     * @throws Exception if unable to write xml
     */
    public void writeContainmentProfile( 
      final ContainmentProfile profile, final OutputStream output )
      throws Exception
    {
        final Writer writer = new OutputStreamWriter( output );
        writeHeader( writer, profile );
        writeContainmentProfile( writer, profile, true, "" );
        writer.flush();
    }

    /**
     * Write out a containment profile to xml.
     *
     * @param writer the stream to write to
     * @param profile the containment profile to write
     * @param pad character offset
     * @throws Exception if unable to write xml
     */
    protected void writeContainmentProfile( 
      final Writer writer, final ContainmentProfile profile, String pad )
      throws Exception
    {
        writeContainmentProfile( writer, profile, false, pad );
    }

    /**
     * Write out a containment profile to xml.
     *
     * @param writer the stream to write to
     * @param profile the containment profile to write
     * @param flag not used !
     * @param pad character offset
     * @throws Exception if unable to write xml
     */
    protected void writeContainmentProfile( 
      final Writer writer, final ContainmentProfile profile, boolean flag, String pad )
      throws Exception
    {
        writer.write( "\n" );
        writer.write( pad + "<container name=\"" + profile.getName() + "\">");
        final String padding = pad + INDENT;
        writeServiceDirectives( writer, profile.getExportDirectives(), padding );
        writeClassLoader( writer, profile.getClassLoaderDirective(), padding );
        writeCategories( writer, profile.getCategories(), padding );
        writeProfiles( writer, profile.getProfiles(), padding );
        writer.write( "\n" + pad + "</container>" );
        writer.write( "\n" );
    }

   /**
    * Write the XML header.
    * @param writer the writer
    * @param profile the containment profile
    * @throws IOException if unable to write xml
    */
    private void writeHeader( 
      final Writer writer, ContainmentProfile profile )
        throws IOException
    {
        writer.write( "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" );
    }

    /**
     * Write out xml representation of the info descriptor from a Type.
     *
     * @param writer the writer
     * @param services the service directives
     * @param pad character offset
     * @throws IOException if unable to write xml
     */
    private void writeServiceDirectives( 
      final Writer writer, final ServiceDirective[] services, String pad )
      throws IOException
    {
        if( 0 == services.length )
        {
            return;
        }

        writer.write( "\n" + pad + "<services>" );
        final String padding = pad + INDENT;
        for( int i = 0; i < services.length; i++ )
        {
            final ServiceDirective service = services[ i ];
            writer.write( "\n" + padding + "<service type=\"" );
            writer.write( service.getReference().getClassname() );
            if( service.getReference().getVersion().getMajor() > -1 )
            {
                writer.write( "\" version=\"" + service.getReference().getVersion() );
            }
            writer.write( "\">" );
            writer.write( "\n" + padding + INDENT 
              + "<source>" 
              + service.getPath()
              + "</source>" );
            writer.write( "\n" + padding + "</service>" );
        }
        writer.write( "\n" + pad + "</services>" );
    }

    /**
     * Write out xml representation of the classloader
     *
     * @param writer the writer
     * @param classloader the classloader directive
     * @param pad character offset
     * @throws IOException if unable to write xml
     */
    private void writeClassLoader( 
      final Writer writer, final ClassLoaderDirective classloader, String pad )
      throws IOException
    {
        if( classloader.isEmpty() ) return;

        writer.write( "\n" + pad + "<classloader>" );
        final String padding = pad + INDENT;

        LibraryDirective library = classloader.getLibrary();
        writeLibrary( writer, library, padding );

        ClasspathDirective classpath = classloader.getClasspathDirective();
        writeClasspathDirective( writer, classpath, padding );

        writer.write( "\n" + pad + "</classloader>" );
    }

    /**
     * Write out xml representation of a set of includes.
     *
     * @param writer the writer
     * @param library the library directives
     * @param pad character offset
     * @throws IOException if unable to write xml
     */
    private void writeLibrary( 
      final Writer writer, final LibraryDirective library, String pad )
      throws IOException
    {
        if( library.isEmpty() ) return;

        final String padding = pad + INDENT;
        writer.write( "\n" + pad + "<library>" );
        String[] includes = library.getIncludes();
        for( int i=0; i<includes.length; i++ )
        {
            writer.write( "\n" + padding
              + "<include>" + includes[i] + "</include>" );
        }

        String[] groups = library.getGroups();
        for( int i=0; i<groups.length; i++ )
        {
            writer.write( "\n" + padding
              + "<group>" + groups[i] + "</group>" );
        }

        writer.write( "\n" + pad + "</library>" );
    }

    /**
     * Write out xml representation of a classpath.
     *
     * @param writer the writer
     * @param classpath the classpath directives
     * @param pad character offset
     * @throws IOException if unable to write xml
     */
    private void writeClasspathDirective( 
      final Writer writer, 
      final ClasspathDirective classpath,
      String pad )
      throws IOException
    {
        if( classpath.isEmpty() ) return;

        final String padding = pad + INDENT;
        writer.write( "\n" + pad + "<classpath>" );

        FilesetDirective[] filesets = classpath.getFilesets();
        if( filesets.length > 0 )
        {
            for( int i=0; i<filesets.length; i++ )
            {
                writeFilesetDirective( writer, filesets[i], padding );
            }
        }

        Artifact[] artifacts = classpath.getArtifacts();
        for( int i=0; i<artifacts.length; i++ )
        {
            writeArtifactDirective( writer, artifacts[i], padding );
        }
        writer.write( "\n" + pad + "</classpath>" );
    }

    /**
     * Write out xml representation of a fileset.
     *
     * @param writer the writer
     * @param fileset the fileset directives
     * @param pad character offset
     * @throws IOException if unable to write xml
     */
    private void writeFilesetDirective( 
      final Writer writer, final FilesetDirective fileset, String pad )
      throws IOException
    {
        writer.write( "\n" + pad 
          + "<fileset dir=\"" 
          + fileset.getBaseDirectory() + "\">" );

        IncludeDirective[] includes = fileset.getIncludes();
        for( int i=0; i<includes.length; i++ )
        {
            writer.write( "\n" + pad + INDENT 
                  + "<include>" 
                  + includes[i].getPath() 
                  + "</include>" );
        }

        ExcludeDirective[] excludes = fileset.getExcludes();
        for( int i=0; i<excludes.length; i++ )
        {
            writer.write( "\n" + pad + INDENT 
                  + "<exclude>" 
                  + excludes[i].getPath() 
                  + "</exclude>" );
        }

        writer.write( "\n" + pad + "</fileset>" );
    }


    /**
     * Write out xml representation of the classloader
     *
     * @param writer the writer
     * @param artifact an artifact directive
     * @param pad character offset
     * @throws IOException if unable to write xml
     */
    private void writeArtifactDirective( 
      final Writer writer, final Artifact artifact, String pad )
      throws IOException
    {
        writer.write( "\n" + pad + "<artifact>" );
        writer.write( artifact.getSpecification() );
        writer.write( "</artifact>" );
    }

    /**
     * Write out xml representation of the embedded profiles
     *
     * @param writer the writer
     * @param profiles the nested profiles
     * @param pad character offset
     * @throws IOException if unable to write xml
     */
    private void writeProfiles( 
      final Writer writer, final DeploymentProfile[] profiles, String pad )
      throws Exception
    {
        for( int i=0; i<profiles.length; i++ )
        {
            DeploymentProfile profile = profiles[i];
            if( profile instanceof ContainmentProfile )
            {
                ContainmentProfile container = (ContainmentProfile) profile;
                writer.write( "\n" );
                writeContainmentProfile( writer, container, pad );
            }
            else if( profile instanceof ComponentProfile )
            {
                ComponentProfile component = (ComponentProfile) profile;
                writer.write( "\n" );
                writeComponentProfile( writer, component, pad );
            }
            else if( profile instanceof BlockIncludeDirective )
            {
                BlockIncludeDirective directive = (BlockIncludeDirective) profile;
                writer.write( "\n" );
                writeBlockIncludeDirective( writer, directive, pad );
            }
            else if( profile instanceof BlockCompositionDirective )
            {
                BlockCompositionDirective directive = (BlockCompositionDirective) profile;
                writer.write( "\n" );
                writeBlockCompositionDirective( writer, directive, pad );
            }
            else
            {
                throw new IllegalArgumentException( 
                  "Unrecognized profile class: " 
                  + profile.getClass().getName() );
            }
        }
    }

    private void writeBlockIncludeDirective( 
      final Writer writer, final BlockIncludeDirective directive, String pad )
      throws Exception
    {
        writer.write( "\n" + pad + "<include name=\"" );
        writer.write( directive.getName() + "\">" );
        writer.write( "\n" + pad + INDENT + "<source>" );
        writer.write( directive.getPath() + "</source>" );
        writer.write( "\n" + pad + "</include>" );
    }

    private void writeBlockCompositionDirective( 
      final Writer writer, final BlockCompositionDirective directive, String pad )
      throws Exception
    {
        writer.write( "\n" + pad + "<include name=\"" );
        writer.write( directive.getName() + "\" id=\"" );
        writer.write( directive.getArtifact().getGroup() );
        writer.write( "/" );
        writer.write( directive.getArtifact().getName() + "\" version=\"" );
        writer.write( directive.getArtifact().getVersion() + "\"/>" );
    }
}

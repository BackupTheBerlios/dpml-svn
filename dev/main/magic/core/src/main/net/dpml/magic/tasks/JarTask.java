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

package net.dpml.magic.tasks;

import net.dpml.magic.model.Definition;
import net.dpml.magic.project.Context;
import net.dpml.magic.project.DeliverableHelper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;

import java.io.File;

/**
 * Creation of a jar file using the content undder the ${project.target}/classes
 * directory.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class JarTask extends ProjectTask
{
    public static final String JAR_EXT = "jar";
    public static final String JAR_MAIN_KEY = "project.jar.main.class";
    public static final String JAR_CLASSPATH_KEY = "project.jar.classpath";
    public static final String DPML = "The Digital Product Meta Library.";

    public void execute() throws BuildException
    {
        final File classes =
          getContext().getClassesDirectory();
        final File deliverables =
          getContext().getDeliverablesDirectory();

        final Definition def = getDefinition();
        final File jarFile = getJarFile( deliverables );
        if( classes.exists() )
        {
            final boolean modified = jar( def, classes, jarFile );
            if( modified )
            {
                DeliverableHelper.checksum( this, jarFile );
                String gpg = getIndex().getProperty( Context.GPG_EXE_KEY );
                DeliverableHelper.asc( this, jarFile, gpg );
            }
        }
    }

    public File getJarFile( final File deliverables )
    {
        final Definition def = getDefinition();
        final File types = new File( deliverables, JAR_EXT + "s" );
        final String filename = def.getFilename( JAR_EXT );
        return new File( types, filename );
    }

    private boolean jar( final Definition def, final File classes, final File jarFile )
    {
        final File dir = jarFile.getParentFile();
        mkDir( dir );

        long modified = -1;
        if( jarFile.exists() )
        {
            modified = jarFile.lastModified();
        }

        final Jar jar = (Jar) getProject().createTask( "jar" );
        jar.setTaskName( getTaskName() );
        jar.setDestFile( jarFile );
        jar.setBasedir( classes );
        jar.setIndex( true );
        addManifest( jar, def );
        jar.init();
        jar.execute();

        return jarFile.lastModified() > modified;
    }

    private void addManifest( final Jar jar, final Definition def )
    {
        try
        {
            final Manifest manifest = new Manifest();
            final Manifest.Section main = manifest.getMainSection();

            addAttribute( main, "Created-By", DPML );
            addAttribute( main, "Built-By", System.getProperty( "user.name" ) );
            final String classpath = getProject().getProperty( JAR_CLASSPATH_KEY );
            if( null != classpath )
            {
                addAttribute( main, "Class-Path", classpath );
            }
            final String mainClass = getProject().getProperty( JAR_MAIN_KEY );
            if( null != mainClass )
            {
                addAttribute( main, "Main-Class", mainClass );
            }

            addAttribute( main, "Extension-Name", def.getInfo().getName() );
            addAttribute( main, "Specification-Vendor", DPML );

            if( null != def.getInfo().getVersion() )
            {

                // TODO: add the declaration of the spec version
                // to the Info object and validate that it is a
                // a dewey decimal

                addAttribute( main, "Specification-Version", "0.0.0" );
            }
            else
            {
                addAttribute( main, "Specification-Version", "0" );
            }
            addAttribute( main, "Implementation-Vendor", DPML );
            addAttribute( main, "Implementation-Vendor-Id", "net.dpml" );

            final String defVersion = def.getInfo().getVersion();
            addAttribute( main, "Implementation-Version", defVersion );

            jar.addConfiguredManifest( manifest );
        }
        catch( Throwable e )
        {
            throw new BuildException( e );
        }
    }

    private void addAttribute(
      final Manifest.Section section, final String name, final String value )
      throws ManifestException
    {
        final Manifest.Attribute attribute = new Manifest.Attribute( name, value );
        section.addConfiguredAttribute( attribute );
    }
}

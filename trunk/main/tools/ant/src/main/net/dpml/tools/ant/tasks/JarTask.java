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

package net.dpml.tools.ant.tasks;

import java.io.File;
import java.rmi.RemoteException;

import net.dpml.tools.ant.tasks.GenericTask;
import net.dpml.tools.ant.Definition;
import net.dpml.tools.ant.Process;
import net.dpml.tools.ant.Phase;
import net.dpml.tools.ant.Process;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;

/**
 * Execute all plugins relative to the current build phase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class JarTask extends GenericTask
{
   /**
    * Constant key for the manifest main class value.
    */
    public static final String JAR_MAIN_KEY = "project.jar.main.class";

   /**
    * Constant key for the manifest classpath value.
    */
    public static final String JAR_CLASSPATH_KEY = "project.jar.classpath";
    
    private File m_source;
    private File m_destination;
    
    public void setSrc( File source )
    {
        if( source.isDirectory() )
        {
            m_source = source;
        }
        else
        {
            final String error = 
              "Jar task src must be a directory.";
            throw new BuildException( error, getLocation() );
        }
    }
    
    public void setDest( File destination )
    {
        m_destination = destination;
    }
    
    private File getSource()
    {
        if( null == m_source )
        {
            final String error = 
              "Missing 'src' attribute in jar task.";
            throw new BuildException( error, getLocation() );
        }
        else
        {
            return m_source;
        }
    }
    
    private File getDestination()
    {
        if( null == m_destination )
        {
            final String error = 
              "Missing 'dest' attribute in jar task.";
            throw new BuildException( error, getLocation() );
        }
        else
        {
            return m_destination;
        }
    }
    
    public void execute()
    {
        File source = getSource();
        if( !source.exists() )
        {
            return;
        }
        Project project = getProject();
        Definition definition = getDefinition();
        File jar = getDestination();
        boolean modified = createJarFile( source, jar );
        if( modified )
        {
            checksum( jar );
            asc( jar );
        }
    }
    
    private boolean createJarFile( final File classes, final File jarFile )
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
        addManifest( jar );
        jar.init();
        jar.execute();

        return jarFile.lastModified() > modified;
    }

    private void addManifest( final Jar jar )
    {
        try
        {
            final Manifest manifest = new Manifest();
            final Manifest.Section main = manifest.getMainSection();

            String publisher = getProject().getProperty( "project.publisher.name" );
            if( null != publisher )
            {
                addAttribute( main, "Created-By", publisher );
            }
            
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

            addAttribute( main, "Extension-Name", getDefinition().getProjectPath() );
            String specificationVendor = getProject().getProperty( "project.specification.vendor" );
            if( null != specificationVendor )
            {
                addAttribute( main, "Specification-Vendor", specificationVendor );
            }

            String version = getProject().getProperty( "project.specification.version" );
            if( null != version )
            {
                addAttribute( main, "Specification-Version", version );
            }
            
            String implementationVendor = getProject().getProperty( "project.implementation.vendor" );
            if( null != implementationVendor )
            {
                addAttribute( main, "Implementation-Vendor", implementationVendor );
            }

            String implementationVendorID = getProject().getProperty( "project.implementation.vendor-id" );
            if( null != implementationVendorID )
            {
                addAttribute( main, "Implementation-Vendor-Id", implementationVendorID );
            }

            final String implementationVersion = getDefinition().getVersion();
            addAttribute( main, "Implementation-Version", implementationVersion );

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

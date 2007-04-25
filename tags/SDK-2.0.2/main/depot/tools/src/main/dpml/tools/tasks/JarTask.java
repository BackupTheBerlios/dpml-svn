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

package dpml.tools.tasks;

import java.io.File;

import net.dpml.lang.Version;

import dpml.library.Resource;
import dpml.library.Type;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.taskdefs.Copy;

/**
 * Execute all plugins relative to the current build phase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
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
    
   /**
    * Task initialization.
    */
    public void init()
    {
        if( !isInitialized() )
        {
            super.init();
        }
    }
    
   /**
    * Set the src directory.
    * @param source the source directory
    */
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
    
   /**
    * Set the destination file.
    * @param destination the destination file
    */
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
    
   /**
    * Execute the task.
    */
    public void execute()
    {
        File source = getSource();
        if( !source.exists() )
        {
            return;
        }
        File jar  = createJarFile( source );
        Resource resource = getResource();
        Type type = resource.getType( "jar" );
        if( !type.getTest() )
        {
            checksum( jar );
            asc( jar );
        }
    }
    
    private File createJarFile( final File classes )
    {   
        final File temp = getTempFile();
        final File tempdir = temp.getParentFile();
        if( null != tempdir )
        {
            mkDir( tempdir );
        }
        
        final Jar jar = (Jar) getProject().createTask( "jar" );
        jar.setTaskName( getTaskName() );
        jar.setDestFile( temp );
        jar.setBasedir( classes );
        jar.setIndex( true );
        addManifest( jar );
        jar.init();
        jar.execute();
        
        File dest = getDestination();
        final File dir = dest.getParentFile();
        if( null != dir )
        {
            mkDir( dir );
        }
        final Copy copy = (Copy) getProject().createTask( "copy" );
        copy.setTaskName( getTaskName() );
        copy.setFile( temp );
        copy.setTofile( dest );
        copy.init();
        copy.execute();
        
        return dest;
    }
    
    private File getTempFile()
    {
        final File temp = getContext().getTargetTempDirectory();
        final String path = getTempFilename();
        return new File( temp, path );
    }
    
    private String getTempFilename()
    {
        Resource resource = getResource();
        Type type = resource.getType( "jar" );
        String name = type.getName();
        return name + "." + type.getID();
        /*
        Version version = resource.getDecimalVersion();
        if( null != version )
        {
            return name + "-" + version.toString() + ".jar";
        }
        else
        {
            String spec = resource.getVersion();
            return name + "-" + spec + ".jar";
        }
        */
    }
    
    private Type getType()
    {
        Resource resource = getResource();
        return resource.getType( "jar" );
    }

    private void addManifest( final Jar jar )
    {
        try
        {
            Type type = getType();
            Resource resource = getResource();
            
            final Manifest manifest = new Manifest();
            final Manifest.Section main = manifest.getMainSection();

            String publisher = resource.getProperty( "project.publisher.name" );
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
            
            final String mainClass = resource.getProperty( JAR_MAIN_KEY );
            if( null != mainClass )
            {
                addAttribute( main, "Main-Class", mainClass );
            }

            addAttribute( main, "Extension-Name", getResource().getResourcePath() );
            String specificationVendor = resource.getProperty( "project.specification.vendor" );
            if( null != specificationVendor )
            {
                addAttribute( main, "Specification-Vendor", specificationVendor );
            }

            String version = resource.getProperty( "project.specification.version" );
            if( null != version )
            {
                addAttribute( main, "Specification-Version", version );
            }
            
            String implementationVendor = resource.getProperty( "project.implementation.vendor" );
            if( null != implementationVendor )
            {
                addAttribute( main, "Implementation-Vendor", implementationVendor );
            }

            String implementationVendorID = resource.getProperty( "project.implementation.vendor-id" );
            if( null != implementationVendorID )
            {
                addAttribute( main, "Implementation-Vendor-Id", implementationVendorID );
            }

            final Version spec = resource.getDecimalVersion();
            if( null != spec )
            {
                String implementationVersion = spec.toString();
                addAttribute( main, "Implementation-Version", implementationVersion );
            }

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

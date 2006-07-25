/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.tools;

import java.io.File;

import net.dpml.library.Library;
import net.dpml.library.Resource;
import net.dpml.library.info.Scope;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

/**
 * Project context.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Context
{
   /**
    * Return the associated project.
    * @return the ant project
    */
    Project getProject();
    
   /**
    * Return the value of a property.
    * @param key the property key
    * @return the property value or null if undefined
    */
    String getProperty( String key );
    
   /**
    * Return the value of a property. If the project contains a declaration 
    * for the property then that value will be returned, otherwise the property
    * will be resolved relative to the current resource.
    *
    * @param key the property key
    * @param value the default value
    * @return the property value or null if undefined
    */
    String getProperty( String key, String value );
    
   /**
    *Initialize the context.
    */
    void init();
    
   /**
    * Return an Ant path suitable for compile or runtime usage. If the supplied scope is 
    * less than Scope.RUNTIME a runtime path is returned otherwise the test path is 
    * returned.
    *
    * @param scope the build scope
    * @return the path object
    */
    Path getPath( Scope scope );
    
   /**
    * Return the active resource.
    * @return the resource definition
    */
    Resource getResource();
    
   /**
    * Return the resource library.
    * @return the library
    */
    Library getLibrary();
    
   /**
    * Return the project source directory.
    * @return the directory
    */
    File getSrcDirectory();
    
   /**
    * Return the project source main directory.
    * @return the directory
    */
    File getSrcMainDirectory();
    
   /**
    * Return the project source test directory.
    * @return the directory
    */
    File getSrcTestDirectory();
    
   /**
    * Return the project source docs directory.
    * @return the directory
    */
    File getSrcDocsDirectory();
    
   /**
    * Return the project etc directory.
    * @return the directory
    */
    File getEtcDirectory();

   /**
    * Return the project etc/main directory.
    * @return the directory
    */
    File getEtcMainDirectory();

   /**
    * Return the project etc/test directory.
    * @return the directory
    */
    File getEtcTestDirectory();

   /**
    * Return the project etc/data directory.
    * @return the directory
    */
    File getEtcDataDirectory();

   /**
    * Return the project target directory.
    * @return the directory
    */
    File getTargetDirectory();
    
   /**
    * Return a directory within the target directory.
    * @param path the path
    * @return the directory
    */
    File getTargetDirectory( String path );
    
   /**
    * Return the project target temp directory.
    * @return the directory
    */
    File getTargetTempDirectory();
    
   /**
    * Return the project target build directory.
    * @return the directory
    */
    File getTargetBuildDirectory();
    
   /**
    * Return the project target build main directory.
    * @return the directory
    */
    File getTargetBuildMainDirectory();
    
   /**
    * Return the project target build test directory.
    * @return the directory
    */
    File getTargetBuildTestDirectory();
    
   /**
    * Return the project target build docs directory.
    * @return the directory
    */
    File getTargetBuildDocsDirectory();
    
   /**
    * Return the project target root classes directory.
    * @return the directory
    */
    File getTargetClassesDirectory();
    
   /**
    * Return the project target main classes directory.
    * @return the directory
    */
    File getTargetClassesMainDirectory();
    
   /**
    * Return the project target test classes directory.
    * @return the directory
    */
    File getTargetClassesTestDirectory();
    
   /**
    * Return the project target reports directory.
    * @return the directory
    */
    File getTargetReportsDirectory();
    
   /**
    * Return the project target test reports directory.
    * @return the directory
    */
    File getTargetReportsTestDirectory();
    
   /**
    * Return the project target main reports directory.
    * @return the directory
    */
    File getTargetReportsMainDirectory();
    
   /**
    * Return the project target javadoc reports directory.
    * @return the directory
    */
    File getTargetReportsJavadocDirectory();
    
   /**
    * Return the project target reports docs directory.
    * @return the directory
    */
    File getTargetDocsDirectory();
    
   /**
    * Return the project target test directory.
    * @return the directory
    */
    File getTargetTestDirectory();
    
   /**
    * Return the project target deliverables directory.
    * @return the directory
    */
    File getTargetDeliverablesDirectory();
    
   /**
    * Return the project target deliverables directory.
    * @param type the deliverable type
    * @return the directory
    */
    File getTargetDeliverable( String type );
    
   /**
    * Create a file relative to the resource basedir.
    * @param path the relative path
    * @return the directory
    */
    File createFile( String path );
    
   /**
    * Return a filename using the layout strategy employed by the cache.
    * @param id the artifact type
    * @return the filename
    */
    String getLayoutFilename( String id );
   
   /**
    * Return the directory path representing the module structure and type
    * using the layout strategy employed by the cache.
    * @param id the artifact type
    * @return the path from the root of the cache to the directory containing the artifact
    */
    String getLayoutBase( String id );
    
   /**
    * Return the full path to an artifact using the layout employed by the cache.
    * @param id the artifact type
    * @return the full path including base path and filename
    */
    String getLayoutPath( String id );
    
   /**
    * Utility operation to construct a new classpath path instance.
    * @param scope the build scope
    * @return the path
    */
    Path createPath( Scope scope );
    
   /**
    * Utility operation to construct a new path using a supplied array of resources.
    * @param resources the resource to use in path construction
    * @return the path
    */
    Path createPath( Resource[] resources );
    
   /**
    * Utility operation to construct a new path using a supplied array of resources.
    * @param resources the resources to use in path construction
    * @param resolve if true force local caching of the artifact 
    * @param filter if true restrict path entries to resources that produce jars
    * @return the path
    */
    Path createPath( Resource[] resources, boolean resolve, boolean filter );
}


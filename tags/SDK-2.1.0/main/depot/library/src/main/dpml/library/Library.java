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

package dpml.library;

import java.io.File;

/**
 * The Library interface is the application root for module management.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Library
{
   /**
    * Index filename.
    */
    public static final String INDEX_FILENAME = "index.xml";
    
   /**
    * Utility operation to sort a collection of resources.
    * @param resources the resources to sort
    * @return the sorted resource array
    */
    Resource[] sort( Resource[] resources );
    
   /**
    * Return a array of the top-level modules within the library.
    * @return the module array
    */
    Module[] getModules();
    
   /**
    * Return a array of all modules in the library.
    * @return module array
    */
    Module[] getAllModules();
    
   /**
    * Return a named module.
    * @param ref the fully qualified module name
    * @return the module
    * @exception ModuleNotFoundException if the module cannot be found
    */
    Module getModule( String ref ) throws ModuleNotFoundException;
    
   /**
    * Recursively lookup a resource using a fully qualified reference.
    * @param ref the fully qualified resource name
    * @return the resource instance
    * @exception ResourceNotFoundException if the resource cannot be found
    */
    Resource getResource( String ref ) throws ResourceNotFoundException;
    
   /**
    * <p>Select a set of resource matching a supplied a resource selection 
    * constraint.  The constraint may contain the wildcards '**' and '*'.
    * @param criteria the selection criteria
    * @param sort if true the returned array will be sorted relative to dependencies
    *   otherwise the array will be sorted alphanumerically with respect to the resource
    *   path
    * @return an array of resources matching the selction criteria
    */
    Resource[] select( String criteria, boolean sort );
    
   /**
    * <p>Select a set of resource matching a supplied a resource selection 
    * constraint.  The constraint may contain the wildcards '**' and '*'.
    * @param local if true limit the selection to local projects
    * @param criteria the selection criteria
    * @param sort if true the returned array will be sorted relative to dependencies
    *   otherwise the array will be sorted alphanumerically with respect to the resource
    *   path
    * @return an array of resources matching the selction criteria
    */
    Resource[] select( String criteria, boolean local, boolean sort );
    
   /**
    * Select all local projects with a basedir equal to or depper than the supplied 
    * directory.
    * @param base the reference basedir
    * @return an array of projects within or lower than the supplied basedir
    */
    Resource[] select( File base );
    
   /**
    * Select all local projects relative to the supplied basedir.
    * @param base the reference basedir
    * @param self if true and the basedir resolves to a project then include the project
    *   otherwise the project will be expluded from selection
    * @return an array of projects relative to the basedir
    */
    Resource[] select( File base, boolean self );
    
   /**
    * Locate a resource relative to a base directory.
    * @param base the base directory
    * @return a resource with a matching basedir
    * @exception ResourceNotFoundException if resource match  relative to the supplied base
    */
    Resource locate( File base ) throws ResourceNotFoundException;
    
}

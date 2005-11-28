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

package net.dpml.build.model;

import net.dpml.build.info.ModuleDirective;

/**
 * The Modele interface defines a node within a module hierachy.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Module extends Resource
{
   /**
    * Return an array of immediate resources contained within the
    * module.
    * @return the resource array
    */
    Resource[] getResources();
    
   /**
    * Return a resource using a supplied name.
    * @param ref a path relative to the module
    * @return the resource array
    * @exception ResourceNotFoundException if the ref value is not recognized
    */
    Resource getResource( String ref ) throws ResourceNotFoundException;
    
   /**
    * Return the array of modules that are direct children of this module.
    * @return the child modules
    */
    Module[] getModules();
    
   /**
    * Return the array of modules that are descendants of this module.
    * @return the descendants module array
    */
    Module[] getAllModules();
    
   /**
    * Return a module using a supplied reference.
    * @param ref a path relative to the module
    * @return the module array
    * @exception ModuleNotFoundException if the ref value is not recognized
    */
    Module getModule( String ref ) throws ModuleNotFoundException;
    
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
    * Return a directive suitable for publication as an external description.
    * @return the module directive
    */
    ModuleDirective export();

}

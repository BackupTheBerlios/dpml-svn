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

package net.dpml.tools.model;

import java.io.File;

/**
 * The Library interface is the application root for module management.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public interface Library
{
   /**
    * Return an array of all registered processors models.
    * @return the processor array
    */
    Processor[] getProcessors();
    
   /**
    * Return the sequence of processor definitions supporting production of a 
    * supplied resource.  The implementation constructs a sequence of process
    * instances based on the types declared by the resource combined with 
    * dependencies declared by respective process defintions. Clients may
    * safely invoke processes sequentially relative to the returned process
    * sequence.
    * 
    * @param resource the resource to be produced
    * @return a sorted array of processor definitions supporting resource production
    */
    Processor[] getProcessorSequence( Resource resource ) throws ProcessorNotFoundException;
    
   /**
    * Return the processor defintions matching a supplied type.  
    * 
    * @param type the type declaration
    * @return the processor definition
    * @exception ProcessorNotFoundException if no processor is registered 
    *   for the supplied type
    */
    Processor getProcessor( Type type ) throws ProcessorNotFoundException;
    
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
    * Locate a resource relative to a base directory.
    * @param base the base directory
    * @return a resource with a matching basedir
    * @exception ResourceNotFoundException if resource match  relative to the supplied base
    */
    Resource locate( File base ) throws ResourceNotFoundException;
    
}

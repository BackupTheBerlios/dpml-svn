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

import net.dpml.library.model.Library;
import net.dpml.library.model.Resource;

import org.apache.tools.ant.Project;

/**
 * The Workbench interface exposes the working configuration of the build system.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Workbench
{
   /**
    * Return the common resource library.
    * @return the library
    */
    Library getLibrary();

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
    * @exception ProcessorNotFoundException if a processor referenced by another 
    *   processor as a dependent cannot be resolved
    */ 
    Processor[] getProcessorSequence( Resource resource ) throws ProcessorNotFoundException;
    
   /**
    * Create a project context.
    * @param resource the resource 
    * @param project the current project
    * @return the project context
    */
    Context createContext( Resource resource, Project project );
    
}

/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.library;

import net.dpml.lang.Type;

import net.dpml.library.info.TypeDirective;

/**
 * Interface implemented by type datastructure builders.
 */
public interface TypeBuilder
{
   /**
    * Return the id of the type produced by the builder.
    * @return the type id
    */
    String getID();
    
   /**
    * Construct a type instance using a supplied classloader and type
    * production directive.
    * @param classloader the base classloader
    * @param type the type production directive
    * @return the type instance
    * @exception Exception if an error occurs
    */
    Type buildType( ClassLoader classloader, TypeDirective type ) throws Exception;
}

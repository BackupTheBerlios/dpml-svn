/*
 * Copyright 2006 Stephen McConnell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dpml.http.demo;

/**
 * The set of management operations dynamically exposed by the HTTP demo.
 */
public interface ManagementOperations
{
   /**
    * Add a new http context to the application.
    * @param path the context path
    */
    void addContext( String path ) throws Exception;
    
   /**
    * Remove an http context from the application.
    * @param path the context path
    */
    void removeContext( String path );
    
   /**
    * Strurn the array of context paths.
    * @return the context path array
    */
    String[] getContextPaths();
    
}


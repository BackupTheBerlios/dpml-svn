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

package org.apache.avalon.composition.model;

/**
 * The Resolver interface defines the contract for instance access and 
 * release.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Resolver.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface Resolver
{
    /**
     * Resolve a object to a value.
     *
     * @return the resolved object
     * @throws Exception if an error occurs
     */
    Object resolve() throws Exception;

    /**
     * Resolve a object to a value.
     *
     * @param proxy if TRUE ruturn a proxied reference if the underlying component
     *   suppports proxied representation otherwise return the raw component instance
     * @return the resolved object
     * @throws Exception if an error occurs
     */
    Object resolve( boolean proxy ) throws Exception;

   /**
    * Release an object.
    * 
    * @param instance the object to release
    */
    void release( Object instance );

}

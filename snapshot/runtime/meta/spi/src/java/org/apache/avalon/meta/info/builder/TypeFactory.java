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

package org.apache.avalon.meta.info.builder;

import org.apache.avalon.meta.info.Type;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.parameters.Parameters;

/**
 * Simple interface used to create {@link Type}
 * from stream. This abstraction was primarily created so
 * that the Type could be built from non-XML
 * sources and no XML classes need be in the classpath.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: TypeFactory.java 31046 2004-07-31 17:20:26Z niclas $
 */
public interface TypeFactory extends TypeCreator
{

    /**
     * Create a {@link Type} using a supplied type configuration and default configuration 
     *
     * @param path the class resource name of component type that we are looking up
     * @param xinfo the configuration fragment for the type
     * @param defaults the configuration fragment for the default configuration
     * @return the newly created {@link Type}
     * @exception Exception if an error occurs
     */
    Type createType( String path, Configuration xinfo, Configuration defaults )
        throws Exception;

    /**
     * Create a {@link Type} using a supplied type configuration and default configuration
     *
     * @param path the class resource name of component type that we are looking up
     * @param xinfo the configuration fragment for the type
     * @param defaults the configuration fragment for the default configuration
     * @param params the default parameters
     * @return the newly created {@link Type}
     * @exception Exception if an error occurs
     */
    Type createType( String path, Configuration xinfo, Configuration defaults, Parameters params )
        throws Exception;


}

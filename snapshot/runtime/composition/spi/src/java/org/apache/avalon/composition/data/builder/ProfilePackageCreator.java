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

package org.apache.avalon.composition.data.builder;

import org.apache.avalon.composition.data.ProfilePackage;

/**
 * Interface used to create a {@link ProfilePackage}
 * from an input stream.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ProfilePackageCreator.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface ProfilePackageCreator
{
    /**
     * Create a {@link ProfilePackage} from a class reference.
     *
     * @param clazz the component class
     * @return the profile package
     * @exception Exception if a error occurs during package creation
     */
    ProfilePackage createProfilePackage( String name, Class clazz )
        throws Exception;

}

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

package org.apache.avalon.extension.manager;

import org.apache.avalon.extension.Extension;

/**
 * <p>Interface used to store a collection of "Optional Packages"
 * (formerly known as "Standard Extensions"). It is assumed that each
 * "Optional Package" is represented by a single file on the file system.</p>
 *
 * <p>This repository is responsible for storing the local repository of
 * packages. The method used to locate packages on local filesystem
 * and install packages is not specified.</p>
 *
 * <p>For more information about optional packages, see the document
 * <em>Optional Package Versioning</em> in the documentation bundle for your
 * Java2 Standard Edition package, in file
 * <code>guide/extensions/versioning.html</code></p>.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ExtensionManager.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface ExtensionManager
{
    String ROLE = ExtensionManager.class.getName();

    /**
     * Return all the {@link OptionalPackage}s that satisfy specified
     * {@link Extension}. The array must be sorted with the packages that
     * "best" satisfy the Extension earlier in the array. Note that the
     * definition of "best" is implementation dependent.
     *
     * @param extension Description of the extension that needs to be provided by
     *                  optional packages
     * @return an array of optional packages that satisfy extension and
     *         the extensions dependencies
     * @see OptionalPackage
     * @see Extension
     */
    OptionalPackage[] getOptionalPackages( Extension extension );
}

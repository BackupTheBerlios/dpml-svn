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

package org.apache.avalon.extension.manager.impl.test;

import org.apache.avalon.extension.Extension;
import org.apache.avalon.extension.manager.ExtensionManager;
import org.apache.avalon.extension.manager.OptionalPackage;

/**
 * a class to help test sorting of ExtensionManager.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: TestExtensionManager.java 30977 2004-07-30 08:57:54Z niclas $
 */
class TestExtensionManager
    implements ExtensionManager
{
    private final OptionalPackage[] m_optionalPackages;

    public TestExtensionManager( final OptionalPackage[] optionalPackages )
    {
        m_optionalPackages = optionalPackages;
    }

    public OptionalPackage[] getOptionalPackages( final Extension extension )
    {
        return m_optionalPackages;
    }
}

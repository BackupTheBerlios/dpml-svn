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
 * Exception indicating an extension was not found in Package Repository.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: UnsatisfiedExtensionException.java 30977 2004-07-30 08:57:54Z niclas $
 * @see Extension
 */
public class UnsatisfiedExtensionException
    extends Exception
{
    /**
     * The unsatisfied Extension.
     */
    private final Extension m_extension;

    /**
     * Construct the <code>UnsatisfiedPackageException</code>
     * for specified {@link Extension}.
     *
     * @param extension the extension that caused exception
     */
    public UnsatisfiedExtensionException( final Extension extension )
    {
        if( null == extension )
        {
            throw new NullPointerException( "extension" );
        }

        m_extension = extension;
    }

    /**
     * Return the unsatisfied {@link Extension} that
     * caused this exception tho be thrown.
     *
     * @return the unsatisfied Extension
     */
    public Extension getUnsatisfiedExtension()
    {
        return m_extension;
    }
}

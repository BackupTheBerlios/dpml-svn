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

package org.apache.avalon.meta.info.locator;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import org.apache.avalon.meta.info.Type;

/**
 * ResourceLocator provides an interface for the readers and writers to
 * get i9nput and output streams relative to a type.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ResourceLocator.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface ResourceLocator
{

    /**
     * Get the required stream based on type and resource identifier.  
     *
     * @param type  The type defintion
     * @param key  The resource identifier
     * @return the input stream
     * @throws MetaInfoException if the stream does not exist or there is a problem obtaining it.
     */
    InputStream getInputStream( Type type, String key ) throws IOException;

    /**
     * Get the required stream based on type and resource identifier. 
     *
     * @param type  The type defintion
     * @param key  The resource identifier
     * @return the output stream
     * @throws MetaInfoException if there is a problem obtaining it.
     */
    OutputStream getOutputStream( Type type, String key ) throws IOException;
}

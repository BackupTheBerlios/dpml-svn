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

package org.apache.avalon.meta.info.writer;

import java.io.OutputStream;

import org.apache.avalon.meta.info.Type;

/**
 * Interface implemented by objects supporting the writing
 * of a {@link Type} to an output stream.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: TypeWriter.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface TypeWriter
{
    /**
     * Write a {@link Type} to a stream
     *
     * @param type the meta info Type instance
     * @param stream the destination stream
     * @throws Exception if an error occurs while writting to the stream
     */
    void writeType( Type type, OutputStream stream )
        throws Exception;
}

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

import java.io.InputStream;
import org.apache.avalon.meta.info.Type;

/**
 * Simple interface used to create {@link Type}
 * from stream. This abstraction was primarily created so
 * that the Type could be built from non-XML
 * sources and no XML classes need be in the classpath.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: TypeCreator.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface TypeCreator
{
    /**
     * Create a {@link Type} from stream
     *
     * @param key the name of component type that we are looking up
     * @param input the input stream that the resource is loaded from
     * @return the newly created {@link Type}
     * @exception Exception if an error occurs
     */
    Type createType( String key, InputStream input )
        throws Exception;

}

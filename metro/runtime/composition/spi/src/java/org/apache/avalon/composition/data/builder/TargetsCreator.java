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

import java.io.InputStream;
import org.apache.avalon.composition.data.Targets;

/**
 * Simple interface used to create a {@link Targets}
 * from an input stream.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: TargetsCreator.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface TargetsCreator
{
    /**
     * Create a {@link Targets} instance from a stream.
     *
     * @param inputStream the stream that the target is loaded from
     * @return the target directive collection
     * @exception Exception if a error occurs during directive creation
     */
    Targets createTargets( InputStream inputStream )
        throws Exception;

}

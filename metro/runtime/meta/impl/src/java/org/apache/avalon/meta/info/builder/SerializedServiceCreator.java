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
import java.io.ObjectInputStream;
import org.apache.avalon.meta.info.Service;

/**
 * Create {@link Service} from stream made up of a serialized object.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SerializedServiceCreator.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class SerializedServiceCreator
    implements ServiceCreator
{

    /**
     * Create of a service instance from a serialized form.
     * @param key parameter not used
     * @param input the input stream
     * @return the meta-info instance that describes the component type
     * @exception Exception if an error occurs
     */
    public Service createService( final String key, final InputStream input )
        throws Exception
    {
        final ObjectInputStream stream = new ObjectInputStream( input );
        return (Service)stream.readObject();
    }
}

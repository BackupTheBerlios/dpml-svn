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

package org.apache.avalon.composition.data.writer;

import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.avalon.composition.data.ContainmentProfile;


/**
 * Write {@link ContainmentProfile} objects to a stream as serialized objects.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SerializedContainmentProfileWriter.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class SerializedContainmentProfileWriter
    implements ContainmentProfileWriter
{
    /**
     * Write a {@link ContainmentProfile} to a stream
     *
     * @param profile the profile instance
     * @param stream the destination stream
     * @throws Exception if an error occurs while writting to the stream
     */
    public void writeContainmentProfile( 
      ContainmentProfile profile, OutputStream stream )
        throws Exception
    {
        final ObjectOutputStream output =
          new ObjectOutputStream( stream );
        output.writeObject( profile );
        output.flush();
    }
}

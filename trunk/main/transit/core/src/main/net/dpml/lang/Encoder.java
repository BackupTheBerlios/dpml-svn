/*
 * Copyright 2006 Stephen J. McConnell.
 *
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

package net.dpml.lang;

import java.io.Writer;
import java.io.IOException;

/**
 * Interface implemented by generic encoders.
 */
public interface Encoder
{
   /**
    * Externalize a object to XML.
    * @param writer the output stream writer
    * @param object the object to externalize
    * @param pad the character offset
    * @exception IOException if an IO error occurs
    */
    public void encode( Writer writer, Object object, String pad ) throws IOException;
}

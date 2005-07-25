/*
 * Copyright 2005 Stephen McConnell
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

package net.dpml.transit;

import java.io.IOException;
import java.net.ContentHandler;

/**
 * A interface supporting access to pluggable content handlers.
 */
public interface ContentRegistry
{
   /**
    * Return a content handler capable for supporting the supplied type. If
    * the a handler is available the handler is returned otherwise the returned
    * value is null.
    *
    * @param type the artifact type
    * @return the content handler or null if not available
    */
    ContentHandler getContentHandler( final String type ) throws IOException;
}


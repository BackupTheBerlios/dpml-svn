/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.runtime;

import java.io.IOException;

import net.dpml.lang.Buffer;

import org.w3c.dom.Element;

/**
 * Internal utility interfaces.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
abstract class Directive
{
   /**
    * Internal interface for encodable entries.
    */
    public interface Encodable
    {
        Element getElement();
        
        void encode( Buffer buffer, String key ) throws IOException;
    }
    
   /**
    * Internal interface for resolvable entries.
    */
    public interface Resolvable extends Encodable
    {
        <T>T resolve( ComponentStrategy parent, Class<T> clazz ) throws Exception;
    }
}

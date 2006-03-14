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

package net.dpml.library;

import java.io.IOException;
import java.io.Writer;

import org.w3c.dom.Element;

import net.dpml.lang.Type;
import net.dpml.lang.Builder;

import net.dpml.library.info.TypeDirective;

/**
 * Construct an Strategy instance from a DOM Element.
 */
public interface TypeBuilder //extends Builder
{
    String getID();
    
    Type buildType( ClassLoader classoader, TypeDirective type ) throws Exception;
    
    //void write( Writer writer, TypeDirective type ) throws IOException;
}

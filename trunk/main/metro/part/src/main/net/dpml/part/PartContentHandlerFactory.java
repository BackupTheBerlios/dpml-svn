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

package net.dpml.part;

import java.io.IOException;
import java.net.URLConnection;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;

/**
 * A utility class supporting the establishment of a part content handler factory.
 * This class is typically used in testcase as a convinience mechanism when dealing
 * with the establishment of component instances from parts.
 */
public class PartContentHandlerFactory implements ContentHandlerFactory
{
    public ContentHandler createContentHandler( String mimetype )
    {
        return new PartContentHandler();
    }
}

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

import net.dpml.lang.UnknownKeyException;

/**
 * A interface supporting access to pluggable content handlers.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface LayoutRegistry
{
   /**
    * Constrant layout plugin key.
    */
    public static final String LAYOUT_HANDLER_PLUGIN_KEY = "dpml.transit.layout.plugin";

   /**
    * Return a location resolver capable for supporting the supplied id. If
    * a handler is available the handler is returned otherwise the returned
    * value is null.
    *
    * @param id the layout identifier
    * @return the location resolver or null if not available
    * @exception UnknownKeyException if the key is not recognized
    * @exception IOException if an IO error occurs
    */
    Layout getLayout( final String id ) throws UnknownKeyException, IOException;
}

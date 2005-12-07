/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.metro.part;

import java.io.Serializable;
import java.net.URI;

/**
 * The common interface implemented by all parts.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Part extends Serializable
{
   /**
    * The constant part type identifier.
    */
    static final String ARTIFACT_TYPE = "part";

   /**
    * Static default controller.
    */
    //static final Controller CONTROLLER = PartContentHandler.CONTROLLER;
    
   /**
    * Return the part handler uri.
    * @return the uri of the part controller
    */
    URI getPartHandlerURI();
    
}


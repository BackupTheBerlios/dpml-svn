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

package net.dpml.part.control;

import java.net.URI;

/**
 * Exception indicating an controller related error.  A controller exception
 * delcares the URI of the controller form which the exception was initiated.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ControlException extends Exception 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final URI m_uri;

   /**
    * Creation of a new ControlException.
    *
    * @param controller the uri identifying the controller iniating the exception
    */
    public ControlException( URI controller )
    {
        this( controller, null );
    }

   /**
    * Creation of a new ControlException.
    *
    * @param controller the uri identifying the controller iniating the exception
    * @param message the description of the exception 
    */
    public ControlException( URI controller, String message )
    {
        this( controller, message, null );
    }

   /**
    * Creation of a new ControlException.
    *
    * @param controller the uri identifying the controller iniating the exception
    * @param message the description of the exception 
    * @param cause the causal exception
    */
    public ControlException( URI controller, String message, Throwable cause )
    {
        super( message, cause );
        m_uri = controller;
    }

   /**
    * Return the uri of the controller initiating this exception.
    * @return the controller uri
    */
    public URI getControlURI()
    {
        return m_uri;
    }
}


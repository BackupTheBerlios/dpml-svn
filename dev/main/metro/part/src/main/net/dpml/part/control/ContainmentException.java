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

/**
 * Exception raised by a container.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ContainmentException extends Exception 
{
   /**
    * Creation of a new containment exception.
    * @param message the excetion message
    */
    public ContainmentException( String message )
    {
        this( message, null );
    }

   /**
    * Creation of a new containment exception.
    * @param message the exception message
    * @param cause the causal exception
    */
    public ContainmentException( String message, Throwable cause )
    {
        super( message, cause );
    }

}


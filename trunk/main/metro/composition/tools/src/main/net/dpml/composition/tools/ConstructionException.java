/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.metro.runtime.tools;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;

/**
 * A construction exception indicates a generation phase error in the construction
 * of a component part.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class ConstructionException extends BuildException
{
   /**
    * Creation of a new construction exception.
    * @param message the exception message
    */
    public ConstructionException( String message )
    {
        super( message );
    }

   /**
    * Creation of a new construction exception.
    * @param message the exception message
    * @param cause the causal exception
    */
    public ConstructionException( String message, Throwable cause )
    {
        super( message, cause );
    }

   /**
    * Creation of a new construction exception.
    * @param message the exception message
    * @param location the location of the error
    */
    public ConstructionException( String message, Location location )
    {
        super( message, location );
    }

   /**
    * Creation of a new construction exception.
    * @param message the exception message
    * @param cause the causal exception
    * @param location location of the task 
    */
    public ConstructionException( String message, Throwable cause, Location location )
    {
        super( message, cause, location );
    }
}

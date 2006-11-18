/*
 * Copyright 2006 Stephen J. McConnell
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

package net.dpml.tools;

import org.apache.tools.ant.Location;

/**
 * A BuildError is thrown when a fatal error occurs during build execution.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class BuildError extends Error
{
    private final Location m_location;
    
   /**
    * Creation of a new BuildError.
    * @param message the exception message
    */
    public BuildError( String message )
    {
        this( message, null );
    }
    
   /**
    * Creation of a new BuildError.
    * @param message the exception message
    * @param cause the causal exception
    */
    public BuildError( String message, Throwable cause )
    {
        this( message, cause, null );
    }
    
   /**
    * Creation of a new BuildError.
    * @param message the exception message
    * @param cause the causal exception
    * @param location the location of the error
    */
    public BuildError( String message, Throwable cause, Location location )
    {
        super( message, cause );
        
        m_location = location;
    }
    
   /**
    * Return the location of the build error.
    * @return the build location
    */
    public Location getLocation()
    {
        return m_location;
    }
}

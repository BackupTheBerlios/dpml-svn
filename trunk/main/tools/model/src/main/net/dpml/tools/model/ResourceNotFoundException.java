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

package net.dpml.tools.model;

/**
 * A ResourceNotFoundException is thrown when a requested for a named resource cannot
 * be resolved.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ResourceNotFoundException extends Exception
{
    public ResourceNotFoundException( String message )
    {
        this( message, null );
    }
    
    public ResourceNotFoundException( String message, Throwable cause )
    {
        super( message, cause );
    }
}

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

package net.dpml.tools.info;

/**
 * A TypeUnknownException is raised in response to a request for a type
 * that does not exist.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class TypeUnknownException extends Exception
{
    public TypeUnknownException( String message )
    {
        this( message, null );
    }
    
    public TypeUnknownException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
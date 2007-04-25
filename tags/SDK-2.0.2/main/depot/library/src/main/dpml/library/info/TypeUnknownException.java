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

package dpml.library.info;

/**
 * A TypeUnknownException is raised in response to a request for a type
 * that does not exist.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class TypeUnknownException extends Exception
{
   /**
    * Creation of a new TypeUnknownException.
    * @param message the exception message
    */
    public TypeUnknownException( String message )
    {
        this( message, null );
    }
    
   /**
    * Creation of a new TypeUnknownException.
    * @param message the exception message
    * @param cause the causal excetion
    */
    public TypeUnknownException( String message, Throwable cause )
    {
        super( message, cause );
    }
}

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
 * A TypeNotFoundException is thrown when a requested for a named type cannot
 * be resolved relative to the enclosing object.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class TypeNotFoundException extends Exception
{
   /**
    * Creation of a new TypeNotFoundException.
    * @param message the exception message
    */
    public TypeNotFoundException( String message )
    {
        this( message, null );
    }
    
   /**
    * Creation of a new TypeNotFoundException.
    * @param message the exception message
    * @param cause the causal excetion
    */
    public TypeNotFoundException( String message, Throwable cause )
    {
        super( message, cause );
    }
}

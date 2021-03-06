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

package net.dpml.tools.tasks;

/**
 * A ProcessorInstantiationException if an occur occurs when attempting to load 
 * a build listener based on a processor definition.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ProcessorInstantiationException extends Exception
{
   /**
    * Creation of a new ProcessorInstantiationException.
    * @param message the exception message
    */
    public ProcessorInstantiationException( String message )
    {
        this( message, null );
    }
    
   /**
    * Creation of a new ProcessorInstantiationException.
    * @param message the exception message
    * @param cause the causal excetion
    */
    public ProcessorInstantiationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}

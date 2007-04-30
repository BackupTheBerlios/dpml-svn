/*
 * Copyright 2005 Stephen J. McConnell.
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

package dpml.lang;

/**
 * Exception throw in the event of a DOM parse fatal condition.
 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DocumentRuntimeException extends RuntimeException
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    /**
     * Construct a new <code>ValueRuntimeException</code> instance.
     *
     * @param message the exception message
     */
    public DocumentRuntimeException( final String message )
    {
        super( message );
    }

    /**
     * Construct a new <code>ValueRuntimeException</code> instance.
     *
     * @param message the exception message
     * @param cause the causal exception
     */
    public DocumentRuntimeException( final String message, Throwable cause )
    {
        super( message, cause );
    }
}


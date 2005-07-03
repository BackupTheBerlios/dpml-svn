/* 
 * Copyright 2004 Stephen J. McConnell.
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
 * Exception to indicate that there was a lifecycle related error.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: LifecycleException.java 259 2004-10-30 07:24:40Z mcconnell $
 */
public class LifecycleException extends Exception
{
    /**
     * Construct a new <code>LifecycleException</code> instance.
     *
     * @param message the exception message
     */
    public LifecycleException( final String message )
    {
        this( message, null );
    }

    /**
     * Construct a new <code>LifecycleException</code> instance.
     *
     * @param message the exception message
     * @param throwable the root cause of the exception
     */
    public LifecycleException( final String message, final Throwable throwable )
    {
        super( null, throwable );
    }
}


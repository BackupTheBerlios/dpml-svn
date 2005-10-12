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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A ModelException is thrown is a general error occurs during model creation.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ModelRuntimeException extends RuntimeException
{
    public ModelRuntimeException( String message )
    {
        this( message, null );
    }
    
    public ModelRuntimeException( String message, Throwable cause )
    {
        super( message, cause );
    }
}

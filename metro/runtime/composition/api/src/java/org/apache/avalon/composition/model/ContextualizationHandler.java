/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.composition.model;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;

/**
 * Definition of an extension handler that handles the Contextualize
 * stage of a component lifecycle.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public interface ContextualizationHandler
{
    /**
     * Handle the contextualization stage of a component lifecycle.
     *
     * @param context the context to apply
     * @param object the object to contextualize
     * @exception ContextException if a contextualization error occurs
     */
    void contextualize( Object object, Context context ) throws ContextException;
}

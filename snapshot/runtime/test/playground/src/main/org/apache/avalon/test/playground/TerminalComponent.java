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

package org.apache.avalon.test.playground;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.avalon.test.playground.basic.BasicService;

/**
 * This is a minimal demonstration component that provides BasicService
 * and has no dependencies
 *
 * @avalon.component name="terminal"
 * @avalon.service type="org.apache.avalon.test.playground.basic.BasicService"
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class TerminalComponent extends AbstractLogEnabled
        implements BasicService, Disposable
{

    //=======================================================================
    // BasicService
    //=======================================================================

    /**
     * Does something trivial.
     */
    public void doPrimeObjective()
    {
        getLogger().info( "hello from TerminalComponent" );
    }

    /**
     * Disposal of the componet.
     */
    public void dispose()
    {
        getLogger().debug( "dispose" );
    }

}

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

package org.apache.avalon.test.testb;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * @avalon.component name="component-b" 
 *    type="org.apache.avalon.test.testb.B"
 *    lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.test.testb.B"
 */
public class TestB extends AbstractLogEnabled
  implements Initializable, B
{
    public void initialize() throws Exception
    {
        getLogger().info( "hello from B" );
    }
}

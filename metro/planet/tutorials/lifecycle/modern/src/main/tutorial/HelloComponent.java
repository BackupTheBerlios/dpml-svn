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

package tutorial;

import java.io.File;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;

/**
 * Component demonstrating access to standard context entries.
 * @avalon.component name="demo" lifestyle="singleton" version="1.0"
 * @avalon.stage id="urn:demo:demonstratable"
 */
public class HelloComponent extends AbstractLogEnabled 
  implements Demonstratable
{

    /**
     * Do something or other.
     * @param stage the stage being applied (as a string)
     */
    public void demo( String stage )
    {
        getLogger().info( "extension said: " + stage );
    }

}

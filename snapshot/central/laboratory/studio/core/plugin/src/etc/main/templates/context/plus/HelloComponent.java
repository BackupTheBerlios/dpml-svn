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

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;

/**
 * Component demonstrating access to standard context entries.
 * @avalon.component name="demo" lifestyle="singleton"
 */
public class HelloComponent 
{
    //------------------------------------------------------
    // immutable state
    //------------------------------------------------------

    private final Logger m_logger;
    private final File m_home;

   /**
    * Creation of a new HelloComponent instance using a 
    * container supplied logging channel and custom context.
    * The context supplied by the container holds the 
    * standard context entries requested under the avalon.entry
    * tags.  The advantages of this approach is the isolation
    * of context entry casting and key into a seperate context 
    * helper class.
    *
    * @avalon.context type="tutorial.DemoContext"
    * @avalon.entry key="urn:avalon:home" type="java.io.File"
    */
    public HelloComponent( Logger logger, DemoContext context )
    {
        m_logger = logger;

        m_home = context.getWorkingDirectory();

        m_logger.info( "working directory set to: " + m_home );
    }
}

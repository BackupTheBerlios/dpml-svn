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

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;

/**
 * Component implementation that demonstrates type safe casting of a supplied 
 * context object.
 *
 * @avalon.component name="demo" lifestyle="singleton"
 */
public class HelloComponent extends AbstractLogEnabled 
    implements Contextualizable
{

   /**
    * Contextualization of the component using a context
    * class that implements a domain specific context interface.
    *
    * @avalon.context type="tutorial.DemoContext"
    * @avalon.entry key="urn:avalon:name"
    * @avalon.entry key="urn:avalon:partition"
    * @avalon.entry key="urn:avalon:home" type="java.io.File"
    * @avalon.entry key="urn:avalon:temp" type="java.io.File"
    */
    public void contextualize( Context context )
      throws ContextException
    {
        DemoContext c = (DemoContext) context;
        getLogger().info( "listing values resolved from domain specific context" );
        getLogger().info( "supplied context class: " + context.getClass().getName() );
        getLogger().info( "name: " + c.getName() );
        getLogger().info( "partition: " + c.getPartition() );
        getLogger().info( "home: " + c.getHomeDirectory() );
        getLogger().info( "temp: " + c.getWorkingDirectory() );
    }
}

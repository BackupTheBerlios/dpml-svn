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

package org.apache.avalon.test.testa;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * @avalon.component name="component-a" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.test.testa.A"
 */
public class TestA extends AbstractLogEnabled
  implements Contextualizable, Initializable, A
{
   /**
    * @avalon.content type="org.apache.avalon.test.testa.Facade"
    * @avalon.entry key="urn:avalon:partition"
    * @avalon.entry key="urn:avalon:classloader" type="java.lang.ClassLoader"
    * @avalon.entry key="urn:avalon:home" type="java.io.File"
    * @avalon.entry key="urn:avalon:temp" type="java.io.File"
    * @avalon.entry key="urn:avalon:name" alias="name"
    * @avalon.entry key="home" type="java.io.File"
    * @avalon.entry key="time" type="java.util.Date" volatile="true"
    * @avalon.entry key="path"
    */
    public void contextualize( Context context ) throws ContextException
    {
        getLogger().info( "name: " + context.get( "urn:avalon:name" ) );
        getLogger().info( "partition: " + context.get( "urn:avalon:partition" ) );
        getLogger().info( "classloader: " + context.get( "urn:avalon:classloader" ) );
        getLogger().info( "work: " + context.get( "urn:avalon:home" ) );
        getLogger().info( "temp: " + context.get( "urn:avalon:temp" ) );
    }

    public void initialize() throws Exception
    {
        getLogger().info( "hello" );
    }
}

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
 * Demonstration of a component that uses a constructed context entry.
 * 
 * @avalon.component name="demo" lifestyle="singleton"
 */
public class HelloComponent extends AbstractLogEnabled 
    implements Contextualizable
{

   /**
    * Contextualization of the component by the container.
    * The context supplied by the container shall contain
    * a NumberCruncher instance as declared in the xinfo resource.
    *
    * @avalon.context
    * @avalon.entry key="cruncher" type="tutorial.NumberCruncher"
    */
    public void contextualize( Context context )
      throws ContextException
    {
        NumberCruncher cruncher = 
          (NumberCruncher) context.get( "cruncher" );
        float value = cruncher.crunch();
        getLogger().info( "result: " + value );
    }
}

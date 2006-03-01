/*
 * Copyright 2006 Stephen J. McConnell.
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

package dpmlx.component;

import java.net.URI;

import dpmlx.lang.Strategy;
import dpmlx.lang.StrategyBuilder;

import org.w3c.dom.Element;

/**
 * Component part handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentStrategyBuilder implements StrategyBuilder
{
   /**
    * Constructs a component deployment strategy.
    *
    * @return the deployment strategy
    * @exception Exception if an error occurs
    */
    public Strategy buildStrategy( Element element ) throws Exception
    {
        return new ComponentStrategy( "Hello World" );
    }
}

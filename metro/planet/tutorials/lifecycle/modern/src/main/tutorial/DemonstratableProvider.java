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

import org.apache.avalon.composition.model.LifecycleCreateExtension;
import org.apache.avalon.composition.model.ComponentModel;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.avalon.meta.info.StageDescriptor;

/**
 * Definition of an extension type that logs messages related to
 * all lifestyle stages.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @avalon.component name="extension" version="1.0" lifestyle="singleton"
 * @avalon.extension id="urn:demo:demonstratable"
 */
public class DemonstratableProvider extends AbstractLogEnabled
        implements LifecycleCreateExtension
{

    //=======================================================================
    // LifecycleCreateExtension
    //=======================================================================

    /**
     * Invocation of the deployment creation stage extension.
     * @param model the model representing the object under deployment
     * @param stage the extension stage descriptor
     * @param object the object under deployment
     * @exception if a deployment error occurs
     */
     public void create( ComponentModel model, StageDescriptor stage, Object object )
       throws Exception
     {
         getLogger().info( "invoking create on target: " + model );
         if( object instanceof Demonstratable )
         {
            ((Demonstratable)object).demo( "creator id: " + System.identityHashCode( this ) );
         }
     }
}

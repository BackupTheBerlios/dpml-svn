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

import java.lang.reflect.Constructor;
import java.util.Map;

import org.apache.avalon.composition.model.ContextualizationHandler;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Context;

/**
 * Definition of an extension handler that handles the Expoitable
 * extension stage interface.
 *
 * @avalon.component name="demo"
 * @avalon.extension id="tutorial.Contextualizable"
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class DemoContextualizationHandler 
    implements ContextualizationHandler
{
    //=======================================================================
    // Extension
    //=======================================================================

    /**
     * Handle the contextualization stage of a component lifecycle.
     * @param object the object to contextualize
     * @param context the component context argument
     * @exception ContextException if a contextualization error occurs
     */
    public void contextualize( Object object, Context context )
      throws ContextException
    {

        //
        // based on the supplied context directives, the container supplied 
        // map of base context entries and a classloader, build and apply
        // a context object to the supplied target object
        //

        if( object instanceof Contextualizable )
        {
            StandardContext standard = new StandardContextImp( context );
            ( (Contextualizable)object ).contextualize( standard );
        }
        else
        {
            final String error =
              "Target object does not implement the "
              + Contextualizable.class.getName() + " interface.";
            throw new ContextException( error );
        }
    }
}

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

import java.util.Map;
import java.io.File;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.context.ContextException;


/**
 * A demonstration class that that we will instantiate via 
 * context directives within the component declaration.
 */
public class DemoContextProvider extends DefaultContext 
    implements DemoContext
{

   /**
    * A custom context type implementation must provide
    * the following constructor.
    * @param entries a map of context entries
    */
    public DemoContextProvider( Context context )
    {
        super( context );
    }
 
   /**
    * Return the working directory.
    * @return the directory
    */
    public File getWorkingDirectory()
    {
        try
        {
            return (File) super.get( "urn:avalon:home" );
        }
        catch( ContextException ce )
        {
            // should not happen 
            throw new RuntimeException( ce.toString() );
        }
    }
}

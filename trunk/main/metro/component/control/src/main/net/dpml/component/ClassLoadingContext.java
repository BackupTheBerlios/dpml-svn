/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.component;

/**
 * Interfact implemented by local components through which a classloader 
 * may be exposed to the managing controller.  Typically a composite component
 * implementation will implement this interface and expose it's classloader 
 * to a controller enabling the controller to build new classloaders relative 
 * the exposed classloader.  A component implementing this interface may choose
 * to restrict the exposure of internals by returning an appropriate API 
 * classloader.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public interface ClassLoadingContext
{
   /**
    * Return the classloader that is to be used for construction of 
    * subsidiary classloaders.
    * @return the anchor classloader
    */
    ClassLoader getClassLoader();
}

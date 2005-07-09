/*
 * Copyright 2004 Niclas Hedhman.
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

package net.dpml.transit.tools;

import org.apache.tools.ant.Target;
import org.apache.tools.ant.BuildException;

/** The factory interface for creation of Ant targets to be used in Magic.
 * <p>
 * Additional semantic contract is that the TargetFactory takes two
 * arguments in its constructor, the first being of type org.apache.tools.ant.Project
 * and the second of type java.net.URI of the artifact URI.
 * </p>
 */
public interface TargetFactory
{
   /**
    * Creation of new targets.
    * @return an array of target instances that this factory can build.
    * @exception BuildException if the Factory is unable to build the requested target.
    */
    Target[] createTargets() throws BuildException;
}

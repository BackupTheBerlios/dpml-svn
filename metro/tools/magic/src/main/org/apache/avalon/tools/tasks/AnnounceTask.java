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

package org.apache.avalon.tools.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;


/**
 * Announce the initiation of a build.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class AnnounceTask extends ContextualTask
{
    public static final String BANNER = 
      "------------------------------------------------------------------------";

    public void execute() throws BuildException 
    {
        final Project project = getProject();
        final String name = project.getProperty( "project.name" );
        project.log( BANNER );
        project.log( "name: " + name );
        project.log( BANNER );
    }
}

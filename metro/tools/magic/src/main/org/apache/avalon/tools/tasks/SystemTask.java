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

import org.apache.avalon.tools.model.Home;
import org.apache.tools.ant.BuildException;

/**
 * Load a goal. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class SystemTask extends ContextualTask
{
    private Home m_home;

   /**
    * Set the home ref id to the system home.  If not supplied the 
    * default system home 'project.home' id will apply.
    *
    * @param id a home id
    */
    public void setRefid( final String id )
    {
        m_home = getHomeFromReference( id );
    }

    public Home getHome()
    {
        if( null == m_home ) 
        {
            m_home = getHomeFromReference( Home.KEY );
        }
        return m_home;
    }

    private Home getHomeFromReference( final String id )
    {
        final Object object = getProject().getReference( id );
        if( null == object )
        {
            final String error = 
              "System home ref id '" + id 
              + "' declared or implied in task [" 
              + getTaskName() 
              + "] is unknown with the project ["
              + getProject().getName() 
              + "].";
            throw new BuildException( error );
        }
        if( object instanceof Home )
        {
            return (Home) object;
        }
        else
        {
            final String error = 
              "System home ref id '" + id 
              + "' declared or implied in task [" 
              + getTaskName() 
              + "] in the project ["
              + getProject().getName() 
              + "] references a object that is not a system home.";
            throw new BuildException( error );
        }
    }
}

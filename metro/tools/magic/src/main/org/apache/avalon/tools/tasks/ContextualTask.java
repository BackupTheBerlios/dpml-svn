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

import org.apache.avalon.tools.model.Context;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.Property;

import java.io.File;

/**
 * Load a goal. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public abstract class ContextualTask extends Task
{
    public static final String USER_PROPERTIES = "user.properties";
    public static final String MODULE_PROPERTIES = "module.properties";

    private boolean m_init = false;

    public void init() throws BuildException 
    {
        if( !isInitialized() )
        {
            m_init = true;
        }
    }

    public boolean isInitialized()
    {
        return m_init;
    }

    public String getKey()
    {
        return getContext().getKey();
    }

    public Context getContext()
    {
        final Context context = (Context) 
          getProject().getReference( Context.KEY );

        if( null == context )
        {
            final Project project = getProject();
            setupProperties( project, project.getBaseDir() );
            return Context.getContext( project );
        }
        else
        {
            return context;
        }
    }

    public void mkDir( final File dir )
    {
        final Mkdir mkdir = (Mkdir) getProject().createTask( "mkdir" );
        mkdir.setTaskName( getTaskName() );
        mkdir.setDir( dir );
        mkdir.init();
        mkdir.execute();
    }

    protected void setupProperties( final Project project, final File dir )
    {
        final File user = Context.getFile( dir, USER_PROPERTIES );
        loadProperties( project, user );

        final File build = Context.getFile( dir, MODULE_PROPERTIES );
        loadProperties( project, build );
    }

    protected void loadProperties( 
      final Project project, final File file ) throws BuildException
    {
        final Property props = (Property) project.createTask( "property" );
        props.setTaskName( getTaskName() );
        props.init();
        props.setFile( file );
        props.execute();
    }
}

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

package org.apache.avalon.meta.info.ant;

import java.io.File;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

/**
 * An abstract build listener. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: MetaBuildListener.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class MetaBuildListener implements BuildListener
{
    private static final String PREPARE_TASK_KEY = 
      "antlib:org.apache.avalon.tools:prepare";

    private static final String META_TASK_KEY = 
      "plugin:avalon/meta/avalon-meta-tools:meta";

    private final String m_uri;

    private boolean m_flag = false;

    public MetaBuildListener( String uri )
    {
        m_uri = uri;
    }

    /**
     * Signals that a build has started. This event
     * is fired before any targets have started.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     */
    public void buildStarted(BuildEvent event)
    {
        // will not happen
    }

    /**
     * Signals that the last target has finished. This event
     * will still be fired if an error occurred during the build.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     *
     * @see BuildEvent#getException()
     */
    public void buildFinished(BuildEvent event)
    {
    }

    /**
     * Signals that a target is starting.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     *
     * @see BuildEvent#getTarget()
     */
    public void targetStarted(BuildEvent event)
    {
    }

    /**
     * Signals that a target has finished. This event will
     * still be fired if an error occurred during the build.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     *
     * @see BuildEvent#getException()
     */
    public void targetFinished(BuildEvent event)
    {
    }

    /**
     * Signals that a task is starting.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     *
     * @see BuildEvent#getTask()
     */
    public void taskStarted(BuildEvent event)
    {
    }

    /**
     * Signals that a task has finished. This event will still
     * be fired if an error occurred during the build.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     *
     * @see BuildEvent#getException()
     */
    public void taskFinished(BuildEvent event)
    {
        if( m_flag ) return;

        String type = event.getTask().getTaskType();
        if( PREPARE_TASK_KEY.equals( type ) )
        {
            Project project = event.getProject();
            generateForMain( project );
            generateForTest( project );
            m_flag = true;
        }
    }

    private void generateForMain( Project project )
    {
        File basedir = project.getBaseDir();
        File main = new File( basedir, "target/build/main" );
        if( main.exists() )
        {
            File classes = new File( basedir, "target/classes" );
            classes.mkdirs();
            MetaTask meta = createMetaTask( project );
            meta.setDestDir( classes );
            FileSet fileset = new FileSet();
            fileset.setDir( main );
            fileset.setIncludes( "**/*.java" );
            meta.addConfigured( fileset );
            meta.execute();
        }
    }

    private void generateForTest( Project project )
    {
        File basedir = project.getBaseDir();
        File test = new File( basedir, "target/build/test" );
        if( test.exists() )
        {
            File classes = new File( basedir, "target/test-classes" );
            classes.mkdirs();
            MetaTask meta = createMetaTask( project );
            meta.setDestDir( classes );
            FileSet fileset = new FileSet();
            fileset.setDir( test );
            fileset.setIncludes( "**/*.java" );
            meta.addConfigured( fileset );
            meta.execute();
        }
    }

    private MetaTask createMetaTask( Project project )
    {
        MetaTask meta = new MetaTask();
        meta.setProject( project );
        meta.setTaskName( "meta" );
        meta.init();
        return meta;
    }

    /**
     * Signals a message logging event.
     *
     * @param event An event with any relevant extra information.
     *              Must not be <code>null</code>.
     *
     * @see BuildEvent#getMessage()
     * @see BuildEvent#getPriority()
     */
    public void messageLogged(BuildEvent event)
    {
    }   
}

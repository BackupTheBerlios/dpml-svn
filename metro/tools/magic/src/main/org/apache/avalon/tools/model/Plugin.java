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

package org.apache.avalon.tools.model;

import java.io.File;

/**
 * Defintion of plugin. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class Plugin extends Definition
{
    private final TaskDef[] m_tasks;
    private final ListenerDef[] m_listeners;

    public Plugin(
      final Home home, final String key, final File basedir, final String path, final Info info,
      final Gump gump, final ResourceRef[] resources, final ResourceRef[] plugins,
      final TaskDef[] tasks, final ListenerDef[] listeners )
    {
        super( home, key, basedir, path, info, gump, resources, plugins );
        m_tasks = tasks;
        m_listeners = listeners;
    }

    public TaskDef[] getTaskDefs()
    {
        return m_tasks;
    }

    public ListenerDef[] getListenerDefs()
    {
        return m_listeners;
    }

    public boolean equals( final Object other )
    {
        if( super.equals( other ) && ( other instanceof Plugin ))
        {
            return true;
        }
        return false;
    }

    public int hashCode()
    {
        return super.hashCode();  // NH: Is this really correct?
    }
        
    
    public static class AbstractDef
    {
        private String m_classname;
        
        public AbstractDef( final String classname )
        {
            m_classname = classname;
        }

        public String getClassname()
        {
            return m_classname;
        }
    }


    public static class TaskDef extends AbstractDef
    {
        private String m_name;
        
        public TaskDef( final String name, final String classname )
        {
            super( classname );
            m_name = name;
        }

        public String getName()
        {
            return m_name;
        }
    }

    public static class ListenerDef extends AbstractDef
    {
        public ListenerDef( final String classname )
        {
            super( classname );
        }
    }

}

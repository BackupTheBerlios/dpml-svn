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

import java.io.File;
import java.util.EventObject;

/**
 * The ControllerContextEvent is an abstract base class for working and 
 * temporty directory change notification events.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class ControllerContextEvent extends EventObject
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private ControllerContext m_context;
    private File m_old;
    private File m_new;

   /**
    * Creation of a new controller context event object.
    *
    * @param source the controller context initiating the event
    * @param oldDir the original directory value
    * @param newDir the new directory value
    */
    public ControllerContextEvent( ControllerContext source, File oldDir, File newDir )
    {
        super( source );
        m_context = source;
        m_old = oldDir;
        m_new = newDir;
    }

   /**
    * Return the controller context that this event is related to.
    * @return the controller context
    */
    public ControllerContext getContext()
    {
        return m_context;
    }

   /**
    * Return the old directory value.
    * @return the directory
    */
    public File getOldDirectory()
    {
        return m_old;
    }

   /**
    * Return the new directory value.
    * @return the directory
    */
    public File getNewDirectory()
    {
        return m_new;
    }
}

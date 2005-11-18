/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.transit.model;

import java.io.File;
import java.util.EventObject;

/**
 * Event signalling a change to the Transit cache directory.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class FileChangeEvent extends EventObject
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final File m_file;

   /**
    * Creation of a new file change event.
    * @param source the object raising the event
    * @param file the new file value
    */
    public FileChangeEvent( Object source, File file )
    {
        super( source );
        m_file = file;
    }
    
   /**
    * Return the new file value.
    * @return the new file
    */
    public File getFile()
    {
        return m_file;
    }
}

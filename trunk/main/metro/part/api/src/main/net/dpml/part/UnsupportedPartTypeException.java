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

package net.dpml.part;

import java.net.URI;

/**
 * Exception thrown when an strategy handler attempts to handle an 
 * incompatible type.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class UnsupportedPartTypeException extends ControlRuntimeException 
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private String m_type;

   /**
    * Creation of a new <tt>UnsupportedPartTypeException</tt>.
    * @param uri the uri identifying the part controller
    * @param classname the unsupport part classname
    * @param message the exception message
    */
    public UnsupportedPartTypeException( URI uri, String classname, String message )
    {
        super( uri, message );
        m_type = classname;
    }

   /**
    * Return the unsupported part classname.
    * @return the classname
    */
    public String getClassname()
    {
        return m_type;
    }
}



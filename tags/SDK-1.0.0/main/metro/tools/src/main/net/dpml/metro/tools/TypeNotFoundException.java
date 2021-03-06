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

package net.dpml.metro.tools;

/**
 * Exception thrown when an attempt is made to reference an unknown type.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TypeNotFoundException extends Exception 
{
    private String m_classname;

   /**
    * Creation of a new <tt>TypeNotFoundException</tt>.
    * @param classname the type classname
    */
    public TypeNotFoundException( String classname )
    {
        super( classname );
        m_classname = classname;
    }

   /**
    * Return the unresolved type classname.
    * @return the classname
    */
    public String getClassname()
    {
        return m_classname;
    }

}


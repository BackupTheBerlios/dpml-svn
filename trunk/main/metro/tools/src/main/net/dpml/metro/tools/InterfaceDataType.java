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

import net.dpml.state.impl.DefaultInterface;

import org.apache.tools.ant.BuildException;

/**
 * Defintion of a context entry parameter directive.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class InterfaceDataType
{
    private String m_name;
    private String m_classname;

   /**
    * Set the operation name.
    * @param name the operation name
    */
    public void setName( final String name )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        m_name = name;
    }

   /**
    * Set the operation method name.
    * @param classname the name of the interface class
    */
    public void setClass( final String classname )
    {
        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }
        m_classname = classname;
    }
    
    DefaultInterface getInterface()
    {
        final String classname = getInterfaceClassname();
        return new DefaultInterface( classname );
    }
    
    String getInterfaceClassname()
    {
        if( null != m_classname )
        {
            return m_classname;
        }
        else
        {
            throw new BuildException( "Missing interface 'classname' attribute." );
        }
    }
}

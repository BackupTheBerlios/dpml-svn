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

import net.dpml.state.DefaultOperation;

import org.apache.tools.ant.BuildException;

/**
 * Defintion of a context entry parameter directive.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class OperationDataType
{
    private String m_name;
    private String m_method;

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
    * @param method the name of the operation method
    */
    public void setMethod( final String method )
    {
        if( null == method )
        {
            throw new NullPointerException( "method" );
        }
        m_method = method;
    }
    
    DefaultOperation getOperation()
    {
        String name = getName();
        String method = getMethodName();
        return new DefaultOperation( name, method );
    }
    
    String getName()
    {
        if( null != m_name )
        {
            return m_name;
        }
        else if( null != m_method )
        {
            return m_method;
        }
        else
        {
            throw new BuildException( "Missing operation name or method attribute." );
        }
    }

    String getMethodName()
    {
        return m_method;
    }
}

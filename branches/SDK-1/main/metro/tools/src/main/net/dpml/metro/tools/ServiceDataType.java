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

import net.dpml.metro.info.ServiceDescriptor;

import net.dpml.lang.Version;

import org.apache.tools.ant.BuildException;

/**
 * Declaration of an exported service.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ServiceDataType
{
    private String m_classname;
    private Version m_version;

   /**
    * Set the service classname.
    * @param classname the name of the service interface class
    */
    public void setClass( final String classname )
    {
        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }
        m_classname = classname;
    }
    
   /**
    * Set the service version.
    * @param spec the version value
    */
    public void setVersion( final String spec )
    {
        if( null == spec )
        {
            throw new NullPointerException( "spec" );
        }
        m_version = Version.parse( spec );
    }
    
    ServiceDescriptor getServiceDescriptor()
    {
        if( null == m_classname )
        {
            throw new BuildException( "Missing interface 'class' attribute." );
        }
        else
        {
            return new ServiceDescriptor( m_classname, m_version );
        }
    }
}

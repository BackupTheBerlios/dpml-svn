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

package org.apache.avalon.test.components;

import java.io.File;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Context;

/**
 * This is a component that can be tested relative a set of 
 * assigned permissions.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @avalon.component name="test" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.test.components.TestService"
 * @avalon.security.permission class="java.io.FilePermission" 
 *                             name="urn:avalon:home"
 *                             actions="read,write"
 */
public class TestComponent extends AbstractLogEnabled 
  implements Contextualizable, TestService
{
    //----------------------------------------------------------------------
    // state
    //----------------------------------------------------------------------

    private File m_home;

    //----------------------------------------------------------------------
    // Contextualizable
    //----------------------------------------------------------------------

   /**
    * @avalon.entry type="java.io.File" key="urn:avalon:home"
    */
    public void contextualize( Context context ) throws ContextException
    {
        m_home = (File) context.get( "urn:avalon:home" );
    }

    //----------------------------------------------------------------------
    // TestService
    //----------------------------------------------------------------------

    /**
     * Does something trivial.
     */
    public void createDirectory()
    {
        m_home.mkdirs();
    }

    /**
     * Does something trivial.
     */
    public void deleteDirectory()
    {
        m_home.delete();
    }

    /**    
     * @avalon.security.permission class="java.util.PropertyPermission" 
     *     name="java.version" actions="read"
     */
    public String getJavaVersion()
    {
        return System.getProperty( "java.version" );
    }

    /**    
     * @avalon.security.permission class="java.util.PropertyPermission" 
     *     name="java.version" actions="write"
     */
    public void setJavaVersion( String newVer )
    {
        System.setProperty( "java.version", newVer );
    }
}

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

package tutorial;

import java.io.File;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * This is a minimal demonstration component that implements the
 * <code>BasicService</code> interface and has no dependencies.
 *
 * @avalon.component name="standard" lifestyle="singleton"
 * @avalon.service type="tutorial.StandardService"
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class StandardComponent extends AbstractLogEnabled
    implements Contextualizable, Initializable, Executable, Disposable, StandardService
{
    private File m_home;
    private File m_work;
    private String m_name;
    private String m_partition;
    private String m_message;
    private StandardContext m_context;

    //=======================================================================
    // Contextualizable
    //=======================================================================

   /**
    * Supply of the component context to the component type.
    *
    * @param context the context value
    *
    * @avalon.context strategy="tutorial.Contextualizable"
    * @avalon.entry key="urn:avalon:name" 
    * @avalon.entry key="urn:avalon:partition"
    * @avalon.entry key="urn:avalon:home" type="java.io.File"
    * @avalon.entry key="urn:avalon:temp" type="java.io.File"
    */
    public void contextualize( StandardContext context )
    {
        m_context = context;
        m_home = context.getHomeDirectory();
        m_work = context.getWorkingDirectory();
        m_name = context.getName();
        m_partition = context.getPartitionName();
    }

    //=======================================================================
    // Initializable
    //=======================================================================

    /**
     * Initialization of the component type by its container.
     */
    public void initialize() throws Exception
    {
        m_message =
          "  strategy: " + Contextualizable.class.getName()
          + "\n  context: " + m_context.getClass().getName()
          + "\n  home: " + m_home
          + "\n  work: " + m_work
          + "\n  name: " + m_name
          + "\n  partition: " + m_partition;
    }

    //=======================================================================
    // Disposable
    //=======================================================================

    /**
     * Dispose of the component.
     */
    public void dispose()
    {
        getLogger().debug( "dispose" );
    }

    //=======================================================================
    // Executable
    //=======================================================================

    /**
     * Execute the component.
     */
    public void execute()
    {
        printMessage();
    }

    //=======================================================================
    // BasicService
    //=======================================================================

    /**
     * Service interface implementation.
     */
    public void printMessage()
    {
        getLogger().info( "contextualization using a custom strategy\n\n"
         + m_message + "\n");
    }
}

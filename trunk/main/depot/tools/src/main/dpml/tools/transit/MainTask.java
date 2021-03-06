/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

package dpml.tools.transit;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;

/**
 * Task that initializes the Transit sub-system during which a transit
 * is assigned to the current project.  In addition the initization procedures
 * establishes a set of ant properties enabling convenient script based access
 * to DPML dirctories.
 *
 * <pre>
  &lt;project name="example"
      xmlns:transit="antlib:dpml.tools.transit">
    &lt;transit:init/>
    &lt;echo message="Home: ${dpml.home}"/>
    &lt;echo message="Cache: ${dpml.cache}"/>
    &lt;echo message="Templates: ${dpml.templates}"/>
    &lt;echo message="Docs: ${dpml.docs}"/>
    &lt;echo message="Docs: ${dpml.dist}"/>
  &lt;/project></pre>
 * <p>Output from the above example is shown below:</p>
 * <pre>
     [echo] Home: C:\system\dpml
     [echo] Cache: C:\system\dpml\main
     [echo] Templates: C:\system\dpml\templates
     [echo] Docs: C:\system\dpml\docs
     [echo] Docs: C:\system\dpml\dist
 * </pre>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class MainTask extends TransitTask
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

   /**
    * List of plugin declarations.
    */
    private List<MapDataType> m_mappings = new ArrayList<MapDataType>();

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new Main task.
    */
    public MainTask()
    {
        super();
    }

    // ------------------------------------------------------------------------
    // Task
    // ------------------------------------------------------------------------

   /**
    * Create and return a new plugin definition.
    * @return the plugin definition
    */
    public MapDataType createMap()
    {
        MapDataType map = new MapDataType();
        m_mappings.add( map );
        return map;
    }

   /**
    * Set the project.
    * @param project the current project
    */
    public void setProject( Project project )
    {
        setTaskName( "transit" );
        super.setProject( project );
    }

   /**
    * Updates properties on the current project and install any plugins declared
    * as children of the transit init tag.
    *
    * @exception BuildException if an execution error occurs
    */
    public void execute() throws BuildException
    {
        Project project = getProject();
        TransitComponentHelper.initialize( project );
        ComponentHelper ch = ComponentHelper.getComponentHelper( project );
        MapDataType[] maps = (MapDataType[]) m_mappings.toArray( new MapDataType[0] );
        TransitComponentHelper.register( maps );
    }
}


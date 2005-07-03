/*
 * Copyright 2004 Niclas Hedhman
 * Copyright 2004 Stephen McConnell
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

package net.dpml.magic.doc;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Location;

import net.dpml.transit.tools.TargetFactory;

import java.net.URI;


public class DocTargetFactory
    implements TargetFactory
{
    private Project m_Project;
    private Location m_Location;

    public DocTargetFactory( Project project, URI uri )
    {
        m_Project = project;
        m_Location = new Location( uri.toString(), 1, 1 );
    }

    public Target[] createTargets()
    {
        Target t = new Target();
        t.setName( "docs" );
        t.setProject( m_Project );
        t.setDescription( "Reads the XML documents in src/docs and executes XSL transforms based on a defined 'theme'" );
        t.setLocation( m_Location );
        t.setDepends( "prepare" );
        DocTask docTask = new DocTask();
        docTask.setOwningTarget( t );
        docTask.setLocation( m_Location );
        docTask.setProject( m_Project );
        docTask.setTaskName( "docs" );
        docTask.init();
        t.addTask( docTask );
        return new Target[] { t };
    }
}

/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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

package net.dpml.test.acme.plus;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.LinkedList;

import junit.framework.TestCase;

import net.dpml.part.PartContentHandlerFactory;
import net.dpml.part.component.Component;
import net.dpml.part.component.Manager;
import net.dpml.part.state.State;

/**
 * Test a simple component case.
 * 
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta
 *         Library</a>
 */
public class ManagementTestCase extends TestCase
{
    public void tearDown()
    {
        System.gc();
    }

    /**
     * Test the construction of the example component and the invocation of 
     * the provided controller via </code>the net.dpml.part.state.StateManager</code> interface
     */
    public void testManagedComponent() throws Exception
    {
        Manager component = (Manager) getComponent( "managed-component.part" );
        component.initialize();
        List list = new LinkedList();
        while ( false == list.contains( component.getState() ) )
        {
            State state = component.getState();
            list.add( state );
            String[] transitions = state.getTransitionNames();
            for( int i=0; i<transitions.length; i++ )
            {
                String name = transitions[i];
                component.apply( name );
                break;
            }
        }
        component.terminate();
    }

    public void testManagingContainer() throws Exception
    {
        Manager component = (Manager) getComponent( "managing-container.part" );
        try
        {
            component.initialize();
        }
        finally
        {
            component.terminate();
        }
    }

    Component getComponent( String path ) throws Exception
    {
        File test = new File( System.getProperty( "project.test.dir" ) );
        URL url = new File( test, path ).toURL();
        return (Component) url.getContent( new Class[]{ Component.class } );
    }

    static
    {
        URLConnection.setContentHandlerFactory( new PartContentHandlerFactory() );
    }
}

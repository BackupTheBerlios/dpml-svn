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

package net.dpml.test.acme;

import java.net.URI;
import java.util.Hashtable;
import java.util.Map.Entry;

import junit.framework.TestCase;

import net.dpml.part.control.Controller;
import net.dpml.part.control.Component;

import net.dpml.metro.central.MetroHelper;

/**
 * Test a simple component case.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: AbstractDescriptorTestCase.java 1556 2005-01-22 12:43:42Z niclas $
 */
public class CompositeTestCase extends TestCase
{    
   /**
    * Test the construction of a component that contains two child components
    * widget and gizmo where both are components and gizmo declares a dependency
    * on widget.
    */
    public void testAcmeContainerUsingComponent() throws Exception
    {
        executeTestOnPart( "acme-container.part" );
    }

   /**
    * Test the construction of a component that contains two child components
    * (widget and gizmo) where the widget is a value object.
    */
    public void testAcmeContainerUsingValue() throws Exception
    {
        executeTestOnPart( "acme-test-container.part" );
    }

   /**
    * Test the construction of a component that contains two child components
    * widget and gizmo where both are components and gizmo declares a dependency
    * on widget.
    */
    public void testBadWidget() throws Exception
    {
        MetroHelper helper = new MetroHelper();
        URI uri = helper.toURI( "acme-bad-widget.part" );

        Component component = helper.getController().newComponent( uri );
        AcmeContainer container = (AcmeContainer) component.resolve( false );

        try
        {
            container.execute();
        }
        catch( BadColorException e )
        {
             // success
        }
        finally
        {
            component.release( container );
            helper.dispose();
        }
    }

    private void executeTestOnPart( String path ) throws Exception
    {
        MetroHelper helper = new MetroHelper();
        URI uri = helper.toURI( path );

        Component component = helper.getController().newComponent( uri );
        AcmeContainer container = (AcmeContainer) component.resolve( false );
        try
        {
             container.execute();
        }
        finally
        {
            component.release( container );
            helper.dispose();
        }
    }
}

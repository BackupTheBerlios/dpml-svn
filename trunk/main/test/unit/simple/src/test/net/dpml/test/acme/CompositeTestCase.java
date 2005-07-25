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

import java.io.File;
import java.net.URL;
import java.net.URLConnection;

import junit.framework.TestCase;

import net.dpml.part.PartContentHandlerFactory;
import net.dpml.part.control.Controller;
import net.dpml.part.component.Component;

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
        File test = new File( System.getProperty( "project.test.dir" ) );
        URL url = new File( test, "acme-bad-widget.part" ).toURL();
        Component component = (Component) url.getContent( new Class[]{ Component.class } );
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
        }
    }

    private void executeTestOnPart( String path ) throws Exception
    {
        File test = new File( System.getProperty( "project.test.dir" ) );
        URL url = new File( test, path ).toURL();
        Component component = (Component) url.getContent( new Class[]{ Component.class } );
        AcmeContainer container = (AcmeContainer) component.resolve( false );
        try
        {
             container.execute();
        }
        finally
        {
            component.release( container );
        }
    }

    static
    {
        URLConnection.setContentHandlerFactory( new PartContentHandlerFactory() );
    }

}

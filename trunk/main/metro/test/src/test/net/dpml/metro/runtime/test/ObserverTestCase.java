/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.metro.runtime.test;

import java.awt.Color;
import java.io.File;
import java.net.URI;

import junit.framework.TestCase;

import net.dpml.part.Component;
import net.dpml.part.Controller;
import net.dpml.part.Provider;
import net.dpml.metro.data.ValueDirective;
import net.dpml.metro.model.ComponentModel;
import net.dpml.metro.model.MutableContextModel;

/**
 * Test aspects of the component model implementation.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ObserverTestCase extends TestCase
{    
    private ComponentModel m_model;
    
   /**
    * Test case setup.
    * @exception Exception if an error occurs
    */
    public void setUp() throws Exception
    {
        final String path = "observer.part";
        final File test = new File( System.getProperty( "project.test.dir" ) );
        final URI uri = new File( test, path ).toURI();
        m_model = (ComponentModel) Controller.STANDARD.createModel( uri );
    }
    
   /**
    * Test mutation of the context model.
    * @exception Exception if an error occurs
    */
    public void testContextModel() throws Exception
    {
        MutableContextModel context = (MutableContextModel) m_model.getContextModel();
        Component component = Controller.STANDARD.createComponent( m_model );
        Provider provider = component.getProvider();
        Object instance = provider.getValue( false );
        String key = "color";
        ValueDirective newDirective = new ValueDirective( Color.class.getName(), "BLUE", (String) null );
        context.setEntryDirective( key, newDirective );
        component.deactivate();
    }
}

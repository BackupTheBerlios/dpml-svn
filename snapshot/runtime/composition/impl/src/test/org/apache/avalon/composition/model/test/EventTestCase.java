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

package org.apache.avalon.composition.model.test;

import org.apache.avalon.composition.data.ComponentProfile;
import org.apache.avalon.composition.data.ContainmentProfile;
import org.apache.avalon.composition.data.DeploymentProfile;

import org.apache.avalon.composition.event.CompositionEvent;
import org.apache.avalon.composition.event.CompositionListener;

import org.apache.avalon.composition.model.ModelException;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.ClassLoaderModel;
import org.apache.avalon.composition.model.TypeRepository;
import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.DependencyModel;

import org.apache.avalon.meta.info.Type;
import org.apache.avalon.meta.info.ReferenceDescriptor;

import org.apache.avalon.util.exception.ExceptionHelper;


public class EventTestCase extends AbstractTestCase
    implements CompositionListener
{      
    private static final String WIDGET = 
      "org.apache.avalon.test.dynamics.Widget";

    private static final String WIDGET_CLASS = 
      "org.apache.avalon.test.dynamics.DefaultWidget";

    private static final String GIZMO = 
      "org.apache.avalon.test.dynamics.Gizmo";

    private static final ContainmentProfile PROFILE = 
      new ContainmentProfile();

    private ContainmentModel m_container;

    private int m_Counter;
    
   //-------------------------------------------------------
   // tests
   //-------------------------------------------------------

   /**
    * Validate resolution of a widget.
    */
    public void testEvents() throws Exception
    {
        m_Counter = 0;
        m_model = super.setUp( "dynamics.xml" );
        m_model.addCompositionListener( this );
        m_model.assemble();
        ContainmentModel c1 = (ContainmentModel) m_model.addModel( PROFILE );
        c1.assemble();
        ContainmentModel c2 = (ContainmentModel) c1.addModel( PROFILE );
        c2.assemble();
        ContainmentModel c3 = (ContainmentModel) c2.addModel( PROFILE );
        c3.assemble();
        ContainmentModel c4 = (ContainmentModel) c3.addModel( PROFILE );
        c4.assemble();
        assertEquals( "ModelAdded events are missing.", 4, m_Counter );
        m_model.removeModel( c1.getName() );
        assertEquals( "ModelRemoved events are missing.", 0, m_Counter );
    }
       
    public void modelAdded( CompositionEvent event )
    {
        System.out.println( "Model Added Event: " + event );
        m_Counter++;
    }
    
    public void modelRemoved( CompositionEvent event )
    {
        System.out.println( "Model Removed Event: " + event );
        m_Counter--;
    }
    
}

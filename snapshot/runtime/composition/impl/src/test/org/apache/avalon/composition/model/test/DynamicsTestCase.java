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
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.ClassLoaderModel;
import org.apache.avalon.composition.model.TypeRepository;
import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.DependencyModel;

import org.apache.avalon.meta.info.Type;
import org.apache.avalon.meta.info.ReferenceDescriptor;

import org.apache.avalon.util.exception.ExceptionHelper;


public class DynamicsTestCase extends AbstractTestCase
{      
    private static final String WIDGET = 
      "org.apache.avalon.test.dynamics.Widget";

    private static final String GIZMO = 
      "org.apache.avalon.test.dynamics.Gizmo";


   //-------------------------------------------------------
   // constructor
   //-------------------------------------------------------

    public DynamicsTestCase()
    {
        super();
    }

    public void setUp() throws Exception
    {
        m_model = super.setUp( "dynamics.xml" );
    }

   //-------------------------------------------------------
   // tests
   //-------------------------------------------------------

   /**
    * Validate resolution of a widget.
    */
    public void testDynamicWidget() throws Exception
    {
        try
        {
            ReferenceDescriptor ref = new ReferenceDescriptor( WIDGET );
            ComponentModel widget = (ComponentModel) m_model.getModel( ref );
            validate( widget );
        }
        catch( Throwable e )
        {
            final String message = "Assembly test failure";
            final String error = ExceptionHelper.packException( message, e, true );
            System.err.println( error );
            fail( error );
        }
    }

   /**
    * Validate resolution of a gizmo.
    */
    public void testDynamicGizmo() throws Exception
    {
        try
        {
            m_model.assemble();
            ReferenceDescriptor ref = new ReferenceDescriptor( GIZMO );
            ComponentModel gizmo = (ComponentModel) m_model.getModel( ref );
            validate( gizmo );
        }
        catch( Throwable e )
        {
            final String message = "Assembly test failure";
            final String error = ExceptionHelper.packException( message, e, true );
            System.err.println( error );
            fail( error );
        }
    }

   /**
    * Validate a model.
    */
    private void validate( ComponentModel model ) throws Exception
    {
        try
        {
            if( !model.isAssembled() )
            {
                fail( "Non-assembled model: " + model );
            }

            DependencyModel[] dependencies = model.getDependencyModels();
            for( int i=0; i<dependencies.length; i++ )
            {
                DependencyModel dependency = dependencies[i];
                if( null == dependency.getProvider() )
                {
                    fail( 
                      "Null provider located in an assembled model: " 
                      + model );
                }
            }
        }
        catch( Throwable e )
        {
            final String message = "Dynamic assembly validation failure";
            final String error = ExceptionHelper.packException( message, e, true );
            System.err.println( error );
            fail( error );
        }
    }
}

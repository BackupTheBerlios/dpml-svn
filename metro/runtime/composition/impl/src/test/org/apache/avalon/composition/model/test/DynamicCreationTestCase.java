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


public class DynamicCreationTestCase extends AbstractTestCase
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

   //-------------------------------------------------------
   // constructor
   //-------------------------------------------------------

    public DynamicCreationTestCase()
    {
        super();
    }

    public void setUp() throws Exception
    {
        m_model = super.setUp( "dynamics.xml" );
        m_container = (ContainmentModel) m_model.addModel( PROFILE );
    }

   //-------------------------------------------------------
   // tests
   //-------------------------------------------------------

   /**
    * Validate resolution of a widget.
    */
    public void testDynamicCreation() throws Exception
    {
        try
        {
            //
            // ensure the container is already assembled
            //

            m_model.assemble();

            //
            // create and add a component to the assembled container
            // causing the container to flag itself as dirty
            //

            ComponentProfile profile = getProfile();
            ComponentModel widget = (ComponentModel) m_model.addModel( profile );
            assertFalse( widget.isAssembled() );

            try
            {
                widget.commission();
                fail( "Commisioning of a non-assembled model should throw an exeption." );
            }
            catch( ModelException me )
            {
                // expected
            }

            //
            // reassemble the container to bring it back into a clean state
            //

            m_model.assemble();
            validate( widget );
        }
        catch( Throwable e )
        {
            final String message = "Dynamic creation test failure";
            final String error = ExceptionHelper.packException( message, e, false );
            System.err.println( error );
            fail( error );
        }
    }

    private ComponentProfile getProfile() throws Exception
    {
        ClassLoaderModel clm = m_model.getClassLoaderModel();
        TypeRepository repository = clm.getTypeRepository();
        Type type = repository.getType( WIDGET_CLASS );
        DeploymentProfile[] profiles = repository.getProfiles( type );
        if( profiles.length > 0 )
        {
            return (ComponentProfile) profiles[0];
        }
        throw new IllegalStateException( "no profile" );
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

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

import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.DependencyModel;

public class DependencyTestCase extends AbstractTestCase
{      
   //-------------------------------------------------------
   // constructor
   //-------------------------------------------------------

    public DependencyTestCase()
    {
        super();
    }

    public void setUp() throws Exception
    {
        m_model = super.setUp( "dependency.xml" );
    }

   //-------------------------------------------------------
   // tests
   //-------------------------------------------------------

   /**
    * Validate the composition model.
    */
    public void testStandardDependencyModel() throws Exception
    {
        DeploymentModel a = (DeploymentModel) m_model.getModel( "test-a" );

        ContainmentModel fred = (ContainmentModel) m_model.getModel( "fred" );
        ComponentModel b = (ComponentModel) fred.getModel( "test-b" );
        ComponentModel c = (ComponentModel) fred.getModel( "test-c" );

        if( c == null )
        {
            fail( "null deployment model 'test-c'" );
        }

        DependencyModel[] deps = c.getDependencyModels();
        assertTrue( "dependency count", deps.length == 3 );

        for( int i=0; i<deps.length; i++ )
        {
            DependencyModel dep = deps[i];
            String key = dep.getDependency().getKey();
            String path = dep.getPath();
            assertNotNull( "selection for key: " + key, path );
        }
    }
}

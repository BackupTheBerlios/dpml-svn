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

import java.io.File;

import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.data.Targets;
import org.apache.avalon.composition.data.TargetDirective;
import org.apache.avalon.composition.data.builder.XMLTargetsCreator;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;

import org.apache.avalon.util.exception.ExceptionHelper;
import org.apache.avalon.util.configuration.ConfigurationUtil;

import org.apache.avalon.logging.data.CategoriesDirective;


public class TargetsTestCase extends AbstractTestCase
{      
   //-------------------------------------------------------
   // constructor
   //-------------------------------------------------------

    public TargetsTestCase()
    {
        super();
    }

    public void setUp() throws Exception
    {
        m_model = super.setUp( "targets.xml" );
    }

   //-------------------------------------------------------
   // tests
   //-------------------------------------------------------

   /**
    * Validate the the included block was created.
    */
    public void testTargetsApplication() throws Exception
    {
        try
        {
            File source = 
              new File( BASEDIR, "targets-config.xml" );
            TargetDirective[] targets = loadTargets( source ).getTargets();
            for( int i=0; i<targets.length; i++ )
            {
                TargetDirective target = targets[i];
                final String path = target.getPath();
                DeploymentModel model = m_model.getModel( path );

                CategoriesDirective categories = 
                  target.getCategoriesDirective();
                if( null != categories )
                {
                    model.setCategories( categories );
                }

                if( model instanceof ComponentModel )
                {
                    ComponentModel deployment = (ComponentModel) model;
                    Configuration config = target.getConfiguration();
                    if( null != config )
                    { 
                        deployment.setConfiguration( config );
                    }
                    getLogger().debug( "model: " + deployment );
                    getLogger().debug( 
                      ConfigurationUtil.list( deployment.getConfiguration() ) );
                }
            }
        }
        catch( Throwable e )
        {
            final String error = "Test failure.";
            final String message = ExceptionHelper.packException( error, e, true );
            getLogger().error( message );
            throw new Exception( message );
        }
    }

    private Targets loadTargets( File file )
      throws Exception
    {
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        Configuration config = builder.buildFromFile( file );
        XMLTargetsCreator creator = new XMLTargetsCreator();
        return creator.createTargets( config );
    }
}

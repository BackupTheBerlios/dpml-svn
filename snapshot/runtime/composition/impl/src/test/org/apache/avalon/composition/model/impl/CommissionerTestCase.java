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

package org.apache.avalon.composition.model.impl;
 
import org.apache.avalon.composition.model.CommissioningException;
import org.apache.avalon.composition.model.FatalCommissioningException;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.Commissionable;

import junit.framework.TestCase;

import org.apache.avalon.framework.logger.ConsoleLogger;

public class CommissionerTestCase extends TestCase
{
    private Commissioner m_commissioner;
    
    public void setUp()
    {
        ConsoleLogger logger = new ConsoleLogger( ConsoleLogger.LEVEL_INFO );
        m_commissioner = new Commissioner( logger, true );
    }

    public void tearDown()
        throws Exception
    {
        m_commissioner.dispose();
        m_commissioner = null;
    }
        
    public void testNormalDeploy()
        throws Exception
    {
        Commissionable d = new SimpleCommissionable();
        DeploymentModel model = new SimpleDeploymentModel( d, 100 );
        m_commissioner.commission( model );
    }

    public void testInterruptableDeploy()
        throws Exception
    {
        try
        {
            Commissionable d = new InterruptableCommissionable();
            DeploymentModel model = new SimpleDeploymentModel( d, 100 );
            m_commissioner.commission( model );

            fail( "The Deployment didn't fail with a CommissioningException." );
        } 
        catch( FatalCommissioningException e )
        {
            fail( "The Exception thrown was a FatalCommissioningException." );
        } 
        catch( CommissioningException e )
        {
            // testcase success
        }
    }
    
    public void testUninterruptableDeploy()
        throws Exception
    {
        UninterruptableCommissionable d = new UninterruptableCommissionable();
        try
        {
            DeploymentModel model = new SimpleDeploymentModel( d, 100 );
            m_commissioner.commission( model );
            fail( "The Deployment didn't fail with a FatalDeploymentException." );
        } 
        catch( FatalCommissioningException e )
        {
            // testcase success
        } 
        catch( CommissioningException e )
        {
            fail( "The Exception thrown was a CommissioningException, when a FatalCommissioningException was expected." );
        } 
        finally
        {
            d.stop();
        }
    }
    
    public void testCustomExceptionDeploy()
        throws Exception
    {
        try
        {
            Commissionable d = new CustomExceptionDeployable();
            DeploymentModel model = new SimpleDeploymentModel( d, 100 );
            m_commissioner.commission( model );
            fail( "The Deployment didn't fail with a DeploymentException." );
        } 
        catch( CustomException e )
        {
            // success
        } 
        catch( FatalCommissioningException e )
        {
            fail( "The Exception thrown was a FatalCommissioningException." );
        } 
        catch( CommissioningException e )
        {
            fail( "The Exception thrown was a CommissioningException." );
        }
    }
    
    public void testCustomErrorDeploy()
        throws Exception
    {
        try
        {
            Commissionable d = new CustomErrorDeployable();
            DeploymentModel model = new SimpleDeploymentModel( d, 100 );
            m_commissioner.commission( model );
            fail( "The Deployment didn't fail with a DeploymentException." );
        } 
        catch( CustomError e )
        {
            // success
        } 
        catch( FatalCommissioningException e )
        {
            fail( "The Exception thrown was a FatalCommissioningException." );
        } 
        catch( CommissioningException e )
        {
            fail( "The Exception thrown was a CommissioningException." );
        }
    }
}

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

import org.apache.avalon.composition.model.AssemblyException;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.CyclicDependencyException;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.ModelRuntimeException;
import org.apache.avalon.composition.model.test.AbstractTestCase;
import org.apache.avalon.framework.logger.ConsoleLogger;

/**
 * checks that cyclic dependencies in the service declarations are properlly
 * found and notified.
 */
public class CyclicDepsTestCase extends AbstractTestCase
{
    //-------------------------------------------------------
    // constructor
    //-------------------------------------------------------
    /**
     * the contructor
     */
    public CyclicDepsTestCase()
    {
        super();
    }

    /**
     * @param container
     *            the container to be printed
     */
    private void printStartup( ContainmentModel container )
    {
        DeploymentModel[] startup = container.getStartupGraph();
        for ( int i = 0; i < startup.length; i++ )
        {
            DeploymentModel current = startup[i];
            if( current instanceof ContainmentModel )
            {
                ContainmentModel currentContainer = (ContainmentModel) current;
                try
                {
                    currentContainer.assemble();
                    printStartup( currentContainer );
                }
                catch ( AssemblyException e )
                {
                    e.printStackTrace();
                }

            }
            System.out.println( current.getPath() + current.getName() );
            DeploymentModel[] deps = current.getProviders();

            for ( int j = 0; j < deps.length; j++ )
            {

                System.out.println( "\tdep<-" + deps[j].getPath()
                        + deps[j].getName() );

            }
        }
    }

    public void setUp() throws Exception
    {
        m_model = super.setUp( "cyclicdeps.xml" );
        ConsoleLogger logger = new ConsoleLogger( ConsoleLogger.LEVEL_INFO );
    }

    //-------------------------------------------------------
    // tests
    //-------------------------------------------------------

    /**
     * Validate the the included block was created.
     */
    public void testCyclicDependency() throws Throwable
    {
        try
        {

            ContainmentModel root = (ContainmentModel) m_model.getModel( "/" );

            root.assemble();
            printStartup( root );
            fail( "an exception should have been thrown" );

        }
        catch ( ModelRuntimeException e )
        {
            //this should be thrown.
        }
    }
}
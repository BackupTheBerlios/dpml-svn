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

package org.apache.avalon.logging.logkit.test;

import junit.framework.TestCase;

import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.logging.LoggingManagerHelper;
import org.apache.avalon.logging.provider.LoggingManager;
import org.apache.avalon.logging.provider.LoggingException;

/**
 * 
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultLoggingManagerTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultLoggingManagerTestCase extends TestCase
{
    //-------------------------------------------------------------------
    // state
    //-------------------------------------------------------------------

    LoggingManager m_manager;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

    /**
     * Constructor for DefaultInitialContextTest.
     * @param name the test name
     */
    public DefaultLoggingManagerTestCase( String name )
    {
        super( name );
    }

    //-------------------------------------------------------------------
    // utilities
    //-------------------------------------------------------------------

    protected void setUp() throws Exception
    {
        m_manager = 
          LoggingManagerHelper.setUpLoggingManager( 
            "avalon-logging-logkit-impl", "logkit/logging.xml" );
    }

    public void testKernelLogging() throws Exception
    {
        Logger logger = m_manager.getLoggerForCategory( "kernel.logger" );

        logger.debug( "this is a debug message from the kernel" );
        logger.info( "this is a info message from the kernel" );
        logger.warn( "this is a warning message from the kernel" );
        logger.error( 
          "this is an error message from the kernel", 
          new LoggingException( 
            "Intentional Exception for TestCase.", 
            new LoggingException( 
              "This is not a problem.", 
              new LoggingException( "It is used to test the Logging framework." ) ) ) );
        logger.fatalError( "If the TestCase does not fail, everything is OK." );
    }

    public void testKernelTestLogging() throws Exception
    {
        Logger logger = m_manager.getLoggerForCategory( "kernel.logger.test" );

        logger.debug( "this is a debug message from test" );
        logger.info( "this is a info message from test" );
        logger.warn( "this is a warning message from test" );
        logger.error( 
          "this is an error message from test", 
          new LoggingException( 
            "woops", 
            new LoggingException( 
              "my fault", 
              new LoggingException( "bad attitude" ) ) ) );
        logger.fatalError( "this is a fatal message from test" );
    }

    public void testRogerRamjetLogging() throws Exception
    {
        Logger logger = m_manager.getLoggerForCategory( "kernel.roger-ramjet" );

        logger.debug( "this is a debug message from roger ramjet" );
        logger.info( "this is a info message from roger ramjet" );
        logger.warn( "this is a warning message from roger ramjet" );
        logger.error( 
          "this is an error message from roger ramjet", 
          new LoggingException( 
            "woops", 
            new LoggingException( 
              "my fault", 
              new LoggingException( "bad attitude" ) ) ) );
        logger.fatalError( "this is a fatal message from roger ramjet" );
    }
}

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
package org.apache.avalon.db.hsql.test;

import java.io.File;

import java.net.InetAddress;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;

import junit.framework.TestCase;

import org.apache.avalon.db.hsql.HsqlServer;
import org.apache.avalon.framework.logger.ConsoleLogger;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.1 $ $Date: 2004/05/06 00:03:47 $
 */
public class HsqlServerTest extends TestCase {
    private ConsoleLogger m_logger;

    /**
     * Sets up the test case.
     */
    public void setUp() throws Exception {
        m_logger = new ConsoleLogger( ConsoleLogger.LEVEL_INFO );
    }

    /**
     * Cleans up the test case.
     */
    public void tearDown() throws Exception {
        m_logger = null;
    }

    /**
     * Tests the startup and shutdown of a HSQL database.
     */
    public void testHsqlServer() throws Exception {
        // create server object...
        InetAddress bindTo = InetAddress.getByName("127.0.0.1");
        HsqlServer server = new HsqlServer( "my-test-hsql",
                                            9001,
                                            bindTo,
                                            true,
                                            false,
                                            m_logger);
        
        // start the server...
        try {
            server.startServer();
        } catch (Exception e) {
            fail( "Failure starting HSQL: " + e.getMessage() );
        }

        // load the HSQL JDBC driver            
        try {
            Class.forName("org.hsqldb.jdbcDriver").newInstance();
        } catch (Exception e) {
            fail( "Failure loading HSQL JDBC driver: " + e.getMessage() );
        }
        
        // make connection
        try {
            Connection conn = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost", "sa", "");
            DatabaseMetaData meta = conn.getMetaData();
            String name = meta.getDatabaseProductName();
            String version = meta.getDatabaseProductVersion();
            m_logger.debug(name + " v" + version);
            conn.close();
        } catch (Exception e) {
            fail( "Failure: " + e.getMessage() );
        }
        
        // shutdown the server...
        try {
            server.stopServer();
        } catch (Exception e) {
            fail( "Failure shutting down HSQL: " + e.getMessage() );
        }
            
    }
    
}

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
package org.apache.avalon.db.hsql;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.avalon.db.DatabaseService;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.Logger;


/**
 * Database service provider implementing an embedded HypersonicSQL
 * database server.
 *
 * @avalon.component name="server" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.db.DatabaseService"
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/05/11 21:08:03 $
 */
public class HsqlServiceProvider implements Contextualizable,
    Configurable, Initializable, Startable, DatabaseService
{

    //---------------------------------------------------------
    // static
    //---------------------------------------------------------

    /**
     * Default HSQL database server TCP socket listening port.
     * The default value is the HSQL standard port 9001.
     */
    private static final int DEFAULT_PORT = 9001;
    /**
     * Default host name to bind the HSQL database server TCP socket
     * to.  The default value is the localhost, that is 127.0.0.1.
     */
    private static final String DEFAULT_HOST = "127.0.0.1";
    
    //---------------------------------------------------------
    // immutable state
    //---------------------------------------------------------

    /**
     * The logging channel for this component.
     */
    private final Logger m_logger;
    
    //---------------------------------------------------------
    // state
    //---------------------------------------------------------

    /**
     * HSQL server wrapper object.
     */
    private HsqlServer m_server;
    /**
     * The working base directory.
     */
    private File m_basedir;
    /**
     * Listening port for database connections. Default is 9001;
     */
    private int m_port;
    /**
     * Host to bind the database TCP/IP listener to. Default is localhost.
     */
    private InetAddress m_host;
    /**
     * Fully qualified filepath to the database file artifacts.
     */
    private File m_dbName;
    /**
     * Run the HSQLDB server in debug mode?
     */
    private boolean m_dbDebug;
    /**
     * Run the HSQLDB server silently?
     */
    private boolean m_dbSilent;

    //---------------------------------------------------------
    // constructor
    //---------------------------------------------------------

    /**
     * Creates a new instance of the HSQL database server with the
     * logging channel supplied by the container.
     *
     * @param logger the container-supplied logging channel
     */
    public HsqlServiceProvider( final Logger logger )
    {
        m_logger = logger;
    }

    //---------------------------------------------------------
    // Contextualizable
    //---------------------------------------------------------

   /**
    * Contextualization of the server by the container.
    *
    * @param context the supplied server context
    * @exception ContextException if a contextualization error occurs
    * @avalon.entry key="urn:avalon:home" type="java.io.File"
    */
    public void contextualize( final Context context )
      throws ContextException
    {
        // cache the working base directory
        m_basedir = getBaseDirectory( context );
    }

    //---------------------------------------------------------
    // Configurable
    //---------------------------------------------------------

    /**
     * Configuration of the server by the container.
     *
     * @param config the supplied server configuration
     * @exception ConfigurationException if a configuration error occurs
     */
    public void configure( final Configuration config )
      throws ConfigurationException
    {
        // get the database listening port/host
        m_port = config.getChild( "port" ).getValueAsInteger( DEFAULT_PORT );
        String host = config.getChild( "host" ).getValue( DEFAULT_HOST );
        try
        {
            m_host = InetAddress.getByName( host );
        }
        catch ( UnknownHostException uhe )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error(
                  "Error configuring HSQLDB component: "
                    + uhe.getMessage(),
                  uhe );
            }
            throw new ConfigurationException( uhe.getMessage(), uhe );
        }

        // get the HSQLDB server parameters...
        m_dbName = new File( m_basedir,
            config.getChild( "db-name" ).getValue( "avalon-hsqldb" ) );
        m_dbDebug = config.getChild( "debug" ).getValueAsBoolean( false );
        m_dbSilent = config.getChild( "silent" ).getValueAsBoolean( true );
    }

    //---------------------------------------------------------
    // Initializable
    //---------------------------------------------------------

    /**
     * Initialization of the component by the container.
     * 
     * @throws Exception if an error occurs while initializing
     * the component
     */
    public void initialize() throws Exception {
        m_server = new HsqlServer( m_dbName.getAbsolutePath(),
                m_port, m_host, m_dbDebug, m_dbSilent, m_logger );
    }

    //---------------------------------------------------------
    // Startable
    //---------------------------------------------------------

    /**
     * Start the server.
     * 
     * @throws Exception if an error occurs while starting the 
     * component
     */
    public void start() throws Exception {
        if (m_server != null) {
            m_server.startServer();
        } else {
            throw new Exception("Server instance not instantiated!!");
        }
    }

    /**
     * Stop the server.
     * 
     * @throws Exception if an error occurs while stopping the component
     */
    public void stop() throws Exception {
        m_server.stopServer();
    }

    /**
     * Return the working base directory.
     * 
     * @param context the supplied server context
     * @return a <code>File</code> object representing the working
     * base directory
     * @exception ContextException if a contextualization error occurs
     */
    private File getBaseDirectory( final Context context ) 
      throws ContextException 
    {
        File home = (File) context.get( "urn:avalon:home" );
        if ( !home.exists() )
        {
            boolean success = home.mkdirs();
            if ( !success )
            {
                throw new ContextException( "Unable to create component "
                    + "home directory: " + home.getAbsolutePath() );
            }
        }
        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Setting home directory to: " + home );
        }
        return home;
    }
    
   /**
    * Return the assigned logging channel.
    * @return the logging channel
    */
    private Logger getLogger()
    {
        return m_logger;
    }

}

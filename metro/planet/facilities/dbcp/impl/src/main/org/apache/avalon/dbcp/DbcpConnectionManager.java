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

package org.apache.avalon.dbcp;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * This is a JDBC Connection Manager implementation that uses the
 * Jakarta-Commons database connection pooling and dbcp packages
 * to serve up pooled connections to datasources.
 *
 * @avalon.component name="dbcp-manager" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.dbcp.ConnectionManager"
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/05/11 21:24:24 $
 */
public class DbcpConnectionManager
	implements
		Contextualizable,
		Configurable,
		Initializable,
        Disposable,
		ConnectionManager
{
	//---------------------------------------------------------
	// state
	//---------------------------------------------------------

	/**
	 * The logging channel assigned by the container.
	 */
	private Logger m_logger;
	/**
	 * The working base directory.
	 */
	private File m_basedir;
    /**
     * Connection pool configuration parameters.
     */
    private Configuration m_pool;
    /**
     * Set of datasource configuration parameters.
     */
    private Configuration[] m_datasources;
	
    //---------------------------------------------------------
    // constructor
    //---------------------------------------------------------

    /**
     * Creates a new instance of the HSQL database server with the
     * logging channel supplied by the container.
     *
     * @param logger the container-supplied logging channel
     */
    public DbcpConnectionManager( final Logger logger )
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
        // cache away the connection pool settings...
        m_pool = config.getChild( "pool", false );
        
        // cache away the datasources settings...
        Configuration datasources = config.getChild( "datasources", false );
        if ( datasources == null )
        {
            throw new ConfigurationException( "The '<datasources>' " +
                    "directive is not included in the component's" +
                    "configuration." );
        }
        else
        {
            m_datasources = datasources.getChildren( "datasource" );
            if ( m_datasources == null)
            {
                throw new ConfigurationException( "One or more " +
                        "'<datasource>' directives are not included in the " +
                        "component's configuration." );
            }
            else
            {
                if ( m_datasources.length == 0 ) {
                    throw new ConfigurationException( "One or more " +
                            "'<datasource>' directives are not included in the " +
                            "component's configuration." );
                }
            }
        }
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
        m_logger.debug( "Initializing..." );
        
        // First, let's load the DBCP's pooling driver class
        m_logger.debug( "Loading DBCP Pooling Driver class..." );
        Class.forName("org.apache.commons.dbcp.PoolingDriver");
        
        // loop through all the configured datasources...
        for ( int i = 0; i < m_datasources.length; i++ )
        {
            // this is the next datasource configuration object to process
            Configuration datasource = m_datasources[i];
            String name = datasource.getAttribute( "name" );
            boolean isDefault = datasource.getAttributeAsBoolean( "default", false );
            m_logger.debug( "Processing datasource [" + name + "]" +
                    " (default=" + isDefault + ")");
            
            // create an object pool that will actually hold our connections
            ObjectPool connectionPool = createObjectPool();
            m_logger.debug( "Object pool created..." );
            
            // load the underlying JDBC driver of the datasource
            loadJDBCDriver( datasource );
            m_logger.debug( "Underlying JDBC driver loaded..." );
            
            // create a driver manager connection factory that will be
            // used to actually create the database connection(s)
            ConnectionFactory connectionFactory = createConnectionFactory( datasource );
            m_logger.debug( "Connection factory created..." );
            
            // create a poolable connection factory wrapper
            PoolableConnectionFactory poolableConnectionFactory =
                createPoolableConnectionFactory( connectionFactory,
                                                 connectionPool,
                                                 datasource );
            m_logger.debug( "Poolable connection factory created..." );
            
            // get an instance of the DBCP pooling driver... 
            PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
            m_logger.debug( "Pooling driver instance obtained..." );
            
            // register our pool with it...
            driver.registerPool( name, connectionPool );
            
            // is this the default datasource?
            if ( isDefault )
            {
                driver.registerPool( "default", connectionPool );
            }
        }
	}

    //---------------------------------------------------------
    // Disposable
    //---------------------------------------------------------

    /**
     *  Cleans up the component. 
     */
    public void dispose() {
        try
        {
            PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
            // loop through all the configured datasources...
            for ( int i = 0; i < m_datasources.length; i++ )
            {
                Configuration datasource = m_datasources[i];
                String name = datasource.getAttribute( "name" );
                driver.closePool( name );
            }
        }
        catch ( Exception e )
        {
            m_logger.warn( e.getMessage() );
        }
    }
    
	/**
     * Returns a <code>java.sql.Connection</code> to the default data source.
     * 
     * @return a <code>java.sql.Connection</code> to the default data source
	 */
	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:apache:commons:dbcp:default");
	}

	/**
     * Returns a <code>java.sql.Connection</code> to the specified data source.
     * 
     * @param datasource the name of the data source to obtain a connection to
     * @return a <code>java.sql.Connection</code> to the specified data source
	 */
	public Connection getConnection(String datasource) throws SQLException {
        return DriverManager.getConnection("jdbc:apache:commons:dbcp:" + datasource);
	}

    /**
     * Returns an object pool configuration object populated with data
     * retrieved from the component's configuration.  Defaults correspond
     * to the <code>GenericObjectPool</code> default values.
     *   
     * @return <code>GenericObjectPool.Config</code> instance containing
     * the pool's configuration parameters
     */
    private GenericObjectPool.Config getPoolConfig()
    {
        GenericObjectPool.Config config = new GenericObjectPool.Config();
        if ( m_pool != null )
        {
            config.maxActive = m_pool.getAttributeAsInteger(
                    "max-active", GenericObjectPool.DEFAULT_MAX_ACTIVE );
            config.maxIdle = m_pool.getAttributeAsInteger(
                    "max-idle", GenericObjectPool.DEFAULT_MAX_IDLE );
            config.maxWait = m_pool.getAttributeAsLong(
                    "max-wait", GenericObjectPool.DEFAULT_MAX_WAIT );
            config.minEvictableIdleTimeMillis = m_pool.getAttributeAsLong(
                    "min-evict-idle-time", GenericObjectPool.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS );
            config.minIdle = m_pool.getAttributeAsInteger(
                    "min-idle", GenericObjectPool.DEFAULT_MIN_IDLE );
            config.numTestsPerEvictionRun = m_pool.getAttributeAsInteger(
                    "num-evict-tests", GenericObjectPool.DEFAULT_NUM_TESTS_PER_EVICTION_RUN );
            config.testOnBorrow = m_pool.getAttributeAsBoolean(
                    "test-on-borrow", GenericObjectPool.DEFAULT_TEST_ON_BORROW );
            config.testOnReturn = m_pool.getAttributeAsBoolean(
                    "test-on-return", GenericObjectPool.DEFAULT_TEST_ON_RETURN );
            config.testWhileIdle = m_pool.getAttributeAsBoolean(
                    "test-while-idle", GenericObjectPool.DEFAULT_TEST_WHILE_IDLE );
            config.timeBetweenEvictionRunsMillis = m_pool.getAttributeAsLong(
                    "time-between-evict-runs", GenericObjectPool.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS );
            config.whenExhaustedAction = GenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION;
        }
        return config;
    }
    
    /**
     * Creates an <code>GenericObjectPool</code> instance that serves
     * as a the actual pool of connections.
     * 
     * @return <code>GenericObjectPool</code> instance.
     */
    private ObjectPool createObjectPool()
    {
        ObjectPool pool = null;
        
        // We'll need a ObjectPool that serves as the actual pool
        // of connections.  We'll use a GenericObjectPool instance.
        if ( m_pool == null )
        {
            pool = new GenericObjectPool( null );
        }
        else
        {
            pool = new GenericObjectPool( null, getPoolConfig() );
        }
        return pool;
    }
    
    /**
     * Loads the underlying JDBC driver defined for the datasource.
     * 
     * @param datasource the datasource
     * @throws DriverNotFoundException if the datasource directive is missing
     * the 'driver' directive in the configuration
     * @throws ConfigurationException if an error occurs while retrieving
     * configuration information about this datasource
     * @throws ClassNotFoundException if the underlying JDBC driver class
     * cannot be loaded
     */
    private void loadJDBCDriver( final Configuration datasource )
        throws DriverNotFoundException, ConfigurationException,
        ClassNotFoundException
    {
        if ( datasource.getChild( "driver", false) == null)
        {
            throw new DriverNotFoundException( "JDBC Driver not defined " +
                    "for datasource " + datasource.getAttribute( "name" ) );
        }
        else
        {
            String driver = datasource.getChild( "driver" ).getValue();
            Class.forName( driver );
        }
        
    }
    
    /**
     * 
     * @param datasource
     * @return
     * @throws DbUrlNotFoundException
     * @throws ConfigurationException
     */
    private ConnectionFactory createConnectionFactory(
            final Configuration datasource ) throws DbUrlNotFoundException,
            ConfigurationException
    {
        ConnectionFactory connectionFactory = null;
        String dbUrl = null;
        
        if ( datasource.getChild( "db-url", false ) == null)
        {
            throw new DbUrlNotFoundException( "Database URL not defined " +
                    "for datasource " + datasource.getAttribute( "name" ) );
        }
        else
        {
            dbUrl = datasource.getChild( "db-url" ).getValue();
        }
        String uname = datasource.getChild( "username" ).getValue( "" );
        String pword = datasource.getChild(" password" ).getValue( "" );
        
        connectionFactory = new DriverManagerConnectionFactory( dbUrl, uname, pword);
        
        return connectionFactory;
    }
    
    /**
     * 
     * @param connFactory
     * @param pool
     * @param datasource
     * @return
     * @throws ConfigurationException
     */
    private PoolableConnectionFactory createPoolableConnectionFactory(
            ConnectionFactory connFactory, ObjectPool pool,
            Configuration datasource ) throws ConfigurationException
    {
        PoolableConnectionFactory factory = null;
        String query = null;
        if ( datasource.getChild( "validation-query", false) != null )
        {
            query = datasource.getChild( "validation-query" ).getValue();
        }
        boolean readonly = datasource.getChild( "read-only" ).getValueAsBoolean( false );
        boolean autocommit = datasource.getChild( "auto-commit" ).getValueAsBoolean( true );
        
        factory = new PoolableConnectionFactory( connFactory, pool,
                null, query, readonly, autocommit );
        return factory;
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

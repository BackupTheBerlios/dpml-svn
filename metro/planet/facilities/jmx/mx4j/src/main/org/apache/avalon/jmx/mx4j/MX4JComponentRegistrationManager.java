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
package org.apache.avalon.jmx.mx4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.context.Context;

import org.apache.avalon.jmx.ComponentRegistrationManager;
import org.apache.avalon.jmx.spi.AbstractJMXComponentRegistrationManager;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import mx4j.log.Log;

/**
 * A component manager using the MX4J implementation of JMX.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $
 *
 * @avalon.component name="jmx-mx4j-manager" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.jmx.ComponentRegistrationManager"
 * @avalon.attribute key="urn:composition:deployment.timeout" value="6000"
 */
public class MX4JComponentRegistrationManager extends AbstractJMXComponentRegistrationManager 
{
    private static final Resources REZ = 
      ResourceManager.getPackageResources( MX4JComponentRegistrationManager.class );
    private static final String DEFAULT_NAMING_FACTORY =
        "com.sun.jndi.rmi.registry.RegistryContextFactory";
    private static final String DEFAULT_HTTPADAPTER_HOST = "localhost";
    private static final int DEFAULT_HTTPADAPTER_PORT = 
      Integer.getInteger( "merlin.adapter.http", 8082 ).intValue();

    private String m_host;
    private int m_port;
    private boolean m_rmi;
    private File m_homeDir;
    private String m_stylesheetDir;
    private String m_namingFactory;
    private String m_password;
    private String m_username;
    private JMXConnectorServer m_connectorServer;

   /**
    * @avalon.entry key="urn:avalon:home" type="java.io.File"
    */
    public MX4JComponentRegistrationManager(
      final Logger logger, final Context context, final Parameters parameters )
      throws Exception
    {
        super( logger );

        m_homeDir = ( File ) context.get( "urn:avalon:home" );
        m_host = parameters.getParameter( "manager-adaptor-host", DEFAULT_HTTPADAPTER_HOST );
        m_port = parameters.getParameterAsInteger( "manager-adaptor-port", DEFAULT_HTTPADAPTER_PORT );
        getLogger().debug( "MX4J HTTP listener port: " + m_port );
        m_rmi = parameters.getParameterAsBoolean( "enable-rmi-adaptor", false );
        m_namingFactory = parameters.getParameter( "rmi-naming-factory", DEFAULT_NAMING_FACTORY );

        final String stylesheets = parameters.getParameter( "stylesheets-dir", null );
        if ( null != stylesheets )
        {
            m_stylesheetDir = new File( m_homeDir, stylesheets ).getAbsolutePath();
        }

        m_username = parameters.getParameter( "name", null );
        m_password = parameters.getParameter( "password", null );

        final MBeanServer mBeanServer = getMBeanServer();
        startHttpAdaptor( mBeanServer );
        if( m_rmi )
        {
            startRMIAdaptor( mBeanServer );
        }
        else
        {
            m_connectorServer = null;
        }
    }

    public void dispose()
    {
        final MBeanServer mBeanServer = getMBeanServer();
        stopHttpAdaptor( mBeanServer );
        if ( m_rmi )
        {
            stopRMIAdaptor( mBeanServer );
        }
        super.dispose();
    }

    private void startHttpAdaptor( final MBeanServer mBeanServer ) throws Exception
    {
        final ObjectName adaptorName = new ObjectName( "Http:name=HttpAdaptor" );
        mBeanServer.createMBean( "mx4j.tools.adaptor.http.HttpAdaptor", adaptorName, null );
        mBeanServer.setAttribute( adaptorName, new Attribute( "Host", m_host ) );
        mBeanServer.setAttribute( adaptorName, new Attribute( "Port", new Integer( m_port ) ) );

        if ( null != m_username )
        {
            configureAuthentication( mBeanServer, adaptorName );
        }

        configureProcessor( mBeanServer, adaptorName );

        // starts the server
        mBeanServer.invoke( adaptorName, "start", null, null );
    }

    private void configureProcessor( final MBeanServer mBeanServer, final ObjectName adaptorName ) throws
        Exception
    {
        final ObjectName processorName = new ObjectName( "Http:name=XSLTProcessor" );
        mBeanServer.createMBean( "mx4j.tools.adaptor.http.XSLTProcessor", processorName, null );
        mBeanServer.setAttribute( adaptorName, new Attribute( "ProcessorName", processorName ) );

        if ( null != m_stylesheetDir )
        {
            final Attribute stylesheetDir = new Attribute( "File", m_stylesheetDir );
            mBeanServer.setAttribute( processorName, stylesheetDir );
        }

        final Attribute useCache = new Attribute( "UseCache", Boolean.FALSE );
        mBeanServer.setAttribute( processorName, useCache );
    }

    private void configureAuthentication( final MBeanServer mBeanServer,
                                          final ObjectName adaptorName ) throws
        InstanceNotFoundException, MBeanException, ReflectionException, AttributeNotFoundException,
        InvalidAttributeValueException
    {
        // add user names
        mBeanServer.invoke( adaptorName, "addAuthorization", new Object[]
                            {m_username, m_password}
                            , new String[]
                            {"java.lang.String", "java.lang.String"} );

        // use basic authentication
        mBeanServer.setAttribute( adaptorName, new Attribute( "AuthenticationMethod", "basic" ) );
    }

    private void startRMIAdaptor( final MBeanServer server ) throws Exception
    {
        // Create and start the naming service
        final ObjectName naming = new ObjectName( "Naming:type=rmiregistry" );
        server.createMBean( "mx4j.tools.naming.NamingService", naming, null );
        server.invoke( naming, "start", null, null );

        // Create and start the JMXConnectorServer
        JMXServiceURL address = new JMXServiceURL( "rmi", "localhost", 0, "/jndi/jrmp" );
        Map environment = new HashMap();
        environment.put( javax.naming.Context.INITIAL_CONTEXT_FACTORY, m_namingFactory );
        environment.put( javax.naming.Context.PROVIDER_URL, "rmi://localhost:1099" );
        m_connectorServer = JMXConnectorServerFactory.newJMXConnectorServer( address, environment, server);
        m_connectorServer.start();
    }

    private void stopHttpAdaptor( final MBeanServer server )
    {
        stopJMXMBean( server, "Http:name=HttpAdaptor" );
    }

    private void stopRMIAdaptor( final MBeanServer server )
    {
        if ( m_connectorServer != null )
        {
            try
            {
                m_connectorServer.stop();
            }
            catch ( Exception ignored )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "JMXConnectorServer shutdown failed (ignoring)", ignored );
                }
            }
        }
        // stop the naming service
        stopJMXMBean( server, "Naming:type=rmiregistry" );
    }

    protected MBeanServer createMBeanServer() throws Exception
    {
        MX4JLoggerAdapter.setLogger( getLogger() );
        Log.redirectTo( new MX4JLoggerAdapter() );
        return MBeanServerFactory.createMBeanServer( "Merlin" );
    }

    private void stopJMXMBean( final MBeanServer mBeanServer, final String name )
    {
        try
        {
            final ObjectName objectName = new ObjectName( name );
            mBeanServer.invoke( objectName, "stop", null, null );
        }
        catch ( final Exception e )
        {
            final String message = REZ.getString( "jmxmanager.error.jmxmbean.dispose", name );
            getLogger().error( message, e );
        }
    }
}

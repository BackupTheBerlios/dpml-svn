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
package org.apache.avalon.jmx.spi;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.jmx.ComponentRegistrationException;
import org.apache.avalon.jmx.util.MBeanInfoBuilder;
import org.apache.avalon.jmx.util.Target;
import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * An abstract implementation of a JMX based Component Manager.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractJMXComponentRegistrationManager 
    extends AbstractComponentRegistrationManager 
    implements Disposable
{
    private static final Resources REZ = ResourceManager.getPackageResources(
        AbstractJMXComponentRegistrationManager.class );
        
    private MBeanInfoBuilder topicBuilder;
    private MBeanServer m_mBeanServer;
    private String m_domain = "Merlin";

    public AbstractJMXComponentRegistrationManager( final Logger logger )
      throws Exception
    {
        super( logger );

        final MBeanServer mBeanServer = createMBeanServer();
        setMBeanServer( mBeanServer );
        
        topicBuilder = new MBeanInfoBuilder( logger );
    }

    public void dispose()
    {
        setMBeanServer( null );
    }

    /**
     * Export the object to the particular management medium using
     * the supplied object and interfaces.
     * This needs to be implemented by subclasses.
     *
     * @param name the name of object
     * @param object the object
     * @param interfaces the interfaces
     * @return the exported object
     * @throws ComponentRegistrationException if an error occurs
     */
    protected Object export( final String name, final Object object, final Class[] interfaces ) 
        throws  ComponentRegistrationException
    {
        try
        {
            final Target target = createTarget( name, object, interfaces );
            exportTarget( target );
            return target;
        }
        catch ( final Exception e )
        {
            final String message = REZ.getString( "jmxmanager.error.export.fail", name );
            getLogger().error( message, e );
            throw new ComponentRegistrationException( message, e );
        }
    }

    /**
     * Stop the exported object from being managed.
     *
     * @param name the name of object
     * @param exportedObject the object return by export
     * @throws ComponentRegistrationException if an error occurs
     */
    protected void unexport( final String name, final Object exportedObject ) 
        throws ComponentRegistrationException
    {
        try
        {
            final Target target = ( Target ) exportedObject;
            final Set topicNames = target.getTopicNames();
            final Iterator i = topicNames.iterator();

            while ( i.hasNext() )
            {
                final ObjectName objectName = 
                  createObjectName( name, ( String ) i.next() );
                getMBeanServer().unregisterMBean( objectName );
            }
        }
        catch ( final Exception e )
        {
            final String message = 
              REZ.getString( "jmxmanager.error.unexport.fail", name );
            getLogger().error( message, e );
            throw new ComponentRegistrationException( message, e );
        }
    }

    /**
     * Verify that an interface conforms to the requirements of management medium.
     *
     * @param clazz the interface class
     * @throws ComponentRegistrationException if verification fails
     */
    protected void verifyInterface( final Class clazz ) 
        throws ComponentRegistrationException
    {
        //TODO: check it extends all right things and that it
        //has all the right return types etc. Blocks must have
        //interfaces extending Service (or Manageable)
    }

    protected MBeanServer getMBeanServer()
    {
        return m_mBeanServer;
    }

    protected void setMBeanServer( MBeanServer mBeanServer )
    {
        m_mBeanServer = mBeanServer;
    }

    protected String getDomain()
    {
        return m_domain;
    }

    protected void setDomain( final String domain )
    {
        m_domain = domain;
    }

    protected Class[] getManagementInterfaces( ComponentModel componentModel )
    {
        final Class objectClass = componentModel.getDeploymentClass();
        final Class[] allInterfaces = objectClass.getInterfaces();
        final Set mbeanInterfaces = new HashSet();
        for ( int i = 0; i < allInterfaces.length; i++ )
        {
            if ( allInterfaces[i].getName().endsWith( "MBean" ) )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    final String message = 
                      REZ.getString( 
                        "jmxmanager.debug.interface",
                        allInterfaces[i].getName() );
                    getLogger().debug( message );
                }

                mbeanInterfaces.add( allInterfaces[i] );
            }
        }

        return ( Class[] ) mbeanInterfaces.toArray( new Class[mbeanInterfaces.size()] );
    }

    protected String getName( ComponentModel componentModel ) 
        throws ComponentRegistrationException
    {
        String[] nameComponents = componentModel.getQualifiedName().split( "/" );
        String qName = componentModel.getQualifiedName();
        if ( qName.startsWith("/") )
        {
        	qName = qName.substring(1);
        }
        qName = qName.replace( '/', '.' );
        int idx = qName.lastIndexOf( '.' );
        StringBuffer name = new StringBuffer();
        if ( idx != -1 )
        {
        	name.append( "container=" ).append( qName.substring( 0, idx )).append( ',' );
        }
        name.append( "name=" ).append( qName.substring( idx + 1) );

        return name.toString();
    }

    /**
     * Creates a new MBeanServer.
     * The subclass should implement this to create specific MBeanServer.
     * @throws Exception if the MBeanServer cannot be created
     * @return the MBeanServer
     */
    protected abstract MBeanServer createMBeanServer() throws Exception;

    /**
     * Creates a target that can then be exported for management. A topic is created
     * for each interface and for topics specified in the mxinfo file, if present
     *
     * @param name name of the target
     * @param object managed object
     * @param interfaces interfaces to be exported
     * @return  the management target
     */
    protected Target createTarget( final String name, final Object object, Class[] interfaces )
    {
        final Target target = new Target( name, object );
        try
        {
            topicBuilder.build( target, object.getClass(), interfaces );
        }
        catch ( final Exception e )
        {
            getLogger().debug( e.getMessage(), e );
        }

        return target;
    }

    /**
     * Exports the target to the management repository.  This is done by exporting
     * each topic in the target.
     *
     * @param target the management target
     * @throws Exception if the Target cannot be exported
     */
    protected void exportTarget( final Target target ) 
        throws Exception
    {
        // loop through each topic and export it
        final Set topicNames = target.getTopicNames();
        final Iterator i = topicNames.iterator();
        while ( i.hasNext() )
        {
            final String topicName = ( String ) i.next();
            final DynamicMBean mBean = target.getTopic( topicName );
            final String targetName = target.getName();
            exportTopic( mBean, topicName, targetName );
        }
    }

    /**
     * Exports the topic to the management repository. The name of the topic in the
     * management repository will be the target name + the topic name
     *
     * @param topic the descriptor for the topic
     * @param target to be managed
     * @param targetName the target's name
     * @throws Exception if the Topic cannot be exported
     * @return the mBean
     */
    protected void exportTopic( final DynamicMBean mBean,
    		                    final String topicName,
                                final String targetName ) throws Exception
    {
        final ObjectName objectName = createObjectName( targetName, topicName );
        getMBeanServer().registerMBean( mBean, objectName );
    }

    /**
     * Create JMX name for object.
     *
     * @param name the name of object
     * @param topic the topic
     * @return the {@link ObjectName} representing object
     * @throws MalformedObjectNameException if malformed name
     */
    private ObjectName createObjectName( final String name, final String topicName ) throws
        MalformedObjectNameException
    {
    	final String objectName = getDomain() + ":" + name + ",topic=" + topicName;
    	getLogger().debug("ObjectName=" + objectName);
        return new ObjectName( objectName );
    }
}

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
package org.apache.avalon.jmx.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.ParameterDescriptor;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Method;

import javax.management.Descriptor;
import javax.management.DynamicMBean;
import javax.management.MBeanParameterInfo;
import javax.management.StandardMBean;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.RequiredModelMBean;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;
import org.xml.sax.InputSource;

/**
 * An MBeanInfoBuilder is responsible for building Management Topic
 * objects from Configuration objects. The format for Configuration object
 * is specified in the MxInfo specification.  The information is loaded into
 * the Target structure.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $
 *
 * TODO: update JavaDoc
 */

public class MBeanInfoBuilder
{
    private static final Resources REZ = ResourceManager.getPackageResources( MBeanInfoBuilder.class );
    private static final String REQ_MODEL_MBEAN = RequiredModelMBean.class.getName();

    private Logger m_logger;
    
    public MBeanInfoBuilder( Logger logger )
    {
    	m_logger = logger;
    }
    
    protected Logger getLogger()
    {
    	return m_logger;
    }

    /**
     * Builds MBeans for the management interfaces of a class.
     *
     * @param target the Target in which to store generated MBeans
     * @param managedClass the Class for which to generate MBeans
     * @param interfaces the interface Classes to expose through the MBean
     * @throws ConfigurationException if the MBeans cannot be generated
     */
    public void build( final Target target, final Class managedClass, final Class[] interfaces ) throws
        ConfigurationException
    {
        final String notice = REZ.getString( "mxinfo.debug.building", managedClass.getName() );
        getLogger().debug( notice );

        // if the managed class has an mxinfo file, build the target from it
        // (this includes any proxies)
        Configuration config = loadMxInfo( managedClass );
        if ( null != config )
        {
            final String message = REZ.getString( "mxinfo.debug.found.mxinfo", managedClass.getName() );
            getLogger().debug( message );
            buildFromMxInfo( target, managedClass, config );
        }

        // for each interface, generate a topic from its mxinfo file
        // or through introspection
        for ( int i = 0, j = interfaces.length; i < j; i++ )
        {
            try
            {
                config = loadMxInfo( interfaces[i] );
                if ( config == null )
                {
                    buildFromIntrospection( target, interfaces[i] );
                }
                else
                {
                    buildFromMxInfo( target, managedClass, config );
                }
            }
            catch ( final Exception e )
            {
                final String message = REZ.getString( "mxinfo.error.target", target.getName() );
                getLogger().error( message, e );
                throw new ConfigurationException( message );
            }
        }
    }

    /**
     * Create a {@link ModelMBeanInfoSupport} object for specified classname from
     * specified configuration data.
     *
     * @param target the Target in which to store the generated MBean
     * @param managedClass the Class for which to create MBeans
     * @param config the Configuration to use to generate the MBeans
     * @throws ConfigurationException if there is an error creating MBeans
     */
    private void buildFromMxInfo( final Target target, final Class managedClass,
                                  final Configuration config ) throws ConfigurationException
    {
        BeanInfo beanInfo;
        try
        {
            beanInfo = Introspector.getBeanInfo( managedClass );
        }
        catch ( final Exception e )
        {
            final String message = REZ.getString( "mxinfo.error.introspect", managedClass.getName() );
            throw new ConfigurationException( message, e );
        }

        // load each topic
        final Configuration[] topicsConfig = config.getChildren( "topic" );
        for ( int i = 0; i < topicsConfig.length; i++ )
        {
            final ModelMBeanInfo topic = buildTopic( topicsConfig[i], beanInfo );
            try
			{
	            final DynamicMBean mbean = createMBean( topic, target.getManagedResource() );
	            target.addTopic( topicsConfig[i].getAttribute( "name" ), mbean );
			}
            catch ( final Exception e)
			{
            	final String message = REZ.getString( "mxinfo.error.mbean", topic.getDescription() );
            	throw new ConfigurationException( message, e );
			}
        }

        // load each proxy
        final Configuration[] proxysConfig = config.getChildren( "proxy" );
        for ( int i = 0; i < proxysConfig.length; i++ )
        {
            final ModelMBeanInfo topic = buildProxyTopic( proxysConfig[i], managedClass );
            try
			{
	            final DynamicMBean mbean = createMBean( topic, target.getManagedResource() );
	            target.addTopic( proxysConfig[i].getAttribute( "name" ), mbean );
			}
            catch ( final Exception e)
			{
            	final String message = REZ.getString( "mxinfo.error.mbean", topic.getDescription() );
            	throw new ConfigurationException( message, e );
			}
        }

    }

    private DynamicMBean createMBean( final ModelMBeanInfo topic, final Object target ) throws Exception
    {
	    final String className = topic.getClassName();
	    // Load the ModelMBean implementation class
	    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
	    
	    final Class clazz;
	    try
	    {
	    	if ( null == cl )
	    	{
	    		clazz = Class.forName( className );
	    	}
	    	else
	    	{
	    		clazz = cl.loadClass( className );
	    	}
	    }
	    catch ( Exception e )
	    {
	        final String message = 
	          REZ.getString( "jmxmanager.error.mbean.load.class", className );
	        getLogger().error( message, e );
	        throw new Exception( message, e );
	    }
	
	    // Create a new ModelMBean instance
	    ModelMBean mbean = null;
	    try
	    {
	        mbean = ( ModelMBean ) clazz.newInstance();
	        mbean.setModelMBeanInfo( topic );
	    }
	    catch ( final Exception e )
	    {
	        final String message = 
	          REZ.getString( "jmxmanager.error.mbean.instantiate", className );
	        getLogger().error( message, e );
	        throw new Exception( message, e );
	    }
	
	    // Set the managed resource (if any)
	    try
	    {
	        if ( null != target )
	        {
	            mbean.setManagedResource( target, "ObjectReference" );
	        }
	    }
	    catch ( Exception e )
	    {
	        final String message = 
	          REZ.getString( "jmxmanager.error.mbean.set.resource", className );
	        getLogger().error( message, e );
	        throw new Exception( message, e );
	    }
	
	    return mbean;
	}

    /**
     * Builds a topic based on introspection of the interface.
     *
     * @param target the Target in which to store generated MBeans
     * @param interfaceClass the Class to introspect for management methods
     * @throws ConfigurationException if there is an error introspecting the class or generating MBeans
     */
    private void buildFromIntrospection( final Target target, final Class interfaceClass ) throws
        ConfigurationException
    {
        try
        {
        	StandardMBean mbean = new StandardMBean( target.getManagedResource(), interfaceClass );
        	String name = interfaceClass.getName();
        	name = name.substring(name.lastIndexOf('.') + 1);
            target.addTopic( name, mbean );
        }
        catch ( final Exception e )
        {
            final String message = REZ.getString( "mxinfo.error.topic", interfaceClass );
            throw new ConfigurationException( message, e );
        }
    }

    /**
     * A utility method to build a {@link ModelMBeanInfoSupport}
     * object from specified configuration and BeanInfo.
     *
     * @param config the Configuration describing the MBean
     * @param beanInfo the BeanInfo describing the MBean
     * @return the created ModelMBeanInfoSupport
     * @throws ConfigurationException if an error occurs
     */
    private ModelMBeanInfo buildTopic( final Configuration config, final BeanInfo beanInfo ) throws
        ConfigurationException
    {
        final ModelMBeanAttributeInfo[] attributes = buildAttributeInfos( config, beanInfo );

        final ModelMBeanOperationInfo[] operations = buildOperationInfos( config, beanInfo );

        final ModelMBeanConstructorInfo[] constructors = new ModelMBeanConstructorInfo[0];

        final ModelMBeanNotificationInfo[] notifications = new ModelMBeanNotificationInfo[0];

        final String name = config.getAttribute( "name" );
        final ModelMBeanInfoSupport topic = new ModelMBeanInfoSupport( REQ_MODEL_MBEAN, name,
            attributes, constructors, operations, notifications );

        return topic;
    }

    /**
     * Build a topic for a proxy management class.
     *
     * @param proxyTagConfig the configuration for the Proxy
     * @param managedClass the Class for which to generate the MBean Proxy
     * @return the created ModelMBeanInfoSupport
     * @throws ConfigurationException if there is a problem generating the Proxy
     */
    private ModelMBeanInfo buildProxyTopic( final Configuration proxyTagConfig,
                                            final Class managedClass ) throws
        ConfigurationException
    {
        try
        {
            final String proxyName = proxyTagConfig.getAttribute( "name" );
            final String message = REZ.getString( "mxinfo.debug.building.proxy.topic", proxyName );
            getLogger().debug( message );

            final Class proxyClass = managedClass.getClassLoader().loadClass( proxyName );
            final Configuration classConfig = loadMxInfo( proxyClass );
            final Configuration topicConfig = classConfig.getChild( "topic" );
            final BeanInfo info = Introspector.getBeanInfo( proxyClass );
            final ModelMBeanInfo topic = buildTopic( topicConfig, info );
            final Descriptor mBeanDescriptor = topic.getMBeanDescriptor();
            mBeanDescriptor.setField( "proxyClassName", proxyName );
            topic.setMBeanDescriptor( mBeanDescriptor );

            return topic;
        }
        catch ( final Exception e )
        {
            if ( e instanceof ConfigurationException )
            {
                throw ( ConfigurationException ) e;
            }
            else
            {
                final String message = REZ.getString( "mxinfo.error.proxy", managedClass.getName() );
                throw new ConfigurationException( message );
            }
        }
    }

    /**
     * Builds the management attributes from the configuration
     *
     * @param config topic's configuration element
     * @param info managed class' BeanInfo from introspector
     * @throws ConfigurationException
     */
    private ModelMBeanAttributeInfo[] buildAttributeInfos( final Configuration config,
                                                           final BeanInfo info ) throws
        ConfigurationException
    {
        final Configuration[] attributesConfig = config.getChildren( "attribute" );

        final ModelMBeanAttributeInfo[] attributeList = new ModelMBeanAttributeInfo[
                                                        attributesConfig.length];

        final PropertyDescriptor[] propertys = info.getPropertyDescriptors();
        for ( int i = 0; i < attributesConfig.length; i++ )
        {
            final Configuration attribute = attributesConfig[i];
            final String name = attribute.getAttribute( "name" );
            final PropertyDescriptor property = getPropertyDescriptor( name, propertys );
            attributeList[i] = buildAttributeInfo( property, attribute );
        }

        return attributeList;
    }

    /**
     * Builds a management config
     *
     * @param property from BeanInfo
     * @param config configuration element - can be null, in which case defaults are used
     */
    private ModelMBeanAttributeInfo buildAttributeInfo( final PropertyDescriptor property,
                                                        final Configuration config )
    {
        final String name = property.getName();
        final Method readMethod = property.getReadMethod();
        final Method writeMethod = property.getWriteMethod();
        final Class propertyType = property.getPropertyType();

        //indexed property
        //TODO: create a ModelMBeanOperationInfo
        if (null == propertyType)
        {
                return null;
        }
        
        final String type = property.getPropertyType().getName();

        String description = property.getDisplayName();
        boolean isReadable = ( readMethod != null );
        boolean isWriteable = ( writeMethod != null );

        if ( config != null )
        {
            // use config info, or BeanInfo if config info is missing
            description = config.getAttribute( "description", description );

            // defaults to true if there is a read method, otherwise defaults to false
            isReadable = config.getAttributeAsBoolean( "isReadable", true ) && isReadable;

            // defaults to true if there is a write method, otherwise defaults to false
            isWriteable = config.getAttributeAsBoolean( "isWriteable", true ) && isWriteable;
        }

        final boolean isIs = ( readMethod != null ) && readMethod.getName().startsWith( "is" );

        final ModelMBeanAttributeInfo info = new ModelMBeanAttributeInfo( name, type, description,
            isReadable, isWriteable, isIs );

        // additional info needed for modelMbean to work
        final Descriptor descriptor = info.getDescriptor();
        descriptor.setField( "currencyTimeLimit", new Integer( 1 ) );
        if ( isReadable )
        {
            descriptor.setField( "getMethod", readMethod.getName() );
        }
        if ( isWriteable )
        {
            descriptor.setField( "setMethod", writeMethod.getName() );
        }
        info.setDescriptor( descriptor );

        return info;
    }

    /**
     *  Returns the PropertyDescriptor with the specified name from the array
     */
    private PropertyDescriptor getPropertyDescriptor( final String name,
                                                      final PropertyDescriptor[] propertys ) throws
        ConfigurationException
    {
        for ( int i = 0; i < propertys.length; i++ )
        {
            if ( propertys[i].getName().equals( name ) )
            {
                return propertys[i];
            }
        }

        final String message = REZ.getString( "mxinfo.error.missing.property", name );
        throw new ConfigurationException( message );
    }

    /**
     * Builds the management operations
     *
     * @param config topic configuration element to build from
     * @param info BeanInfo for managed class from introspector
     * @throws ConfigurationException
     */
    private ModelMBeanOperationInfo[] buildOperationInfos( final Configuration config,
                                                           final BeanInfo info ) throws
        ConfigurationException
    {
        final Configuration[] operationsConfig = config.getChildren( "operation" );

        final ModelMBeanOperationInfo[] operations = new ModelMBeanOperationInfo[operationsConfig.
                                                     length];

        final MethodDescriptor[] methodDescriptors = info.getMethodDescriptors();

        for ( int i = 0; i < operationsConfig.length; i++ )
        {
            final Configuration operation = operationsConfig[i];
            final String name = operation.getAttribute( "name" );
            final MethodDescriptor method = getMethodDescriptor( name, methodDescriptors );
            operations[i] = buildOperationInfo( method, operation );
        }

        return operations;
    }

    /**
     * Builds an operation descriptor from a configuration node
     *
     * @param method method as returned from beanInfo
     * @param config configuration element, can be null
     * @throws ConfigurationException if the configiration has the wrong elements
     * @return  the operation descriptor based on the configuration
     */
    private ModelMBeanOperationInfo buildOperationInfo( final MethodDescriptor method,
                                                        final Configuration config ) throws
        ConfigurationException
    {
        ModelMBeanOperationInfo info;

        if ( config == null )
        {
            final Class[] methodSignature = method.getMethod().getParameterTypes();
            final ParameterDescriptor[] paramDescriptors = method.getParameterDescriptors();
            if (methodSignature.length == 0
                    || null == paramDescriptors
                    || methodSignature.length != paramDescriptors.length)
            {
                info = new ModelMBeanOperationInfo( method.getDisplayName(), method.getMethod() );
            }
            else
            {
                final String name = method.getName();
                final String type = method.getMethod().getReturnType().getName();
                final String description = method.getDisplayName();
                final int impact = ModelMBeanOperationInfo.UNKNOWN;
                final MBeanParameterInfo[] params = new MBeanParameterInfo[methodSignature.length];
                for (int i = 0; i < methodSignature.length; i++)
                {
                    params[i] = buildParameterInfo( methodSignature[i], paramDescriptors[i] );
                }

                info = new ModelMBeanOperationInfo( name, description, params, type, impact );
            }

        

        }
        else
        {
            final String name = method.getName();
            final String type = method.getMethod().getReturnType().getName();
            final String description = config.getAttribute( "description", method.getDisplayName() );
            final int impact = config.getAttributeAsInteger( "impact",
                                                             ModelMBeanOperationInfo.UNKNOWN );

            final Configuration[] paramConfig = config.getChildren( "param" );
            final MBeanParameterInfo[] params = new MBeanParameterInfo[paramConfig.length];
            for ( int i = 0; i < paramConfig.length; i++ )
            {
                params[i] = buildParameterInfo( paramConfig[i] );
            }

            info = new ModelMBeanOperationInfo( name, description, params, type, impact );
        }

        // additional info needed for modelMbean to work
        final Descriptor descriptor = info.getDescriptor();
        // TODO: might want to make this configurable. It controls the caching behavior
        // of the invoke results. MX4J appears to cache this per operation regardless
        // of what the invoke parameters are *SIGH* - PR
        descriptor.setField( "currencyTimeLimit", new Integer( 0 ) );
        info.setDescriptor( descriptor );
        return info;
    }

    /**
     *  Returns the MethodDescriptor with the specified name from the array
     */
    private MethodDescriptor getMethodDescriptor( final String name,
                                                  final MethodDescriptor[] methods ) throws
        ConfigurationException
    {

        for ( int i = 0; i < methods.length; i++ )
        {
            if ( methods[i].getName().equals( name ) )
            {
                return methods[i];
            }
        }
        final String message = REZ.getString( "mxinfo.error.missing.method", name );
        throw new ConfigurationException( message );
    }

    /**
     * Builds the param descriptor from the configuration data
     *
     * @throws ConfigurationException if configuration not structured corretly
     * @return the descriptor
     */
    private MBeanParameterInfo buildParameterInfo( Configuration paramConfig ) throws
        ConfigurationException
    {
        final String name = paramConfig.getAttribute( "name" );
        final String description = paramConfig.getAttribute( "description" );
        final String type = paramConfig.getAttribute( "type" );

        return new MBeanParameterInfo( name, type, description );
    }

    /**
     * Builds the param descriptor from bean info.
     *
     * @throws ConfigurationException if configuration not structured corretly
     * @return the descriptor
     */
    private MBeanParameterInfo buildParameterInfo(Class parameterType, ParameterDescriptor descriptor)
    {
        final String name = descriptor.getDisplayName();
        final String description = descriptor.getShortDescription();
        final String type = parameterType.getName();

        return new MBeanParameterInfo( name, type, description );
    }

    /**
     * Returns the configuration for the class or null if there is no mxinfo
     * file for it.
     *
     * @param clazz the class to load the configuration for
     * @throws ConfigurationException
     * @return the configuration file, or null if none exists
     */
    private Configuration loadMxInfo( final Class clazz ) throws ConfigurationException
    {
        final String mxinfoName = "/" + clazz.getName().replace( '.', '/' ) + ".mxinfo";
        try
        {
            InputStream stream = clazz.getResourceAsStream( mxinfoName );
            if ( null == stream )
            {
                return null;
            }

            final InputSource source = new InputSource( stream );

            // build with validation against DTD
            return ConfigurationBuilder.build( source, true );
        }
        catch ( Exception e )
        {
            final String message = REZ.getString( "mxinfo.error.file", mxinfoName );
            getLogger().error( message, e );
            throw new ConfigurationException( message );
        }
    }

    /**
     *  Returns the class name without the package name
     */
    private String getShortName( final String className )
    {
        return className.substring( className.lastIndexOf( '.' ) + 1 );
    }
}

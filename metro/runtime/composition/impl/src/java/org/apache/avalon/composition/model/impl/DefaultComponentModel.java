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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.lang.reflect.Constructor;

import org.apache.avalon.composition.data.DeploymentProfile;
import org.apache.avalon.composition.data.DependencyDirective;
import org.apache.avalon.composition.data.StageDirective;
import org.apache.avalon.composition.data.ContextDirective;
import org.apache.avalon.composition.data.Mode;
import org.apache.avalon.composition.info.DeliveryDescriptor;
import org.apache.avalon.composition.info.StagedDeliveryDescriptor;
import org.apache.avalon.composition.model.ContextModel;
import org.apache.avalon.composition.model.DependencyModel;
import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.ModelException;
import org.apache.avalon.composition.model.ModelRuntimeException;
import org.apache.avalon.composition.model.StageModel;
import org.apache.avalon.composition.provider.ComponentContext;
import org.apache.avalon.composition.provider.SecurityModel;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.meta.info.ContextDescriptor;
import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.InfoDescriptor;
import org.apache.avalon.meta.info.ServiceDescriptor;
import org.apache.avalon.meta.info.ReferenceDescriptor;
import org.apache.avalon.meta.info.StageDescriptor;
import org.apache.avalon.meta.info.Type;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.avalon.util.configuration.ConfigurationUtil;
import org.apache.avalon.util.configuration.CascadingConfiguration;

/**
 * Deployment model defintion.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultComponentModel.java 31079 2004-08-01 17:04:08Z niclas $
 */
public class DefaultComponentModel extends DefaultDeploymentModel 
  implements ComponentModel
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( DefaultComponentModel.class );

    private static final String CONTEXTUALIZABLE = 
     "org.apache.avalon.framework.context.Contextualizable";

    private static final Configuration EMPTY_CONFIGURATION =
      new DefaultConfiguration( 
        "configuration", ComponentModel.class.getName() );

    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private final ComponentContext m_context;

    private final ContextModel m_contextModel;

    private final DependencyModel[] m_dependencies;

    private final StageModel[] m_stages;

    private final DefaultState m_assembly = new DefaultState();

    //--------------------------------------------------------------
    // mutable state
    //--------------------------------------------------------------

    private Configuration m_config;

    private Parameters m_parameters;

    private boolean m_activation;

    private int m_collection;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

   /**
    * Creation of a new deployment model.
    *
    * @param context the deployment context
    */
    public DefaultComponentModel( ComponentContext context, SecurityModel security )
      throws ModelException
    {
        super( context, security );

        m_context = context;

        m_activation = getDefaultActivationPolicy();

        setCollectionPolicy( m_context.getComponentProfile().getCollectionPolicy() );

        ClassLoader classLoader = m_context.getClassLoader();

        if( isConfigurable() )
        {
            final Configuration defaults = 
              m_context.getType().getConfiguration();
            final Configuration explicit = 
              m_context.getComponentProfile().getConfiguration();
            final Configuration consolidated = 
              consolidateConfigurations( explicit, defaults );
            if( consolidated != null )
            {
                m_config = consolidated;
            }
            else
            {
                m_config = EMPTY_CONFIGURATION;
            }
        }

        if( isParameterizable() )
        {
            Parameters staticDefaults = m_context.getType().getParameters();
            final Parameters parameters = 
              m_context.getComponentProfile().getParameters();
            if( parameters != null )
            {
                if( null == staticDefaults )
                {
                    m_parameters = parameters;
                }
                else
                {
                    m_parameters = new Parameters();
                    m_parameters.merge( staticDefaults );
                    m_parameters.merge( parameters );
                }
            }
            else
            {
                if( null == staticDefaults )
                {
                    m_parameters = Parameters.EMPTY_PARAMETERS;
                }
                else
                {
                    m_parameters = staticDefaults;
                }
            }
        }

        final ContextDescriptor contextDescriptor = 
          m_context.getType().getContext();
        final ContextDirective contextDirective = 
          m_context.getComponentProfile().getContext();
        final Logger log = getLogger().getChildLogger( "context" );
        m_contextModel = 
          new DefaultContextModel( 
            log, contextDescriptor, contextDirective, context );

        //
        // create the dependency models for subsequent assembly
        // management
        //

        DependencyDescriptor[] dependencies = 
          m_context.getType().getDependencies();
        m_dependencies = new DefaultDependencyModel[ dependencies.length ];

        for( int i=0; i<dependencies.length; i++ )
        {
            DependencyDescriptor descriptor = dependencies[i];
            DependencyDirective directive = 
              context.getComponentProfile().getDependencyDirective( descriptor.getKey() );
            m_dependencies[i] = 
              new DefaultDependencyModel( 
                context.getLogger().getChildLogger( "deps" ), 
                context.getPartitionName(), 
                context.getProfile().getName(), 
                descriptor,
                directive );
        }

        //
        // create the stage models for subsequent assembly
        // management
        //

        StageDescriptor[] stages = 
          m_context.getType().getStages();
        m_stages = new DefaultStageModel[ stages.length ];

        for( int i=0; i<stages.length; i++ )
        {
            StageDescriptor descriptor = stages[i];
            StageDirective directive = 
              context.getComponentProfile().getStageDirective( descriptor.getKey() );
            m_stages[i] = 
              new DefaultStageModel( 
                context.getLogger().getChildLogger( "stages" ), 
                context.getPartitionName(), descriptor, directive );
        }
    }

   /**
    * Get the default activation policy for the model. 
    */
    public boolean getDefaultActivationPolicy()
    {
        final int activation = 
          m_context.getComponentProfile().getActivationDirective();

        if( activation != DeploymentProfile.DEFAULT )
        {
            return (activation == DeploymentProfile.ENABLED );
        }
        else
        {
            if( m_context.getProfile().getMode() == Mode.EXPLICIT )
            {
                Type type = m_context.getType();
                if( type.getInfo().getLifestyle().equals( InfoDescriptor.TRANSIENT ) )
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
            else
            {
                return false;
            }
        }
    }

    //--------------------------------------------------------------
    // Commissionable
    //--------------------------------------------------------------

   /**
    * Commission the model. 
    *
    * @exception Exception if a commissioning error occurs
    */
    public void commission() throws Exception
    {
        if( isAssembled() )
        {
            super.commission();
        }
        else
        {
            final String error = 
              "Illegal attempt to commission a non-assembled component model: "
              + this;
            throw new ModelException( error );
        }
    }

    //--------------------------------------------------------------
    // Composite
    //--------------------------------------------------------------

    /**
     * Returns the assembled state of the model.
     * @return true if this model is assembled
     */
    public boolean isAssembled()
    {
        return ( isContextAssembled() 
          && isStageAssembled()
          && isServiceAssembled() );
    }

    private boolean isContextAssembled()
    {
        ContextModel model = getContextModel();
        if( model.isEnabled() )
        {
            DeliveryDescriptor delivery = 
              getContextModel().getDeliveryDescriptor();
            if( delivery instanceof StagedDeliveryDescriptor )
            {
                return ( null != getContextModel().getProvider() );
            }
        }
        return true;
    }

    private boolean isStageAssembled()
    {
        StageModel[] stages = getStageModels();
        for( int i=0; i<stages.length; i++ )
        {
            if( null == stages[i].getProvider() ) 
                return false;
        }
        return true;
    }

    private boolean isServiceAssembled()
    {
        DependencyModel[] dependencies = getDependencyModels();
        for( int i=0; i<dependencies.length; i++ )
        {
            DependencyModel dep = dependencies[i];
            if( null == dep.getProvider() && 
                dep.getDependency().isRequired() ) 
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the set of models assigned as providers.
     * @return the providers consumed by the model
     * @exception IllegalStateException if the model is not in an assembled state 
     */
    public DeploymentModel[] getProviders()
    {
        if( !isAssembled() ) 
        {
             final String error = 
               "Model is not assembled " + this;
             throw new IllegalStateException( error );
        }

        final ArrayList list = new ArrayList();
        if( getContextModel().isEnabled() )
        {
            DeploymentModel provider = getContextModel().getProvider();
            if( provider != null )
            {
                list.add( provider );
            }
        }
        
        StageModel[] stages = getStageModels();
        for( int i=0; i<stages.length; i++ )
        {
            StageModel stage = stages[i];
            list.add( stage.getProvider() );
        }

        DependencyModel[] dependencies = getDependencyModels();
        for( int i=0; i<dependencies.length; i++ )
        {
            DependencyModel dependency = dependencies[i];
            DeploymentModel provider = dependency.getProvider();
            if( provider != null )
                list.add( provider );
        }

        return (DeploymentModel[]) list.toArray( new DeploymentModel[0] );
    }

    //--------------------------------------------------------------
    // DeploymentModel
    //--------------------------------------------------------------

   /**
    * Return the set of services produced by the model.
    *
    * @return the service descriptors
    */
    public ServiceDescriptor[] getServices()
    {
        return m_context.getType().getServices();
    }

   /**
    * Return TRUE is this model is capable of supporting a supplied 
    * depedendency.
    *
    * @param dependency the dependency descriptor
    * @return true if this model can fulfill the dependency
    */
    public boolean isaCandidate( DependencyDescriptor dependency )
    {
        return isaCandidate( dependency.getReference() );
    }

   /**
    * Return TRUE is this model is capable of supporting a supplied 
    * stage dependency.
    *
    * @param stage the stage descriptor
    * @return TRUE if this model can fulfill the stage dependency
    */
    public boolean isaCandidate( StageDescriptor stage )
    {
        return m_context.getType().getExtension( stage ) != null;
    }

   /**
    * Return TRUE is this model is capable of supporting a supplied 
    * service.
    *
    * @param reference the service reference descriptor
    * @return true if this model can fulfill the service
    */
    public boolean isaCandidate( ReferenceDescriptor reference )
    {
        return m_context.getType().getService( reference ) != null;
    }

    //--------------------------------------------------------------
    // ComponentModel
    //--------------------------------------------------------------

   /**
    * Return the collection policy for the model. If a profile
    * does not declare a collection policy, then the collection
    * policy declared by the underlying component type
    * will be used.
    *
    * @return the collection policy
    * @see InfoDescriptor#WEAK
    * @see InfoDescriptor#SOFT
    * @see InfoDescriptor#HARD
    * @see InfoDescriptor#UNDEFINED
    */
    public int getCollectionPolicy()
    {
        if( m_collection == InfoDescriptor.UNDEFINED )
        {
            return getTypeCollectionPolicy();
        }
        else
        {
            return m_collection;
        }
    }

   /**
    * Set the collection policy for the model.
    *
    * @param policy the collection policy
    * @see InfoDescriptor#WEAK
    * @see InfoDescriptor#SOFT
    * @see InfoDescriptor#HARD
    */
    public void setCollectionPolicy( int policy )
    {
        if( policy == InfoDescriptor.UNDEFINED )
        {
            m_collection = InfoDescriptor.UNDEFINED;
        }
        else
        {
            int minimum = getTypeCollectionPolicy();
            if( policy >= minimum )
            {
                m_collection = policy;
            }
            else
            {
                final String warning = 
                  "Ignoring collection policy override [" + policy 
                  + "] because the value is higher that type threshhold [" 
                  + minimum + "].";
                getLogger().warn( warning );
            }
        }
    }

    private int getTypeCollectionPolicy()
    {
        return m_context.getType().getInfo().getCollectionPolicy();
    }
    
   /**
    * Return the proxy enabled policy for the model. If the system wide
    * proxy enabled is disabled the operation will return false otherwise the 
    * value returned is true unless overriden by the "urn:composition:proxy"
    * attribute.
    *
    * @return the proxy policy
    */
    public boolean getProxyPolicy()
    {
        if( m_context.getSystemContext().isProxyEnabled() )
        {
            if( getType().getInfo().getAttribute( PROXY_KEY, "true" ).equals( "false" ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }        
    }

   /**
    * Return the activation policy for the model. 
    * @return the activaltion policy
    */
    public boolean getActivationPolicy()
    {
        return m_activation;
    }

   /**
    * Set the activation policy for the model.
    *
    * @param policy the activaltion policy
    */
    public void setActivationPolicy( boolean policy )
    {
        m_activation = policy;
    }

   /**
    * Return the component type descriptor.
    * @return the type descriptor
    */
    public Type getType()
    {
        return m_context.getType();
    }

   /**
    * Return the class for the deployable target.
    * @return the class
    */
    public Class getDeploymentClass()
    {
        return m_context.getDeploymentClass();
    }

   /**
    * Rest if the component type backing the model is 
    * parameterizable.
    *
    * @return TRUE if the compoent type is parameterizable
    *   otherwise FALSE
    */
    public boolean isParameterizable()
    {
        return ( 
          Parameterizable.class.isAssignableFrom( getDeploymentClass() ) 
          || isaConstructorArg( Parameters.class ) );
    }

   /**
    * Set the parameters to the supplied value.  The supplied 
    * parameters value will replace the existing parameters value.
    *
    * @param parameters the supplied parameters value
    * @exception IllegalStateException if the component type backing the 
    *   model does not implement the parameteriazable interface
    * @exception NullPointerException if the supplied parameters are null
    */
    public void setParameters( Parameters parameters )
    {
        setParameters( parameters, true );
    }

   /**
    * Set the parameters to the supplied value.  The supplied 
    * parameters value may suppliment or replace the existing 
    * parameters value.
    *
    * @param parameters the supplied parameters
    * @param merge if TRUE the supplied parameters are merged with existing 
    *    parameters otherwise the supplied parameters replace any existing
    *    parameters
    * @exception IllegalStateException if the component type backing the 
    *   model does not implement the parameteriazable interface
    * @exception NullPointerException if the supplied parameters are null
    */
    public void setParameters( Parameters parameters, boolean merge )
      throws IllegalStateException
    {
        if( !isParameterizable() )
        {
            final String error = 
              REZ.getString( 
                "deployment.parameters.irrational", 
                getDeploymentClass().getName(), 
                this.toString() );
            throw new IllegalStateException( error );
        }

        if( parameters == null )
        {
            throw new NullPointerException( "parameters" );
        }

        if( merge )
        {
            Properties props = Parameters.toProperties( m_parameters );
            Properties suppliment = Parameters.toProperties( parameters );
            Enumeration list = suppliment.propertyNames();
            while( list.hasMoreElements() )
            {
                String name = (String) list.nextElement();
                String value = suppliment.getProperty( name );
                if( value == null )
                {
                    props.remove( name );
                }
                else
                {
                    props.setProperty( name, value );
                }
            }
            m_parameters = Parameters.fromProperties( props );
        }
        else
        {        
            m_parameters = parameters;
        }
    }

   /**
    * Return the parameters to be applied to the component.
    *
    * @return the assigned parameters
    */
    public Parameters getParameters()
    {
        Parameters params = new Parameters();
        if( null != m_parameters )
        {
            params.merge( m_parameters );
        }
        params.makeReadOnly();
        return params;
    }

   /**
    * Rest if the component type backing the model is 
    * configurable.
    *
    * @return TRUE if the component type is configurable
    *   otherwise FALSE
    */
    public boolean isConfigurable()
    {
        return ( 
          Configurable.class.isAssignableFrom( getDeploymentClass() ) 
          || isaConstructorArg( Configuration.class ) );
    }

   /**
    * Set the configuration to the supplied value.  The supplied 
    * configuration will replace the existing configuration.
    *
    * @param config the supplied configuration
    * @exception IllegalStateException if the component type backing the 
    *   model does not implement the configurable interface
    * @exception NullPointerException if the supplied configuration is null
    */
    public void setConfiguration( Configuration config )
      throws IllegalStateException, NullPointerException
    {
        setConfiguration( config, true );
    }

   /**
    * Set the configuration to the supplied value.  The supplied 
    * configuration may suppliment or replace the existing configuration.
    *
    * @param config the supplied configuration
    * @param policy if FALSE the supplied configuration replaces the current
    *   configuration otherwise the resoved configuration shall be layed above
    *   the configuration supplied with the profile which in turn is layer above 
    *   the type default configuration (if any)
    * @exception IllegalStateException if the component type backing the 
    *   model does not implement the configurable interface
    * @exception NullPointerException if the supplied configuration is null
    */
    public void setConfiguration( Configuration config, boolean policy )
      throws IllegalStateException, NullPointerException
    {
        if( !isConfigurable() )
        {
            final String error = 
              REZ.getString( 
                "deployment.configuration.irrational", 
                getDeploymentClass().getName(), 
                this.toString() );
            throw new IllegalStateException( error );
        }

        if( config == null )
        {
            throw new NullPointerException( "config" );
        }

        if( policy )
        {
            m_config = consolidateConfigurations( config, m_config );
        }
        else
        {
            m_config = config;
        }
    }

   /**
    * Return the configuration to be applied to the component.
    * The implementation returns the current configuration state.
    * If the the component type does not implementation the 
    * Configurable interface, the implementation returns null. 
    *
    * @return the qualified configuration
    */
    public Configuration getConfiguration()
    {
        return m_config;
    }

   /**
    * Return the context model for this deployment model.
    * 
    * @return the context model if this model is context dependent, else
    *   the return value is null
    */
    public ContextModel getContextModel()
    {
        return m_contextModel;
    }

   /**
    * Return the dependency models for this component type.
    *
    * @return the dependency models
    */
    public DependencyModel[] getDependencyModels()
    {
        return m_dependencies;
    }

   /**
    * Return a dependency model matching the supplied descriptor. If 
    * no model matches the supplied descriptor the implementation
    * will return null.
    *
    * @param dependency the dependency descriptor
    * @return the matching stage model
    */
    public DependencyModel getDependencyModel( DependencyDescriptor dependency )
    {
        DependencyModel[] models = getDependencyModels();
        for( int i=0; i<models.length; i++ )
        {
            DependencyModel model = models[i];
            if( dependency.equals( model.getDependency() ) )
            {
                return model;
            }
        }
        return null;
    }


   /**
    * Return the stage models for this component type.
    *
    * @return the stage models
    */
    public StageModel[] getStageModels()
    {
        return m_stages;
    }

   /**
    * Return a stage model matching the supplied descriptor. If 
    * no stage model matches the supplied descriptor the implementation
    * will return null.
    *
    * @param stage the stage descriptor
    * @return the matching stage model
    */
    public StageModel getStageModel( StageDescriptor stage )
    {
        StageModel[] stages = getStageModels();
        for( int i=0; i<stages.length; i++ )
        {
            StageModel model = stages[i];
            if( stage.equals( model.getStage() ) )
            {
                return model;
            }
        }
        return null;
    }


   /**
    * Return the set of services produced by the model as a array of classes.
    *
    * @return the service classes
    */
    public Class[] getInterfaces()
    {
        //
        // TODO: add a SoftReference to hold the service class array
        // instad of generating each time
        //

        ClassLoader classLoader = m_context.getClassLoader();
        ArrayList list = new ArrayList();
        ServiceDescriptor[] services = getServices();
        for( int i=0; i<services.length; i++ )
        {
            final ServiceDescriptor service = services[i];
            final String classname = service.getReference().getClassname();
            list.add( getComponentClass( classLoader, classname ) );
        }

        //
        // if the component is an extension then add all implemented 
        // interfaces
        //

        if( getType().getExtensions().length > 0 )
        {
            Class[] interfaces = getDeploymentClass().getInterfaces();
            for( int i=0; i<interfaces.length; i++ )
            {
                list.add( interfaces[i] );
            }
        }

        return (Class[]) list.toArray( new Class[0] );
    }

   /** 
    * Return the deployment timeout value for the component.
    *
    * @return the default deployment timeout value
    */
    public long getDeploymentTimeout()
    {
        String value = 
          m_context.getType().getInfo().getAttribute( 
            DEPLOYMENT_TIMEOUT_KEY, null );
        if( null != value )
        {
            try
            {
                return Long.parseLong( value );
            }
            catch( NumberFormatException nfe )
            {
                final String error =
                  "Invalid timout parameter [" 
                  + value 
                  + "] in component type [" 
                  + m_context.getType()
                  + "].";
                throw new ModelRuntimeException( error, nfe );
            }
        }
        else
        {
            return super.getDeploymentTimeout();
        }
    }

    //==============================================================
    // implementation
    //==============================================================

   /**
    * Test to determin if the first constructor supports the context
    * base class as a parameter type.
    * @return TRUE or FALSE
    */
    private boolean isaConstructorArg( Class base )
    {
        if( null == base ) return false;
        Class clazz = getDeploymentClass();
        Constructor[] constructors = clazz.getConstructors();
        if( constructors.length == 0 ) return false;
        Constructor constructor = constructors[0];
        Class[] types = constructor.getParameterTypes();
        for( int i=0; i<types.length; i++ )
        {
            if( base.isAssignableFrom( types[i] ) ) return true;
        }
        return false;
    }

    private Class getComponentClass( ClassLoader classLoader, String classname )
    {
        if( classLoader == null )
        {
            throw new NullPointerException( "classLoader" );
        }
        if( classname == null )
        {
            throw new NullPointerException( "classname" );
        }

        try
        {
            return classLoader.loadClass( classname );
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
    }

    private Configuration consolidateConfigurations( 
      final Configuration primary, final Configuration defaults )
    {
        if( primary == null )
        {
            return defaults;
        }
        else
        {
            if( defaults == null )
            {
                 return primary;
            }
            else
            {
                return new CascadingConfiguration( primary, defaults );
            }
        }
    }
}

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

import java.security.Permissions;
import java.security.AccessControlContext;
import java.security.ProtectionDomain;

import org.apache.avalon.composition.data.Mode;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.Commissionable;
import org.apache.avalon.composition.provider.DeploymentContext;
import org.apache.avalon.composition.provider.SystemContext;
import org.apache.avalon.composition.provider.SecurityModel;

import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.avalon.logging.data.CategoriesDirective;
import org.apache.avalon.logging.provider.LoggingManager;


/**
 * Abstract model base class.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultDeploymentModel.java 30977 2004-07-30 08:57:54Z niclas $
 */
public abstract class DefaultDeploymentModel
  implements DeploymentModel
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( DefaultDeploymentModel.class );

    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private final DeploymentContext m_context;

    private final SecurityModel m_security;

    private final AccessControlContext m_access;

    //--------------------------------------------------------------
    // muttable state
    //--------------------------------------------------------------

    private Commissionable m_handler = null;

    private CategoriesDirective m_categories;

    private Logger m_logger;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

   /**
    * Creation of an abstract model.  The model associated a 
    * name and a partition.
    *
    * @param context the deployment context
    */
    public DefaultDeploymentModel( 
      DeploymentContext context, SecurityModel security )
    { 
        if( null == context )
        {
            throw new NullPointerException( "context" );
        }
        if( null == security )
        {
            throw new NullPointerException( "security" );
        }

        m_context = context;
        m_security = security;

        m_logger = m_context.getLogger();
        m_categories = m_context.getProfile().getCategories();

        Permissions permissions = security.getPermissions();
        ProtectionDomain domain = 
          new ProtectionDomain( null, permissions );
        ProtectionDomain[] domains = new ProtectionDomain[]{ domain };
        m_access = new AccessControlContext( domains );
    }

    //--------------------------------------------------------------
    // Commssionable
    //--------------------------------------------------------------

   /**
    * Commission the model. 
    *
    * @exception Exception if a commissioning error occurs
    */
    public void commission() throws Exception
    {
        m_context.getSystemContext().commission( this );
    }

   /**
    * Decommission the model.  Once an model is 
    * decommissioned it may be re-commissioned.
    */
    public void decommission()
    {
        m_context.getSystemContext().decommission( this );
    }

    //--------------------------------------------------------------
    // Resolver
    //--------------------------------------------------------------

    /**
     * Resolve a object to a value.
     *
     * @return the resolved object
     * @throws Exception if an error occurs
     */
    public Object resolve() throws Exception
    {
        return m_context.getSystemContext().resolve( this );
    }

    /**
     * Resolve a object to a value.
     *
     * @param proxy if TRUE ruturn a proxied reference if the underlying component
     *   suppports proxied representation otherwise return the raw component instance
     * @return the resolved object
     * @throws Exception if an error occurs
     */
    public Object resolve( boolean proxy ) throws Exception
    {
        return m_context.getSystemContext().resolve( this, proxy );
    }

    /**
     * Release an object
     *
     * @param instance the object to be released
     */
    public void release( Object instance )
    {
        m_context.getSystemContext().release( this, instance );
    }

    //--------------------------------------------------------------
    // DeploymentModel
    //--------------------------------------------------------------

   /**
    * Return the profile name.
    * @return the name
    */
    public String getName()
    {
        return m_context.getName();
    }

   /**
    * Return the profile path.
    * @return the path
    */
    public String getPath()
    {
        if( null == m_context.getPartitionName() )
        {
            return SEPARATOR;
        }
        return m_context.getPartitionName();
    }

   /**
    * Return the model fully qualified name.
    * @return the fully qualified name
    */
    public String getQualifiedName()
    {
        return getPath() + getName();
    }

   /**
    * Return the mode of establishment.
    * @return the mode
    */
    public Mode getMode()
    {
        return m_context.getMode();
    }

   /**
    * Return the set of models consuming this model.
    * @return the consumers
    */
    public DeploymentModel[] getConsumerGraph()
    {
        return m_context.getDependencyGraph().getConsumerGraph( this );
    }

   /**
    * Return the set of models supplying this model.
    * @return the providers
    */
    public DeploymentModel[] getProviderGraph()
    {
        return m_context.getDependencyGraph().getProviderGraph( this );
    }

   /**
    * Return the assigned logging channel.
    * @return the logging channel
    */
    public Logger getLogger()
    {
        return m_logger;
    }

   /**
    * Return the assigned permissions.
    * @return the permissions
    */
    public Permissions getPermissions()
    {
        return m_security.getPermissions();
    }

   /**
    * Return the access control context.
    * @return the access control context
    */
    public AccessControlContext getAccessControlContext()
    {
        return m_access;
    }


    //--------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------

    public String toString()
    {
        return "[" + getQualifiedName() + "]";
    }

    public boolean equals( Object other )
    {
        boolean equal = super.equals( other ); 
        return equal;
    }

   /** 
    * Return the default deployment timeout value declared in the 
    * kernel configuration.  The implementation looks for a value
    * assigned under the property key "urn:composition:deployment.timeout"
    * and defaults to 1000 msec if undefined.
    *
    * @return the default deployment timeout value
    */
    public long getDeploymentTimeout()
    {
        SystemContext system = m_context.getSystemContext();
        return system.getDefaultDeploymentTimeout();
    }

   /**
    * Return the logging categories. 
    * @return the logging categories
    */
    public CategoriesDirective getCategories()
    {
        if( m_categories == null ) 
          return m_context.getProfile().getCategories();
        return m_categories;
    }

   /**
    * Set categories. 
    * @param categories the logging categories
    */
    public void setCategories( CategoriesDirective categories )
    {
        m_categories = categories;
        
        LoggingManager logging = 
          m_context.getSystemContext().getLoggingManager();
        final String path = getQualifiedName();
        logging.addCategories( path, categories );
        m_logger = logging.getLoggerForCategory( path );
    }
}

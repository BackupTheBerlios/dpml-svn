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

package org.apache.avalon.activation.impl;

import java.lang.reflect.Proxy;

import org.apache.avalon.activation.ApplianceException;

import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.TransientRuntimeException;
import org.apache.avalon.composition.provider.LifestyleManager;
import org.apache.avalon.composition.util.DefaultState;

import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * Abstract appliance.
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultAppliance.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultAppliance extends AbstractAppliance
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( 
        DefaultAppliance.class );

    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    private final ComponentModel m_model;

    private final LifestyleManager m_lifestyle;

    private final DefaultState m_commissioned = new DefaultState();

    private long m_delay = 0;

    private final boolean m_secure;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

    public DefaultAppliance( 
      ComponentModel model, LifestyleManager lifestyle, boolean secure )
    {
        super( model );
        m_model = model;
        m_lifestyle = lifestyle;
        m_secure = secure;
    }

    //-------------------------------------------------------------------
    // Commissionable
    //-------------------------------------------------------------------

   /**
    * Commission the appliance. 
    *
    * @exception Exception if a commissioning error occurs
    */
    public void commission() throws Exception
    {
        synchronized( m_commissioned )
        {
            if( m_commissioned.isEnabled() ) return;

            try
            {
                m_delay = m_model.getDeploymentTimeout();
                m_lifestyle.commission();
                m_delay = 0;
                m_commissioned.setEnabled( true );
            }
            finally
            {
                m_delay = 0;
            }
        }
    }

   /**
    * Decommission the appliance.  Once an appliance is 
    * decommissioned it may be re-commissioned.
    */
    public void decommission()
    {
        synchronized( m_commissioned )
        {
            if( !m_commissioned.isEnabled() ) return;
            m_lifestyle.decommission();
            m_commissioned.setEnabled( false );
        }
    }

    //-------------------------------------------------------------------
    // Resolver
    //-------------------------------------------------------------------


    /**
     * Resolve a object to a value.
     *
     * @return the resolved object
     * @throws Exception if an error occurs
     */
    public Object resolve() throws Exception
    {
        //
        // handle the legacy 3.3.0 usage of the "urn:activation:proxy" key
        // if after handle the 3.4.0 semantics for resolution against the model
        //

        if( getComponentModel().getType().getInfo().
              getAttribute( "urn:activation:proxy", "true" ).equals( "false" ) )
        {
            final String message = 
              "Component type references the deprecated 'urn:activation:proxy' key."
              + " Please update to the key '" + ComponentModel.PROXY_KEY + "'.";
            getLogger().warn( message );
            return resolve( false );
        }
        else        
        {
            return resolve( getComponentModel().getProxyPolicy() );
        }
    }

    public Object resolve( boolean proxy ) throws Exception
    {
        if( !proxy )
        {
            if( m_delay > 0 )
            {
                final String error = 
                  REZ.getString( 
                    "appliance.error.resolve.transient", 
                    this.toString(),
                    "" + m_delay );
                 throw new TransientRuntimeException( error, m_delay );
            }
            else if( !m_commissioned.isEnabled() )
            {
                final String error = 
                  REZ.getString( 
                    "appliance.error.resolve.non-commission-state", 
                    this.toString() );
                throw new IllegalStateException( error );
            }
            else
            {
                return m_lifestyle.resolve();
            }
        }
        else
        {
            ComponentModel model = getComponentModel();
            Logger logger = model.getLogger().getChildLogger( "proxy" );
            ApplianceInvocationHandler handler = 
              new ApplianceInvocationHandler( this, logger, m_secure );

            try
            {
                return Proxy.newProxyInstance( 
                  model.getDeploymentClass().getClassLoader(),
                  model.getInterfaces(),
                  handler );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Proxy establishment failure in appliance: " + this;
                throw new ApplianceException( error, e );
            }
        }
    }

    /**
     * Release an object
     *
     * @param instance the object to be released
     */
    public void release( Object instance )
    {
        if( null == instance ) return;
        if( !m_commissioned.isEnabled() ) return;
        if( Proxy.isProxyClass( instance.getClass() ) )
        {
            ApplianceInvocationHandler handler = 
              (ApplianceInvocationHandler) Proxy.getInvocationHandler( instance );
            handler.release();
        }
        else
        {
            m_lifestyle.release( instance );
        }
    }

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

    /**
     * Return the model backing the handler.
     * @return the type that the appliance is managing
     */
    protected ComponentModel getComponentModel()
    {
        return m_model;
    }

    //-------------------------------------------------------------------
    // Object
    //-------------------------------------------------------------------

    public String toString()
    {
        return "appliance:" + getComponentModel().getQualifiedName();
    }
}

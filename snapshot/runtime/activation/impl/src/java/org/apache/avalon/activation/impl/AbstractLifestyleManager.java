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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;

import org.apache.avalon.activation.LifecycleException;
import org.apache.avalon.activation.ComponentFactory;

import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.StageModel;
import org.apache.avalon.composition.provider.LifestyleManager;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.context.Context;

import org.apache.avalon.lifecycle.Accessor;

import org.apache.avalon.meta.info.InfoDescriptor;
import org.apache.avalon.meta.info.StageDescriptor;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * Abstract implentation class for a lifestyle handler.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: AbstractLifestyleManager.java 30977 2004-07-30 08:57:54Z niclas $
 */
public abstract class AbstractLifestyleManager implements LifestyleManager
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( AbstractLifestyleManager.class );

    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    private final ComponentModel m_model;

    private final ComponentFactory m_factory;

    private final Logger m_logger;

    private final ReferenceQueue m_liberals = new ReferenceQueue();

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

   /**
    * Creation of a new instance.
    * @param model the component model
    * @param factory the component factory
    */
    public AbstractLifestyleManager( ComponentModel model, ComponentFactory factory  )
    {
        m_factory = factory;
        m_model = model;
        m_logger = model.getLogger();
    }

    //-------------------------------------------------------------------
    // Commissionable
    //-------------------------------------------------------------------

   /**
    * Commission the runtime handler. 
    *
    * @exception Exception if a hanfdler commissioning error occurs
    */
    public abstract void commission() throws Exception;

   /**
    * Invokes the decommissioning phase.  Once a handler is 
    * decommissioned it may be re-commissioned.
    */
    public abstract void decommission();

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
        Object instance = handleResolve();
        return applyExtensionStages( instance, true );
    }

    /**
     * Resolve a object to a value.
     *
     * @return the resolved object
     * @throws Exception if an error occurs
     */
    public Object resolve( boolean proxy ) throws Exception
    {
        return resolve();
    }

    /**
     * Release an object
     *
     * @param instance the object to be released
     */
    public void release( Object instance )
    {
        try
        {
            applyExtensionStages( instance, false );
        }
        catch( Throwable e )
        {
            final String error = 
              "Ignoring error returned from release extension.";
            getLogger().error( error, e );
        }
        handleRelease( instance );
    }

    //-------------------------------------------------------------------
    // LifecycleManager
    //-------------------------------------------------------------------

    public void finalize( Object instance )
    {
        synchronized( m_factory )
        {
            if( instance != null )
            {
                m_factory.etherialize( instance );
            }
        }
    }

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

    protected abstract Object handleResolve() throws Exception;

    protected abstract void handleRelease( Object instance );


    protected Logger getLogger()
    {
        return m_logger;
    }

    protected ComponentModel getComponentModel()
    { 
        return m_model;
    }

    protected ComponentFactory getComponentFactory()
    { 
        return m_factory;
    }

   /**
    * Return the liberal queue.
    */
    ReferenceQueue getLiberalQueue()
    {
        return m_liberals;
    }

    protected Reference getReference( Object instance )
    {
        final int policy = getComponentModel().getCollectionPolicy();
        if( policy == InfoDescriptor.WEAK )
        {
             return new WeakReference( instance, m_liberals );
        }
        else if( policy == InfoDescriptor.SOFT )
        {
             return new SoftReference( instance );
        }
        else
        {
             return new StrongReference( instance );
        }
    }

    private class StrongReference extends WeakReference
    {
        private final Object m_instance;

        public StrongReference( Object instance )
        {
            super( instance );
            m_instance = instance;
        }

        public Object get()
        {
            return m_instance;
        }
    }

    private Object applyExtensionStages( Object instance, boolean flag ) 
      throws Exception
    {
        StageDescriptor[] stages = m_model.getType().getStages();
        for( int i=0; i<stages.length; i++ )
        {
            StageDescriptor descriptor = stages[i];
            StageModel stage = m_model.getStageModel( descriptor );

            ComponentModel provider = getStageProvider( stage );
            Class c = provider.getDeploymentClass();

            if( Accessor.class.isAssignableFrom( c ) )
            {
                Accessor handler = (Accessor) provider.resolve();
                try
                {
                    Context context = (Context) m_model.getContextModel().getContext();
                    if( flag )
                    {
                        if( getLogger().isDebugEnabled() )
                        {
                            int id = System.identityHashCode( instance );
                            getLogger().debug( "applying access stage to: " + id );
                        }
                        handler.access( instance, context );
                    }
                    else
                    {
                        if( getLogger().isDebugEnabled() )
                        {
                            int id = System.identityHashCode( instance );
                            getLogger().debug( "applying release stage to: " + id );
                        }
                        handler.release( instance, context );
                    }
                }
                catch( Throwable e )
                {
                    final String error = 
                      REZ.getString( 
                        "lifecycle.stage.accessor.error",
                        stage.getStage().getKey() );
                    if( flag )
                    {
                        throw new LifecycleException( error, e );
                    }
                    else
                    {
                        getLogger().warn( error, e );
                    }
                }
                finally
                {
                    provider.release( handler );
                }
            }
        }
        return instance;
    }

    private ComponentModel getStageProvider( StageModel stage ) throws LifecycleException
    {
        try
        {
            return (ComponentModel) stage.getProvider();
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to resolve access stage provider.";
            throw new LifecycleException( error, e );
        }
    }

    //-------------------------------------------------------------------
    // Object
    //-------------------------------------------------------------------

    public void finalize()
    {
        decommission();
    }

}

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

package org.apache.avalon.finder.impl;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

import org.apache.avalon.finder.Finder;
import org.apache.avalon.finder.FinderException;

import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.ProviderNotFoundException;
import org.apache.avalon.composition.model.AssemblyException;
import org.apache.avalon.composition.model.Reclaimer;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;

import org.apache.avalon.meta.info.ReferenceDescriptor;

/**
 * A default implementation of a finder service that provides 
 * support for pull-based service activation semantics. The default 
 * implementation deals with activation of standard avalon components
 * (i.e. components that declare semantics using the Avalon Meta 
 * contract).
 *
 * @avalon.component name="finder" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.finder.Finder"
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/04/08 08:35:15 $
 */
public class DefaultFinder implements Finder
{
    //---------------------------------------------------------
    // immutable state
    //---------------------------------------------------------

    /**
     * The logging channel for this component.
     */
    private final Logger m_logger;

    //---------------------------------------------------------
    // mutable state
    //---------------------------------------------------------

    private ContainmentModel m_model;

    //---------------------------------------------------------
    // constructor
    //---------------------------------------------------------

   /**
    * Creation of a new default finder.
    * 
    * @param logger the container assigned logging channel
    * @param context the supplied context
    * @avalon.entry key="urn:composition:containment.model" 
    *    type="org.apache.avalon.composition.model.ContainmentModel" 
    * @exception ContextException if a contextualization error occurs
    */
    public DefaultFinder( final Logger logger, Context context )
      throws ContextException
    {
        if( null == logger )
        {
            throw new NullPointerException( "logger" );
        }
        if( null == context )
        {
            throw new NullPointerException( "context" );
        }

        m_logger = logger;
        m_model = 
         (ContainmentModel) context.get( 
           "urn:composition:containment.model" );
    }

    //---------------------------------------------------------
    // Finder
    //---------------------------------------------------------

    public Object find( Class type ) throws FinderException
    {
        DeploymentModel model = resolveModel( type );
        try
        {
            model.commission();
            return model.resolve();
        }
        catch( Throwable e )
        {
            final String error = 
              "Service establishment failure for type [" 
              + type.getName() + "].";
            throw new FinderException( error, e );
        }
    }

   /**
    * Release an object that was resolved using the find operation.
    * 
    * @param instance the object to release
    */
    public void release( Object instance )
    {
        if( Proxy.isProxyClass( instance.getClass() ) )
        {
            InvocationHandler handler = 
                Proxy.getInvocationHandler( instance );
            if( handler instanceof Reclaimer )
            { 
                Reclaimer source = (Reclaimer) handler;
                source.release();
            }
        }
    }

    //---------------------------------------------------------
    // private implementation
    //---------------------------------------------------------

    private DeploymentModel resolveModel( Class type ) 
      throws FinderException
    {
        ReferenceDescriptor reference = 
          new ReferenceDescriptor( type.getName() );

        try
        {
            return (ComponentModel) m_model.getModel( reference );
        }
        catch( ProviderNotFoundException e )
        {
            final String error = 
              "Unresolvable type [" + type.getName() + "].";
            throw new FinderException( error );
        }
        catch( AssemblyException e )
        {
            throw new FinderException( e.getMessage(), e.getCause() );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected resolution failure for type [" 
              + type.getName() + "].";
            throw new FinderException( error, e );
        }
    }

    private Logger getLogger()
    {
        return m_logger;
    }
}

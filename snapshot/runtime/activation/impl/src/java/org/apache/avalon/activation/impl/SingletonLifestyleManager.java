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

import org.apache.avalon.activation.ComponentFactory;

import org.apache.avalon.composition.model.ComponentModel;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SingletonLifestyleManager.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class SingletonLifestyleManager extends AbstractLifestyleManager
{
    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    private Reference m_reference;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

    public SingletonLifestyleManager( ComponentModel model, ComponentFactory factory )
    {
        super( model, factory );
    }

    //-------------------------------------------------------------------
    // Commissionable
    //-------------------------------------------------------------------

   /**
    * Commission the runtime handler. 
    *
    * @exception Exception if a hanfdler commissioning error occurs
    */
    public void commission() throws Exception
    {
        if( getComponentModel().getActivationPolicy() )
        {
            refreshReference();
        }
    }

   /**
    * Invokes the decommissioning phase.  Once a handler is 
    * decommissioned it may be re-commissioned.
    */
    public void decommission()
    {
        if( m_reference != null )
        {
            finalize( m_reference.get() );
            m_reference = null;
        }
    }

    //-------------------------------------------------------------------
    // Resolver
    //-------------------------------------------------------------------

    /**
     * Resolve a object to a value relative to a supplied set of 
     * interface classes.
     *
     * @return the resolved object
     * @throws Exception if an error occurs
     */
    protected synchronized Object handleResolve() throws Exception
    {
        Object instance = null;

        if( m_reference == null )
        {
            return refreshReference();
        }
        else
        {
            instance = m_reference.get();
            if( instance == null )
            {
                return refreshReference();
            }
            else
            {
                return instance;
            }
        }
    }

    /**
     * Release an object
     *
     * @param instance the object to be released
     */
    protected synchronized void handleRelease( Object instance )
    {
        // continue with the current singleton reference
    }

    //-------------------------------------------------------------------
    // LifecycleManager
    //-------------------------------------------------------------------

    /**
     * Release an object
     *
     * @param instance the object to be released
     */
    public synchronized void finalize( Object instance )
    {
        getComponentFactory().etherialize( instance );
        m_reference = null;
    }

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

    private Object refreshReference() throws Exception
    {
        ComponentFactory factory = getComponentFactory();
        synchronized( factory )
        {
            Object instance = factory.incarnate();
            m_reference = getReference( instance );
            return instance;
        }
    }
}

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

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;
import java.util.ArrayList;

import org.apache.avalon.activation.ComponentFactory;

import org.apache.avalon.composition.model.ComponentModel;

import org.apache.avalon.meta.info.InfoDescriptor;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: TransientLifestyleManager.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class TransientLifestyleManager extends AbstractLifestyleManager
{
    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    private ArrayList m_list = new ArrayList();

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

    public TransientLifestyleManager( ComponentModel model, ComponentFactory factory )
    {
        super( model, factory );
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
        // TODO: setup a background thread to check queues for 
        // released references and remove them from our list, otherwise we
        // have a memory leak due to accumulation of weak references

        if( getComponentModel().getActivationPolicy() )
        {
            handleResolve();
        }
    }

   /**
    * Decommission the appliance.  Once an appliance is 
    * decommissioned it may be re-commissioned.
    */
    public void decommission()
    {
        Reference[] refs = (Reference[]) m_list.toArray( new Reference[0] );
        for( int i=0; i<refs.length; i++ )
        {
            Reference ref = refs[i];
            finalize( refs[i].get() );
        }
        m_list.clear();
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
    protected Object handleResolve() throws Exception
    {
        Object instance = getComponentFactory().incarnate();
        Reference reference = getReference( instance );
        m_list.add( reference );
        return instance;
    }

    /**
     * Release an object
     *
     * @param instance the object to be released
     */
    protected void handleRelease( Object instance )
    {
        finalize( instance );
    }

    //-------------------------------------------------------------------
    // LifestyleManager
    //-------------------------------------------------------------------

   /**
    * Overriding getReference to ensure that we never return a hard 
    * reference for a transient.
    */
    protected Reference getReference( Object instance )
    {
        final int policy = getComponentModel().getCollectionPolicy();
        if( policy == InfoDescriptor.SOFT )
        {
             return new SoftReference( instance );
        }
        else
        {
             return new WeakReference( 
               instance, 
               getLiberalQueue() );
        }
    }
}

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

import org.apache.avalon.activation.LifestyleRuntimeException;
import org.apache.avalon.activation.ComponentFactory;

import org.apache.avalon.composition.model.ComponentModel;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ThreadLifestyleManager.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ThreadLifestyleManager extends AbstractLifestyleManager
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

    /**
     * Internal utility class to hold the thread local instance.
     */
    private static final class ThreadLocalHolder extends ThreadLocal
    {
        private final ComponentFactory m_factory;

        protected ThreadLocalHolder( ComponentFactory factory )
        {
            m_factory = factory;
        }

        //
        // TODO: the current implementation is hard coded to HARD
        // collection policy - we need to update the ThreadLocalHolder so 
        // it regenerates the value relative to SOFT or WEAK policies
        // (but I just need to check docs on thread local state access
        // semantics)
        //

        protected Object initialValue()
        {
            try
            {
                return m_factory.incarnate();
            }
            catch( Exception e )
            {
                final String error = 
                  "Unable to establish thread local variable.";
                throw new LifestyleRuntimeException( error, e );
            }
        }
    }

    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    private ThreadLocalHolder m_local;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

    public ThreadLifestyleManager( ComponentModel model, ComponentFactory factory  )
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
            resolve();
        }
    }

   /**
    * Invokes the decommissioning phase.  Once a handler is 
    * decommissioned it may be re-commissioned.
    */
    public synchronized void decommission()
    {
        if( m_local != null )
        {
            finalize( m_local.get() );
            m_local = null;
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
    protected Object handleResolve() throws Exception
    {
        if( m_local == null )
        {
            ComponentFactory factory = getComponentFactory();
            m_local = new ThreadLocalHolder( factory );
        }
        return m_local.get();
    }

    /**
     * Release an object.
     *
     * @param instance the object to be reclaimed
     */
    protected void handleRelease( Object instance )
    {
        // don't release because this is a shared reference
    }

}

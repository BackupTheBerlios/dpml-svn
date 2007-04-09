/*
 * Copyright 2006 Stephen J. McConnell.
 *
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

package net.dpml.runtime;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationTargetException;
import java.util.ServiceLoader;

import net.dpml.annotation.CollectionPolicy;

import net.dpml.state.State;
import net.dpml.state.StateMachine;

import net.dpml.util.Logger;
import dpml.util.DefaultLogger;

/**
 * Abstract lifestyle handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
abstract class LifestyleHandler
{
    private static final EventQueue QUEUE = 
      new EventQueue( 
        new DefaultLogger( "dpml.metro" ), 
        "DPML Component Event Queue" );

    private final ComponentStrategy m_strategy;
    
    LifestyleHandler( ComponentStrategy strategy )
    {
        m_strategy = strategy;
    }
    
   /**
    * Return the component strategy.
    * @return a component strategy
    */
    protected ComponentStrategy getComponentStrategy()
    {
        return m_strategy;
    }
    
   /**
    * Return a component provider.
    * @return a provider instance
    * @exception Exception if an error occurs
    */
    abstract void release( Provider provider );
    
   /**
    * Return an instance of the component taking into account the lifestyle policy.
    * @return a component instance
    * @exception Exception if an error occurs
    */
    abstract Provider getProvider();
    
    abstract void terminate();
    
    protected <T> Reference<T> createReference( T referent )
    {
        CollectionPolicy policy = getComponentStrategy().getCollectionPolicy();
        if( policy.equals( CollectionPolicy.SOFT ) )
        {
            return new SoftReference<T>( referent );
        }
        else if( policy.equals( CollectionPolicy.WEAK ) )
        {
            return new WeakReference<T>( referent );
        }
        else
        {
            return new HardReference<T>( referent );
        }
    }

   /**
    * A reference class that implements hard reference semantics.
    */
    private static class HardReference<T> extends SoftReference<T>
    {
        private final T m_referent; // hard reference
        
       /**
        * Creation of a new hard reference.
        * @param referent the referenced object
        */
        public HardReference( T referent )
        {
            super( referent );
            m_referent = referent;
        }
        
       /**
        * Return the referent.
        * @return the referent object
        */
        public T get()
        {
            return m_referent;
        }
    }
}


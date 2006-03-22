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

package net.dpml.job.impl;

import java.lang.reflect.InvocationTargetException;

import net.dpml.job.Commissionable;
import net.dpml.job.Commissioner;
import net.dpml.job.CommissionerController;
import net.dpml.job.CommissionerEvent;
import net.dpml.job.TimeoutException;
import net.dpml.job.TimeoutError;

/**
 * Runnable deployment thread that handles the commissioning of an 
 * arbitary number of commissionable instances.  The commissioner maintains a 
 * list of commissioning requests which are queued on a first come first 
 * serve basis.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultCommissioner implements Commissioner, Runnable
{
    //------------------------------------------------------------
    // static
    //------------------------------------------------------------

    private static int m_counter = 0;
    
    //------------------------------------------------------------
    // immutable state
    //------------------------------------------------------------

    private final FIFO m_queue = new FIFO();

    private final boolean m_flag;
    
    private final CommissionerController m_listener;
    
    //------------------------------------------------------------
    // mutable static
    //------------------------------------------------------------

    private Thread m_thread;

    //------------------------------------------------------------
    // constructor
    //------------------------------------------------------------

    public DefaultCommissioner( String name, boolean flag, CommissionerController listener )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        if( null == listener )
        {
            throw new NullPointerException( "listener" );
        }
        m_flag = flag;
        m_listener = listener;
        m_thread = new Thread( this, name );
        m_thread.start();
    }

    //------------------------------------------------------------
    // implementation
    //------------------------------------------------------------

    /** 
     * Commissions the given Commissonable, and allows a maximum time
     * for commissioning/decommissioning to complete.
     *
     * @param commissionable the commissionable instance
     * @param timeout the maximum number of milliseconds to allow 
     *
     * @throws TimeoutException if the deployment was not 
     *   completed within the timeout deadline and interuption
     *   of the deployment was successful
     * @throws TimeoutError if the deployment was not 
     *   completed within the timeout deadline and interuption
     *   of the deployment was not successful
     * @throws InvocationTargetException if the deployment target raises an exception
     **/
    public void add( Commissionable commissionable, long timeout )
        throws TimeoutException, TimeoutError, InvocationTargetException
    {
        if( null == commissionable )
        {
            throw new NullPointerException( "commissionable" );
        }
        
        if( null != m_thread )
        {
            CommissionerEvent event = 
              new CommissionerEvent(
                commissionable, m_flag, timeout );
            m_listener.started( event );
            
            Job job = new Job( commissionable, m_thread, timeout );
            m_queue.put( job );
            try
            {
                long t = job.waitForCompletion();
                
                CommissionerEvent completionEvent = 
                  new CommissionerEvent(
                    commissionable, m_flag, t );
                m_listener.completed( completionEvent );
            }
            catch( TimeoutException e )
            {
                CommissionerEvent interruptedEvent = 
                  new CommissionerEvent(
                    commissionable, m_flag, e.getDuration() );
                m_listener.interrupted( interruptedEvent );
            }
            catch( TimeoutError e )
            {
                CommissionerEvent terminatedEvent = 
                  new CommissionerEvent(
                    commissionable, m_flag, e.getDuration() );
                m_listener.terminated( terminatedEvent );
            }
            catch( Throwable e )
            {
                CommissionerEvent failedEvent = 
                  new CommissionerEvent(
                    commissionable, m_flag, timeout );
                m_listener.failed( failedEvent, e );
            }
        }
        else
        {
            throw new IllegalStateException( "disposed" );
        }
    }
    
    /** 
     * Disposal of the Commissioner.
     * The Commissioner allocates a deployment thread, which needs to be
     * disposed of before releasing the Commissioner reference.
     **/
    public void dispose()
    {
        if( null != m_thread )
        { 
            m_thread.interrupt();
        }
    }
    
    public void run()
    {
        try
        {
            while( true )
            {
                Job job = (Job) m_queue.get();
                Commissionable model = job.getCommissionable();
                try
                {
                    if( m_flag )
                    {
                        model.commission();
                    }
                    else
                    {
                        model.decommission();
                    }
                    job.done();
                } 
                catch( InterruptedException e )
                {
                    job.interrupted();
                }
                catch( Throwable e )
                {
                    job.exception( e );
                }
            }
        } 
        catch( InterruptedException e )
        { 
            // ignore, part of dispose;
        }
        m_thread = null;
    }
}

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
import net.dpml.job.TimeoutException;
import net.dpml.job.TimeoutError;

/**
 * A job request handler.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class Job
{
    //------------------------------------------------------------
    // immutable state
    //------------------------------------------------------------

    private final Commissionable m_commissionable;
    private final Thread m_thread;
    private final long m_timeout;
    
    //------------------------------------------------------------
    // mutable state
    //------------------------------------------------------------

    private boolean m_completed;
    private boolean m_interrupted;
    private Throwable m_exception;

    //------------------------------------------------------------
    // constructor
    //------------------------------------------------------------

   /**
    * Creation of a new commission request.
    * @param model the model to commission
    * @param thread the deployment thread
    */
    Job( Commissionable model, Thread thread, long timeout )
    {
        m_commissionable = model;
        m_completed = false;
        m_interrupted = false;
        m_exception = null;
        m_thread = thread;
        m_timeout = timeout;
    }

    //------------------------------------------------------------
    // implementation
    //------------------------------------------------------------

   /**
    * Return the deployment model that it the subject of the 
    * commission request.
    * @return the model
    */
    Commissionable getCommissionable()
    {
        return m_commissionable;
    }

    long waitForCompletion()
        throws Exception
    {
        long t1 = System.currentTimeMillis();
        synchronized( this )
        {
            waitForCompletion( m_timeout );
            processException();
            if( m_completed )
            {
                long t2 = System.currentTimeMillis();
                return t2-t1;
            }
            m_thread.interrupt();
            waitForCompletion( m_timeout ); // wait for shutdown
            processException();
            if( m_interrupted || m_completed )
            {
                throw new TimeoutException( m_timeout );
            }
            else
            {
                throw new TimeoutError( m_timeout );
            }
        }
    }

    private void waitForCompletion( long timeout ) throws InterruptedException
    {
        if( timeout > 0 )
        {
            if( !m_completed ) 
            {
                wait( timeout );
            }
        }
        else
        {
            while( !m_completed )
            {
                wait( 60 );
            }
        }
    }

    private void processException()
        throws Exception
    {
        if( m_exception != null )
        {
            if( m_exception instanceof Exception )
            {
                throw (Exception) m_exception;
            }
            else if( m_exception instanceof Error )
            {
                throw (Error) m_exception;
            }
            else
            {
                final String error = 
                  "Unexpected deployment error.";
                throw new InvocationTargetException( m_exception, error );
            }
        }
    }

    void done()
    {
        synchronized( this )
        {
            m_completed = true;
            notifyAll();
        }
    }

    void interrupted()
    {
        m_interrupted = true;
        synchronized( this )
        {
            notify();
        }
    }

    void exception( Throwable e )
    {
        m_exception = e;
        synchronized( this )
        {
            m_completed = true;
            notify();
        }
    }
}


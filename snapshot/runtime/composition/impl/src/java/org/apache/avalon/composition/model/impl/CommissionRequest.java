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

import java.lang.reflect.InvocationTargetException;

import org.apache.avalon.composition.model.CommissioningException;
import org.apache.avalon.composition.model.FatalCommissioningException;
import org.apache.avalon.composition.model.DeploymentModel;

/**
 * A deployment request handler.
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: CommissionRequest.java 30977 2004-07-30 08:57:54Z niclas $
 */
class CommissionRequest
{
    //------------------------------------------------------------
    // immutable state
    //------------------------------------------------------------

    private final DeploymentModel m_model;
    private final Thread m_thread;
    private long m_timeout;
    
    //------------------------------------------------------------
    // mutable state
    //------------------------------------------------------------

    private boolean   m_completed;
    private boolean   m_interrupted;
    private Throwable m_exception;

    //------------------------------------------------------------
    // constructor
    //------------------------------------------------------------

   /**
    * Creation of a new commission request.
    * @param model the model to commission
    * @param thread the deployment thread
    */
    CommissionRequest( DeploymentModel model, Thread thread )
    {
        m_model = model;
        m_completed = false;
        m_interrupted = false;
        m_exception = null;
        m_thread = thread;
    }

    //------------------------------------------------------------
    // implementation
    //------------------------------------------------------------

   /**
    * Return the deployment model that it the subject of the 
    * commission request.
    * @return the model
    */
    DeploymentModel getDeploymentModel()
    {
        return m_model;
    }

    long waitForCompletion()
        throws Exception
    {
        long t1 = System.currentTimeMillis();
        synchronized( this )
        {
            long timeout = getDeploymentModel().getDeploymentTimeout();
            waitForCompletion( timeout );
            processException();
            if( m_completed )
            {
                long t2 = System.currentTimeMillis();
                return t2-t1;
            }
            m_thread.interrupt();
            waitForCompletion( timeout ); // wait for shutdown
            processException();
            if( m_interrupted || m_completed )
            {
                final String error = 
                  "target: [" 
                  + m_model 
                  + "] did not respond within the timeout period: [" 
                  + timeout
                  + "] and was successfully interrupted.";
                throw new CommissioningException( error );
            }
            else
            {
                final String error = 
                  "target: [" 
                  + m_model 
                  + "] did not respond within the timeout period: [" 
                  + timeout
                  + "] and failed to respond to an interrupt.";
                throw new FatalCommissioningException( error );
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


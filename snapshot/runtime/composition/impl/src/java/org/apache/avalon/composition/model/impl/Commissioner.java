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

import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.ContainmentModel;

import org.apache.avalon.framework.logger.Logger;

/**
 * Runnable deployment thread that handles the commissioning of an 
 * arbitary number of commissionable instances.  The commissioner maintains a 
 * list of commissioning requests which are queued on a first come first 
 * serve basis.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Commissioner.java 43609 2004-09-09 15:06:28Z hammett $
 * @see CommissionRequest
 */
class Commissioner implements Runnable
{
    //------------------------------------------------------------
    // static
    //------------------------------------------------------------

    static private int m_ThreadCounter = 0;
    
    //------------------------------------------------------------
    // immutable state
    //------------------------------------------------------------

    private final Logger m_logger;

    private final SimpleFIFO m_queue = new SimpleFIFO();

    private final String m_message;

    private final boolean m_flag;
    
    //------------------------------------------------------------
    // mutable static
    //------------------------------------------------------------

    private Thread m_thread;

    //------------------------------------------------------------
    // constructor
    //------------------------------------------------------------

    Commissioner( Logger logger, boolean flag )
    {
        this( logger, flag, "unnamed" );
    }

    Commissioner( Logger logger, boolean flag, String tag )
    {
        m_logger = logger;
        m_ThreadCounter++;
        final String name = 
          "Commissioner [" + tag + ", " + m_ThreadCounter + "]";
        m_flag = flag;
        if( flag ) 
        {
            m_message = "commissioning";
        }
        else
        {
            m_message = "decommissioning";
        }
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
     * @param model the deployment model
     *
     * @throws CommissioningException if the deployment was not 
     *   completed within the timeout deadline and interuption
     *   of the deployment was successful
     * @throws FatalCommissioningException if the deployment was not 
     *   completed within the timeout deadline and interuption
     *   of the deployment was not successful
     * @throws Exception any Exception or Error thrown within the
     *   deployment of the component is forwarded to the caller.
     * @throws InvocationTargetException if the deployment throws a
     *   Throwable subclass that is NOT of type Exception or Error.
     **/
    void commission( DeploymentModel model )
        throws Exception
    {
        if( null == model )
        {
            throw new NullPointerException( "model" );
        }

        if( null != m_thread )
        {
            if( m_logger.isDebugEnabled() )
            {
                if( model instanceof ContainmentModel )
                {
                    m_logger.debug( 
                      m_message 
                      + " container [" 
                      + model.getName() 
                      + "]" );
                }
                else
                {
                    m_logger.debug( 
                      m_message 
                      + " component [" 
                      + model.getName() 
                      + "]" );
                }
            }
	       
            CommissionRequest request = 
              new CommissionRequest( model, m_thread );
           m_queue.put( request );
          long t = request.waitForCompletion();
            if( m_logger.isDebugEnabled() )
            {
                m_logger.debug( 
                  m_message 
                  + " of [" 
                  + model.getName() 
                  + "] completed in " 
                  + t 
                  + " milliseconds" );
            }
        }
        else
        {
            final String warning = 
              "Ignoring " 
              + m_message 
              + " request on a disposed commissioner.";
            m_logger.warn( warning );
        }
    }

    /** 
     * Disposal of the Commissioner.
     * The Commissioner allocates a deployment thread, which needs to be
     * disposed of before releasing the Commissioner reference.
     **/
    void dispose()
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
                CommissionRequest request = (CommissionRequest) m_queue.get();
                DeploymentModel model = request.getDeploymentModel();
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
                    request.done();
                } 
                catch( InterruptedException e )
                {
                    request.interrupted();
                }
                catch( Throwable e )
                {
                    request.exception( e );
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

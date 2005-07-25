/*
 * Copyright 2004 Niclas Hedhman.
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

package net.dpml.transit.monitor;

/**
 * Abstract adapter utility class.
 */
abstract class AbstractAdapter implements Monitor
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

   /**
    * The assigned adapter.
    */
    private Adapter m_adapter;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new adaptive monitor.
    * @param adapter the adapter to which monitor events will be redirected.
    */
    public AbstractAdapter( Adapter adapter )
    {
        m_adapter = adapter;
    }

    // ------------------------------------------------------------------------
    // Monitor
    // ------------------------------------------------------------------------

   /**
    * Test is debug trace monitoring is enabled.
    * @return true if debug trace messages are enabled else false
    */
    public boolean isTraceEnabled()
    {
        return getAdapter().isDebugEnabled();
    }

   /**
    * Notify the monitor of a trace level message to log.  An implement
    * is responsible for checking if trace debug logging is enabled 
    * before handling content.
    *
    * @param message the trace level message to log
    */
    public void trace( String message )
    {
        if( getAdapter().isDebugEnabled() )
        {
            getAdapter().debug( message );
        }
    }

    // ------------------------------------------------------------------------
    // implementation
    // ------------------------------------------------------------------------

   /**
    * Returns the adapter assigned to this adaptive monitor.
    * @return the assigned adapter
    */
    protected Adapter getAdapter()
    {
        return m_adapter;
    }
}

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

package org.apache.avalon.logging.logkit;

import java.util.Map;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.log.LogTarget;

/**
 * A <code>LoggerManager</code> interface declares operation supporting
 * the management of a logging hierarchy.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
class DefaultLogTargetManager implements LogTargetManager
{
    //---------------------------------------------------------------
    // static
    //---------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( 
        DefaultLogTargetManager.class );

    //---------------------------------------------------------------
    // state
    //---------------------------------------------------------------

   /** 
    * Map for id to target mapping.
    */
    private final Map m_targets;

    //---------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------

    /**
     * Creation of a new log target manager.
     * @param targets a map of log targets
     */
    public DefaultLogTargetManager( Map targets ) throws Exception
    {
        if( null == targets )
        {
            throw new NullPointerException( "targets" );
        }
        m_targets = targets;
    }

    //---------------------------------------------------------------
    // LogTargetManager
    //---------------------------------------------------------------
    
   /**
    * Return a log target using a supplied target id.  If the 
    * supplied id is unknown a null value is returned.
    * @param id the logging target id
    * @return the logging target
    */
    public LogTarget getLogTarget( final String id )
    {
        return (LogTarget) m_targets.get( id );
    }
}

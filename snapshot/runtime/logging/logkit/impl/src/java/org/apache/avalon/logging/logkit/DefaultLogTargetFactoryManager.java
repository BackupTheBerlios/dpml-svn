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

/**
 * A <code>LoggerManager</code> interface declares operation supporting
 * the management of a logging hierarchy.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
class DefaultLogTargetFactoryManager implements LogTargetFactoryManager
{
    //---------------------------------------------------------------
    // static
    //---------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( DefaultLogTargetFactoryManager.class );

    //---------------------------------------------------------------
    // state
    //---------------------------------------------------------------

   /** 
    * Map for id to factory mapping.
    */
    private final Map m_map;

    //---------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------

    /**
     * Creation of a new log target manager.
     * @param targets a map of log targets
     */
    public DefaultLogTargetFactoryManager( Map map ) throws Exception
    {
        if( null == map )
        {
            throw new NullPointerException( "map" );
        }
        m_map = map;
    }

    //---------------------------------------------------------------
    // LogTargetFactoryManager
    //---------------------------------------------------------------
    
   /**
    * Return a log target factory using a supplied factory key.  If the 
    * supplied key is unknown a null value is returned.
    * @param key the logging target factory key
    * @return the logging target factory
    */
    public LogTargetFactory getLogTargetFactory( final String key )
    {
        return (LogTargetFactory) m_map.get( key );
    }
}

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

package org.apache.avalon.logging.logkit.factory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.avalon.logging.logkit.LogTargetException;
import org.apache.avalon.logging.logkit.LogTargetFactory;
import org.apache.avalon.logging.logkit.LogTargetManager;
import org.apache.avalon.logging.logkit.MissingIdException;
import org.apache.avalon.logging.logkit.UnknownLogTargetException;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.log.LogTarget;
import org.apache.log.LogEvent;
import org.apache.log.format.ExtendedPatternFormatter;
import org.apache.log.format.Formatter;
import org.apache.log.format.PatternFormatter;
import org.apache.log.format.RawFormatter;
import org.apache.log.output.net.DatagramOutputTarget;

/**
 * A log target factory that handles creation of a new multicast log
 * target instances.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: MulticastTargetFactory.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class MulticastTargetFactory implements LogTargetFactory
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( MulticastTargetFactory.class );

    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private final LogTargetManager m_manager;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public MulticastTargetFactory( LogTargetManager manager )
    {
        m_manager = manager;
    }

    //--------------------------------------------------------------
    // LogTargetFactory
    //--------------------------------------------------------------

    /**
     * Create a LogTarget based on a supplied configuration
     * @param config the target configuration
     * @return the multicast target
     * @exception MissingIdException if a nested target reference 
     *    does not declare an id attribute
     * @exception UnknownLogTargetException if nasted target reference 
     *    references an unknown target id
     */
    public LogTarget createTarget( final Configuration config )
        throws LogTargetException
    {
        Configuration[] references = config.getChildren( "targetref" );
        LogTarget[] targets = new LogTarget[ references.length ];
        for( int i=0; i<references.length; i++ )
        {
            Configuration ref = references[i];
            final String id = ref.getAttribute( "id", null );
            if( null == id ) 
            {
                final String error = 
                  REZ.getString( "multicast.error.missing-id" );
                throw new MissingIdException( error );
            }
            LogTarget target = m_manager.getLogTarget( id );
            if( null == target ) 
            {
                final String error = 
                  REZ.getString( "multicast.error.unknown-id", id );
                throw new UnknownLogTargetException( error );
            }
            targets[i] = target;
        }
        return new MulticastLogTarget( targets );
    }

    private final class MulticastLogTarget implements LogTarget
    {
        private final LogTarget[] m_targets;

        public MulticastLogTarget( LogTarget[] targets )
        {
            m_targets = targets;
        }

        public void processEvent( LogEvent event )
        {
            for( int i=0; i<m_targets.length; i++ )
            {
                m_targets[i].processEvent( event );
            }
        }
    }
}


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
package org.apache.avalon.jmx.mx4j;

import mx4j.log.Logger;

/**
 * A class to pipe MX4J's own logger to the one the container wants to use.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.1 $
 */
public class MX4JLoggerAdapter extends Logger
{
    private static org.apache.avalon.framework.logger.Logger m_avalonLogger;

    /**
     * This is really bad.  A static way of introducing a logger to a tool.
     * @param logger the Avalon logger.
     */
    public static void setLogger( org.apache.avalon.framework.logger.Logger logger )
    {
        m_avalonLogger = logger;
    }

    /**
     * This overides the method in the super class to actually deliver Avalon
     * Logging to MX4J
     *
     * @param level the debug/warn/error level.
     * @param message the message to log.
     * @param throwable a message that may be sent.
     */
    protected void log( int level, Object message, Throwable throwable )
    {
        switch ( level )
        {
            case mx4j.log.Logger.DEBUG:
                m_avalonLogger.debug( message.toString(), throwable );
                break;
            case mx4j.log.Logger.ERROR:
                m_avalonLogger.error( message.toString(), throwable );
                break;
            case mx4j.log.Logger.FATAL:
                m_avalonLogger.fatalError( message.toString(), throwable );
                break;
            case mx4j.log.Logger.INFO:
                m_avalonLogger.info( message.toString(), throwable );
                break;
            case mx4j.log.Logger.TRACE:
                m_avalonLogger.debug( message.toString(), throwable );
                break;
            case mx4j.log.Logger.WARN:
                m_avalonLogger.warn( message.toString(), throwable );
                break;
        }
    }
}

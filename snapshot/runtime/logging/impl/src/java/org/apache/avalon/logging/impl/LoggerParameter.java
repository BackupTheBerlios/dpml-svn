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

package org.apache.avalon.logging.impl;

import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.util.criteria.Parameter;
import org.apache.avalon.util.criteria.CriteriaException;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * A parameter descriptor that supports transformation of a 
 * a string to a Logger instance.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: LoggerParameter.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class LoggerParameter extends Parameter
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( LoggerParameter.class );

    private static final int PRIORITY = ConsoleLogger.LEVEL_WARN;

    //--------------------------------------------------------------
    // constructors
    //--------------------------------------------------------------

   /**
    * Creation of a new logger parameter.  The parameter support
    * convertion of strings in the form "debug", "info", "warn", 
    * "error", "fatal" and "none" to an equivalent logger.
    *
    * @param key the parameter key
    * @param logger the default logger
    */
    public LoggerParameter( final String key, final Logger logger )
    {
        super( key, Logger.class, logger );
    }

   /**
    * Resolve a supplied string to a configuration
    * @param value the value to resolve
    * @exception CriteriaException if an error occurs
    */
    public Object resolve( Object value ) 
      throws CriteriaException
    {
        if( value == null )
        {
            return new ConsoleLogger( PRIORITY );
        }
        if( value instanceof Logger )
        {
            return value;
        }
        if( value instanceof String )
        {
            String priority = ((String)value).toLowerCase();
            if( priority.equals( "debug" ) )
            {
                return new ConsoleLogger( ConsoleLogger.LEVEL_DEBUG );
            }
            else if( priority.equals( "info" ) )
            {
                return new ConsoleLogger( ConsoleLogger.LEVEL_INFO );
            }
            else if( priority.equals( "warn" ) )
            {
                return new ConsoleLogger( ConsoleLogger.LEVEL_WARN );
            }
            else if( priority.equals( "error" ) )
            {
                return new ConsoleLogger( ConsoleLogger.LEVEL_ERROR );
            }
            else if( priority.equals( "fatal" ) )
            {
                return new ConsoleLogger( ConsoleLogger.LEVEL_FATAL );
            }
            else if( priority.equals( "none" ) )
            {
                return new ConsoleLogger( ConsoleLogger.LEVEL_DISABLED );
            }
        }
        final String error = 
          REZ.getString( 
            "parameter.unknown", 
            value.getClass().getName(), Logger.class.getName() );
        throw new CriteriaException( error );
    }
}

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

package org.apache.avalon.logging.provider;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.apache.avalon.framework.logger.Logger;

/**
 * LoggingCriteria is convinience interface that extends Map with 
 * a set of operations that enable easy manipulation of the logging
 * system parameters.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: LoggingCriteria.java 30977 2004-07-30 08:57:54Z niclas $
 */
public interface LoggingCriteria extends Map 
{
    //--------------------------------------------------------------
    // criteria keys
    //--------------------------------------------------------------

   /**
    * The logging configuration key.
    */
    String LOGGING_CONFIGURATION_KEY = 
      "avalon.logging.configuration";

   /**
    * The logging system bootstrap logger key.
    */
    String LOGGING_BOOTSTRAP_KEY = 
      "avalon.logging.bootstrap";

   /**
    * The key of a the criteria argument that declares the base 
    * directory to be used for persistent content. For example,
    * logging file should be created in the directory 
    * assigned to this key.
    */
    String LOGGING_BASEDIR_KEY = 
      "avalon.logging.basedir";

   /**
    * Update Interval.
    * Logging subsystems that supports changes on-the-fly, will
    * be passed this argument.
    */
    String LOGGING_INTERVAL_KEY = 
      "avalon.logging.update";

   /**
    * Debug mode.
    */
    String LOGGING_DEBUG_KEY = 
      "avalon.logging.debug";

   /**
    * Set the debug enabled policy
    * @param mode TRUE to enabled debug mode else FALSE
    */
    void setDebugEnabled( boolean mode );

   /**
    * Set the bootstrap logging channel
    * @param logger the boootstrap logging channel
    */
    void setBootstrapLogger( Logger logger );

   /**
    * Set the base directory.
    * @param dir the base directory
    */
    void setBaseDirectory( File dir );

   /**
    * Set the configuration URL.
    * @param url the configuration URL
    */
    void setLoggingConfiguration( URL url );

   /**
    * Get the bootstrap logging channel
    * @return the boootstrap logging channel
    */
    Logger getBootstrapLogger();

   /**
    * Returns the base directory for logging resources.
    * @return the base directory
    */
    File getBaseDirectory();

   /**
    * Returns debug policy.  If TRUE all logging channels will be 
    * set to debug level.
    *
    * @return the debug policy
    */
    boolean isDebugEnabled();

   /**
    * Returns an external logging system configuration file
    * @return the configuration file (possibly null)
    */
    URL getLoggingConfiguration();

   /** 
    * Returns the logging configuration update interval.
    */
    long getUpdateInterval();
    
}

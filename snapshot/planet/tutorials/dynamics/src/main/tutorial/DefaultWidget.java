/*
 * Copyright 2004 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tutorial;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.activity.Disposable;

/**
 * A component that implements the Widget service.
 *
 * @avalon.component name="widget" lifestyle="singleton"
 * @avalon.service type="tutorial.Widget"
 */
public class DefaultWidget implements Widget, Configurable, Disposable
{
   //---------------------------------------------------------
   // immutable state
   //---------------------------------------------------------

  /**
   * The logging channel supplied by the container.
   */
   private final Logger m_logger;

   //---------------------------------------------------------
   // constructor
   //---------------------------------------------------------

  /**
   * Creation of a new hello facility.
   * @param logger a logging channel
   */
   public DefaultWidget( Logger logger )
   {
       m_logger = logger;
       m_logger.info( "hello" );
   }

   //---------------------------------------------------------
   // Configurable
   //---------------------------------------------------------

   /**
    * Configuration of the gizmo by the container.
    * @param config the supplied configuration
    */
    public void configure( Configuration config ) throws ConfigurationException
    {
        final String message = config.getChild( "message" ).getValue( null );
        if( null != message )
        {
            m_logger.info( message );
        }
    }

   //---------------------------------------------------------
   // Disposable
   //---------------------------------------------------------

  /**
   * End-of-life processing initiated by the container.
   */
   public void dispose()
   {
        m_logger.info( "time to die" );
   }

   //---------------------------------------------------------
   // Object
   //---------------------------------------------------------

   public String toString()
   {
       return "[widget:" + System.identityHashCode( this ) + "]";
   }
}


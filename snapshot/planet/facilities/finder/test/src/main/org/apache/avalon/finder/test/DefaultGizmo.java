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

package org.apache.avalon.finder.test;

import java.io.File;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;

/**
 * A component that implements the Gizmo service.
 *
 * @avalon.component name="gizmo" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.finder.test.Gizmo"
 */
public class DefaultGizmo implements Gizmo, Contextualizable
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
   public DefaultGizmo( Logger logger )
   {
       m_logger = logger;
       m_logger.debug( "I've been created" );
   }

   //---------------------------------------------------------
   // Contextualizable
   //---------------------------------------------------------

   /**
    * Contextualization of the gizmo by the container.
    * @param context the supplied runtime context
    * @avalon.entry key="urn:avalon:home"
    */
    public void contextualize( Context context ) throws ContextException
    {
        File home = (File) context.get( "urn:avalon:home" );
        m_logger.debug( "home: " + home );
    }

   //---------------------------------------------------------
   // Object
   //---------------------------------------------------------

   public String toString()
   {
       return "[gizmo:" + System.identityHashCode( this ) + "]";
   }
}

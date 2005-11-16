/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.composition;

import net.dpml.part.Service;
import net.dpml.part.Version;

/**
 * Default service implementation.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: LoggingAdapter.java 2684 2005-06-01 00:22:50Z mcconnell@dpml.net $
 */
final class DefaultService implements Service
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private Class m_class;
    private Version m_version;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Creation of a new service definition.
    */
    public DefaultService( Class clazz, Version version )
    {
         m_class = clazz;
         m_version = version;
    }

    // ------------------------------------------------------------------------
    // Service
    // ------------------------------------------------------------------------

   /**
    * Return the service class.
    * @param the class
    */
    public Class getServiceClass()
    {
        return m_class;
    }
    
   /**
    * Return the service version.
    * @return the version
    */
    public Version getVersion()
    {
        return m_version;
    }
    
}


/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.profile.info;

import java.net.URI;

/**
 * The ApplicationDescriptor is immutable datastructure used to 
 * describe an application.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ApplicationDescriptor extends CodeBaseDescriptor
{
    private final String m_base;
    private final StartupPolicy m_policy;
    private final int m_startup;
    private final int m_shutdown;
    
   /**
    * Creation of a new codebase descriptor.
    * @param codebase the codebase uri 
    * @param parameters an array of plugin parameter descriptors
    * @param base working directory path
    * @param policy the application startup policy
    * @param startupTimeout startup timeout value
    * @param shutdownTimeout shutdown timeout value
    */
    public ApplicationDescriptor( 
      String codebase, ValueDescriptor[] parameters, String base, 
      StartupPolicy policy, int startupTimeout, int shutdownTimeout )
    {
        super( codebase, parameters );
        
        m_base = base;
        m_policy = policy;
        m_startup = startupTimeout;
        m_shutdown = shutdownTimeout;
    }
    
   /**
    * Returns the path defining the basedir that the application will be deployed within.
    * 
    * @return the bassedir path
    */
    public String getBasePath()
    {
        return m_base;
    }

   /**
    * Return the codebase URI.
    *
    * @return the codebase uri
    */
    public StartupPolicy getStartupPolicy()
    {
        return m_policy;
    }
    
   /**
    * Get the duration in seconds to wait for startup
    * of the application before considering deployment as a timeout failure.
    * 
    * @return the startup timeout value
    */    
    public int getStartupTimeout()
    {
        return m_startup;
    }

   /**
    * Get the duration in seconds to wait for the shutdown
    * of the application before considering the process as non-responsive.
    * 
    * @return the shutdown timeout value
    */
    public int getShutdownTimeout()
    {
        return m_shutdown;
    }
}

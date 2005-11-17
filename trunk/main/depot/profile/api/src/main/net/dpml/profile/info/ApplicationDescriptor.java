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

package net.dpml.station.info;

import java.util.Properties;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * The ApplicationDescriptor is immutable datastructure used to 
 * describe an application.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ApplicationDescriptor extends CodeBaseDescriptor
{
   /**
    * The default startup timeout in seconds.
    */
    public static int DEFAULT_STARTUP_TIMEOUT = 6;

   /**
    * The default shutdown timeout in seconds.
    */
    public static int DEFAULT_SHUTDOWN_TIMEOUT = 6;

    private final String m_base;
    private final StartupPolicy m_policy;
    private final int m_startup;
    private final int m_shutdown;
    private final Properties m_properties;
    private final String m_config;
    private final String m_title;
    
   /**
    * Creation of a new codebase descriptor.
    * @param codebase the codebase uri 
    * @param parameters an array of plugin parameter descriptors
    * @param base working directory path
    * @param policy the application startup policy
    * @param startupTimeout startup timeout value
    * @param shutdownTimeout shutdown timeout value
    * @param properties system properties
    * @param config uri to a part configuration
    */
    public ApplicationDescriptor( 
      String codebase, String title, ValueDescriptor[] parameters, String base, 
      StartupPolicy policy, int startupTimeout, int shutdownTimeout,
      Properties properties, String config ) throws URISyntaxException
    {
        super( codebase, parameters );
        
        m_base = base;
        m_policy = policy;
        m_startup = startupTimeout;
        m_shutdown = shutdownTimeout;
        m_properties = properties;
        m_title = title;
        m_config = config;
    }
    
   /**
    * Returns the application title.
    * 
    * @return the title
    */
    public String getTitle()
    {
        return m_title;
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
    * Return the application startup policy.
    *
    * @return the startup policy
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
    
   /**
    * Get the system properties.
    * 
    * @return the system properties
    */
    public Properties getSystemProperties()
    {
        return m_properties;
    }
    
   /**
    * Get the configuration uri specification.
    * 
    * @return the configuration uri spec
    */
    public String getConfigurationURISpec()
    {
        return m_config;
    }
    
   /**
    * Get the configuration uri.
    * 
    * @return the configuration uri
    */
    public URI getConfigurationURI() throws URISyntaxException
    {
        if( null == m_config )
        {
            return null;
        }
        else
        {
            return new URI( m_config );
        }
    }
}

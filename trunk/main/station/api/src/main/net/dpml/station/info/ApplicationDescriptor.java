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

import net.dpml.transit.info.CodeBaseDirective;
import net.dpml.lang.ValueDirective;

/**
 * The ApplicationDescriptor is immutable datastructure used to 
 * describe an application.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ApplicationDescriptor extends CodeBaseDirective
{
   /**
    * The default startup timeout in seconds.
    */
    public static final int DEFAULT_STARTUP_TIMEOUT = 6;

   /**
    * The default shutdown timeout in seconds.
    */
    public static final int DEFAULT_SHUTDOWN_TIMEOUT = 6;
    
    private final String m_base;
    private final StartupPolicy m_policy;
    private final int m_startup;
    private final int m_shutdown;
    private final Properties m_properties;
    private final URI m_config;
    private final String m_title;
    
   /**
    * Creation of a new codebase descriptor.
    * @param codebase the codebase uri 
    * @param title the profile title
    * @param parameters an array of plugin parameter descriptors
    * @param base working directory path
    * @param policy the application startup policy
    * @param startupTimeout startup timeout value
    * @param shutdownTimeout shutdown timeout value
    * @param properties system properties
    * @param config uri to a part configuration
    * @exception URISyntaxException if the codebase URI is invalid
    */
    public ApplicationDescriptor( 
      String codebase, String title, ValueDirective[] parameters, String base, 
      StartupPolicy policy, int startupTimeout, int shutdownTimeout,
      Properties properties, String config ) throws URISyntaxException
    {
        super( codebase, parameters );
        
        if( null == properties )
        {
            throw new NullPointerException( "properties" );
        }
        if( null == parameters )
        {
            throw new NullPointerException( "parameters" );
        }
        if( null == title )
        {
            throw new NullPointerException( "title" );
        }
        if( null == policy )
        {
            throw new NullPointerException( "policy" );
        }
        
        m_base = base;
        m_policy = policy;
        m_startup = startupTimeout;
        m_shutdown = shutdownTimeout;
        m_properties = properties;
        m_title = title;
        if( null != config )
        {
            m_config = new URI( config );
        }
        else
        {
            m_config = null;
        }
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
        if( null == m_config )
        {
            return null;
        }
        else
        {
            return m_config.toASCIIString();
        }
    }
    
   /**
    * Get the configuration uri.
    * 
    * @return the configuration uri
    */
    public URI getConfigurationURI()
    {
        return m_config;
    }
    
    /**
     * Compare this object with another for equality.
     * @param other the object to compare this object with
     * @return TRUE if the supplied object equivalent
     */
    public boolean equals( Object other )
    {
        if( !super.equals( other ) )
        {
            return false;
        }
        else if( !( other instanceof ApplicationDescriptor ) )
        {
            return false;
        }
        else
        {
            ApplicationDescriptor descriptor = (ApplicationDescriptor) other;
            if( !equals( m_base, descriptor.m_base ) )
            {
                return false;
            }
            else if( !equals( m_policy, descriptor.m_policy ) )
            {
                return false;
            }
            else if( m_startup != descriptor.m_startup )
            {
                return false;
            }
            else if( m_shutdown != descriptor.m_shutdown )
            {
                return false;
            }
            else if( !equals( m_properties, descriptor.m_properties ) )
            {
                return false;
            }
            else if( !equals( m_config, descriptor.m_config ) )
            {
                return false;
            }
            else
            {
                return equals( m_title, descriptor.m_title );
            }
        }
    }
    
   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= hashValue( m_base );
        hash ^= hashValue( m_policy );
        hash ^= m_startup;
        hash ^= m_shutdown;
        hash ^= hashValue( m_properties );
        hash ^= hashValue( m_config );
        hash ^= hashValue( m_title );
        return hash;
    }

   /**
    * Return a string representation of the application descriptor.
    * @return the string value
    */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer( "[application uri=" );
        buffer.append( getCodeBaseURISpec() );
        buffer.append( " policy=" + m_policy );
        buffer.append( " ]" );
        return buffer.toString();
    }
}

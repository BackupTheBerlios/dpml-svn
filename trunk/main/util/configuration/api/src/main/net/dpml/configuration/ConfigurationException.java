/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 2004 Apache Software Foundation
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
package net.dpml.configuration;

/**
 * Thrown in response to a missing or invalid configuration fragment.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ConfigurationException
    extends Exception
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final Configuration m_config;

   /**
    * Construct a new <code>ConfigurationException</code> instance.
    *
    * @param message The detail message for this exception.
    */
    public ConfigurationException( final String message )
    {
        this( message, (Configuration) null );
    }

   /**
    * Construct a new <code>ConfigurationException</code> instance.
    *
    * @param message The detail message for this exception.
    * @param config  The configuration object
    */
    public ConfigurationException( final String message, final Configuration config )
    {
        this( message, config, null );
    }

   /**
    * Construct a new <code>ConfigurationException</code> instance.
    *
    * @param message The detail message for this exception.
    * @param throwable the root cause of the exception
    */
    public ConfigurationException( final String message, final Throwable throwable )
    {
        this( message, null, throwable );
    }

   /**
    * Construct a new <code>ConfigurationException</code> instance.
    *
    * @param message The detail message for this exception.
    * @param config The source configuration
    * @param throwable the root cause of the exception
    */
    public ConfigurationException( 
      final String message, final Configuration config, final Throwable throwable )
    {
        super( message, throwable );
        m_config = config;
    }

   /**
    * Return the configuration instance.
    * @return the configuration
    */
    public Configuration getConfiguration()
    {
        return m_config;
    }

   /**
    * Return the configuration error message.
    * @return the message
    */
    public String getMessage()
    {
        StringBuffer message = new StringBuffer( super.getMessage() );

        if( null != m_config )
        {
            message.append( "@" );
            message.append( m_config.getLocation() );
        }
        return message.toString();
    }
}

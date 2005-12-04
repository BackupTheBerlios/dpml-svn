/*
 * Copyright 2004 Niclas Hedman.
 * Copyright 2005 Stephen McConnell.
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
package net.dpml.http.impl;

import net.dpml.configuration.Configuration;
import net.dpml.configuration.ConfigurationException;

import net.dpml.logging.Logger;

import net.dpml.http.spi.HttpContextService;

/**
 * Jetty SecurityConstraint wrapper.
 */
public class SecurityConstraint
    extends org.mortbay.http.SecurityConstraint
{
   /**
    * Deployment context.
    */
    public interface Context
    {
       /**
        * Return the handler name.
        * @return the handler name
        */
        String getName();
        
       /**
        * Return the assigned http context.
        * @return the http context
        */
        HttpContextService getHttpContext();
    }

    private Logger m_logger;
    private HttpContextService m_context;

   /**
    * Creation of a new SecurityConstraint.
    * @param logger the assigned logging channel
    * @param context the deployment context
    * @param conf a supplimentary configuration
    * @exception ConfigurationException if a configuration error occurs
    */
    public SecurityConstraint( Logger logger, Context context, Configuration conf )
        throws ConfigurationException
    {
        m_logger = logger;
        m_context = context.getHttpContext();
        String name = context.getName();
        setName( name );

        String path = conf.getChild( "path" ).getValue( "/" );
        boolean authenticate = conf.getChild( "authenticate" ).getValueAsBoolean( false );
        setAuthenticate( authenticate );
        String dcValue = conf.getChild( "data-constraint" ).getValue( "none" );
        int dc = DC_UNSET;
        if( "none".equals( dcValue ) )
        {
            dc = DC_NONE;
        }
        else if( "integral".equals( dcValue ) )
        {
            dc = DC_INTEGRAL;
        }
        else if( "confidential".equals( dcValue ) )
        {
            dc = DC_CONFIDENTIAL;
        }
        else
        {
            final String error = 
              "Illegal value: \"" 
              + dcValue 
              + "\". Only \"none\", \"integral\" or \"confidential\" is allowed.";
            throw new ConfigurationException( error );
        }
        Configuration roles = conf.getChild( "roles" );
        configureRoles( roles );
        Configuration methods = conf.getChild( "methods" );
        configureMethods( methods );
        
        m_context.addSecurityConstraint( path, this );
    }

    private void configureRoles( Configuration rolesConf )
        throws ConfigurationException
    {
        Configuration[] roles = rolesConf.getChildren( "role" );
        for( int i=0; i<roles.length; i++ )
        {
            addRole( roles[i].getValue() );
        }
    }
    
    private void configureMethods( Configuration methodsConf )
        throws ConfigurationException
    {
        Configuration[] methods = methodsConf.getChildren( "method" );
        for( int i=0; i<methods.length; i++ )
        {
            addMethod( methods[i].getValue() );
        }
    }
}


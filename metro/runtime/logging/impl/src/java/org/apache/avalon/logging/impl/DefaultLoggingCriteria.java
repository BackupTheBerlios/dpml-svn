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

import java.io.File;
import java.net.URL;
import java.io.IOException;
import java.util.Properties;

import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.logging.provider.LoggingCriteria;
import org.apache.avalon.logging.provider.LoggingRuntimeException;

import org.apache.avalon.repository.provider.InitialContext;

import org.apache.avalon.util.criteria.Criteria;
import org.apache.avalon.util.criteria.Parameter;

import org.apache.avalon.util.defaults.Defaults;
import org.apache.avalon.util.defaults.DefaultsBuilder;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * DefaultLoggingCriteria is a class holding the values supplied by a user 
 * for application to a LoggingManager factory.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultLoggingCriteria.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultLoggingCriteria extends Criteria implements LoggingCriteria
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    private static final String[] KEYS = 
      new String[]{
        LOGGING_CONFIGURATION_KEY,
        LOGGING_BASEDIR_KEY,
        LOGGING_DEBUG_KEY,
        LOGGING_BOOTSTRAP_KEY,
        LOGGING_INTERVAL_KEY };

    private static final String DEFAULTS = "/avalon.logging.properties";

    private static final Resources REZ =
      ResourceManager.getPackageResources( DefaultLoggingCriteria.class );

   /**
    * The factory parameters template.
    * @return the set of parameters constraining the criteria
    */
    private static Parameter[] buildParameters( InitialContext context ) 
    {
        return new Parameter[] {
            new ConfigurationParameter( 
              LOGGING_CONFIGURATION_KEY ),
            new Parameter( 
              LOGGING_BASEDIR_KEY, 
              File.class, 
              context.getInitialWorkingDirectory() ),
            new Parameter( 
              LOGGING_DEBUG_KEY, 
              Boolean.class, 
              new Boolean( false ) ),
            new LoggerParameter( 
              LOGGING_BOOTSTRAP_KEY, 
              new ConsoleLogger( ConsoleLogger.LEVEL_WARN ) ),
            new Parameter( 
              LOGGING_INTERVAL_KEY, 
              Long.class, 
              new Long( -1 ) )
        };
    }

    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private final InitialContext m_context;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

   /**
    * Creation of a new default logging criteria.
    * @param context the initial repository context
    */
    public DefaultLoggingCriteria( InitialContext context )
    {
        super( buildParameters( context ) );
        m_context = context;

        try
        {
            //
            // get the properties declared relative to the application
            //

            final String key = context.getApplicationKey();
            final File work = context.getInitialWorkingDirectory();
            DefaultsBuilder builder = new DefaultsBuilder( key, work );
            Properties defaults = 
              Defaults.getStaticProperties( LoggingCriteria.class );

            final String[] keys = super.getKeys();
            Properties properties = 
              builder.getConsolidatedProperties( defaults, keys );

            //
            // apply any non-null properties to the criteria
            //

            for( int i=0; i<keys.length; i++ )
            {
                final String propertyKey = keys[i];
                final String value = properties.getProperty( propertyKey );
                if( null != value )
                {
                    put( propertyKey, value );
                }
            }
        }
        catch ( IOException e )
        {
            throw new LoggingRuntimeException( 
             "Failed to load implementation default resources.", e );
        }
    }

    //--------------------------------------------------------------
    // LoggingCriteria
    //--------------------------------------------------------------

   /**
    * Set the debug enabled policy
    * @param mode TRUE to enabled debug mode else FALSE
    */
    public void setDebugEnabled( boolean mode )
    {
        put( LOGGING_DEBUG_KEY, new Boolean( mode ) );
    }

   /**
    * Set the bootstrap logging channel
    * @param logger the boootstrap logging channel
    */
    public void setBootstrapLogger( Logger logger )
    {
        put( LOGGING_BOOTSTRAP_KEY, logger );
    }

   /**
    * Set the base directory.
    * @param dir the base directory
    */
    public void setBaseDirectory( File dir )
    {
        put( LOGGING_BASEDIR_KEY, dir );
    }

   /**
    * Set the configuration URL.
    * @param url the configuration URL
    */
    public void setLoggingConfiguration( URL url )
    {
        put( LOGGING_CONFIGURATION_KEY, url );
    }

   /**
    * Get the bootstrap logging channel
    * @return the boootstrap logging channel
    */
    public Logger getBootstrapLogger()
    {
        return (Logger) get( LOGGING_BOOTSTRAP_KEY );
    }

   /**
    * Returns the base directory for logging resources.
    * @return the base directory
    */
    public File getBaseDirectory()
    {
        return (File) get( LOGGING_BASEDIR_KEY );
    }

   /**
    * Returns debug policy.  If TRUE all logging channels will be 
    * set to debug level.
    *
    * @return the debug policy
    */
    public boolean isDebugEnabled()
    {
        Boolean value = (Boolean) get( LOGGING_DEBUG_KEY );
        if( null != value ) 
            return value.booleanValue();
        return false;
    }

   /**
    * Returns an external logging system configuration file
    * @return the configuration file (possibly null)
    */
    public URL getLoggingConfiguration()
    {
        return (URL) get( LOGGING_CONFIGURATION_KEY );
    }

    /** Returns the logging configuration update interval.
     */
    public long getUpdateInterval()
    {
        Long value = (Long) get( LOGGING_INTERVAL_KEY );
        if( null != value ) 
            return value.longValue();
        return -1;
    }
    
    private static File getCanonicalForm( File file )
    {
        try
        {
            return file.getCanonicalFile();
        }
        catch( Throwable e )
        {
            final String error = 
              REZ.getString( 
                "criteria.artifact.cononical.error", 
                file.toString() );
            throw new LoggingRuntimeException( error, e );
        }
    }
}

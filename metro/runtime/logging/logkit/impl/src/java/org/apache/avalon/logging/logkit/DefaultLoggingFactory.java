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

package org.apache.avalon.logging.logkit;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;

import org.apache.avalon.logging.impl.ConsoleLogger;
import org.apache.avalon.logging.impl.DefaultLoggingCriteria;

import org.apache.avalon.logging.data.CategoriesDirective;
import org.apache.avalon.logging.data.CategoryDirective;

import org.apache.avalon.logging.provider.LoggingCriteria;
import org.apache.avalon.logging.provider.LoggingFactory;
import org.apache.avalon.logging.provider.LoggingException;
import org.apache.avalon.logging.provider.LoggingManager;

import org.apache.avalon.logging.logkit.factory.FileTargetFactory;
import org.apache.avalon.logging.logkit.factory.StreamTargetFactory;
import org.apache.avalon.logging.logkit.factory.MulticastTargetFactory;
import org.apache.avalon.logging.logkit.factory.PluginTargetFactory;

import org.apache.avalon.repository.provider.InitialContext;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.avalon.util.configuration.ConfigurationUtil;

import org.apache.log.LogTarget;

/**
 * The DefaultLoggingFactory provides support for the establishment of a 
 * new logging system using LogKit as the implementation.
 */
public class DefaultLoggingFactory implements LoggingFactory
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( DefaultLoggingFactory.class );

    private static final FormatterFactory FORMATTER = 
      new DefaultFormatterFactory();

    //--------------------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------------------

    private final ClassLoader m_classloader;
    private final InitialContext m_context;
    
    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private Logger m_logger;
    private File m_basedir;
    private LogTargetFactoryManager m_factories;
    private LogTargetManager m_targets;
    private LogTargetFactoryBuilder m_builder;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * Creation of a new default factory.
    * @param context the repository inital context
    * @param classloader the factory classloader
    */
    public DefaultLoggingFactory( InitialContext context, ClassLoader classloader )
    {
        m_context = context;
        m_classloader = classloader;
    }

    //--------------------------------------------------------------------------
    // LoggingFactory
    //--------------------------------------------------------------------------

   /**
    * Return of map containing the default parameters.
    *
    * @return the default parameters 
    */
    public LoggingCriteria createDefaultLoggingCriteria()
    {
        return new DefaultLoggingCriteria( m_context );
    }

   /**
    * Create a new LoggingManager using the supplied logging criteria.
    *
    * @param criteria the logging system factory criteria
    * @exception LoggingException is a logging system creation error occurs
    */
    public LoggingManager createLoggingManager( LoggingCriteria criteria ) 
      throws LoggingException
    {
        try
        {
            return (LoggingManager) create( criteria );
        }
        catch( Throwable e )
        {
            final String error = 
              "Cannot build logging manager.";
            throw new LoggingException( error, e );
        }
    }

    //--------------------------------------------------------------------------
    // Factory
    //--------------------------------------------------------------------------

   /**
    * Return of map containing the default parameters.
    *
    * @return the default parameters 
    */
    public Map createDefaultCriteria()
    {
        return createDefaultLoggingCriteria();
    }

   /**
    * Creation of a new kernel using the default criteria.
    *
    * @return the kernel instance
    * @exception Exception if an error occurs during root block establishment
    */
    public Object create() throws Exception
    {
        return create( createDefaultCriteria() );
    }

   /**
    * Creation of a new logging manager using the supplied criteria.
    *
    * @param map the parameters 
    * @return the logging manager instance
    * @exception Exception if an error occurs
    */
    public Object create( final Map map ) throws Exception
    {
        if( null == map )
        {
            throw new NullPointerException( "map" );
        }

        final LoggingCriteria criteria = getLoggingCriteria( map );

        //
        // get the logging system configuration, base directory
        // and bootstrap logger and setup the primary managers
        //

        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        
        URL configURL = criteria.getLoggingConfiguration();
        Configuration config;
        if( configURL != null )
        {
            config = builder.build( configURL.toExternalForm() );
        }
        else
        {
            InputStream defaultConf = 
              getClass().getClassLoader().
                getResourceAsStream( 
                  "org/apache/avalon/logging/logkit/default-logging.xml" 
                );
            config = builder.build( defaultConf );
        }
        
        m_logger = setUpBootstrapLogger( criteria, config );
        m_basedir = criteria.getBaseDirectory();

        Map factoriesMap = new HashMap();
        m_factories = new DefaultLogTargetFactoryManager( factoriesMap );

        Map targetsMap = new HashMap();
        m_targets = new DefaultLogTargetManager( targetsMap );

        m_builder = 
          new DefaultLogTargetFactoryBuilder( 
            m_context, m_classloader, m_logger, m_basedir, 
            m_factories, m_targets );

        //
        // setup the logging targets
        //

        final Configuration targetsConfig = config.getChild( "targets" );
        setupTargets( factoriesMap, targetsMap, targetsConfig );

        //
        // setup the logging categories directive
        //

        CategoriesDirective categories = 
          getCategoriesDirective( config.getChild( "categories" ), true );

        //
        // setup the internal logging channel name
        //

        String internal = 
          config.getChild( "logger" ).getAttribute( "name", "logger" );

        //
        // get the debug policy
        //

        boolean debug = criteria.isDebugEnabled();

        //
        // create a logkit logging mananager
        //

        LoggingManager manager = 
          new DefaultLoggingManager( m_logger, m_targets, categories, internal, debug );

        //
        // setup the default log target
        //

        return manager;
    }

    private LoggingCriteria getLoggingCriteria( Map map )
    {
        if( map instanceof LoggingCriteria )
        {
            return (LoggingCriteria) map;
        }
        else
        {
            final String error = 
              REZ.getString( 
                "factory.bad-criteria", 
                map.getClass().getName() );
            throw new IllegalArgumentException( error );
        }
    }

    private Logger setUpBootstrapLogger( LoggingCriteria criteria, Configuration config )
    {
        if( config.getAttribute( "debug", "false" ).equals( "true" ) )
        {
            return new ConsoleLogger( ConsoleLogger.LEVEL_DEBUG );
        }
        else
        {
            return criteria.getBootstrapLogger();
        }
    }

   /**
    * Setup of the log targets declared in the logging configuration.
    * @param logger the logging channel to log establishment events
    * @param manager the log factory manager from which target factories 
    *    are resolved
    * @param config the log targets configuration
    */
    private void setupTargets( 
      final Map factories, final Map targets, final Configuration config )
      throws LoggingException
    {
        Configuration[] children = config.getChildren();
        for( int i = 0; i < children.length; i++ )
        {
            Configuration child = children[i];
            final String id = getTargetId( child );
            try
            {
                final LogTarget target = 
                  createLogTarget( factories, id, child );
                targets.put( id, target );
                final String message = 
                  REZ.getString( "target.notice.add", id );
                m_logger.debug( message );
            }
            catch( Throwable e )
            {
                final String error = 
                  REZ.getString( "target.notice.fail", id );
                m_logger.error( error, e );
                throw new LoggingException( error, e );
            }
        }
    }

   /**
    * Create a new log target using a supplied configuration.
    * @param manager the log target factory manager
    * @param config the target configuration
    * @return the logging target
    * @exception Exception if an error occurs during factory creation
    */ 
    private LogTarget createLogTarget( 
      Map factories, final String id, final Configuration config )
      throws LoggingException
    {
        final String key = getTargetFactoryKey( config );
        final LogTargetFactory factory = 
          getLogTargetFactory( factories, key );
        return factory.createTarget( config );
    }

    private LogTargetFactory getLogTargetFactory( Map factories, String key )
      throws LoggingException
    {
        final LogTargetFactory factory = 
          m_factories.getLogTargetFactory( key );
        if( factory != null )
        {
            return factory;
        }
        else
        {
            Class clazz = getLogTargetFactoryClass( key );
            LogTargetFactory newFactory = 
              m_builder.buildLogTargetFactory( clazz );
            factories.put( key, newFactory );
            return newFactory;
        }
    }

    private Class getLogTargetFactoryClass( final String key )
      throws LoggingException
    {
        if( key.equals( "file" ) )
        {
            return FileTargetFactory.class;
        }
        else if( key.equals( "stream" ) )
        {
            return StreamTargetFactory.class;
        }
        else if( key.equals( "multicast" ) )
        {
            return MulticastTargetFactory.class;
        }
        else if( key.equals( "target" ) )
        {
            return PluginTargetFactory.class;
        }
        else
        {
            final String message = 
              REZ.getString( "factory.error.unknown", key );
            throw new LoggingException( message );
        }
    }

   /**
    * Return the factory key declared by a log target configuration.
    * @param the target configuration
    * @return the factory key
    */
    private String getTargetFactoryKey( Configuration config )
      throws LoggingException
    { 
        return config.getName();
    }

   /**
    * Return the id assigned to a target.
    * @param config the target configuration
    * @return the target id
    * @exception LoggingException if the id is not declared
    */
    private String getTargetId( Configuration config )
      throws LoggingException
    { 
        try
        {
            return config.getAttribute( "id" );
        }
        catch( ConfigurationException e )
        {
            final String listing = ConfigurationUtil.list( config );
            final String error = 
              REZ.getString( 
                "target.error.missing-id", 
                listing );
            throw new LoggingException( error );
        }
    }

    private CategoriesDirective getCategoriesDirective( Configuration config )
      throws LoggingException
    {
        return getCategoriesDirective( config, false );
    }

    private CategoriesDirective getCategoriesDirective( 
      Configuration config, boolean root )
      throws LoggingException
    {
        final String name = getCategoryName( config, root );
        final String priority = config.getAttribute( "priority", null );
        final String target = config.getAttribute( "target", null );
        CategoryDirective[] categories = 
          getCategoryDirectives( config );
        return new CategoriesDirective( name, priority, target, categories );
    }

    private String getCategoryName( Configuration config, boolean root )
      throws LoggingException
    {
        if( root ) return "";
        final String name = config.getAttribute( "name", null );
        if( null != name ) return name;

        final String error = 
          REZ.getString( "target.error.missing-category-name" );
        throw new LoggingException( error );
    }

    private CategoryDirective[] getCategoryDirectives( Configuration config )
      throws LoggingException
    {
        ArrayList list = new ArrayList();
        Configuration[] children = config.getChildren();
        for( int i=0; i<children.length; i++ )
        {
            Configuration child = children[i];
            if( child.getName().equals( "category" ) )
            {
                CategoryDirective directive = 
                  getCategoryDirective( child );
                list.add( directive );
            }
            else if( child.getName().equals( "categories" ) )
            {
                CategoriesDirective directive = 
                  getCategoriesDirective( child );
                list.add( directive );
            }
        }
        return (CategoryDirective[]) list.toArray( new CategoryDirective[0] );
    }

    private CategoryDirective getCategoryDirective( Configuration config ) 
      throws LoggingException
    {
        final String name = getCategoryName( config, false );
        final String priority = config.getAttribute( "priority", null );
        final String target = config.getAttribute( "target", null );
        return new CategoryDirective( name, priority, target );
    }
}

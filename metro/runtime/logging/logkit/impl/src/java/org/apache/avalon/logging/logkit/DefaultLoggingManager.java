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

import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.logging.data.CategoryDirective;
import org.apache.avalon.logging.data.CategoriesDirective;

import org.apache.avalon.logging.provider.LoggingRuntimeException;
import org.apache.avalon.logging.provider.LoggingManager;
import org.apache.avalon.logging.provider.LoggingException;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Priority;
import org.apache.log.output.io.StreamTarget;
import org.apache.log.format.Formatter;

/**
 * A <code>LoggerManager</code> interface declares operation supporting
 * the management of a logging hierarchy.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class DefaultLoggingManager implements LoggingManager
{
    //---------------------------------------------------------------
    // static
    //---------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( DefaultLoggingManager.class );

    private static final String DEFAULT_PRIORITY = "INFO";

    public static final String DEFAULT_FORMAT =
       "[%7.7{priority}] (%{category}): %{message}\\n%{throwable}";

    public static final Formatter FORMAT = 
       new StandardFormatter( DEFAULT_FORMAT );

    public static final Formatter CONSOLE = 
       new StandardFormatter( DEFAULT_FORMAT, false );

    //---------------------------------------------------------------
    // immutable state
    //---------------------------------------------------------------

    /**
     * The list of named logging targets.
     */
    private final LogTargetManager m_targets;

    /**
     * The implementation log hierarchy.
     */
    private final Hierarchy m_hierarchy = new Hierarchy();

    /**
     * Debug mode flag.
     */
    private final boolean m_debug;

    //--------------------------------------------------------------
    // mutable state
    //--------------------------------------------------------------

    /**
     * The logging channel.
     */
    private Logger m_logger;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    /**
     * Creation of a new logging manager using based on LogKit.
     *
     * @param logger boostrap logging channel
     * @param targets the logging targets manager
     * @param categories the initial logging categories
     * @param channel the logging category to be used for internal log message
     * @param debug a debug flag
     */
    public DefaultLoggingManager( 
      Logger logger, LogTargetManager targets, CategoriesDirective categories, 
      String channel, boolean debug ) 
      throws Exception
    {
        if( null == targets )
        {
            throw new NullPointerException( "targets" );
        }

        m_debug = debug;
        m_targets = targets;
        m_logger = logger;

        //
        // setup the default logging priority
        //

        if( !debug )
        {
            String priority = getDefaultPriority( categories );
            getHierarchy().setDefaultPriority( 
              Priority.getPriorityForName( priority ) ); 
        }
        else
        {
            getHierarchy().setDefaultPriority( 
              Priority.getPriorityForName( "DEBUG" ) ); 
        }

        LogTarget target = getDefaultLogTarget( categories );
        getHierarchy().setDefaultLogTarget( target );

        addCategories( categories );

        m_logger = getLoggerForCategory( channel );
        m_logger.debug( "logging system established" );
    }

    private LogTarget getDefaultLogTarget( CategoriesDirective categories )
      throws LoggingException
    {
        String id = categories.getTarget();
        if( null == id )
        {
            return new StreamTarget( System.out, FORMAT ); 
        }
        else
        {
            LogTarget target = m_targets.getLogTarget( id );
            if( null != target ) return target;

            final String error = 
              REZ.getString( 
                "manager.invalid-default-target", id );
            throw new LoggingException( error );
        }
    }

    private String getDefaultPriority( CategoriesDirective categories )
    {
        if( null != categories )
        { 
            String priority = categories.getPriority();
            if( priority != null ) return priority;
        }
        return DEFAULT_PRIORITY;
    }


    //===============================================================
    // LoggingManager
    //===============================================================

    /**
     * Add a set of category entries using the supplied categories descriptor.
     *
     * @param descriptor a set of category descriptors to be added under the path
     */
    public void addCategories( CategoriesDirective descriptor )
    {
        addCategories( "", descriptor );
    }

    /**
     * Add a set of category entries relative to the supplied base category
     * path, using the supplied descriptor as the definition of subcategories.
     *
     * @param root the category base path
     * @param directive a category directive to add
     */
    public void addCategories( String root, CategoriesDirective directive )
    {
        final String path = filter( root );
        addCategory( path, directive.getPriority(), directive.getTarget() );
        CategoryDirective[] categories = directive.getCategories();
        for( int i = 0; i < categories.length; i++ )
        {
            CategoryDirective category = categories[i];
            final String name = filter( category.getName() );
            final String base = getBasePath( path, name );
            if( category instanceof CategoriesDirective )
            {
                CategoriesDirective c = (CategoriesDirective) category;
                addCategories( base, c );
            }
            else
            {
                final String priority = category.getPriority();
                final String target = category.getTarget();
                addCategory( base, priority, target );
            }
        }
    }

    private String getBasePath( String root, String name )
    {
        if( root.equals( "" ) ) return name;
        return filter( root + "." + name );
    }

    /**
     * Return the Logger for the specified category.
     * @param category the category path
     * @return the logging channel
     */
    public Logger getLoggerForCategory( final String category )
    {
        org.apache.log.Logger log = addCategory( category, null, null );
        return new LogKitLogger( log );
    }

    //===============================================================
    // implementation
    //===============================================================

    private org.apache.log.Logger addCategory( 
      String path, String priority, String target )
    {
        return addCategory( path, priority, target, true );
    }

    private org.apache.log.Logger addCategory( 
      String path, String priority, String target, boolean notify )
    {
        final String name = filter( path );
        final org.apache.log.Logger logger;

        if( null != priority ) 
        {
            if( !name.equals( "" ) )
            {
                final String message = 
                  REZ.getString( 
                    "manager.notify.add-category-name-priority", name, priority.toLowerCase() );
                debug( message );
            }
            else
            {
                final String message = 
                  REZ.getString( 
                    "manager.notify.add-category-priority", priority.toLowerCase() );
                debug( message );
            }
        }
        else
        {
            if( !name.equals( "" ) )
            {
                final String message = 
                  REZ.getString( 
                    "manager.notify.add-category-name", name );
                debug( message );
            }
            else
            {
                final String message = 
                  REZ.getString( 
                    "manager.notify.add-category" );
                debug( message );
            }
        }

        try
        {
            logger = getHierarchy().getLoggerFor( name );
        }
        catch( Throwable e )
        {
            final String error = 
              REZ.getString( 
                "manager.error.internal", name );
            throw new LoggingRuntimeException( error, e );
        }

        if( !m_debug && priority != null )
        {
            final Priority priorityValue = Priority.getPriorityForName( priority );
            if( !priorityValue.getName().equals( priority ) )
            {
                final String message = 
                  REZ.getString( "manager.error.priority", priority, name );
                throw new IllegalArgumentException( message );
            }
            logger.setPriority( priorityValue );
        }

        if( target != null )
        {
            if( !target.equals( "default" ) )
            {
                final LogTarget logTarget = 
                  (LogTarget) m_targets.getLogTarget( target );
                if( logTarget != null )
                {
                    logger.setLogTargets( new LogTarget[]{ logTarget } );
                }
            }
        }

        return logger;
    }

    private String filter( String name )
    {
        if( name == null ) return "";
        String path = name.replace( '/', '.' );
        if( path.startsWith( "." ) )
        {
            path = path.substring( 1 );
            return filter( path );
        }
        if( path.endsWith( "." ) )
        {
            path = path.substring( 0, path.length() -1 );
            return filter( path );
        }
        return path;
    }

    private Hierarchy getHierarchy()
    {
        return m_hierarchy;
    }

    private void debug( String message )
    {
        if( m_logger != null ) m_logger.debug( message );
    }
}

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

import java.lang.reflect.Constructor;

import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.logging.provider.LoggingException;

import org.apache.avalon.repository.provider.InitialContext;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * The DefaultLoggingFactory provides support for the establishment of a 
 * new logging system using LogKit as the implementation.
 */
public class DefaultLogTargetFactoryBuilder implements LogTargetFactoryBuilder
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( DefaultLogTargetFactoryBuilder.class );

    private static final FormatterFactory FORMATTER = 
      new DefaultFormatterFactory();

    //--------------------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------------------

    private final ClassLoader m_classloader;
    private final InitialContext m_context;
    private final Logger m_logger;
    private final File m_basedir;
    private final LogTargetFactoryManager m_factories;
    private final LogTargetManager m_targets;
    
    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * Creation of a new default factory.
    * @param context the repository inital context
    * @param classloader the factory classloader
    */
    public DefaultLogTargetFactoryBuilder( 
      InitialContext context, ClassLoader classloader, Logger logger, File basedir, 
      LogTargetFactoryManager factories, LogTargetManager targets )
    {
        m_context = context;
        m_classloader = classloader;
        m_logger = logger;
        m_basedir = basedir;
        m_factories = factories;
        m_targets = targets;
    }

    //--------------------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------------------

   /**
    * Build a log target factory using a supplied class.  The implementation
    * checks the first available constructor arguments and builds a set of 
    * arguments based on the arguments supplied to this builder instance.
    *
    * @param clazz the log target factory class
    * @return a instance of the class
    * @exception LoggingException if the class does not expose a public 
    *    constructor, or the constructor requires arguments that the 
    *    builder cannot resolve, or if a unexpected instantiation error 
    *    ooccurs
    */ 
    public LogTargetFactory buildLogTargetFactory( Class clazz ) 
      throws LoggingException
    {
        Constructor[] constructors = clazz.getConstructors();
        if( constructors.length < 1 ) 
        {
            final String error = 
              REZ.getString( 
                "factory.error.no-constructor", 
                clazz.getName() );
            throw new LoggingException( error );
        }

        //
        // log target factories only have one constructor
        //

        Constructor constructor = constructors[0];
        Class[] classes = constructor.getParameterTypes();
        Object[] args = new Object[ classes.length ];
        for( int i=0; i<classes.length; i++ )
        {
            Class c = classes[i];
            if( File.class.isAssignableFrom( c ) )
            {
                args[i] = m_basedir;
            }
            else if( Logger.class.isAssignableFrom( c ) )
            {
                args[i] = m_logger;
            }
            else if( LogTargetFactoryManager.class.isAssignableFrom( c ) )
            {
                args[i] = m_factories;
            }
            else if( LogTargetManager.class.isAssignableFrom( c ) )
            {
                args[i] = m_targets;
            }
            else if( FormatterFactory.class.isAssignableFrom( c ) )
            {
                args[i] = FORMATTER;
            }
            else if( ClassLoader.class.isAssignableFrom( c ) )
            {
                args[i] = m_classloader;
            }
            else if( InitialContext.class.isAssignableFrom( c ) )
            {
                args[i] = m_context;
            }
            else if( LogTargetFactoryBuilder.class.isAssignableFrom( c ) )
            {
                args[i] = this;
            }
            else
            {
                final String error = 
                  REZ.getString( 
                    "factory.error.unrecognized-parameter", 
                    c.getName(),
                    clazz.getName() );
                throw new LoggingException( error );
            }
        }

        //
        // instantiate the factory
        //

        return instantiateLogTargetFactory( constructor, args );
    }

   /**
    * Instantiation of a factory instance using a supplied constructor 
    * and arguments.
    * 
    * @param constructor the factory constructor
    * @param args the constructor arguments
    * @return the factory instance
    * @exception LoggingException if an instantiation error occurs
    */
    private LogTargetFactory instantiateLogTargetFactory( 
      Constructor constructor, Object[] args ) 
      throws LoggingException
    {
        Class clazz = constructor.getDeclaringClass();
        try
        {
            return (LogTargetFactory) constructor.newInstance( args );
        }
        catch( Throwable e )
        {
            final String error = 
              REZ.getString( 
                "target.error.instantiation", 
                clazz.getName() );
            throw new LoggingException( error, e );
        }
    }
}

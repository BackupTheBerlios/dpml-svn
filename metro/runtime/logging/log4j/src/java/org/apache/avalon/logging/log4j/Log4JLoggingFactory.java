 
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

package org.apache.avalon.logging.log4j;
 
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.Map;
import java.util.Properties;

import org.apache.avalon.logging.impl.DefaultLoggingCriteria;
import org.apache.avalon.logging.provider.LoggingCriteria;
import org.apache.avalon.logging.provider.LoggingFactory;
import org.apache.avalon.logging.provider.LoggingManager;
import org.apache.avalon.logging.provider.LoggingException;

import org.apache.avalon.repository.provider.InitialContext;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * A Log4J factory.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Log4JLoggingFactory.java 46128 2004-09-15 19:42:08Z niclas $
 */
public class Log4JLoggingFactory
    implements LoggingFactory
{
    private static final Resources REZ =
      ResourceManager.getPackageResources( Log4JLoggingFactory.class );
    
    private final ClassLoader    m_Classloader;
    private final InitialContext m_Context;
    private       File           m_BaseDirectory;
   
   /**
    * Creation of a new default factory.
    * @param context the repository inital context
    * @param classloader the factory classloader
    */
    public Log4JLoggingFactory( InitialContext context, ClassLoader classloader )
    {
        m_Context = context;
        m_Classloader = classloader;
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
        return new DefaultLoggingCriteria( m_Context );
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
    * Return a new instance of default criteria for the factory.
    * @return a new criteria instance
    */
    public Map createDefaultCriteria()
    {
        return createDefaultLoggingCriteria();
    }

   /**
    * Create a new instance of an application.
    * @return the application instance
    */
    public Object create() throws Exception
    {
        return new LoggingManagerImpl();
    }

   /**
    * Create a new instance of an application.
    * @param criteriaMap the creation criteria
    * @return the application instance
    */
    public Object create( Map criteriaMap ) 
        throws Exception
    {
        if( null == criteriaMap )
        {
            throw new NullPointerException( "criteriaMap" );
        }
        LoggingCriteria criteria = getLoggingCriteria( criteriaMap );
        m_BaseDirectory = criteria.getBaseDirectory();
        
        String cwd = System.getProperty( "user.dir" );
        try
        {
            System.setProperty( "user.dir", m_BaseDirectory.getAbsolutePath() );
            URL conf = criteria.getLoggingConfiguration();
            long updateInterval = criteria.getUpdateInterval();
            configure( conf, updateInterval );
            return new LoggingManagerImpl();
        } finally
        {
            System.setProperty( "user.dir", cwd );
        }
    }

    private void configure( URL url, long interval )
        throws IOException
    {
        if( url == null )
        {
            configureDefault();
            return;
        }
        String src = url.toExternalForm();
        if( src.startsWith( "file:" ) )
        {
            src = src.substring( 5 );
            while( src.startsWith( "//" ) )
                src = src.substring( 1 );
            configureFile( src, interval );
        }
        else
        {
            configureURL( url );
        }
    }
    
    private void configureFile( String src, long interval )
    {
        if( interval > 0 )
        {
            if( src.endsWith( ".xml" ) )
            {
                DOMConfigurator.configureAndWatch( src, interval );
            }
            else
            {
                PropertyConfigurator.configureAndWatch( src, interval );
            }
        }
        else
        {
            if( src.endsWith( ".xml" ) )
            {
                DOMConfigurator.configureAndWatch( src );
            }
            else
            {
                PropertyConfigurator.configureAndWatch( src );
            }
        }
    }
    
    private void configureURL( URL url )
    {
        String src = url.toExternalForm();
        
        if( src.endsWith( ".xml" ) )
        {
            DOMConfigurator.configure( url );
        }
        else
        {
            PropertyConfigurator.configure( url );
        }
    }

    private void configureDefault()
        throws IOException
    {
        Properties conf = new Properties();
        InputStream in = getClass().getClassLoader().getResourceAsStream( "default.log4j.conf" );
        conf.load( in );
        PropertyConfigurator.configure( conf );
    }
    
    private LoggingCriteria getLoggingCriteria( Map criteriaMap )
    {
        if( criteriaMap instanceof LoggingCriteria )
        {
            return (LoggingCriteria) criteriaMap;
        }
        else
        {
            final String error = 
              REZ.getString( 
                "factory.bad-criteria", 
                criteriaMap.getClass().getName() );
            throw new IllegalArgumentException( error );
        }
    }
}

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

package org.apache.avalon.logging.logkit.factory;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.logging.logkit.LogTargetException;
import org.apache.avalon.logging.logkit.LogTargetFactory;
import org.apache.avalon.logging.logkit.LogTargetManager;
import org.apache.avalon.logging.logkit.LogTargetFactoryBuilder;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.provider.InitialContext;
import org.apache.avalon.repository.provider.Builder;
import org.apache.avalon.repository.provider.Factory;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.avalon.util.configuration.ConfigurationUtil;

import org.apache.log.LogTarget;
import org.apache.log.LogEvent;
import org.apache.log.format.ExtendedPatternFormatter;
import org.apache.log.format.Formatter;
import org.apache.log.format.PatternFormatter;
import org.apache.log.format.RawFormatter;
import org.apache.log.output.net.DatagramOutputTarget;

/**
 * A log target factory that establishes log targets based on a 
 * artifact reference.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: PluginTargetFactory.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class PluginTargetFactory implements LogTargetFactory
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( PluginTargetFactory.class );

    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private final LogTargetFactoryBuilder m_builder;

    private final ClassLoader m_classloader;

    private final InitialContext m_context;

    private final Map m_factories = new HashMap();

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public PluginTargetFactory( 
      ClassLoader classloader, InitialContext context, LogTargetFactoryBuilder builder )
    {
        m_builder = builder;
        m_classloader = classloader;
        m_context = context;
    }

    //--------------------------------------------------------------
    // LogTargetFactory
    //--------------------------------------------------------------

    /**
     * Create a LogTarget based on a supplied configuration
     * @param config the target coonfiguration
     * @return the log target
     */
    public LogTarget createTarget( final Configuration config )
        throws LogTargetException
    {
        final String spec = config.getAttribute( "artifact", null );
        if( null == spec )
        {
            final String error = 
              REZ.getString( "plugin.error.missing-artifact" );
            throw new LogTargetException( error );
        }

        LogTargetFactory factory = getLogTargetFactory( spec );
        return factory.createTarget( config );
    }

    private LogTargetFactory getLogTargetFactory( String spec )
      throws LogTargetException
    {
        if( m_factories.containsKey( spec ) )
        {
            return (LogTargetFactory) m_factories.get( spec );
        }

        //
        // otherwise we need to construct the factory, register it
        // under the spec key and return it to the client
        //
        
        try
        {
            String uri = getURI( spec );
            Artifact artifact = Artifact.createArtifact( uri );
            Builder builder = 
              m_context.newBuilder( m_classloader, artifact );
            Class clazz = builder.getFactoryClass();
            LogTargetFactory factory = 
              m_builder.buildLogTargetFactory( clazz );
            m_factories.put( uri, factory );
            return factory;
        }
        catch( Throwable e )
        {
            final String error = 
              REZ.getString( "plugin.error.build", spec );
            throw new LogTargetException( error, e );
        }
    }

    private String getURI( String spec )
    {
        if( spec.startsWith( "artifact:" ) ) return spec;
        return "artifact:" + spec;
    }
}


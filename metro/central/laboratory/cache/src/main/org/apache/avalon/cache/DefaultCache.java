/*
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.avalon.cache;

import java.net.URL;
import java.io.File;
import java.io.IOException;
import javax.naming.directory.Attributes;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.Repository;
import org.apache.avalon.repository.RepositoryException;
import org.apache.avalon.repository.provider.InitialContext;
import org.apache.avalon.repository.provider.InitialContextFactory;
import org.apache.avalon.repository.main.DefaultInitialContextFactory;

/**
 * A component implementing a file cache using the Avalon Repository API
 * as the service contract.
 *
 * @avalon.component name="cache" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.repository.Repository"
 * @avalon.attribute key="urn:composition:deployment.timeout" value="0"
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.6 $
 */
public class DefaultCache implements Repository
{
    public static final String DEFAULT_CACHE_KEY = "cache";
    public static final String DEFAULT_CACHE_NAME = "cache";
    public static final String CACHE_DIR_KEY = "urn:avalon:cache.dir";
    public static final String CACHE_KEY_KEY = "urn:avalon:cache.key";

    private final Logger m_logger;
    private final Repository m_repository;

   /**
    * Creation of a new cashe instance.  The cache implementation
    * will establish a cache relative to the supplied context 
    * entry urn:avalon:cache.dir" if supplied (optional context entry).
    * If not declared the cache will be established under ${avalon.home}/cache
    * (where ${avalon.home} is the value of the file returned from 
    *  "urn:avalon:home".
    *
    * @avalon.entry key="urn:avalon:home" type="java.io.File"
    * @avalon.entry key="urn:avalon:cache.dir" type="java.io.File" optional="true"
    * @avalon.entry key="urn:avalon:cache.key" optional="true"
    *
    * @param logger the assigned logging channel
    * @param context the component context
    * @param config the component configuration
    */
    public DefaultCache( Logger logger, Context context, Configuration config ) 
      throws ContextException, ConfigurationException, 
      RepositoryException, IOException
    {
        m_logger = logger;

        File basedir = (File) context.get( "urn:avalon:home" );
        File cache = getCacheDirectory( basedir, context );
        String key = getCacheKey( context );


        String host = config.getAttribute( "default" );
        Configuration conf = config.getChild( "hosts" );
        Configuration[] children = conf.getChildren( "host" );
        String[] hosts = new String[ children.length ];
        for( int i=0; i<hosts.length; i++ )
        {
            Configuration child = children[i];
            hosts[i] = child.getValue();
        }

        m_repository = setupRepository( basedir, cache, key, hosts, host );

        getLogger().debug( "Cache: [" + cache + "].");
    }

    //---------------------------------------------------------------------
    // Repository
    //---------------------------------------------------------------------

   /**
    * Return the metadata of an artifact as attributes.
    * @param artifact the artifact
    * @return the attributes resolved relative to the artifact address
    * @exception RepositoryException if an error occurs while resolving
    *   artifact metadata attributes
    */
    public Attributes getAttributes( Artifact artifact ) 
        throws RepositoryException
    {
        return m_repository.getAttributes( artifact );
    }
    
    /**
     * Get a resource url relative to the supplied artifact.
     * 
     * @param artifact the artifact describing the resource
     * @return the resource url
     */
    public URL getResource( Artifact artifact ) throws RepositoryException
    {
        return m_repository.getResource( artifact );
    }

   /**
    * Return the set of available artifacts capable of providing the  
    * supplied service class.
    *
    * @return the set of candidate factory artifacts
    */
    public Artifact[] getCandidates( Class service )
    {
        return m_repository.getCandidates( service );
    }

    /**
     * Creates a ClassLoader chain returning the lowest ClassLoader containing 
     * the jar artifact in the loader's path.  The dependencies of the argument 
     * artifact jar and an api, spi and implementation attribute on the jar and 
     * its dependencies are used to construct the ClassLoaders.
     * 
     * @param artifact the implementation artifact
     * @return the lowest ClassLoader in a chain
     * @throws RepositoryException if there is a problem caching and accessing
     * repository artifacts and reading their attributes.
     */
    public ClassLoader getClassLoader( Artifact artifact )
        throws RepositoryException
    {
        return m_repository.getClassLoader( artifact );
    }

    /**
     * Creates a ClassLoader chain returning the lowest ClassLoader containing 
     * the jar artifact in the loader's path.  The dependencies of the argument 
     * artifact jar and an api, spi and implementation attribute on the jar and 
     * its dependencies are used to construct the ClassLoaders.
     * 
     * @param parent the parent classloader
     * @param artifact the implementation artifact
     * @return the lowest ClassLoader in a chain
     * @throws RepositoryException if there is a problem caching and accessing
     * repository artifacts and reading their attributes.
     */
    public ClassLoader getClassLoader( ClassLoader parent, Artifact artifact )
        throws RepositoryException
    {
        return m_repository.getClassLoader( parent, artifact );
    }

    //---------------------------------------------------------------------
    // private
    //---------------------------------------------------------------------

    private File getCacheDirectory( File basedir, Context context ) 
      throws ContextException
    {
        File cache = (File) context.get( CACHE_DIR_KEY );
        if( null == cache )
        {
            return new File( basedir, DEFAULT_CACHE_NAME );
        }
        else
        {
            return cache;
        }
    }

    private String getCacheKey( Context context ) throws ContextException
    {
        String key = (String) context.get( CACHE_KEY_KEY );
        if( null == key )
        {
            return DEFAULT_CACHE_KEY;
        }
        else
        {
            return key;
        }
    }

    private Repository setupRepository( 
      File basedir, File cache, String key, String[] hosts, String host ) 
      throws RepositoryException, IOException
    {
        getLogger().debug( "Repository context factory creation." );

        InitialContextFactory factory = 
          new DefaultInitialContextFactory( key, cache );
        factory.setCacheDirectory( cache );

        if( hosts.length > 0 )
        {
            factory.setHosts( hosts );
        }
        else
        {
            factory.setHosts( new String[]{ host } );
        }

        getLogger().debug( "Repository context creation." );

        InitialContext context = factory.createInitialContext();

        //
        // prints out the working and cache directory, and the 
        // set of initial hosts assigned to the initial context
        //

        if( getLogger().isDebugEnabled() )
        {
            getLogger().debug( 
              "InitialContext work: " 
              + context.getInitialWorkingDirectory() );
            getLogger().debug( 
              "InitialContext cache: " 
              + context.getInitialCacheDirectory() );
            final String[] theHosts = context.getInitialHosts();
            for( int i=0; i<theHosts.length; i++ )
            {
                getLogger().debug( 
                  "  host (" + (i+1) + "): " 
                  + theHosts[i] );
            }
        }

        getLogger().debug( "Repository creation." );

        return context.getRepository();
    }

    private Logger getLogger()
    {
        return m_logger;
    }
}

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

package org.apache.avalon.repository.impl;


import java.io.File;

import java.util.Map;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.RepositoryRuntimeException;
import org.apache.avalon.repository.provider.RepositoryCriteria;
import org.apache.avalon.repository.provider.InitialContext;
import org.apache.avalon.repository.provider.Factory;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * The default repository factory implementation.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultFactory.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultFactory implements Factory
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------

    private static Resources REZ =
        ResourceManager.getPackageResources( DefaultFactory.class );

    private String[] m_hosts;

    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final InitialContext m_context;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * Creation of a new default repository manager factory.
    * @param context the initial context
    * @exception NullPointerException if the supplied context is null
    */
    public DefaultFactory( InitialContext context )
    {
        if( null == context ) 
          throw new NullPointerException( "context" ); 
        m_context = context;
    }

    //--------------------------------------------------------------------------
    // Factory
    //--------------------------------------------------------------------------

   /**
    * Create a new instance of the default criteria.
    * @return a new default criteria instance
    */
    public Map createDefaultCriteria()
    {
        try
        {
            return new DefaultRepositoryCriteria( m_context );
        }
        catch( Throwable e )
        {
            final String error =
              "Could not create default factory criteria.";
            throw new RepositoryRuntimeException( error, e );
        }
    }

   /**
    * Create a new instance of a repository administrator 
    * using the default parameters.
    * @return the application instance
    * @exception Exception if a repository creation error occurs
    */
    public Object create() throws Exception
    {
        return create( createDefaultCriteria() );
    }

   /**
    * Create a new instance of a repository 
    * using the supplied parameters.
    *
    * @param map a map of repository parameters
    * @return the repository
    */
    public Object create( Map map ) throws Exception
    {
        if( null == map )
        {
            throw new NullPointerException( "map" );
        }

        File root = getCache( map );
        String[] hosts = getHosts( map );
        boolean online = getOnlineMode( map );
        Artifact[] candidates = getFactoryArtifacts( map );
        return new DefaultRepository( root, hosts, online, candidates );
    }

    private boolean getOnlineMode( Map map )
    {
        Boolean value = (Boolean) map.get( 
            RepositoryCriteria.REPOSITORY_ONLINE_MODE );
        if( null != value ) return value.booleanValue();
        return true;
    }

    private File getCache( Map map )
    {
        return (File) map.get( 
            RepositoryCriteria.REPOSITORY_CACHE_DIR );
    }

    private String[] getHosts( Map map )
    {
        return (String[]) map.get( 
            RepositoryCriteria.REPOSITORY_REMOTE_HOSTS );
    }

    private Artifact[] getFactoryArtifacts( Map map )
    {
        return (Artifact[]) map.get( 
            RepositoryCriteria.REPOSITORY_FACTORY_ARTIFACTS );
    }
}

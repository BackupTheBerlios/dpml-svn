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

import java.io.IOException;
import java.io.File;
import java.util.Properties;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.RepositoryException;
import org.apache.avalon.repository.provider.InitialContext;
import org.apache.avalon.repository.provider.RepositoryCriteria;

import org.apache.avalon.util.criteria.Criteria;
import org.apache.avalon.util.criteria.Parameter;
import org.apache.avalon.util.criteria.PackedParameter;
import org.apache.avalon.util.defaults.Defaults;
import org.apache.avalon.util.defaults.DefaultsBuilder;


/**
 * A Criteria is a class holding the values supplied by a user 
 * for application to a factory.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultRepositoryCriteria.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultRepositoryCriteria extends Criteria implements RepositoryCriteria
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

   /**
    * The factory parameters template.
    * @return the set of parameters constraining the criteria
    */
    private static Parameter[] buildParameters( InitialContext context )
    {
        return new Parameter[]{
          new Parameter( 
            REPOSITORY_ONLINE_MODE, 
            Boolean.class, new Boolean( context.getOnlineMode() ) ),
          new Parameter( 
            REPOSITORY_CACHE_DIR,
            File.class,
            context.getInitialCacheDirectory() ),
          new PackedParameter( 
            REPOSITORY_REMOTE_HOSTS,
            ",",
            context.getInitialHosts() ),
          new ArtifactSequenceParameter( 
            REPOSITORY_FACTORY_ARTIFACTS,
            ",",
            new Artifact[0] ) };
    }

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    /**
     * Creation of a new criteria.
     * @param context the initial context
     */
    public DefaultRepositoryCriteria( InitialContext context ) 
      throws RepositoryException
    {
        super( buildParameters( context ) );

        //
        // create the consolidated properties
        //

        try
        {

            final String key = context.getApplicationKey();
            final File work = context.getInitialWorkingDirectory();
            Properties defaults = getDefaultProperties();
            DefaultsBuilder builder = new DefaultsBuilder( key, work );
            Properties properties = 
              builder.getConsolidatedProperties( defaults, getKeys() );

            //
            // Populate the empty repository criteria using
            // the values from the consilidated defaults.
            //

            String[] keys = super.getKeys();
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
        catch( IOException ioe )
        {
            final String error = 
              "Failed to resolve repository parameters.";
            throw new RepositoryException( error, ioe );
        }
    }

    //--------------------------------------------------------------
    // RepositoryCriteria
    //--------------------------------------------------------------

    public void setOnlineMode( boolean mode )
    {
        put( REPOSITORY_ONLINE_MODE, new Boolean( mode ) );
    }

    public void setCacheDirectory( File cache )
    {
        put( REPOSITORY_CACHE_DIR, cache );
    }

    public void setHosts( String[] hosts )
    {
        put( REPOSITORY_REMOTE_HOSTS, hosts );
    }

    public void setFactoryArtifacts( Artifact[] artifacts )
    {
        put( REPOSITORY_FACTORY_ARTIFACTS, artifacts );
    }

    //--------------------------------------------------------------
    // Object
    //--------------------------------------------------------------

    public String toString()
    {
        return "[repository: " + super.toString() + "]";
    }

    //--------------------------------------------------------------
    // private
    //--------------------------------------------------------------

    private Properties getDefaultProperties() throws RepositoryException
    {
        try
        {
            return Defaults.getStaticProperties( DefaultRepositoryCriteria.class );
        }
        catch ( IOException e )
        {
            throw new RepositoryException( 
             "Failed to load implementation defaults resource for the class: "
             + DefaultRepositoryCriteria.class.getName(), e );
        }
    }
}

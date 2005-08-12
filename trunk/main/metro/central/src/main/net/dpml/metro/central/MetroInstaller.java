/*
 * Copyright 2005 Stephen J. McConnell.
 *
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

package net.dpml.metro.central;

import java.net.URI;

import net.dpml.transit.model.Logger;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.model.ContentRegistryModel;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.TransitRegistryModel;
import net.dpml.transit.model.UnknownKeyException;

/**
 */
public class MetroInstaller
{
    private final URI m_production;
    private final URI m_development;
    private final Logger m_logger;

    public MetroInstaller( TransitRegistryModel home, Logger logger, Boolean install ) throws Exception
    {
        m_logger = logger;

        m_production = new URI( PRODUCTION_PATH );
        m_development = new URI( DEVELOPMENT_PATH );

        if( install.booleanValue() )
        {
            TransitModel[] models = home.getTransitModels();
            for( int i=0; i<models.length; i++ )
            {
                TransitModel model = models[i];
                ContentRegistryModel registry = model.getContentRegistryModel();
                synchronized( registry )
                {
                    setContentModel( model, registry );
                }
            }
        }
        else
        {
            TransitModel[] models = home.getTransitModels();
            for( int i=0; i<models.length; i++ )
            {
                TransitModel model = models[i];
                ContentRegistryModel registry = model.getContentRegistryModel();
                try
                {
                    ContentModel content = registry.getContentModel( PART );
                    URI uri = content.getCodeBaseURI();
                    if( uri.equals( m_production ) || uri.equals( m_development ) )
                    {
                        registry.removeContentModel( content );
                    }
                }
                catch( UnknownKeyException e )
                {
                    // nothing to remove
                }
            }
        }
    }

    private Logger getLogger()
    {
        return m_logger;
    }

    private void setContentModel( TransitModel transit, ContentRegistryModel registry ) throws Exception
    {
        String id = transit.getID();
        URI codebase = selectURI( transit );
        try
        {
            ContentModel model = registry.getContentModel( PART );
            URI uri = model.getCodeBaseURI();
            if( codebase.equals( uri ) )
            {
                getLogger().info( "transit profile [" + transit.getID() + "] is uptodate (no change)" );
            }
            else
            {
                getLogger().info( "updating transit profile [" + transit.getID() + "]" );
                model.setCodeBaseURI( codebase );
            }
        }
        catch( UnknownKeyException e )
        {
            getLogger().info( 
              "adding 'part' content handler to transit profile " 
              + transit.getID() );
            registry.addContentModel( PART, TITLE, codebase );
        }
    }

    private URI selectURI( TransitModel model ) throws Exception
    {
        String id = model.getID();
        if( "development".equals( id ) )
        {
            return m_development;
        }
        else
        {
            return m_production;
        }
    }

    private static final String PART = "part";
    private static final String TITLE = "Part Content Handler";
    private static final String PRODUCTION_PATH = "@PRODUCTION_URI@";
    private static final String DEVELOPMENT_PATH = "@DEVELOPMENT_URI@";

}

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

import net.dpml.transit.Logger;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.model.ContentRegistryModel;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.TransitRegistryModel;
import net.dpml.transit.model.UnknownKeyException;

/**
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class MetroInstaller
{
    private final URI m_production;
    private final Logger m_logger;

    public MetroInstaller( TransitRegistryModel home, Logger logger, boolean install ) throws Exception
    {
        m_logger = logger;

        m_production = new URI( PRODUCTION_PATH );

        if( install )
        {
            TransitModel[] models = home.getTransitModels();
            for( int i=0; i<models.length; i++ )
            {
                TransitModel model = models[i];
                setupContentModel( model );
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
                    registry.removeContentModel( content );
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

    private void setupContentModel( TransitModel model ) throws Exception
    {
        ContentRegistryModel registry = model.getContentRegistryModel();
        String id = model.getID();
        URI codebase = m_production;
        try
        {
            ContentModel content = registry.getContentModel( PART );
            URI uri = content.getCodeBaseURI();
            if( codebase.equals( uri ) )
            {
                getLogger().info( "part handler codebase in [" + id + "] is uptodate (no change)" );
            }
            else
            {
                getLogger().info( "updating codebase in [" + id + "]" );
                content.setCodeBaseURI( codebase );
            }
        }
        catch( UnknownKeyException e )
        {
            getLogger().info( 
              "adding 'part' handler to profile " + id );
            registry.addContentModel( PART, TITLE, codebase );
        }
    }

    private static final String PART = "part";
    private static final String TITLE = "Part Content Handler";
    private static final String PRODUCTION_PATH = "@PRODUCTION_URI@";

}

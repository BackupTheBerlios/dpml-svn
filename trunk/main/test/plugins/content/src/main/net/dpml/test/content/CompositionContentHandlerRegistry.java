/*
 * Copyright 2005 Stephen McConnell
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

package net.dpml.test.content;

import java.io.IOException;
import java.net.URI;
import java.net.ContentHandler;
import java.util.Date;

import net.dpml.transit.TransitException;
import net.dpml.transit.model.Logger;
import net.dpml.transit.model.ContentRegistryModel;
import net.dpml.transit.model.ContentModel;
import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.runtime.DefaultContentRegistry;

/**
 * A registry of descriptions of plugable content handlers.  This implementation
 * maps user defined preferences to instance of ContentHandlerDescriptor.
 */
public class CompositionContentHandlerRegistry extends DefaultContentRegistry
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final Logger m_logger;
    private final ContentRegistryModel m_model;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Responsible for the establishment of a map containing keys that correspond to 
    * artifact types, and values that correspond to the description of a 
    * content type handler plugins.
    *
    * @param model the registry model
    */
    public CompositionContentHandlerRegistry( ContentRegistryModel model, Logger logger ) throws Exception
    {
        super( model, logger );
        m_logger = logger;
        m_model = model;

        try
        {
            ContentModel m = model.getContentModel( PART );

            // TODO:
            // part handler already registered - but as soon as we have  
            // links in place we need to validate that the assigned handler is 
            // up-to-date and potentially update it if required
        }
        catch( UnknownKeyException uke )
        {
            try
            {
                URI uri = new URI( PART_HANDLER_PLUGIN_PATH );
                model.addContentModel( PART, TITLE, uri );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while attempting to establish part handler."
                  + "\nPlugin URI: " + PART_HANDLER_PLUGIN_PATH
                  + "\nContent Type: " + PART;
                IOException ioe = new IOException( error );
                ioe.initCause( e );
                throw ioe;
            }
        }
    }

    // ------------------------------------------------------------------------
    // static (util)
    // ------------------------------------------------------------------------

    private static final String PART = "part";
    private static final String TITLE = "Part Content Handler";

    private static final String PART_HANDLER_PLUGIN_PATH = "@COMPOSITION-CONTENT-PART-HANDLER-PLUGIN-URI@";
}

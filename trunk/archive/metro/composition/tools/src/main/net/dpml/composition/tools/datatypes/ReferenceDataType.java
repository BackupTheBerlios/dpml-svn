/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.composition.tools.datatypes;

import java.net.URI;
import java.net.URISyntaxException;

import net.dpml.component.info.Type;
import net.dpml.component.info.PartReference;
import net.dpml.component.data.ReferenceDirective;

import net.dpml.composition.tools.ConstructionException;
import net.dpml.composition.tools.PartReferenceBuilder;

import net.dpml.part.Part;

/**
 * A simple part datatype.
 */
public class ReferenceDataType implements PartReferenceBuilder
{
    private String m_key;
    private ClassLoader m_classloader;
    private URI m_uri;

   /**
    * Set the key that this directive qualifies.
    * @param key the context entry key
    */
    public void setKey( final String key )
    {
        m_key = key;
    }

   /**
    * Set the uri that this directive references.
    * @param uri the uri
    */
    public void setURI( final URI uri )
    {
        m_uri = uri;
    }

   /**
    * Return the uri reference.
    * @return the uri
    */
    public URI getURI()
    {
        return m_uri;
    }

    //---------------------------------------------------------------------
    // Builder
    //---------------------------------------------------------------------

   /**
    * Return a uri identitifying the builder.
    *
    * @return the builder uri
    */
    public URI getBuilderURI()
    {
        return PART_BUILDER_URI;
    }

    //---------------------------------------------------------------------
    // PartReferenceBuilder
    //---------------------------------------------------------------------

   /**
    * Return the key identifying the part that this builder is building.
    */
    public String getKey()
    {
        if( null == m_key )
        {
            final String error = 
              "Missing 'key' attribute declaration.";
            throw new ConstructionException( error );
        }
        return m_key;
    }

    public PartReference buildPartReference( ClassLoader classloader, Type type )
    {
        String key = getKey();
        URI uri = getURI();
        Part part = new ReferenceDirective( m_uri );
        return new PartReference( key, part );
    }

    //---------------------------------------------------------------------
    // PartBuilder
    //---------------------------------------------------------------------

   /**
    * Return a urn identitifying the part handler for this builder.
    *
    * @return a strategy uri
    */
    public URI getPartHandlerURI()
    {
        return PART_HANDLER_URI;
    }

    //---------------------------------------------------------------------
    // static utilities
    //---------------------------------------------------------------------

    private static URI PART_HANDLER_URI = setupURI( "@PART-HANDLER-URI@" );
    private static URI PART_BUILDER_URI = setupURI( "@PART-BUILDER-URI@" );

    protected static URI setupURI( String spec )
    {
        try
        {
            return new URI( spec );
        }
        catch( URISyntaxException ioe )
        {
            return null;
        }
    }
}


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

package net.dpml.composition.builder.datatypes;

import java.net.URI;
import java.net.URISyntaxException;

import net.dpml.part.Part;
import net.dpml.part.PartReference;
import net.dpml.composition.builder.ConstructionException;
import net.dpml.composition.builder.PartReferenceBuilder;
import net.dpml.composition.data.ValueDirective;
import net.dpml.composition.data.ValueDirective.Value;
import net.dpml.composition.data.ReferenceDirective;
import net.dpml.composition.data.FeatureDirective;
import net.dpml.composition.info.EntryDescriptor;
import net.dpml.composition.info.PartDescriptor;
import net.dpml.composition.info.Type;

import org.apache.tools.ant.BuildException;

/**
 * A simple part datatype.
 */
public class EntryDataType extends ValueDataType implements PartReferenceBuilder
{
    private String m_key;
    private ClassLoader m_classloader;
    private URI m_uri;
    private int m_feature = -1;

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
    * Set the feature that this directive references.
    * @param feature the component feature
    */
    public void setFeature( String feature )
    {
        if( null != m_uri )
        {
            final String error = 
              "Attributes 'feature' and 'uri' are mutually exlusive.";
            throw new BuildException( error ); 
        }
        try
        {
            m_feature = FeatureDirective.getFeatureForName( feature );
        }
        catch( IllegalArgumentException e )
        {
            final String error = e.getMessage();
            throw new BuildException( error );
        }
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

   /**
    * Return the uri reference.
    * @return the uri
    */
    public URI getURI()
    {
        return m_uri;
    }

    public PartReference buildPartReference( ClassLoader classloader, Type type )
    {
        String key = getKey();
        URI uri = getURI();
        if( null != uri )
        {
            Part part = new ReferenceDirective( m_uri );
            return new PartReference( key, part );
        }
        else if( m_feature > -1 )
        {
            Part part = new FeatureDirective( key, m_feature );
            return new PartReference( key, part );
        }
        else
        {
            Part part = getValueDirective( classloader, type );
            return new PartReference( key, part );
        }
    }

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
    // implementation
    //---------------------------------------------------------------------

    public ValueDirective getValueDirective( ClassLoader classloader, Type type )
    {
        String key = getKey();
        EntryDescriptor entry = type.getContext().getEntry( key );
        PartDescriptor part = type.getPartDescriptor( key );
        if( ( null == entry ) && ( null == part ) )
        {
            final String error = 
              "The value key ["
              + key
              + "] is unknown relative to the component type ["
              + type.getInfo().getClassname()
              + "].";
            throw new ConstructionException( error );
        }

        String classname = getClassname();
        if( null == classname )
        {
            if( null != entry )
            {
                classname = entry.getClassname();
            }
            else
            {
                classname = part.getReturnType();
            }
        }

        if( null == classname )
        {
            final String error = 
              "Unable to construct a value directive for the key ["
              + key 
              + "] relative to the component type ["
              + type.getInfo().getClassname() 
              + "] as the primary classname was not resolvable.";
            throw new ConstructionException( error );
        }

        String value = getValue();
        if( null != value )
        {  
            return new ValueDirective( key, classname, value );
        }
        else
        {
            ValueDataType[] params = getValueDataTypes();
            Value[] values = new Value[ params.length ];
            for( int i=0; i<params.length; i++ )
            {
                 ValueDataType p = params[i];
                 values[i] = p.constructValue();
            }
            return new ValueDirective( key, classname, values );
        }
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


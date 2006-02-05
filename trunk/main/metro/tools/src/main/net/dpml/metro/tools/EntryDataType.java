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

package net.dpml.metro.tools;

import java.net.URI;
import java.net.URISyntaxException;

import net.dpml.metro.info.PartReference;
import net.dpml.metro.data.ValueDirective;
import net.dpml.part.Directive;
import net.dpml.metro.data.ReferenceDirective;
import net.dpml.metro.data.NullDirective;
import net.dpml.metro.data.FeatureDirective;
import net.dpml.metro.info.EntryDescriptor;
import net.dpml.metro.info.Type;

import net.dpml.transit.Value;
import net.dpml.transit.Construct;

import org.apache.tools.ant.BuildException;

/**
 * A simple part datatype.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class EntryDataType extends ValueDataType implements PartReferenceBuilder
{
    private String m_key;
    private ClassLoader m_classloader;
    private URI m_uri;
    private int m_feature = -1;
    private boolean m_validate = true;

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
    * Set the validation flag.
    * @param flag if false entry validation is disabled
    */
    public void setValidate( final boolean flag )
    {
        m_validate = flag;
    }
    
   /**
    * Set the interface classname to locate with the enclosing component.
    * @param service the service classname
    */
    public void setLookup( final String service )
    {
        if( null != m_uri )
        {
            final String error = 
              "Attributes 'feature', 'lookup' and 'uri' are mutually exlusive.";
            throw new BuildException( error ); 
        }
        try
        {
            m_uri = new URI( "lookup:" + service );
        }
        catch( Exception e )
        {
            final String error = 
              "Failed to set ["
              + service
              + "] lookup reference.";
            throw new BuildException( error, e );
        }
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
              "Attributes 'feature', 'lookup' and 'uri' are mutually exlusive.";
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
    * @return the key
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

   /**
    * Build a part reference.
    * @param classloader the classloader to use
    * @param type the underlying component type
    * @return the part reference
    */
    public PartReference buildPartReference( ClassLoader classloader, Type type )
    {
        String key = getKey();
        if( !m_validate )
        {
            Directive directive = new NullDirective();
            return new PartReference( key, directive );
        }
        else
        {
            URI uri = getURI();
            if( null != uri )
            {
                Directive directive = new ReferenceDirective( m_uri );
                return new PartReference( key, directive );
            }
            else if( m_feature > -1 )
            {
                Directive directive = new FeatureDirective( key, m_feature );
                return new PartReference( key, directive );
            }
            else
            {
                Directive directive = getValueDirective( classloader, type );
                return new PartReference( key, directive );
            }
        }
    }

   /**
    * Return a urn identitifying the part handler for this builder.
    *
    * @return the part handler uri
    */
    public URI getPartHandlerURI()
    {
        return PART_HANDLER_URI;
    }

    //---------------------------------------------------------------------
    // implementation
    //---------------------------------------------------------------------

   /**
    * Return the value directive.
    * @param classloader the classloader to use
    * @param type the underlying component type
    * @return the value directive
    */
    public ValueDirective getValueDirective( ClassLoader classloader, Type type )
    {
        String key = getKey();
        String classname = getClassname();
        if( null != classname )
        {
            try
            {
                classloader.loadClass( classname );
            }
            catch( ClassNotFoundException e )
            {
                final String error =
                  "Entry directive overriding class ["
                  + classname
                  + "] is unknown.";
                throw new BuildException( error, e );
            }
        }
        
        String method = getMethodName();

        if( null != type )
        {
            EntryDescriptor entry = type.getContextDescriptor().getEntryDescriptor( key );
            if( null == entry )
            {
                final String error = 
                  "The value key ["
                  + key
                  + "] is unknown relative to the component type ["
                  + type.getInfo().getClassname()
                  + "].";
                throw new ConstructionException( error );
            }
            else if( null == classname )
            {
                classname = entry.getClassname();
            }
        }

        if( null == classname )
        {
            final String error = 
              "Missing 'class' attribute for entry key ["
              + key 
              + "].";
            throw new ConstructionException( error );
        }
        
        String value = getValue();
        if( null != value )
        {
            return new ValueDirective( classname, method, value );
        }
        else
        {
            ValueBuilder[] params = getValueBuilders();
            Value[] values = new Value[ params.length ];
            for( int i=0; i<params.length; i++ )
            {
                 ValueBuilder p = params[i];
                 values[i] = p.buildValue( classloader );
            }
            return new ValueDirective( classname, method, values );
        }
    }

    //---------------------------------------------------------------------
    // static utilities
    //---------------------------------------------------------------------

    private static final URI PART_HANDLER_URI = setupURI( "@PART-HANDLER-URI@" );
    private static final URI PART_BUILDER_URI = setupURI( "@PART-BUILDER-URI@" );

   /**
    * Utility function top create a static uri.
    * @param spec the uri spec
    * @return the uri value
    */
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


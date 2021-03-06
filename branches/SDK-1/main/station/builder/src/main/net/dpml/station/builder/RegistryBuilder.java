/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.station.builder;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import net.dpml.station.info.RegistryDescriptor;
import net.dpml.station.info.RegistryDescriptor.Entry;
import net.dpml.station.info.ApplicationDescriptor;
import net.dpml.station.info.StartupPolicy;

import net.dpml.util.DOM3DocumentBuilder;

import net.dpml.util.DecodingException;
import net.dpml.util.Decoder;
import net.dpml.util.Resolver;
import net.dpml.util.SimpleResolver;

import net.dpml.lang.ValueDirective;
import net.dpml.util.ElementHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test example application sources.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class RegistryBuilder implements Decoder 
{
    private static final DOM3DocumentBuilder DOCUMENT_BUILDER = 
      new DOM3DocumentBuilder();

   /**
    * Build a registry descriptior from a uri.
    * @param uri the uri to the descriptor XML document
    * @return the registry descriptor
    * @exception DecodingException if a decoding error occurs
    * @exception IOException if a IO error occurs
    */
    public Object build( URI uri ) throws DecodingException, IOException
    {
        Document document = DOCUMENT_BUILDER.parse( uri );
        Element root = document.getDocumentElement();
        Resolver resolver = new SimpleResolver();
        return decode( root , resolver );
    }
    
   /**
    * Decode a registry descriptior from a DOM element.
    * @param element the element representing the root registry
    * @param resolver build-time uri resolver
    * @return the registry descriptor
    * @exception DecodingException if a decoding error occurs
    */
    public Object decode( Element element, Resolver resolver ) throws DecodingException
    {
        String tag = element.getTagName();
        if( "application".equals( tag ) )
        {
            return buildApplicationDescriptor( element, resolver );
        }
        else if( "registry".equals( tag ) )
        {
            return buildRegistryDescriptor( element, resolver );
        }
        else
        {
            final String error = 
              "Document element name [" + tag + "] not recognized.";
            throw new DecodingException( element, error );
        }
    }
    
    private RegistryDescriptor buildRegistryDescriptor( 
      Element element, Resolver resolver ) throws DecodingException
    {
        Element[] elements = ElementHelper.getChildren( element );
        Entry[] entries = new Entry[ elements.length ];
        for( int i=0; i<elements.length; i++ )
        {
            Element elem = elements[i];
            String key = ElementHelper.getAttribute( elem, "key" );
            if( null == key )
            {
                final String error =
                  "Missing 'key' attribute in application element.";
                throw new DecodingException( elem, error );
            }
            ApplicationDescriptor descriptor = buildApplicationDescriptor( elem, resolver );
            entries[i] = new Entry( key, descriptor );
        }
        return new RegistryDescriptor( entries );
    }
    
    private ApplicationDescriptor buildApplicationDescriptor( 
      Element element, Resolver resolver ) throws DecodingException
    {
        String title = ElementHelper.getAttribute( element, "title" );
        StartupPolicy policy = buildStartupPolicy( element );
        Element jvm = ElementHelper.getChild( element, "jvm" );
        String base = ElementHelper.getAttribute( jvm, "base" );
        Element startupElement = ElementHelper.getChild( jvm, "startup" );
        Element shutdownElement = ElementHelper.getChild( jvm, "shutdown" );
        int startup = buildTimeout( startupElement, ApplicationDescriptor.DEFAULT_STARTUP_TIMEOUT );
        int shutdown = buildTimeout( startupElement, ApplicationDescriptor.DEFAULT_SHUTDOWN_TIMEOUT );
        Element propertiesElement = ElementHelper.getChild( jvm, "properties" );
        Properties properties = buildProperties( propertiesElement );
        Element codebase = ElementHelper.getChild( element, "codebase" );
        URI uri = decodeURI( codebase );
        Element[] params = ElementHelper.getChildren( codebase, "param" );
        ValueDirective[] values = buildValueDirectives( params );
        
        // need to rework ApplicationDescriptor such that the codebase is presented
        // as an abstract type - e.g. net.dpml.lang.CodeBaseDirective verus 
        // net.dpml.metro.CodeBaseDirective
        
        return new ApplicationDescriptor( 
          uri, title, values, base, policy, startup, shutdown, properties, null );
    }
    
    private URI decodeURI( Element element ) throws DecodingException
    {
        String uri = ElementHelper.getAttribute( element, "uri" );
        if( null == uri )
        {
            final String error = "Missing uri attribute.";
            throw new DecodingException( element, error );
        }
        else
        {
            try
            {
                return new URI( uri );
            }
            catch( Exception e )
            {
                final String error = "Bad uri argument [" + uri + "].";
                throw new DecodingException( element, error );
                
            }
        }
    }

    private StartupPolicy buildStartupPolicy( Element element ) throws DecodingException
    {
        String policy = ElementHelper.getAttribute( element, "policy" );
        if( null == policy )
        {
            return StartupPolicy.MANUAL;
        }
        else
        {
            return StartupPolicy.parse( policy );
        }
    }
    
    private int buildTimeout( Element element, int fallback ) throws DecodingException
    {
        if( null == element )
        {
            return fallback;
        }
        else
        {
            String value = ElementHelper.getValue( element );
            if( null == value )
            {
                return fallback;
            }
            else
            {
                return Integer.parseInt( value );
            }
        }
    }
    
    private Properties buildProperties( Element element ) throws DecodingException
    {
        Properties properties = new Properties();
        if( null == element )
        {
            return properties;
        }
        else
        {
            Element[] children = ElementHelper.getChildren( element );
            for( int i=0; i<children.length; i++ )
            {
                Element child = children[i];
                String name = ElementHelper.getAttribute( child, "name" );
                if( null == name )
                {
                    final String error =
                      "Property declaration does not contain a 'name' attribute.";
                    throw new DecodingException( child, error );
                }
                else
                {
                    String value = ElementHelper.getAttribute( child, "value" );
                    properties.setProperty( name, value );
                }
            }
            return properties;
        }
    }

   /**
    * Construct a value directive array.
    * @param elements the array of DOM elements representing value directive assertions
    * @return the array of value directives
    */
    protected ValueDirective[] buildValueDirectives( Element[] elements )
    {
        ValueDirective[] values = new ValueDirective[ elements.length ];
        for( int i=0; i<elements.length; i++ )
        {
            values[i] = buildValueDirective( elements[i] );
        }
        return values;
    }
    
   /**
    * Construct a single value directive.
    * @param element the DOM element representing the value directive assertions
    * @return the value directive
    */
    protected ValueDirective buildValueDirective( Element element )
    {
        String classname = ElementHelper.getAttribute( element, "class" );
        String method = ElementHelper.getAttribute( element, "method" );
        Element[] elements = ElementHelper.getChildren( element, "param" );
        if( elements.length > 0 )
        {
            ValueDirective[] values = buildValueDirectives( elements );
            return new ValueDirective( classname, method, values );
        }
        else
        {
            String value = ElementHelper.getAttribute( element, "value" );
            return new ValueDirective( classname, method, value );
        }
    }
}

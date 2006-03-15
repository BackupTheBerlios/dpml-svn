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

import java.net.URI;
import java.util.Properties;

import net.dpml.station.info.RegistryDescriptor;
import net.dpml.station.info.RegistryDescriptor.Entry;
import net.dpml.station.info.ApplicationDescriptor;
import net.dpml.station.info.StartupPolicy;

import net.dpml.part.DOM3DocumentBuilder;

import net.dpml.lang.BuilderException;
import net.dpml.lang.Builder;

import net.dpml.transit.info.ValueDirective;
import net.dpml.transit.util.ElementHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test example application sources.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class RegistryBuilder implements Builder 
{
    private static final DOM3DocumentBuilder DOCUMENT_BUILDER = 
      new DOM3DocumentBuilder();

    public Object build( URI uri ) throws Exception
    {
        Document document = DOCUMENT_BUILDER.parse( uri );
        Element root = document.getDocumentElement();
        return build( null, root );
    }
    
    public Object build( ClassLoader classloader, Element element ) throws Exception
    {
        String tag = element.getTagName();
        if( "application".equals( tag ) )
        {
            return buildApplicationDescriptor( element );
        }
        else if( "registry".equals( tag ) )
        {
            return buildRegistryDescriptor( element );
        }
        else
        {
            final String error = 
              "Document element name [" + tag + "] not recognized.";
            throw new BuilderException( element, error );
        }
    }
    
    private RegistryDescriptor buildRegistryDescriptor( Element element ) throws Exception
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
                throw new BuilderException( elem, error );
            }
            ApplicationDescriptor descriptor = buildApplicationDescriptor( elem );
            entries[i] = new Entry( key, descriptor );
        }
        return new RegistryDescriptor( entries );
    }
    
    private ApplicationDescriptor buildApplicationDescriptor( Element element ) throws Exception
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
        if( null == codebase )
        {
            final String error = 
              "Missing codebase element.";
            throw new BuilderException( element, error );
        }
        String spec = ElementHelper.getAttribute( codebase, "uri" );
        if( null == spec )
        {
            final String error = 
              "Missing codebase uri attribute.";
            throw new BuilderException( codebase, error );
        }
        Element[] params = ElementHelper.getChildren( codebase, "param" );
        ValueDirective[] values = buildValueDirectives( params );
        
        // need to rework ApplicationDescriptor such that the codebase is presented
        // as an abstract type - e.g. net.dpml.part.CodeBaseDirective verus 
        // net.dpml.metro.CodeBaseDirective
        
        return new ApplicationDescriptor( spec, title, values, base, policy, startup, shutdown, properties, null );
    }
    
    private StartupPolicy buildStartupPolicy( Element element ) throws Exception
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
    
    private int buildTimeout( Element element, int fallback ) throws Exception
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
    
    private Properties buildProperties( Element element )
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
                    throw new BuilderException( child, error );
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

    protected ValueDirective[] buildValueDirectives( Element[] elements )
    {
        ValueDirective[] values = new ValueDirective[ elements.length ];
        for( int i=0; i<elements.length; i++ )
        {
            values[i] = buildValueDirective( elements[i] );
        }
        return values;
    }
    
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

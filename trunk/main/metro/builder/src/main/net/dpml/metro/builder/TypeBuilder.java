/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.metro.builder;

import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import net.dpml.lang.Version;
import net.dpml.lang.BuilderException;

import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.info.Type;
import net.dpml.metro.info.InfoDescriptor;
import net.dpml.metro.info.ContextDescriptor;
import net.dpml.metro.info.CategoryDescriptor;
import net.dpml.metro.info.ServiceDescriptor;
import net.dpml.metro.info.EntryDescriptor;
import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.ThreadSafePolicy;
import net.dpml.metro.info.Priority;

import net.dpml.part.DOM3DocumentBuilder;

import net.dpml.state.State;
import net.dpml.state.impl.StateBuilder;
import net.dpml.state.impl.DefaultState;

import net.dpml.transit.util.ElementHelper;

import org.w3c.dom.Element;
import org.w3c.dom.Document;


/**
 * Type builder.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class TypeBuilder extends TypeWriter
{
    private static final StateBuilder STATE_BUILDER = new StateBuilder();
    
    private static final DOM3DocumentBuilder BUILDER = new DOM3DocumentBuilder();
    
   /**
    * Load a type.
    * @param subject the component implementation class
    * @return the component type descriptor
    * @exception IOException if an error occurs reading the type definition
    */
    public Type loadType( Class subject ) throws IOException
    {
        String classname = subject.getName();
        String path = classname.replace( '.', '/' );
        String target = path + ".type";
        ClassLoader classloader = subject.getClassLoader();
        URL url = classloader.getResource( target );
        if( null == url )
        {
            return Type.createType( subject );
        }
        else
        {
            try
            {
                URI uri = new URI( url.toString() );
                return loadType( uri );
            }
            catch( URISyntaxException e )
            {
                final String error =
                  "Bad uri value."
                  + "\nURI: " + url;
                IOException exception = new IOException( error );
                exception.initCause( e );
                throw exception;
            }
        }
    }

   /**
    * Load a type.
    * @param uri the component type source uri
    * @return the component type descriptor
    * @exception IOException if an error occurs reading the type definition
    */
    public Type loadType( URI uri ) throws IOException
    {
        try
        {
            final Document document = BUILDER.parse( uri );
            final Element root = document.getDocumentElement();
            return buildType( root );
        }
        catch( Throwable e )
        {
            final String error =
              "An error while attempting to load a type."
              + "\nType URI: " + uri;
            IOException exception = new IOException( error );
            exception.initCause( e );
            throw exception;
        }
    }
    
    private Type buildType( Element root ) throws Exception
    {
        InfoDescriptor info = buildNestedInfoDescriptor( root );
        ServiceDescriptor[] services = buildNestedServices( root );
        ContextDescriptor context = buildNestedContext( root );
        CategoryDescriptor[] categories = buildNestedCategories( root );
        State state = buildNestedState( root );
        PartReference[] parts = buildNestedParts( root );
        return new Type( info, categories, context, services, parts, state );
    }
    
    private InfoDescriptor buildNestedInfoDescriptor( Element root )
    {
        Element info = ElementHelper.getChild( root, "info" );
        if( null == info )
        {
            final String error = 
              "Definition of <type> is missing the required <info> element.";
            throw new BuilderException( root, error );
        }
        
        String name = ElementHelper.getAttribute( info, "name" );
        String classname = ElementHelper.getAttribute( info, "class" );
        String version = ElementHelper.getAttribute( info, "version" );
        String collection = ElementHelper.getAttribute( info, "collection", "system" );
        String lifestyle = ElementHelper.getAttribute( info, "lifestyle", "system" );
        String threadsafe = ElementHelper.getAttribute( info, "threadsafe", "unknown" );
        Properties properties = buildNestedProperties( info );
        
        return new InfoDescriptor( 
          name, 
          classname, 
          Version.getVersion( version ),
          LifestylePolicy.parse( lifestyle ),
          CollectionPolicy.parse( collection ),
          ThreadSafePolicy.parse( threadsafe ),
          properties );
    }
    
    private ServiceDescriptor[] buildNestedServices( Element root )
    {
        Element element = ElementHelper.getChild( root, "services" );
        if( null == element )
        {
            return new ServiceDescriptor[0];
        }
        else
        {
            Element[] children = ElementHelper.getChildren( element, "service" );
            ServiceDescriptor[] services = new ServiceDescriptor[ children.length ];
            for( int i=0; i<children.length; i++ )
            {
                Element child = children[i];
                String classname = ElementHelper.getAttribute( child, "class" );
                String version = ElementHelper.getAttribute( child, "version", "1.0.0" );
                services[i] = new ServiceDescriptor( classname, Version.getVersion( version ) );
            }
            return services;
        }
    }
    
    private ContextDescriptor buildNestedContext( Element root )
    {
        Element context = ElementHelper.getChild( root, "context" );
        if( null == context )
        {
            return new ContextDescriptor( new EntryDescriptor[0] );
        }
        else
        {
            Element[] children = ElementHelper.getChildren( context, "entry" );
            EntryDescriptor[] entries = new EntryDescriptor[ children.length ];
            for( int i=0; i<children.length; i++ )
            {
                Element child = children[i];
                String key = ElementHelper.getAttribute( child, "key" );
                String classname = ElementHelper.getAttribute( child, "class" );
                boolean optional = ElementHelper.getBooleanAttribute( child, "optional", false );
                boolean isVolatile = ElementHelper.getBooleanAttribute( child, "volatile", false );
                entries[i] = new EntryDescriptor( key, classname, optional, isVolatile );
            }
            return new ContextDescriptor( entries );
        }
    }
    
    private CategoryDescriptor[] buildNestedCategories( Element root )
    {
        Element element = ElementHelper.getChild( root, "categories" );
        if( null == element )
        {
            return new CategoryDescriptor[0];
        }
        else
        {
            Element[] children = ElementHelper.getChildren( element, "category" );
            CategoryDescriptor[] categories = new CategoryDescriptor[ children.length ];
            for( int i=0; i<children.length; i++ )
            {
                Element elem = children[i];
                String name = ElementHelper.getAttribute( elem, "name" );
                String priority = ElementHelper.getAttribute( elem, "priority" );
                Properties properties = buildNestedProperties( elem );
                categories[i] = new CategoryDescriptor( name, Priority.parse( priority ), properties );
            }
            return categories;
        }
    }
    
    private State buildNestedState( Element root )
    {
        // ISSUE: selection does not support namespace resolution
        // and will fail with something like <acme:state>
        
        Element element = ElementHelper.getChild( root, "state" );
        if( null == element )
        {
            return new DefaultState();
        }
        else
        {
            return STATE_BUILDER.buildStateGraph( element );
        }
    }
    
    private PartReference[] buildNestedParts( Element root ) throws Exception
    {
        Element element = ElementHelper.getChild( root, "parts" );
        if( null == element )
        {
            return new PartReference[0];
        }
        else
        {
            Element[] children = ElementHelper.getChildren( element );
            if( children.length == 0 )
            {
                return new PartReference[0];
            }
            else
            {
                PartReference[] refs = new PartReference[ children.length ];
                for( int i=0; i<children.length; i++ )
                {
                    Element child = children[i];
                    refs[i] = buildPartReference( child );
                }
                return refs;
            }
        }
    }
    
    private PartReference buildPartReference( Element element ) throws Exception
    {
        String key = ElementHelper.getAttribute( element, "key" );
        ComponentDirective definition = COMPONENT_BUILDER.buildComponent( element );
        return new PartReference( key, definition );
    }
    
    private Properties buildNestedProperties( Element element )
    {
        Element[] entries = ElementHelper.getChildren( element, "property" );
        if( entries.length == 0 )
        {
            return null;
        }
        else
        {
            Properties properties = new Properties();
            for( int i=0; i<entries.length; i++ )
            {
                Element elem = entries[i];
                String name = ElementHelper.getAttribute( elem, "name" );
                String value = ElementHelper.getAttribute( elem, "value" );
                properties.setProperty( name, value );
            }
            return properties;
        }
    }
    
}


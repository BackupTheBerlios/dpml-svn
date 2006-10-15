/*
 * Copyright 2006 Stephen J. McConnell.
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
import java.net.URI;

import net.dpml.component.ActivationPolicy;

import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.data.CategoryDirective;
import net.dpml.metro.data.CategoriesDirective;
import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.data.ValueDirective;
import net.dpml.metro.data.LookupDirective;

import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.Priority;

import net.dpml.lang.ValueDecoder;
import net.dpml.lang.Value;

import net.dpml.util.Resolver;
import net.dpml.util.DOM3DocumentBuilder;
import net.dpml.util.ElementHelper;
import net.dpml.util.DecodingException;
import net.dpml.util.SimpleResolver;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Construct a state graph.
 */
public class ComponentDecoder
{
    private static final String STATE_SCHEMA_URN = "@STATE-XSD-URI@";
    
    private static final String SCHEMA_URN = "@COMPONENT-XSD-URI@";
    
    private static final DOM3DocumentBuilder DOCUMENT_BUILDER = new DOM3DocumentBuilder();
    
    private static final ComponentTypeDecoder TYPE_DECODER = new ComponentTypeDecoder();
    
    private static final ValueDecoder VALUE_DECODER = new ValueDecoder();
    
   /**
    * Construct a component directive using the supplied uri. The uri
    * must refer to an XML document containing a root component element
    * (typically used in component data testcases).
    *
    * @param uri the part uri
    * @return the component directive
    * @exception IOException if an error occurs during directive creation
    */
    public ComponentDirective loadComponentDirective( URI uri ) throws IOException
    {
        if( null == uri )
        {
            throw new NullPointerException( "uri" );
        }
        try
        {
            final Document document = DOCUMENT_BUILDER.parse( uri );
            final Element root = document.getDocumentElement();
            Resolver resolver = new SimpleResolver();
            return buildComponent( root, resolver );
        }
        catch( Throwable e )
        {
            final String error =
              "An error while attempting to load a component directive."
              + "\nURI: " + uri;
            IOException exception = new IOException( error );
            exception.initCause( e );
            throw exception;
        }
    }
    
   /**
    * Construct a component directive using the supplied DOM element.
    * @param root the element representing the component directive definition
    * @param resolver build-time uri resolver
    * @return the component directive
    * @exception DecodingException if an error occurs during directive creation
    */
    public ComponentDirective buildComponent( Element root, Resolver resolver ) throws DecodingException
    {
        if( null == root )
        {
            throw new NullPointerException( "root" );
        }
        String tag = root.getTagName();
        if( "component".equals( tag ) )
        {
            return createComponentDirective( root, resolver );
        }
        else
        {
            final String error = 
              "Component directive element name [" 
              + tag 
              + "] is not recognized.";
            throw new DecodingException( root, error );
        }
    }
    
    private ComponentDirective createComponentDirective( 
      Element element, Resolver resolver ) throws DecodingException
    {
        String classname = buildComponentClassname( element );
        String name = buildComponentName( element );
        ActivationPolicy activation = buildActivationPolicy( element );
        CollectionPolicy collection = buildCollectionPolicy( element );
        LifestylePolicy lifestyle = buildLifestylePolicy( element );
        CategoriesDirective categories = getNestedCategoriesDirective( element );
        ContextDirective context = getNestedContextDirective( element );
        PartReference[] parts = getNestedParts( element, resolver );
        URI base = getBaseURI( element, resolver );
        
        if( null == base )
        {
            if( null == classname )
            {
                final String error = 
                  "Missing component type attribute.";
                throw new DecodingException( element, error );
            }
        }
        else
        {
            if( null != classname )
            {
                final String error = 
                  "llegal attempt to override a base type in a supertype.";
                throw new DecodingException( element, error );
            }
        }
        
        try
        {
            return new ComponentDirective( 
              name, activation, collection, lifestyle, classname, 
              categories, context, parts, base );
        }
        catch( Exception e )
        {
            final String error = 
              "Component directive creation error.";
            throw new DecodingException( element, error, e );
        }
    }
    
    private URI getBaseURI( Element element, Resolver resolver ) throws DecodingException
    {
        String base = ElementHelper.getAttribute( element, "uri" );
        if( null == base )
        {
            return null;
        }
        else
        {
            try
            {
                return resolver.toURI( base );
            }
            catch( Exception e )
            {
                final String error = 
                  "Error resolving 'uri' attribute value: " 
                  + base;
                throw new DecodingException( element, error, e );
            }
        }
    }
    
    private String buildComponentClassname( Element element ) throws DecodingException
    {
        return ElementHelper.getAttribute( element, "type" );
    }
    
    private ActivationPolicy buildActivationPolicy( Element element ) throws DecodingException
    {
        String policy = ElementHelper.getAttribute( element, "activation" );
        if( null == policy )
        {
            return null;
        }
        else
        {
            return ActivationPolicy.parse( policy );
        }
    }
    
    private LifestylePolicy buildLifestylePolicy( Element element ) throws DecodingException
    {
        String policy = ElementHelper.getAttribute( element, "lifestyle", null );
        if( null != policy )
        { 
            return LifestylePolicy.parse( policy );
        }
        else
        {
            return null;
        }
    }
    
    private CollectionPolicy buildCollectionPolicy( Element element ) throws DecodingException
    {
        String policy = ElementHelper.getAttribute( element, "collection" );
        if( null != policy )
        { 
            return CollectionPolicy.parse( policy );
        }
        else
        {
            return null;
        }
    }
    
    private String buildComponentName( Element element )
    {
        return ElementHelper.getAttribute( element, "name" );
    }
    
    private CategoriesDirective getNestedCategoriesDirective( Element root )
    {
        Element element = ElementHelper.getChild( root, "categories" );
        if( null == element )
        {
            return null;
        }
        else
        {
            return createCategoriesDirective( element );
        }
    }
    
    private CategoriesDirective createCategoriesDirective( Element element )
    {
        if( null == element )
        {
            return null;
        }
        else
        {
            String name = ElementHelper.getAttribute( element, "name" );
            Priority priority = createPriority( element );
            String target = ElementHelper.getAttribute( element, "target" );
            CategoryDirective[] categories = createCategoryDirectiveArray( element );
            return new CategoriesDirective( name, priority, target, categories );
        }
    }
    
    private CategoryDirective createCategoryDirective( Element element )
    {
        String name = ElementHelper.getAttribute( element, "name" );
        Priority priority = createPriority( element );
        String target = ElementHelper.getAttribute( element, "target" );
        return new CategoryDirective( name, priority, target );
    }
    
    private CategoryDirective[] createCategoryDirectiveArray( Element element )
    {
        Element[] children = ElementHelper.getChildren( element );
        CategoryDirective[] categories = new CategoryDirective[ children.length ];
        for( int i=0; i<categories.length; i++ )
        {
            Element elem = children[i];
            if( "category".equals( elem.getTagName() ) )
            {
                categories[i] = createCategoryDirective( elem );
            }
            else
            {
                categories[i] = createCategoriesDirective( elem );
            }
        }
        return categories;
    }
    
    private Priority createPriority( Element element )
    {
        String priority = ElementHelper.getAttribute( element, "priority" );
        if( null == priority )
        {
            return null;
        }
        else
        {
            return Priority.parse( priority );
        }
    }
    
    private ContextDirective getNestedContextDirective( Element root ) throws DecodingException
    {
        Element context = ElementHelper.getChild( root, "context" );
        if( null == context )
        {
            return null;
        }
        else
        {
            return createContextDirective( context );
        }
    }
    
    private ContextDirective createContextDirective( Element element ) throws DecodingException
    {
        String classname = ElementHelper.getAttribute( element, "class" );
        Element[] children = ElementHelper.getChildren( element );
        PartReference[] entries = new PartReference[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element elem = children[i];
            entries[i] = createContextEntryPartReference( elem );
        }
        return new ContextDirective( classname, entries );
    }
    
    private PartReference createContextEntryPartReference( Element element ) throws DecodingException
    {
        String key = ElementHelper.getAttribute( element, "key" );
        String spec = ElementHelper.getAttribute( element, "lookup" );
        if( null != spec )
        {
            LookupDirective directive = new LookupDirective( spec );
            return new PartReference( key, directive );
        }
        else
        {
            String name = element.getTagName();
            if( "entry".equals( name ) )
            {
                ValueDirective directive = buildValueDirective( element );
                return new PartReference( key, directive );
            }
            //else if( "component".equals( name ) )
            //{
            //    ComponentDirective directive = buildComponent( element );
            //    return new PartReference( key, directive );
            //}
            else
            {
                final String error = 
                  "Context entry element is not recognized.";
                throw new DecodingException( element, error );
            }
        }
    }
    
   /**
    * Build a value directive using a supplied DOM element.
    * @param element the DOM element
    * @return the value directive
    */
    protected ValueDirective buildValueDirective( Element element )
    {
        String classname = ElementHelper.getAttribute( element, "class" );
        String method = ElementHelper.getAttribute( element, "method" );
        Element[] elements = ElementHelper.getChildren( element, "param" );
        if( elements.length > 0 )
        {
            Value[] values = VALUE_DECODER.decodeValues( elements );
            return new ValueDirective( classname, method, values );
        }
        else
        {
            String value = ElementHelper.getAttribute( element, "value" );
            return new ValueDirective( classname, method, value );
        }
    }
    
    private PartReference[] getNestedParts( Element root, Resolver resolver ) throws DecodingException
    {
        Element parts = ElementHelper.getChild( root, "parts" );
        if( null == parts )
        {
            return null;
        }
        else
        {
            return createParts( parts, resolver );
        }
    }
    
    private PartReference[] createParts( Element element, Resolver resolver ) throws DecodingException
    {
        Element[] children = ElementHelper.getChildren( element );
        PartReference[] parts = new PartReference[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element elem = children[i];
            parts[i] = createPartReference( elem, resolver );
        }
        return parts;
    }
    
    private PartReference createPartReference( Element element, Resolver resolver ) throws DecodingException
    {
        String tag = element.getTagName();
        String key = ElementHelper.getAttribute( element, "key" );
        int priority = getPriority( element );
        if( "component".equals( tag ) )
        {
            ComponentDirective directive = buildComponent( element, resolver );
            return new PartReference( key, directive, priority );
        }
        else
        {
            final String error = 
              "Component part element name [" 
              + tag 
              + "] is not recognized.";
            throw new DecodingException( element, error );
        }
    }
    
    private int getPriority( Element element ) throws DecodingException
    {
        String priority = ElementHelper.getAttribute( element, "priority", "0" );
        try
        {
            return Integer.parseInt( priority );
        }
        catch( Exception e )
        {
            final String error = 
              "Unable to parse priority value.";
            throw new DecodingException( element, error, e );
        }
    }
    
   /**
    * Internal utility to get the name of the class without the package name. Used
    * when constructing a default component name.
    * @param classname the fully qualified classname
    * @return the short class name without the package name
    */
    private String toName( String classname )
    {
        int i = classname.lastIndexOf( "." );
        if( i == -1 )
        {
            return classname.toLowerCase();
        }
        else
        {
            return classname.substring( i + 1, classname.length() ).toLowerCase();
        }
    }

}

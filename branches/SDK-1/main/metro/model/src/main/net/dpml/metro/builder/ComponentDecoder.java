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
            //Resolver resolver = new SimpleResolver();
            //return buildComponent( root, resolver );
            return buildComponent( root, null );
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
        String classname = buildComponentClassname( element, resolver );
        String name = buildComponentName( element, resolver );
        ActivationPolicy activation = buildActivationPolicy( element, resolver );
        CollectionPolicy collection = buildCollectionPolicy( element, resolver );
        LifestylePolicy lifestyle = buildLifestylePolicy( element, resolver );
        CategoriesDirective categories = getNestedCategoriesDirective( element, resolver );
        ContextDirective context = getNestedContextDirective( element, resolver );
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
        String base = ElementHelper.getAttribute( element, "uri", null, resolver );
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
    
    private String buildComponentClassname( Element element, Resolver resolver ) throws DecodingException
    {
        return ElementHelper.getAttribute( element, "type", null, resolver );
    }
    
    private ActivationPolicy buildActivationPolicy( Element element, Resolver resolver ) throws DecodingException
    {
        String policy = ElementHelper.getAttribute( element, "activation", null, resolver );
        if( null == policy )
        {
            return null;
        }
        else
        {
            return ActivationPolicy.parse( policy );
        }
    }
    
    private LifestylePolicy buildLifestylePolicy( Element element, Resolver resolver ) throws DecodingException
    {
        String policy = ElementHelper.getAttribute( element, "lifestyle", null, resolver );
        if( null != policy )
        { 
            return LifestylePolicy.parse( policy );
        }
        else
        {
            return null;
        }
    }
    
    private CollectionPolicy buildCollectionPolicy( Element element, Resolver resolver ) throws DecodingException
    {
        String policy = ElementHelper.getAttribute( element, "collection", null, resolver );
        if( null != policy )
        {
            return CollectionPolicy.parse( policy );
        }
        else
        {
            return null;
        }
    }
    
    private String buildComponentName( Element element, Resolver resolver )
    {
        return ElementHelper.getAttribute( element, "name", null, resolver );
    }
    
    private CategoriesDirective getNestedCategoriesDirective( Element root, Resolver resolver )
    {
        Element element = ElementHelper.getChild( root, "categories" );
        if( null == element )
        {
            return null;
        }
        else
        {
            return createCategoriesDirective( element, resolver );
        }
    }
    
    private CategoriesDirective createCategoriesDirective( Element element, Resolver resolver )
    {
        if( null == element )
        {
            return null;
        }
        else
        {
            String name = ElementHelper.getAttribute( element, "name", null, resolver );
            Priority priority = createPriority( element, resolver );
            String target = ElementHelper.getAttribute( element, "target", null, resolver );
            CategoryDirective[] categories = createCategoryDirectiveArray( element, resolver );
            return new CategoriesDirective( name, priority, target, categories );
        }
    }
    
    private CategoryDirective createCategoryDirective( Element element, Resolver resolver )
    {
        String name = ElementHelper.getAttribute( element, "name", null, resolver );
        Priority priority = createPriority( element, resolver);
        String target = ElementHelper.getAttribute( element, "target", null, resolver );
        return new CategoryDirective( name, priority, target );
    }
    
    private CategoryDirective[] createCategoryDirectiveArray( Element element, Resolver resolver )
    {
        Element[] children = ElementHelper.getChildren( element );
        CategoryDirective[] categories = new CategoryDirective[ children.length ];
        for( int i=0; i<categories.length; i++ )
        {
            Element elem = children[i];
            if( "category".equals( elem.getTagName() ) )
            {
                categories[i] = createCategoryDirective( elem, resolver );
            }
            else
            {
                categories[i] = createCategoriesDirective( elem, resolver );
            }
        }
        return categories;
    }
    
    private Priority createPriority( Element element, Resolver resolver )
    {
        String priority = ElementHelper.getAttribute( element, "priority", null, resolver );
        if( null == priority )
        {
            return null;
        }
        else
        {
            return Priority.parse( priority );
        }
    }
    
    private ContextDirective getNestedContextDirective( Element root, Resolver resolver ) throws DecodingException
    {
        Element context = ElementHelper.getChild( root, "context" );
        if( null == context )
        {
            return null;
        }
        else
        {
            return createContextDirective( context, resolver );
        }
    }
    
    private ContextDirective createContextDirective( Element element, Resolver resolver ) throws DecodingException
    {
        String classname = ElementHelper.getAttribute( element, "class", null, resolver );
        Element[] children = ElementHelper.getChildren( element );
        PartReference[] entries = new PartReference[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element elem = children[i];
            entries[i] = createContextEntryPartReference( elem, resolver );
        }
        return new ContextDirective( classname, entries );
    }
    
    private PartReference createContextEntryPartReference( Element element, Resolver resolver ) throws DecodingException
    {
        String key = ElementHelper.getAttribute( element, "key", null, resolver );
        String spec = ElementHelper.getAttribute( element, "lookup", null, resolver );
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
                ValueDirective directive = buildValueDirective( element, resolver );
                return new PartReference( key, directive );
            }
            else if( "context".equals( name ) )
            {
                ContextDirective directive = createContextDirective( element, resolver );
                return new PartReference( key, directive );
            }
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
    protected ValueDirective buildValueDirective( Element element, Resolver resolver )
    {
        String classname = ElementHelper.getAttribute( element, "class", null, resolver );
        String method = ElementHelper.getAttribute( element, "method", null, resolver );
        Element[] elements = ElementHelper.getChildren( element, "param" );
        if( elements.length > 0 )
        {
            Value[] values = VALUE_DECODER.decodeValues( elements, resolver );
            return new ValueDirective( classname, method, values );
        }
        else
        {
            String value = ElementHelper.getAttribute( element, "value", null, resolver );
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
        String key = ElementHelper.getAttribute( element, "key", null, resolver );
        int priority = getPriority( element, resolver );
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
    
    private int getPriority( Element element, Resolver resolver ) throws DecodingException
    {
        String priority = ElementHelper.getAttribute( element, "priority", "0", resolver );
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

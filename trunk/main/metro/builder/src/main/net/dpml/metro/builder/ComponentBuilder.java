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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.XMLConstants;

import net.dpml.state.State;
import net.dpml.state.Action;
import net.dpml.state.Trigger.TriggerEvent;
import net.dpml.state.StateBuilderRuntimeException;

import net.dpml.part.DOM3DocumentBuilder;
import net.dpml.lang.BuilderException;
import net.dpml.part.PartDirective;
import net.dpml.part.ActivationPolicy;

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
import net.dpml.metro.info.Type;

import net.dpml.transit.util.ElementHelper;
import net.dpml.transit.Value;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.TypeInfo;

/**
 * Construct a state graph.
 */
public class ComponentBuilder extends ComponentWriter
{
    private static final String STATE_SCHEMA_URN = "@STATE-XSD-URI@";
    
    private static final String SCHEMA_URN = "@COMPONENT-XSD-URI@";
    
    private static DOM3DocumentBuilder BUILDER = new DOM3DocumentBuilder();
    
    private static TypeBuilder TYPE_BUILDER = new TypeBuilder();
    
    public ComponentDirective loadComponentDirective( URI uri ) throws IOException
    {
        if( null == uri )
        {
            throw new NullPointerException( "uri" );
        }
        try
        {
            final Document document = BUILDER.parse( uri );
            final Element root = document.getDocumentElement();
            return buildComponent( root );
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
    
    public ComponentDirective buildComponent( Element root ) throws Exception
    {
        if( null == root )
        {
            throw new NullPointerException( "root" );
        }
        
        return createComponentDirective( root );
    }
    
    private ComponentDirective createComponentDirective( Element element ) throws BuilderException
    {
        String classname = buildComponentClassname( element );
        String name = buildComponentName( element );
        ActivationPolicy activation = buildActivationPolicy( element );
        CollectionPolicy collection = buildCollectionPolicy( element );
        LifestylePolicy lifestyle = buildLifestylePolicy( element );
        CategoriesDirective categories = getNestedCategoriesDirective( element );
        ContextDirective context = getNestedContextDirective( element );
        PartReference[] parts = getNestedParts( element );
        
        return new ComponentDirective( 
          name, activation, collection, lifestyle, classname, 
          categories, context, null, null, parts );
    }
    
    private String buildComponentClassname( Element element ) throws BuilderException
    {
        String classname = ElementHelper.getAttribute( element, "class" );
        if( null == classname )
        {
            final String error =
              "Missing component 'class' attribute.";
            throw new BuilderException( element, error );
        }
        else
        {
            return classname;
        }
    }
    
    private ActivationPolicy buildActivationPolicy( Element element ) throws BuilderException
    {
        String defaultValue = ActivationPolicy.SYSTEM.getName();
        String policy = ElementHelper.getAttribute( element, "activation", defaultValue );
        return ActivationPolicy.parse( policy );
    }
    
    private LifestylePolicy buildLifestylePolicy( Element element ) throws BuilderException
    {
        String defaultValue = LifestylePolicy.TRANSIENT.getName();
        String policy = ElementHelper.getAttribute( element, "lifestyle", defaultValue );
        return LifestylePolicy.parse( policy );
    }
    
    private CollectionPolicy buildCollectionPolicy( Element element ) throws BuilderException
    {
        String defaultValue = CollectionPolicy.SYSTEM.getName();
        String policy = ElementHelper.getAttribute( element, "collection", defaultValue );
        return CollectionPolicy.parse( policy );
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
    
    private ContextDirective getNestedContextDirective( Element root )
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
    
    private ContextDirective createContextDirective( Element element )
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
    
    private PartReference createContextEntryPartReference( Element element )
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
            ValueDirective directive = buildValueDirective( element );
            return new PartReference( key, directive );
        }
    }
    
    protected ValueDirective buildValueDirective( Element element )
    {
        String classname = ElementHelper.getAttribute( element, "class" );
        String method = ElementHelper.getAttribute( element, "method" );
        Element[] elements = ElementHelper.getChildren( element, "param" );
        if( elements.length > 0 )
        {
            Value[] values = buildValues( elements );
            return new ValueDirective( classname, method, values );
        }
        else
        {
            String value = ElementHelper.getAttribute( element, "value" );
            return new ValueDirective( classname, method, value );
        }
    }
    
    private PartReference[] getNestedParts( Element root )
    {
        Element parts = ElementHelper.getChild( root, "parts" );
        if( null == parts )
        {
            return null;
        }
        else
        {
            return createParts( parts );
        }
    }
    
    private PartReference[] createParts( Element element )
    {
        Element[] children = ElementHelper.getChildren( element );
        PartReference[] parts = new PartReference[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element elem = children[i];
            parts[i] = createPartReference( elem );
        }
        return parts;
    }
    
    private PartReference createPartReference( Element element )
    {
        String key = ElementHelper.getAttribute( element, "key" );
        ComponentDirective directive = createComponentDirective( element );
        return new PartReference( key, directive );
    }
}

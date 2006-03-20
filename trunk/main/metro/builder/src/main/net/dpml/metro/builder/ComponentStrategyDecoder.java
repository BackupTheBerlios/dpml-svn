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

import java.net.URI;

import net.dpml.lang.Decoder;
import net.dpml.lang.DecodingException;
import net.dpml.lang.Value;

import net.dpml.metro.data.ComponentDirective;

import net.dpml.part.Strategy;
import net.dpml.part.PartDirective;
import net.dpml.part.DecoderFactory;
import net.dpml.part.ValueDecoder;

import net.dpml.library.Type;

import net.dpml.transit.util.ElementHelper;

import org.w3c.dom.Element;
import org.w3c.dom.TypeInfo;

/**
 * Component part handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentStrategyDecoder extends ComponentConstants implements Decoder
{
    private DecoderFactory m_factory;
    
    private static final ComponentDecoder COMPONENT_DECODER = new ComponentDecoder();
    
    private static final ValueDecoder VALUE_DECODER = new ValueDecoder();
    
   /**
    * Creation of a new component strategy builder.
    * @param factory the decoder factory
    */
    public ComponentStrategyDecoder( DecoderFactory factory )
    {
        m_factory = factory;
    }
    
   /**
    * Constructs a component deployment strategy.
    * @param classloader the base classloader
    * @param element the DOM element
    * @return the deployment strategy
    * @exception DecodingException if an error occurs during element evaluation
    */
    public Object decode( ClassLoader classloader, Element element ) throws DecodingException
    {
        return buildStrategy( classloader, element );
    }
    
   /**
    * Constructs a type definition.
    * @param classloader the base classloader
    * @param element the DOM element
    * @return the component part strategy
    * @exception DecodingException if an error occurs during element evaluation
    */
    public Type buildType( ClassLoader classloader, Element element ) throws DecodingException
    {
        boolean alias = ElementHelper.getBooleanAttribute( element, "alias", false );
        Strategy strategy = buildStrategy( classloader, element );
        return new DefaultType( "part", alias, strategy );
    }
    
   /**
    * Constructs a component deployment strategy.
    * @param classloader the base classloader
    * @param element the DOM element
    * @return the deployment strategy
    * @exception DecodingException if an error occurs during element evaluation
    */
    public Strategy buildStrategy( ClassLoader classloader, Element element ) throws DecodingException
    {
        TypeInfo info = element.getSchemaTypeInfo();
        String namespace = info.getTypeNamespace();
        String name = info.getTypeName();
        
        boolean alias = ElementHelper.getBooleanAttribute( element, "alias", false );
        
        if( "component".equals( name ) )
        {
            PartDirective control = createControllerDirective( null );
            ComponentDirective component = createComponentDirective( element );
            return new Strategy( BUILDER_URI, control, component, alias );
        }
        else if( "strategy".equals( name ) )
        {
            Element controller = ElementHelper.getChild( element, "controller" );
            PartDirective control = createControllerDirective( controller );
            Element directive = ElementHelper.getChild( element, "component" );
            ComponentDirective component = createComponentDirective( directive );
            return new Strategy( BUILDER_URI, control, component, alias );
        }
        else
        {
            final String error = 
              "Strategy element name [" 
              + name
              + "] is not recognized (expecting either 'component' or 'strategy').";
            throw new DecodingException( element, error );
        }
    }
    
    private ComponentDirective createComponentDirective( Element element ) throws DecodingException
    {
        return COMPONENT_DECODER.buildComponent( element );
    }
    
    private PartDirective createControllerDirective( Element element ) throws DecodingException
    {
        if( null == element )
        {
            try
            {
                return new PartDirective( CONTROLLER_URI, null );
            }
            catch( Exception e )
            {
                final String error = 
                  "Unexpected error during part directive creation.";
                throw new RuntimeException( error, e );
            }
        }
        else
        {
            URI uri = decodeURI( element );
            Element[] elements = ElementHelper.getChildren( element, "param" );
            Value[] values = VALUE_DECODER.decodeValues( elements );
            return new PartDirective( uri, values );
        }
    }
    
    private URI decodeURI( Element element ) throws DecodingException
    {
        String spec = ElementHelper.getAttribute( element, "uri" );
        if( null == spec )
        {
            final String error = 
              "Missing uri attribute.";
            throw new DecodingException( element, error );
        }
        try
        {
            return new URI( spec );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to construct uri from element uri attribute value ["
              + spec
              + "].";
            throw new DecodingException( element, error, e );
        }
    }
}

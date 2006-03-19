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
import java.util.Map;

import net.dpml.metro.data.ComponentDirective;

import net.dpml.lang.Value;
import net.dpml.transit.util.ElementHelper;

import net.dpml.part.Strategy;
import net.dpml.part.StrategyBuilder;
import net.dpml.part.PartDirective;

import net.dpml.lang.Builder;
import net.dpml.lang.BuilderException;
import net.dpml.library.Type;
import net.dpml.lang.DefaultType;

import org.w3c.dom.Element;
import org.w3c.dom.TypeInfo;

/**
 * Component part handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentStrategyBuilder extends ComponentStrategyWriter implements StrategyBuilder, Builder
{
   /**
    * Creation of a new component strategy builder.
    */
    public ComponentStrategyBuilder()
    {
        super( null );
    }
    
   /**
    * Creation of a new component strategy builder.
    * @param map namespace to builder uri map
    */
    public ComponentStrategyBuilder( Map map )
    {
        super( map );
    }
    
   /**
    * Constructs a component deployment strategy.
    * @param classloader the base classloader
    * @param element the DOM element
    * @return the deployment strategy
    * @exception Exception if an error occurs
    */
    public Object build( ClassLoader classloader, Element element ) throws Exception
    {
        return buildStrategy( classloader, element );
    }
    
   /**
    * Constructs a type definition.
    * @param classloader the base classloader
    * @param element the DOM element
    * @return the component part strategy
    * @exception Exception if an error occurs
    */
    public Type buildType( ClassLoader classloader, Element element ) throws Exception
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
    * @exception Exception if an error occurs
    */
    public Strategy buildStrategy( ClassLoader classloader, Element element ) throws Exception
    {
        TypeInfo info = element.getSchemaTypeInfo();
        String namespace = info.getTypeNamespace();
        String name = info.getTypeName();
        
        URI uri = new URI( BUILDER_URI );
        boolean alias = ElementHelper.getBooleanAttribute( element, "alias", false );
        
        if( "component".equals( name ) )
        {
            PartDirective control = createControllerDirective( null );
            ComponentDirective component = createComponentDirective( element );
            return new Strategy( uri, control, component, alias );
        }
        else if( "strategy".equals( name ) )
        {
            Element controller = ElementHelper.getChild( element, "controller" );
            PartDirective control = createControllerDirective( controller );
            Element directive = ElementHelper.getChild( element, "component" );
            ComponentDirective component = createComponentDirective( directive );
            return new Strategy( uri, control, component, alias );
        }
        else
        {
            final String error = 
              "Strategy element name [" 
              + name
              + "] is not recognized (expecting either 'component' or 'strategy').";
            throw new BuilderException( element, error );
        }
    }
    
    private ComponentDirective createComponentDirective( Element element ) throws Exception
    {
        return BUILDER.buildComponent( element );
    }
    
    private PartDirective createControllerDirective( Element element ) throws Exception
    {
        if( null == element )
        {
            try
            {
                URI uri = new URI( CONTROLLER_URI );
                return new PartDirective( uri, null );
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
            String spec = ElementHelper.getAttribute( element, "uri" );
            URI uri = new URI( spec );
            Element[] elements = ElementHelper.getChildren( element, "param" );
            Value[] values = buildValues( elements );
            return new PartDirective( uri, values );
        }
    }
}

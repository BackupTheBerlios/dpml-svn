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

package dpmlx.component;

import java.net.URI;

import net.dpml.metro.data.ComponentDirective;

import net.dpml.transit.info.ValueDirective;
import net.dpml.transit.util.ElementHelper;

import dpmlx.lang.Strategy;
import dpmlx.lang.StrategyBuilder;
import dpmlx.lang.PartDirective;
import dpmlx.lang.BuilderException;

import org.w3c.dom.Element;

/**
 * Component part handler.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentStrategyBuilder implements StrategyBuilder
{
    private final String CONTROLLER = "@CONTROLLER_URI@";
    
   /**
    * Constructs a component deployment strategy.
    *
    * @return the deployment strategy
    * @exception Exception if an error occurs
    */
    public Strategy buildStrategy( Element element ) throws Exception
    {
        Element controller = ElementHelper.getChild( element, "controller" );
        PartDirective control = createControllerDirective( controller );
        Element directive = ElementHelper.getChild( element, "component" );
        ComponentDirective component = createComponentDirective( directive );
        return new Strategy( control, component );
    }
    
    private ComponentDirective createComponentDirective( Element element ) throws BuilderException
    {
        String classname = getComponentClassname( element );
        String name = getComponentName( element );
        return new ComponentDirective( name, classname );
    }
    
    private String getComponentClassname( Element element ) throws BuilderException
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
    
    private String getComponentName( Element element )
    {
        return ElementHelper.getAttribute( element, "name" );
    }
    
    private PartDirective createControllerDirective( Element element ) throws Exception
    {
        if( null == element )
        {
            try
            {
                URI uri = new URI( CONTROLLER );
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
            ValueDirective[] values = createValueDirectives( elements );
            return new PartDirective( uri, values );
        }
    }

    private ValueDirective[] createValueDirectives( Element[] elements )
    {
        ValueDirective[] values = new ValueDirective[ elements.length ];
        for( int i=0; i<elements.length; i++ )
        {
            values[i] = createValueDirective( elements[i] );
        }
        return values;
    }
    
    private ValueDirective createValueDirective( Element element )
    {
        String classname = ElementHelper.getAttribute( element, "class" );
        String method = ElementHelper.getAttribute( element, "method" );
        Element[] elements = ElementHelper.getChildren( element, "param" );
        if( elements.length > 0 )
        {
            ValueDirective[] values = createValueDirectives( elements );
            return new ValueDirective( classname, method, values );
        }
        else
        {
            String value = ElementHelper.getAttribute( element, "value" );
            return new ValueDirective( classname, method, value );
        }
    }
}

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

package net.dpml.state.impl;

import java.io.IOException;
import java.net.URI;

import javax.xml.XMLConstants;

import net.dpml.state.State;
import net.dpml.state.Action;
import net.dpml.state.Trigger.TriggerEvent;
import net.dpml.state.StateBuilderRuntimeException;

import net.dpml.util.DOM3DocumentBuilder;

import net.dpml.util.ElementHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Construct a state graph.
 */
public class StateDecoder
{
    private static final String XML_HEADER = 
      "<?xml version=\"1.0\"?>";
    
    private static final String STATE_SCHEMA_URN = "@STATE-XSD-URI@";
    
    private static final String STATE_HEADER = 
      "<state xmlns=\"" 
      + STATE_SCHEMA_URN 
      + "\""
      + "\n    xmlns:xsi=\"" 
      + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI
      + "\">";

    private static final String STATE_FOOTER = "</state>";
    
    private static final DOM3DocumentBuilder BUILDER = new DOM3DocumentBuilder();
    
   /**
    * Load a state graph.
    * @param uri the graph uri
    * @return the constructed state graph
    * @exception IOException if an IO error occurs while reading the 
    *   graph data
    */
    public State loadState( URI uri ) throws IOException
    {
        if( null == uri )
        {
            throw new NullPointerException( "uri" );
        }
        try
        {
            final Document document = BUILDER.parse( uri );
            final Element root = document.getDocumentElement();
            return buildStateGraph( root );
        }
        catch( Throwable e )
        {
            final String error =
              "An error while attempting to load a state graph."
              + "\nURI: " + uri;
            IOException exception = new IOException( error );
            exception.initCause( e );
            throw exception;
        }
    }
    
   /**
    * Build a state graph.
    * @param element a DOM element representing the root of the state graph
    * @return the constructed state
    */
    public State buildStateGraph( Element element )
    {
        if( null == element )
        {
            throw new NullPointerException( "element" );
        }
        
        boolean terminal = ElementHelper.getBooleanAttribute( element, "terminal" );
        DefaultTransition[] transitions = buildNestedTransitions( element );
        DefaultOperation[] operations = buildNestedOperations( element );
        DefaultInterface[] interfaces = buildNestedInterfaces( element );
        DefaultState[] states = buildNestedStates( 1, element );
        DefaultTrigger[] triggers = buildNestedTriggers( element );
        return new DefaultState( 
          "root", triggers, transitions, interfaces, operations, states, terminal );
    }
    
    private DefaultTransition[] buildNestedTransitions( Element element )
    {
        Element[] children = ElementHelper.getChildren( element, "transition" );
        DefaultTransition[] transitions = new DefaultTransition[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            transitions[i] = buildTransition( child );
        }
        return transitions;
    }
    
    private DefaultOperation[] buildNestedOperations( Element element )
    {
        Element[] children = ElementHelper.getChildren( element, "operation" );
        DefaultOperation[] operations = new DefaultOperation[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            operations[i] = buildOperation( child );
        }
        return operations;
    }
    
    private DefaultInterface[] buildNestedInterfaces( Element element )
    {
        Element[] children = ElementHelper.getChildren( element, "interface" );
        DefaultInterface[] interfaces = new DefaultInterface[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            interfaces[i] = buildInterface( child );
        }
        return interfaces;
    }
    
    private DefaultState[] buildNestedStates( int n, Element element )
    {
        Element[] children = ElementHelper.getChildren( element, "state" );
        DefaultState[] states = new DefaultState[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            states[i] = buildState( n, child );
        }
        return states;
    }
    
    private DefaultTrigger[] buildNestedTriggers( Element element )
    {
        Element[] children = ElementHelper.getChildren( element, "trigger" );
        DefaultTrigger[] triggers = new DefaultTrigger[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            triggers[i] = buildTrigger( child );
        }
        return triggers;
    }
    
    private DefaultTransition buildTransition( Element element )
    {
        String name = ElementHelper.getAttribute( element, "name" );
        String target = ElementHelper.getAttribute( element, "target" );
        Element child = ElementHelper.getChild( element, "operation" );
        DefaultOperation operation = buildOperation( child );
        return new DefaultTransition( name, target, operation );
    }
    
    private DefaultOperation buildOperation( Element element )
    {
        if( null == element )
        {
            return null;
        }
        String name = ElementHelper.getAttribute( element, "name" );
        String method = ElementHelper.getAttribute( element, "method" );
        return new DefaultOperation( name, method );
    }
    
    private DefaultInterface buildInterface( Element element )
    {
        String classname = ElementHelper.getAttribute( element, "class" );
        return new DefaultInterface( classname );
    }
    
    private DefaultState buildState( int n, Element element )
    {
        String name = ElementHelper.getAttribute( element, "name" );
        boolean terminal = ElementHelper.getBooleanAttribute( element, "terminal" );
        DefaultTransition[] transitions = buildNestedTransitions( element );
        DefaultOperation[] operations = buildNestedOperations( element );
        DefaultInterface[] interfaces = buildNestedInterfaces( element );
        DefaultState[] states = buildNestedStates( n+1, element );
        DefaultTrigger[] triggers = buildNestedTriggers( element );
        return new DefaultState( 
          name, triggers, transitions, interfaces, operations, states, terminal );
    }
    
    private DefaultTrigger buildTrigger( Element element )
    {
        String type = ElementHelper.getAttribute( element, "event" );
        TriggerEvent event = TriggerEvent.parse( type );
        Element child = getSingleNestedElement( element );
        Action action = buildAction( child );
        return new DefaultTrigger( event, action );
    }
    
    private Action buildAction( Element element )
    {
        String name = element.getTagName();
        if( name.equals( "transition" ) )
        {
            return buildTransition( element );
        }
        else if( name.equals( "operation" ) )
        {
            return buildOperation( element );
        }
        else if( name.equals( "apply" ) )
        {
            String id = ElementHelper.getAttribute( element, "id" );
            return new ApplyAction( id );
        }
        else if( name.equals( "exec" ) )
        {
            String id = ElementHelper.getAttribute( element, "id" );
            return new ExecAction( id );
        }
        else
        {
            final String error = 
              "Illegal element name ["
              + name
              + "] supplied to action builder.";
            throw new StateBuilderRuntimeException( error );
        }
    }
    
    private Element getSingleNestedElement( Element parent )
    {
        if( null == parent )
        {
            throw new NullPointerException( "parent" );
        }
        else
        {
            Element[] children = ElementHelper.getChildren( parent );
            if( children.length == 1 )
            {
                return children[0];
            }
            else
            {
                final String error = 
                  "Parent element does not contain a single child.";
                throw new IllegalArgumentException( error );
            }
        }
    }
}

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

package net.dpml.state;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;

import javax.xml.XMLConstants;

import net.dpml.state.State;
import net.dpml.state.Action;
import net.dpml.state.Trigger.TriggerEvent;
import net.dpml.state.Trigger;
import net.dpml.state.Transition;
import net.dpml.state.Operation;
import net.dpml.state.Interface;

/**
 * Construct a state graph.
 */
public class StateEncoder
{
    private static final String XML_HEADER = 
      "<?xml version=\"1.0\"?>";
    
    private static final String STATE_SCHEMA_URN = "@STATE-XSD-URI@";
    
   /**
    * Externalize the part to XML.
    * @param state the state graph to externalize
    * @param output the output stream 
    * @exception IOException if an IO error occurs
    */
    public void export( State state, OutputStream output ) throws IOException
    {
        final Writer writer = new OutputStreamWriter( output );

        writer.write( XML_HEADER );
        writer.write( "\n\n" );
        writer.write( "<state xmlns=\"" 
          + STATE_SCHEMA_URN 
          + "\""
          + "\n    xmlns:xsi=\"" 
          + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI
          + "\"" );
        if( state.isTerminal() )
        {
            writer.write( " terminal=\"true\">" );
        }
        else
        {
            writer.write( ">" );
        }
        writer.write( "\n" );
        writeBody( writer, state, "" );
        writer.write( "\n" );
        writer.write( "\n</state>" );
        writer.write( "\n" );
        writer.flush();
        output.close();
    }
    
   /**
    * Write the state.
    * @param writer the stream writer
    * @param state the state to externalize
    * @param pad the pad offset
    * @exception IOException if an IO error occurs
    */
    public void writeState( Writer writer, State state, String pad ) throws IOException
    {
        if( isEmpty( state ) )
        {
            return;
        }
        else
        {
            writer.write( "\n" + pad + "<state xmlns=\"" + STATE_SCHEMA_URN + "\"" );
            if( state.isTerminal() )
            {
                writer.write( " terminal=\"true\"" );
            }
            writer.write( ">" );
            writeBody( writer, state, pad + "  " );
            writer.write( "\n" + pad + "</state>" );
        }
    }
    
    private void writeBody( Writer writer, State state, String pad )  throws IOException
    {
        Trigger[] triggers = state.getTriggers();
        Transition[] transitions = state.getTransitions();
        Operation[] operations = state.getOperations();
        Interface[] interfaces = state.getInterfaces();
        State[] states = state.getStates();
        
        writeTriggers( writer, triggers, pad );
        writeTransitions( writer, transitions, pad );
        writeOperations( writer, operations, pad );
        writeInterfaces( writer, interfaces, pad );
        writeStates( writer, states, pad );
    }
    
    private void writeTriggers( Writer writer, Trigger[] triggers, String pad )  throws IOException
    {
        if( triggers.length == 0 )
        {
            return;
        }
        else
        {
            for( int i=0; i<triggers.length; i++ )
            {
                Trigger trigger = triggers[i];
                writeTrigger( writer, trigger, pad );
            }
        }
    }
    
    private void writeTrigger( Writer writer, Trigger trigger, String pad )  throws IOException
    {
        TriggerEvent event = trigger.getEvent();
        writer.write( "\n" + pad + "<trigger event=\"" );
        writer.write( event.getName() );
        writer.write( "\">" );
        Action action = trigger.getAction();
        writeAction( writer, action, pad + "  " );
        writer.write( "\n" + pad + "</trigger>" );
    }
    
    private void writeTransitions( Writer writer, Transition[] transitions, String pad )  throws IOException
    {
        if( transitions.length == 0 )
        {
            return;
        }
        else
        {
            for( int i=0; i<transitions.length; i++ )
            {
                Transition transition = transitions[i];
                writeTransition( writer, transition, pad );
            }
        }
    }
    
    private void writeOperations( Writer writer, Operation[] operations, String pad )  throws IOException
    {
        if( operations.length == 0 )
        {
            return;
        }
        else
        {
            for( int i=0; i<operations.length; i++ )
            {
                Operation operation = operations[i];
                writeOperation( writer, operation, pad );
            }
        }
    }
    
    private void writeInterfaces( Writer writer, Interface[] interfaces, String pad )  throws IOException
    {
        if( interfaces.length == 0 )
        {
            return;
        }
        else
        {
            for( int i=0; i<interfaces.length; i++ )
            {
                Interface spec = interfaces[i];
                writeInterface( writer, spec, pad );
            }
        }
    }
    
    private void writeStates( Writer writer, State[] states, String pad )  throws IOException
    {
        if( states.length == 0 )
        {
            return;
        }
        else
        {
            for( int i=0; i<states.length; i++ )
            {
                State state = states[i];
                writeNestedState( writer, state, pad );
            }
        }
    }
    
    private void writeTransition( Writer writer, Transition transition, String pad )  throws IOException
    {
        String name = transition.getName();
        String target = transition.getTargetName();
        writer.write( "\n" + pad + "<transition name=\"" + name + "\" target=\"" + target + "\"" );
        Operation operation = transition.getOperation();
        if( null == operation )
        {
            writer.write( "/>" );
        }
        else
        {
            writer.write( ">" );
            writeOperation( writer, operation, pad + "  " );
            writer.write( "\n" + pad + "</transition>" );
        }
    }

    private void writeOperation( Writer writer, Operation operation, String pad )  throws IOException
    {
        String name = operation.getName();
        String method = operation.getMethodName();
        writer.write( "\n" + pad + "<operation name=\"" + name + "\"" );
        if( null != method )
        {
            writer.write( " method=\"" + method + "\"" );
        }
        writer.write( "/>" );
    }
    
    private void writeInterface( Writer writer, Interface spec, String pad )  throws IOException
    {
        String classname = spec.getClassname();
        writer.write( "\n" + pad + "<interface class=\"" + classname + "\"/>" );
    }
    
    private void writeAction( Writer writer, Action action, String pad )  throws IOException
    {
        if( action instanceof Transition )
        {
            Transition transition = (Transition) action;
            writeTransition( writer, transition, pad );
        }
        else if( action instanceof Operation )
        {
            Operation operation = (Operation) action;
            writeOperation( writer, operation, pad );
        }
        else if( action instanceof Interface )
        {
            Interface spec = (Interface) action;
            writeInterface( writer, spec, pad );
        }
        else if( action instanceof ExecAction )
        {
            ExecAction exec = (ExecAction) action;
            writeExecAction( writer, exec, pad );
        }
        else if( action instanceof ApplyAction )
        {
            ApplyAction apply = (ApplyAction) action;
            writeApplyAction( writer, apply, pad );
        }
        else
        {
            final String error = 
              "Unrecognized action class ["
              + action.getClass().getName()
              + "].";
            throw new IOException( error );
        }
    }
    private void writeExecAction( Writer writer, ExecAction action, String pad )  throws IOException
    {
        String id = action.getID();
        writer.write( "\n" + pad + "<exec id=\"" + id + "\"/>" );
    }
    
    private void writeApplyAction( Writer writer, ApplyAction action, String pad )  throws IOException
    {
        String id = action.getID();
        writer.write( "\n" + pad + "<apply id=\"" + id + "\"/>" );
    }
    
    private void writeNestedState( Writer writer, State state, String pad )  throws IOException
    {
        String name = state.getName();
        writer.write( "\n" + pad + "<state name=\"" + name + "\"" );
        if( state.isTerminal() )
        {
            writer.write( " terminal=\"true\">" );
        }
        else
        {
            writer.write( ">" );
        }
        writeBody( writer, state, pad + "  " );
        writer.write( "\n" + pad + "</state>" );
    }

    private boolean isEmpty( State state )
    {
        if( state.getTriggers().length > 0 )
        {
            return false;
        }
        if( state.getTransitions().length > 0 )
        {
            return false;
        }
        if( state.getOperations().length > 0 )
        {
            return false;
        }
        if( state.getInterfaces().length > 0 )
        {
            return false;
        }
        if( state.getStates().length > 0 )
        {
            return false;
        }
        return true;
    }
}

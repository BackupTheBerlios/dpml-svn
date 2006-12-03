/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.metro.tools;

import org.apache.tools.ant.BuildException;

import net.dpml.state.Trigger.TriggerEvent;
import net.dpml.state.DefaultTrigger;
import net.dpml.state.DefaultTransition;
import net.dpml.state.DefaultOperation;
import net.dpml.state.ApplyAction;
import net.dpml.state.ExecAction;

/**
 * Utility datatype supporting a Transition instance construction.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TriggerDataType
{
    private String m_event;
    private OperationDataType m_operation;
    private TransitionDataType m_transition;
    private ApplyDataType m_apply;
    private ExecDataType m_exec;
    
   /**
    * Set the trigger event class.
    * @param event the event name
    */
    public void setEvent( final String event )
    {
        if( null == event )
        {
            throw new NullPointerException( "event" );
        }
        m_event = event;
    }
    
   /**
    * Add an operation to the trigger.
    * @return the operation datatype
    */
    public OperationDataType createOperation()
    {
        if( ( null != m_operation ) || ( null != m_transition ) || ( null != m_apply ) || ( null != m_exec ) )
        {
            final String error = 
              "Trigger action is already defined.";
            throw new IllegalStateException( error );
        }
        m_operation = new OperationDataType();
        return m_operation;
    }
    
   /**
    * Add an transition to the trigger.
    * @return the transition datatype
    */
    public TransitionDataType createTransition()
    {
        if( ( null != m_operation ) || ( null != m_transition ) || ( null != m_apply ) || ( null != m_exec ) )
        {
            final String error = 
              "Trigger action is already defined.";
            throw new IllegalStateException( error );
        }
        m_transition = new TransitionDataType();
        return m_transition;
    }
    
   /**
    * Add an apply to the trigger.
    * @return the apply datatype
    */
    public ApplyDataType createApply()
    {
        if( ( null != m_operation ) || ( null != m_transition ) || ( null != m_apply ) || ( null != m_exec ) )
        {
            final String error = 
              "Trigger action is already defined.";
            throw new IllegalStateException( error );
        }
        m_apply = new ApplyDataType();
        return m_apply;
    }
    
   /**
    * Add an exec to the trigger.
    * @return the exec datatype
    */
    public ExecDataType createExec()
    {
        if( ( null != m_operation ) || ( null != m_transition ) || ( null != m_apply ) || ( null != m_exec ) )
        {
            final String error = 
              "Trigger action is already defined.";
            throw new IllegalStateException( error );
        }
        m_exec = new ExecDataType();
        return m_exec;
    }

    DefaultTrigger getTrigger()
    {
        TriggerEvent event = getTriggerEvent();
        if( m_apply != null )
        {
            ApplyAction action = m_apply.getAction();
            return new DefaultTrigger( event, action );
        }
        else if( m_exec != null )
        {
            ExecAction action = m_exec.getAction();
            return new DefaultTrigger( event, action );
        }
        else if( m_operation != null )
        {
            DefaultOperation action = m_operation.getOperation();
            return new DefaultTrigger( event, action );
        }
        else if( m_transition != null )
        {
            DefaultTransition action = m_transition.getTransition();
            return new DefaultTrigger( event, action );
        }
        else
        {
            final String error = 
              "Missing action ('operation'|transition'|'apply'|'exec') element in trigger.";
            throw new BuildException( error );
        }
    }
    
    TriggerEvent getTriggerEvent()
    {
        if( null == m_event )
        {
            throw new BuildException( "Missing event attribute in trigger." );
        }
        else
        {
            return TriggerEvent.parse( m_event );
        }
    }
}

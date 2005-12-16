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

import java.util.ArrayList;
import java.util.List;

import net.dpml.state.Operation;
import net.dpml.state.impl.DefaultTransition;
import net.dpml.state.impl.DefaultOperation;

import org.apache.tools.ant.BuildException;

/**
 * Utility datatype supporting a Transition instance construction.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TransitionDataType
{
    private String m_name;
    private String m_target;
    private OperationDataType m_operation;
    
   /**
    * Set the operation name.
    * @param name the operation name
    */
    public void setName( final String name )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        m_name = name;
    }
    
   /**
    * Set the transition target.
    * @param target the relative address of the target state
    */
    public void setTarget( final String target )
    {
        if( null == target )
        {
            throw new NullPointerException( "target" );
        }
        m_target = target;
    }
    
   /**
    * Add an operation to the transition.
    * @return the operation datatype
    */
    public OperationDataType createOperation()
    {
        if( null != m_operation )
        {
            final String error = 
              "Transition is attempting to declare more than one operation.";
            throw new IllegalStateException( error );
        }
        m_operation = new OperationDataType();
        return m_operation;
    }
    
    DefaultTransition getTransition()
    {
        String name = getName();
        String target = getTargetName();
        if( null == m_operation )
        {
            return new DefaultTransition( name, target, null );
        }
        else
        {
            DefaultOperation operation = m_operation.getOperation();
            return new DefaultTransition( name, target, operation );
        }
    }
    
    String getName()
    {
        if( null != m_name )
        {
            return m_name;
        }
        else
        {
            throw new BuildException( "Missing transition name attribute." );
        }
    }

    String getTargetName()
    {
        if( null != m_target )
        {
            return m_target;
        }
        else
        {
            throw new BuildException( "Missing transition target attribute." );
        }
    }
}

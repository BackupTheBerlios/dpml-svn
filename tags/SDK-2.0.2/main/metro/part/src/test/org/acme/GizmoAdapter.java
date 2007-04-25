/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

package org.acme;

import net.dpml.lang.Strategy;

import net.dpml.runtime.ComponentError;

/**
 * Example of a component that is declared as a service (via META=INF/services)
 * while actual deployment is handled via net.dpml.runtime.ComponentStrategy.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class GizmoAdapter implements Gizmo
{
    private Gizmo m_delegate;
    
   /** 
    * Get a number.
    * @return a number
    */
    public int getNumber()
    {
        return getDelegate().getNumber();
    }
    
    private Gizmo getDelegate()
    {
        if( null == m_delegate )
        {
            m_delegate = createDelegate();
        }
        return m_delegate;
    }
    
    private Gizmo createDelegate()
    {
        try
        {
            Strategy strategy = Strategy.load( DefaultGizmo.class, null, "gizmo" );
            return strategy.getInstance( Gizmo.class );
        }
        catch( Exception e )
        {
            final String error = 
              "Error in gizmo adapter delegate establishment.";
            throw new ComponentError( error, e );
        }
    }

   /**
    * Test the supplied object for equality with this object.
    * @param other the supplied object 
    * @return the equality result
    */
    public boolean equals( Object other )
    {
        return ( hashCode() == other.hashCode() );
    }

   /**
    * Get the component hashcode.
    * @return the hash value
    */
    public int hashCode()
    {
        if( null != m_delegate )
        {
            return m_delegate.hashCode();
        }
        else
        {
            return getClass().hashCode();
        }
    }
}

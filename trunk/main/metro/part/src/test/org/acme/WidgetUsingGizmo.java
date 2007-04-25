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

import net.dpml.annotation.Context;
import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import net.dpml.util.Logger;

/**
 * Component playing around with a corner case.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( name="foreign" )
@Services( Widget.class )
public class WidgetUsingGizmo implements Widget
{
   /**
    * Deployment contract.
    */
    @Context
    public interface WidgetContext
    {
       /**
        * Return the 'gizmo' context value.
        * @return a gizmo
        */
        Gizmo getGizmo();
    }
    
    private final Gizmo m_gizmo;
    
   /**
    * Component constructor.
    * @param logger the logging channel
    * @param context the deployment context
    */
    public WidgetUsingGizmo( final Logger logger, final WidgetContext context )
    {
        logger.info( "instantiated" );
        m_gizmo = context.getGizmo();
        logger.info( getMessage() );
    }
    
   /**
    * Get a message.
    * @return a message
    */
    public String getMessage()
    {
        return "" + m_gizmo.getNumber();
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
        return m_gizmo.hashCode();
    }
}

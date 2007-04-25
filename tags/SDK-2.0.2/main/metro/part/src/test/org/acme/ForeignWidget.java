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

import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import net.dpml.util.Logger;

/**
 * Sample component that contains a foreign type in the constructor.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( name="foreign" )
@Services( Widget.class )
public class ForeignWidget implements Widget
{
    private final Gizmo m_gizmo;
    
   /**
    * Component constructor containing a standard logging argument and 
    * a foreign gizmo object.  In this example the container will recognize the 
    * logging channel type but will not recognize the Gizmo service.  As such the 
    * container has to go hunting for a solution.
    * @param logger the assigned logging channel
    * @param gizmo a gizmo
    */
    public ForeignWidget( final Logger logger, final Gizmo gizmo )
    {
        logger.info( "instantiated" );
        m_gizmo = gizmo;
        logger.info( getMessage() );
    }
    
   /**
    * Return a message.
    * @return the m4essage
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

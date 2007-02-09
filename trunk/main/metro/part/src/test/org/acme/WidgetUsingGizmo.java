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

import java.awt.Color;

import net.dpml.annotation.Context;
import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import net.dpml.util.Logger;

import static net.dpml.annotation.LifestylePolicy.TRANSIENT;

/**
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( name="foreign" )
@Services( Widget.class )
public class WidgetUsingGizmo implements Widget
{
    @Context
    public interface WidgetContext
    {
        Gizmo getGizmo();
    }
    
    private final Gizmo m_gizmo;
    
    public WidgetUsingGizmo( final Logger logger, final WidgetContext context )
    {
        logger.info( "instantiated" );
        m_gizmo = context.getGizmo();
        logger.info( getMessage() );
    }
    
    public String getMessage()
    {
        return "" + m_gizmo.getNumber();
    }
    
    public boolean equals( Object other )
    {
        return ( hashCode() == other.hashCode() );
    }
}

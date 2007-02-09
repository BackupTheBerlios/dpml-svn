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

import net.dpml.util.Logger;

/**
 * Sample plugin-based implementation of a gizmo.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultGizmo implements Gizmo
{
    private final Logger m_logger;
    
    public DefaultGizmo( Logger logger )
    {
        m_logger = logger;
        m_logger.info( "instantiated" );
    }
    
    public int getNumber()
    {
        return 42;
    }

    public boolean equals( Object other )
    {
        return ( hashCode() == other.hashCode() );
    }
}

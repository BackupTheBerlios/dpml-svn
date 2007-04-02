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
import java.util.Map;
import java.util.SortedMap;

import net.dpml.util.Logger;

/**
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class MapWidget implements Widget
{
    public interface Context
    {
        Map<String,String> getPrimary();
        SortedMap<String,String> getSecondary();
        Map<String,Color> getColors();
    }
    
    private Context m_context;
    
    public MapWidget( Logger logger, Context context )
    {
        m_context = context;
        logger.info( "" + getMessage() );
    }
    
    public String getMessage()
    {
        return m_context.getPrimary().get( "message" );
    }
    
    public Context getContext()
    {
        return m_context;
    }
    
    public boolean equals( Object other )
    {
        return ( hashCode() == other.hashCode() );
    }
}

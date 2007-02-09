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

import static net.dpml.annotation.LifestylePolicy.TRANSIENT;

import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import net.dpml.util.Logger;

/**
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component
@Services( Widget.class )
public class StandaloneWidget implements Widget
{
    public interface Context
    {
        String getMessage( String value );
        ColorManager getColors();
        int[] getNumbers();
    }
    
    private final Context m_context;
    
    public StandaloneWidget( Logger logger, Context context )
    {
        logger.info( "instantiated" );
        logger.info( "message: " + context.getMessage( null ) );
        logger.info( "colors: " + context.getColors() );
        for( int n : context.getNumbers() )
        {
            logger.info( "  " + n );
        }
        m_context = context;
    }
    
    public String getMessage()
    {
        return m_context.getMessage( "Hello" );
    }
    
    public Color getPrimary()
    {
        return m_context.getColors().getPrimary();
    }
    
    public Color getSecondary()
    {
        return m_context.getColors().getSecondary();
    }
    
    public int[] getNumbers()
    {
        return m_context.getNumbers();
    }
    
    @net.dpml.annotation.Context
    public interface ColorManager
    {
        Color getPrimary();
        Color getSecondary();
    }
    
    public boolean equals( Object other )
    {
        return ( hashCode() == other.hashCode() );
    }
}

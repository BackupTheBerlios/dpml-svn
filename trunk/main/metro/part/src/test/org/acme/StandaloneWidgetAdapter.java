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

import dpml.util.DefaultLogger;
import net.dpml.util.Logger;

import net.dpml.runtime.ComponentError;
import net.dpml.runtime.ComponentStrategyHandler;

/**
 * Example of a component that is declared as a service (via META=INF/services)
 * while actual deployment is handled via net.dpml.runtime.ComponentStrategy.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class StandaloneWidgetAdapter implements Widget
{
    private Widget m_delegate;
    private Logger m_logger = new DefaultLogger( "adapter" );
    
    public StandaloneWidgetAdapter()
    {
        m_logger.info( "instantiated" ); 
    }
    
    public String getMessage()
    {
        return getDelegate().getMessage();
    }
    
    private Widget getDelegate()
    {
        if( null == m_delegate )
        {
            m_delegate = createDelegate();
        }
        return m_delegate;
    }
    
    private Widget createDelegate()
    {
        m_logger.info( "creating delegate" );
        try
        {
            Strategy strategy = Strategy.load( StandaloneWidget.class, null, "adapter.standalone" );
            return strategy.getInstance( Widget.class );
        }
        catch( Exception e )
        {
            final String error = 
              "Error in widget adapter delegate establishment.";
            throw new ComponentError( error, e );
        }
    }

    public boolean equals( Object other )
    {
        return ( hashCode() == other.hashCode() );
    }
}

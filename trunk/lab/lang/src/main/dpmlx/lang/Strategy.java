/*
 * Copyright 2006 Stephen J. McConnell.
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

package dpmlx.lang;

import java.io.Serializable;
import java.net.URI;

/**
 * Part deployment strategy description.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Strategy implements Serializable
{
    private final URI m_controller;
    private final Serializable m_data;
    
    public Strategy( URI controller, Serializable data )
    {
        if( null == controller )
        {
            throw new NullPointerException( "controller" );
        }
        if( null == data )
        {
            throw new NullPointerException( "data" );
        }
        m_controller = controller;
        m_data = data;
    }
    
   /**
    * Get the uri identifying the deployment controller.
    *
    * @return the deployment controller uri
    */
    public URI getControllerURI()
    {
        return m_controller;
    }
    
   /**
    * Get the deployment data.
    *
    * @return the deployment datastructure
    */
    public Object getDeploymentData()
    {
        return m_data;
    }
    
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof Strategy )
        {
            Strategy strategy = (Strategy) other;
            if( !m_controller.equals( strategy.m_controller ) )
            {
                return false;
            }
            else
            {
                return m_data.equals( strategy.m_data );
            }
        }
        else
        {
            return false;
        }
    }
    
    public int hashCode()
    {
        int hash = m_controller.hashCode();
        hash ^= m_data.hashCode();
        return hash;
    }
}

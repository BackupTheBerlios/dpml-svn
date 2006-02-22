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

package net.dpml.lang;

import java.io.Serializable;
import java.util.Properties;

/**
 * Plugin strategy.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultStrategy implements Strategy
{
    private final String m_handler;
    private final Properties m_properties;
    
   /**
    * Creation of a new strategy definition.
    * @param handler the plugin handler classname
    * @param properties the handler supplimentary properties
    */
    public DefaultStrategy( final String handler, final Properties properties )
    {
        if( null == handler )
        {
            throw new NullPointerException( "handler" );
        }
        m_handler = handler;
        if( null == properties )
        {
            m_properties = new Properties();
        }
        else
        {
            m_properties = properties;
        }
    }
    
   /**
    * Return the classname of the custom plugin instantiation handler.
    * @return the handler classname
    */
    public String getHandlerClassname()
    {
        return m_handler;
    }
    
   /**
    * Return the the properties to be supplied to the handler.
    * @return the custom properties
    */
    public Properties getProperties()
    {
        return m_properties;
    }

   /**
    * Test this object for equality with the supplied object.
    * @param other the other object
    * @return true if the objects are equal
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof Strategy )
        {
            Strategy strategy = (Strategy) other;
            if( !m_handler.equals( strategy.getHandlerClassname() ) ) 
            {
                return false;
            }
            else
            {
                return m_properties.equals( strategy.getProperties() );
            }
        }
        else
        {
            return false;
        }
    }

   /**
    * Return the hashcode for the instance.
    * @return the instance hashcode
    */
    public int hashCode()
    {
        int hash = m_handler.hashCode();
        hash ^= m_properties.hashCode();
        return hash;
    }
}

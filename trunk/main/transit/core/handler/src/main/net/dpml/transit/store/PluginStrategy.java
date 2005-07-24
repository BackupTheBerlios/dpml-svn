/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.transit.store;

import java.net.URI;

/**
 * Serializable class that describes a plugin stratelgy.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class PluginStrategy extends Strategy
{
    private URI m_uri;

   /**
    * Creation of a new plugin strategy instance.
    * @param uri the plugin codebase uri
    */
    public PluginStrategy( URI uri )
    {
        m_uri = uri;
    }

   /**
    * Return the codebase uri.
    * @return the uri
    */
    public URI getURI()
    {
        return m_uri;
    }

   /**
    * Test if this object is equal to the supplied object.
    * @param other the other object
    * @return TRUE if the supplied object is equal to this object
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        if( other instanceof PluginStrategy )
        {
            PluginStrategy plugin = (PluginStrategy) other;
            return m_uri.equals( plugin.getURI() );
        }
        else
        {
            return false;
        }
    }

   /**
    * Return the object hashcode.
    * @return the hashcode value
    */
    public int hashCode()
    {
        return m_uri.hashCode();
    }
}

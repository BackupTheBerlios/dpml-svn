/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.part;

import java.io.Serializable;
import java.net.URI;
import java.util.Properties;

/**
 * The generic part header datastructure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartHeader implements Serializable
{
    private final URI m_controllerArtifactURI;
    private final Properties m_properties;
    
   /**
    * Creation of a new PartHeader.
    * @param uri the part uri
    * @param properties part properties
    */
    public PartHeader( URI uri, Properties properties )
    {
        if( null == uri )
        {
            throw new NullPointerException( "uri" );
        }
        if( null == properties )
        {
            throw new NullPointerException( "properties" );
        }
        m_controllerArtifactURI = uri;
        m_properties = properties;
    }
    
   /**
    * Return the part handler implementation plugin uri.
    * @return the uri of the part controller
    */
    public URI getControllerURI()
    {
        return m_controllerArtifactURI;
    }
    
   /**
    * Return the optional properties.
    * @return the a property set containing any properties associated with the part
    */
    public Properties getProperties()
    {
        return m_properties;
    }
    
   /**
    * Test is the supplied object is equal to this object.
    * @param other the other object
    * @return true if the object are equivalent
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( !( other instanceof PartHeader ) )
        {
            return false;
        }
        PartHeader header = (PartHeader) other;
        if( !m_controllerArtifactURI.equals( header.m_controllerArtifactURI ) )
        {
            return false;
        }
        else
        {
            return m_properties.equals( header.m_properties );
        }
    }

   /**
    * Return the hashcode for the object.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = getClass().hashCode();
        hash ^= m_controllerArtifactURI.hashCode();
        hash ^= m_properties.hashCode();
        return hash;
    }
}


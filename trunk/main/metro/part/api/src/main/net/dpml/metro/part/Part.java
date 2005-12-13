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

package net.dpml.metro.part;

import java.io.Serializable;
import java.net.URI;
import java.util.Properties;

/**
 * The generic part datastructure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Part implements Serializable
{
   /**
    * The constant part type identifier.
    */
    static final String ARTIFACT_TYPE = "part";
    
    private final URI m_controllerArtifactURI;
    private final Properties m_properties;
    private final Directive m_directive;
    
   /**
    * Creation of a new Part.
    * @param uri the part uri
    * @param properties optional part properties
    * @param directive the controller specific part datastructure
    */
    public Part( URI uri, Properties properties, Directive directive )
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
        m_directive = directive;
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
    * Return the part directive.  The value returned from 
    * this method may be null if the part has loaded by the 
    * part API.  Part handler implemetnations are required 
    * to fully support this operation following delegation 
    * by the default controller of part loading requests.
    * 
    * @return the part directive
    */
    public Directive getDirective()
    {
        return m_directive;
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
        else if( !( other instanceof Part ) )
        {
            return false;
        }
        Part part = (Part) other;
        if( !m_controllerArtifactURI.equals( part.m_controllerArtifactURI ) )
        {
            return false;
        }
        else if( !m_properties.equals( part.m_properties ) )
        {
            return false;
        }
        else
        {
            if( m_directive == null )
            {
                return ( null == part.m_directive );
            }
            else
            {
                return m_directive.equals( part.m_directive );
            }
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
        if( null != m_directive )
        {
            hash ^= m_directive.hashCode();
        }
        return hash;
    }
}


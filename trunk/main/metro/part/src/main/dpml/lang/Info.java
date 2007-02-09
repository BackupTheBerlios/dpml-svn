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

package dpml.lang;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;

import javax.xml.XMLConstants;

import net.dpml.util.Logger;
import dpml.util.ObjectUtils;


/**
 * Part info description.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Info
{
    private final String m_title;
    private final String m_description;
    private final URI m_uri;
    
   /**
    * Creation of a new part info descriptor.
    * @param uri the part uri
    * @param title the title of the part
    * @param description the part description
    */
    public Info( URI uri, String title, String description )
    {
        m_uri = uri;
        m_title = title;
        m_description = description;
    }
    
   /**
    * Get the part uri.
    *
    * @return the uri
    */
    public URI getURI()
    {
        return m_uri;
    }
    
   /**
    * Get the part title.
    *
    * @return the title
    */
    public String getTitle()
    {
        return m_title;
    }
    
   /**
    * Get the part description.
    *
    * @return the description
    */
    public String getDescription()
    {
        return m_description;
    }
    
   /**
    * Test if this instance is equal to the supplied instance.
    * @param other the other instance
    * @return the equality status
    */
    public boolean equals( Object other )
    {
        if( other instanceof Info )
        {
            Info info = (Info) other;
            if( !ObjectUtils.equals( m_title, info.m_title ) )
            {
                return false;
            }
            else
            {
                return ObjectUtils.equals( m_description, info.m_description );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Get the hashcode for this instance.
    * @return the hash value
    */
    public int hashCode()
    {
        int hash = getClass().hashCode();
        hash ^= ObjectUtils.hashValue( m_title );
        hash ^= ObjectUtils.hashValue( m_description );
        return hash;
    }
}
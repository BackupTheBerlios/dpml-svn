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

package net.dpml.transit.info;

import java.net.URISyntaxException;

/**
 * The CodeBaseDirective is immutable datastructure used to 
 * describe a codebase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ContentDirective extends CodeBaseDirective
{
    private final String m_id;
    private final String m_title;
    
   /**
    * Creation of a new codebase descriptor.
    * @param id the datatype identifier
    * @param title the handler title
    * @param codebase the codebase uri 
    * @param parameters an array of plugin parameter descriptors
    * @exception URISyntaxException if the codebase URI is invalid
    * @exception NullPointerException if the id is null
    */
    public ContentDirective( 
      String id, String title, String codebase, ValueDirective[] parameters ) 
      throws URISyntaxException, NullPointerException
    {
        super( codebase, parameters );
        if( null == id )
        {
            throw new NullPointerException( "id" );
        }
        m_id = id;
        if( null == title )
        {
            m_title = id;
        }
        else
        {
            m_title = title;
        }
    }
    
   /**
    * Return the unique content handler datatype identifier.
    *
    * @return the handler id
    */
    public String getID()
    {
        return m_id;
    }
    
   /**
    * Return the content handler title.
    *
    * @return the title
    */
    public String getTitle()
    {
        return m_title;
    }
    
   /**
    * Test if the supplied object is equal to this object.
    * @param other the object to evaluate
    * @return true if this object is equal to the supplied object
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ContentDirective ) )
        {
            ContentDirective directive = (ContentDirective) other;
            if( !equals( m_id, directive.m_id ) )
            {
                return false;
            }
            else
            {
                return equals( m_title, directive.m_title );
            }
        }
        else
        {
            return false;
        }
    }

   /**
    * Compute the instance hashcode value.
    * @return the hashcode
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= hashValue( m_id );
        hash ^= hashValue( m_title );
        return hash;
    }
}

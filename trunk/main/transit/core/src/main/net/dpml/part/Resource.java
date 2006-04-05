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

package net.dpml.part;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;

import net.dpml.lang.Logger;
import net.dpml.lang.Classpath;

/**
 * Resource part strategy implementation datatype.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class Resource extends Part
{
    private final String m_urn;
    private final String m_path;
    
   /**
    * Creation of resource datatype.
    * @param logger the assigned logging channel
    * @param info the part info descriptor
    * @param classpath the part classpath descriptor
    * @param urn the resource urn
    * @param path the resource path
    * @exception IOException if an I/O error occurs
    */
    public Resource( Logger logger, Info info, Classpath classpath, String urn, String path )
      throws IOException
    {
        super( logger, info, classpath );
        if( null == urn )
        {
            throw new NullPointerException( "urn" );
        }
        if( null == path )
        {
            throw new NullPointerException( "path" );
        }
        m_urn = urn;
        m_path = path;
    }

   /**
    * Return the part content or null if the result type is unresolvable 
    * relative to the supplied classes argument. Class arguments recognized
    * over an above plugin include the URL and String classes.  If the URL
    * class is supplied a URL referencing the resource identified by path 
    * is returned.  If a String is requested the urn value is returned.
    *
    * @param classes the content type selection classes
    * @return the content
    * @exception IOException if an IO error occurs
    */
    protected Object getContent( Class c ) throws IOException
    {
        if( URL.class.equals( c ) )
        {
            return getClassLoader().getResource( m_path );
        }
        else if( String.class.equals( c ) )
        {
            return getURN();
        }
        else
        {
            return super.getContent( c );
        }
    }
    
   /**
    * Get the resource urn.
    * @return the urn
    */
    public String getURN()
    {
        return m_urn;
    }
    
   /**
    * Get the resource path.
    * @return the path
    */ 
    public String getPath()
    {
        return m_path;
    }
    
   /**
    * Instantiate a value.
    * @param args supplimentary arguments
    * @return the resolved instance
    * @exception Exception if a deployment error occurs
    */
    public Object instantiate( Object[] args ) throws Exception
    {
        return getClassLoader().getResource( m_path );
    }
    
   /**
    * Encode the resource strategy to XML.
    * @param writer the output stream writer
    * @param pad the character offset
    * @exception IOException if an I/O error occurs
    */
    protected void encodeStrategy( Writer writer, String pad ) throws IOException
    {
        String urn = getURN();
        String path = getPath();
        writer.write( "\n  <strategy xsi:type=\"resource\"" );
        writer.write( " urn=\"" + urn );
        writer.write( "\" path=\"" + path );
        writer.write( "\"/>" );
    }
    
   /**
    * Test if this instance is equal to the supplied instance.
    * @param other the supplied instance
    * @return the equality status
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) )
        {
            if( other instanceof Resource )
            {
                Resource resource = (Resource) other;
                if( !m_path.equals( resource.m_path ) )
                {
                    return false;
                }
                else
                {
                    return m_urn.equals( resource.m_urn );
                }
            }
            else
            {
                return false;
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
        int hash = super.hashCode();
        hash ^= m_path.hashCode();
        hash ^= m_urn.hashCode();
        return hash;
    }
}

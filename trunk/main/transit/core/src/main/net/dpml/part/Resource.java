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
    * @param urn the resource urn
    * @param path the resource path
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
        throw new UnsupportedOperationException( "instantiate/1" );
    }
    
    protected void encodeStrategy( Writer writer, String pad ) throws IOException
    {
        String urn = getURN();
        String path = getPath();
        writer.write( "\n  <strategy xsi:type=\"resource\"" );
        writer.write( " urn=\"" + urn );
        writer.write( "\" path=\"" + path );
        writer.write( "\"/>" );
    }
    
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
    
    
}
